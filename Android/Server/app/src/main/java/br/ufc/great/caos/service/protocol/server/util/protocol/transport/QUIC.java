package br.ufc.great.caos.service.protocol.server.util.protocol.transport;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import io.quiche4j.Config;
import io.quiche4j.ConfigBuilder;
import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import io.quiche4j.Connection;
import io.quiche4j.PacketHeader;
import io.quiche4j.PacketType;
import io.quiche4j.Quiche;
import io.quiche4j.Utils;
import io.quiche4j.http3.Http3;
import io.quiche4j.http3.Http3Config;
import io.quiche4j.http3.Http3ConfigBuilder;
import io.quiche4j.http3.Http3Connection;
import io.quiche4j.http3.Http3EventListener;
import io.quiche4j.http3.Http3Header;


public class QUIC implements ProtocolService {

	private static int MAX_DATAGRAM_SIZE = 1350;
	private static String SERVER_NAME = "Quiche4j";
	private static byte[] SERVER_NAME_BYTES = SERVER_NAME.getBytes();
	private static int SERVER_NAME_BYTES_LEN = SERVER_NAME_BYTES.length;

	private static String HEADER_NAME_STATUS = ":status";
	private static String HEADER_NAME_SERVER = "server";
	private static String HEADER_NAME_CONTENT_LENGTH = "content-length";

	byte[] buf;
	byte[] out;

	protected static class PartialResponse {
		protected List<Http3Header> headers;
		protected byte[] body;
		protected long written;

		PartialResponse(List<Http3Header> headers, byte[] body, long written) {
			this.headers = headers;
			this.body = body;
			this.written = written;
		}
	}

	protected static class Client {

		private Connection conn;
		private Http3Connection h3Conn;
		private HashMap<Long, PartialResponse> partialResponses;
		private SocketAddress sender;

		public Client(Connection conn, SocketAddress sender) {
			this.conn = conn;
			this.sender = sender;
			this.h3Conn = null;
			this.partialResponses = new HashMap<>();
		}

		public Connection connection() {
			return this.conn;
		}

		public SocketAddress sender() {
			return this.sender;
		}

		public Http3Connection http3Connection() {
			return this.h3Conn;
		}

		public void setHttp3Connection(Http3Connection conn) {
			this.h3Conn = conn;
		}

	}


	@Override
	public boolean init() {
		try {
			buf = new byte[65535];
			out = new byte[MAX_DATAGRAM_SIZE];

			return true;
		}catch (Exception e){
			Log.i("QUIC", "Initialization Error (Exception): "+e.getMessage());
			return false;
		}
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {

			Config config = new ConfigBuilder(Quiche.PROTOCOL_VERSION)
					.withApplicationProtos(Http3.APPLICATION_PROTOCOL)
					.withVerifyPeer(false)
					.loadCertChainFromPemFile(Utils.copyFileFromJAR("certs", "/cert.crt"))
					.loadPrivKeyFromPemFile(Utils.copyFileFromJAR("certs", "/cert.key"))
					.withMaxIdleTimeout(5_000)
					.withMaxUdpPayloadSize(MAX_DATAGRAM_SIZE)
					.withInitialMaxData(10_000_000)
					.withInitialMaxStreamDataBidiLocal(1_000_000)
					.withInitialMaxStreamDataBidiRemote(1_000_000)
					.withInitialMaxStreamDataUni(1_000_000)
					.withInitialMaxStreamsBidi(100)
					.withInitialMaxStreamsUni(100)
					.withDisableActiveMigration(true)
					.enableEarlyData()
					.build();

			DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName(ip));
			socket.setSoTimeout(100);

			Http3Config h3Config = new Http3ConfigBuilder().build();
			byte[] connIdSeed = Quiche.newConnectionIdSeed();
			HashMap<String, Client> clients = new HashMap<>();
			AtomicBoolean running = new AtomicBoolean(true);

			Log.i("QUIC",String.format("! listening on %s:%d", ip, port));

			while (running.get()) {
				// READING
				while (true) {
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					try {
						socket.receive(packet);
					} catch (SocketTimeoutException e) {
						// TIMERS
						for (Client client : clients.values()) {
							client.connection().onTimeout();
						}
						break;
					}

					int offset = packet.getOffset();
					int len = packet.getLength();
					// xxx(okachaiev): can we avoid doing copy here?
					byte[] packetBuf = Arrays.copyOfRange(packet.getData(), offset, len);

					Log.i("QUIC","> socket.recv " + len + " bytes");

					// PARSE QUIC HEADER
					PacketHeader hdr;
					try {
						hdr = PacketHeader.parse(packetBuf, Quiche.MAX_CONN_ID_LEN);
						Log.i("QUIC","> packet " + hdr);
					} catch (Exception e) {
						Log.i("QUIC","! failed to parse headers " + e);
						continue;
					}

					// SIGN CONN ID
					byte[] connId = Quiche.signConnectionId(connIdSeed, hdr.destinationConnectionId());
					Client client = clients.get(Utils.asHex(hdr.destinationConnectionId()));
					if (null == client)
						client = clients.get(Utils.asHex(connId));
					if (null == client) {
						// CREATE CLIENT IF MISSING
						if (PacketType.INITIAL != hdr.packetType()) {
							Log.i("QUIC","! wrong packet type");
							continue;
						}

						// NEGOTIATE VERSION
						if (!Quiche.versionIsSupported(hdr.version())) {
							Log.i("QUIC","> version negotiation");

							int negLength = Quiche.negotiateVersion(hdr.sourceConnectionId(),
									hdr.destinationConnectionId(), out);
							if (negLength < 0) {
								Log.i("QUIC","! failed to negotiate version " + negLength);
								System.exit(1);
								return false;
							}
							DatagramPacket negPacket = new DatagramPacket(out, negLength, packet.getAddress(),
									packet.getPort());
							socket.send(negPacket);
							continue;
						}

						// RETRY IF TOKEN IS EMPTY
						if (null == hdr.token()) {
							Log.i("QUIC","> stateless retry");

							byte[] token = mintToken(hdr, packet.getAddress());
							int retryLength = Quiche.retry(hdr.sourceConnectionId(), hdr.destinationConnectionId(),
									connId, token, hdr.version(), out);
							if (retryLength < 0) {
								Log.i("QUIC","! retry failed " + retryLength);
								System.exit(1);
								return false;
							}

							Log.i("QUIC","> retry length " + retryLength);

							DatagramPacket retryPacket = new DatagramPacket(out, retryLength, packet.getAddress(),
									packet.getPort());
							socket.send(retryPacket);
							continue;
						}

						// VALIDATE TOKEN
						byte[] odcid = validateToken(packet.getAddress(), hdr.token());
						if (null == odcid) {
							Log.i("QUIC","! invalid address validation token");
							continue;
						}

						byte[] sourceConnId = connId;
						byte[] destinationConnId = hdr.destinationConnectionId();
						if (sourceConnId.length != destinationConnId.length) {
							Log.i("QUIC","! invalid destination connection id");
							continue;
						}
						sourceConnId = destinationConnId;
						Connection conn = Quiche.accept(sourceConnId, odcid, config);

						Log.i("QUIC","> new connection " + Utils.asHex(sourceConnId));
						client = new Client(conn, packet.getSocketAddress());
						clients.put(Utils.asHex(sourceConnId), client);

						Log.i("QUIC","! # of clients: " + clients.size());
					}

					// POTENTIALLY COALESCED PACKETS
					Connection conn = client.connection();
					int read = conn.recv(packetBuf);
					if (read < 0 && read != Quiche.ErrorCode.DONE) {
						Log.i("QUIC","> recv failed " + read);
						break;
					}
					if (read <= 0)
						break;

					Log.i("QUIC","> conn.recv " + read + " bytes");
					Log.i("QUIC","> conn.established " + conn.isEstablished());

					// ESTABLISH H3 CONNECTION IF NONE
					Http3Connection h3Conn = client.http3Connection();
					if ((conn.isInEarlyData() || conn.isEstablished()) && null == h3Conn) {
						Log.i("QUIC","> handshake done " + conn.isEstablished());
						h3Conn = Http3Connection.withTransport(conn, h3Config);
						client.setHttp3Connection(h3Conn);

						Log.i("QUIC","> new H3 connection " + h3Conn);
					}

					if (null != h3Conn) {
						// PROCESS WRITABLES
						Client current = client;
						client.connection().writable().forEach(streamId -> {
							handleWritable(current, streamId);
						});

						// H3 POLL
						while (true) {
							long streamId = h3Conn.poll(new Http3EventListener() {
								public void onHeaders(long streamId, List<Http3Header> headers, boolean hasBody) {
									headers.forEach(header -> {
										Log.i("QUIC","< got header " + header.name() + " on " + streamId);
									});
									handleRequest(current, streamId, headers);
								}

								public void onData(long streamId) {
									Log.i("QUIC","< got data on " + streamId);
								}

								public void onFinished(long streamId) {
									Log.i("QUIC","< finished " + streamId);
								}
							});

							if (streamId < 0 && streamId != Quiche.ErrorCode.DONE) {
								Log.i("QUIC","! poll failed " + streamId);

								// xxx(okachaiev): this should actially break from 2 loops
								break;
							}
							// xxx(okachaiev): this should actially break from 2 loops
							if (Quiche.ErrorCode.DONE == streamId)
								break;

							Log.i("QUIC","< poll " + streamId);
						}
					}
				}

				// WRITES
				int len = 0;
				for (Client client : clients.values()) {
					Connection conn = client.connection();

					while (true) {
						len = conn.send(out);
						if (len < 0 && len != Quiche.ErrorCode.DONE) {
							Log.i("QUIC","! conn.send failed " + len);
							break;
						}
						if (len <= 0)
							break;
						Log.i("QUIC","> conn.send " + len + " bytes");
						DatagramPacket packet = new DatagramPacket(out, len, client.sender());
						socket.send(packet);
					}
				}

				// CLEANUP CLOSED CONNS
				for (String connId : clients.keySet()) {
					if (clients.get(connId).connection().isClosed()) {
						Log.i("QUIC","> cleaning up " + connId);

						clients.remove(connId);

						Log.i("QUIC","! # of clients: " + clients.size());
					}
				}

				// BACK TO READING
			}

			Log.i("QUIC","> server stopped");
			socket.close();
			return true;
		}catch(Exception e){
			return false;
		}
	}
		@Override
	public boolean disconnect() {
		return false;
	}


	@Override
	public String isInstanceOf() {
		return "QUIC";
	}

	public  static byte[] mintToken(PacketHeader hdr, InetAddress address) {
		 byte[] addr = address.getAddress();
		 byte[] dcid = hdr.destinationConnectionId();
		 int total = SERVER_NAME_BYTES_LEN + addr.length + dcid.length;
		 ByteBuffer buf = ByteBuffer.allocate(total);
		buf.put(SERVER_NAME_BYTES);
		buf.put(addr);
		buf.put(dcid);
		return buf.array();
	}

	public  static byte[] validateToken(InetAddress address, byte[] token) {
		if (token.length <= 8)
			return null;
		if (!Arrays.equals(SERVER_NAME_BYTES, Arrays.copyOfRange(token, 0, SERVER_NAME_BYTES_LEN)))
			return null;
		 byte[] addr = address.getAddress();
		if (!Arrays.equals(addr, Arrays.copyOfRange(token, SERVER_NAME_BYTES_LEN, addr.length + SERVER_NAME_BYTES_LEN)))
			return null;
		return Arrays.copyOfRange(token, SERVER_NAME_BYTES_LEN + addr.length, token.length);
	}

	public  static void handleRequest(Client client, Long streamId, List<Http3Header> req) {
		Log.i("QUIC","< request " + streamId);

		 Connection conn = client.connection();
		 Http3Connection h3Conn = client.http3Connection();

		// SHUTDOWN STREAM
		conn.streamShutdown(streamId, Quiche.Shutdown.READ, 0L);

		 byte[] body = "Hello world".getBytes();
		 List<Http3Header> headers = new ArrayList<>();
		headers.add(new Http3Header(HEADER_NAME_STATUS, "200"));
		headers.add(new Http3Header(HEADER_NAME_SERVER, SERVER_NAME));
		headers.add(new Http3Header(HEADER_NAME_CONTENT_LENGTH, Integer.toString(body.length)));

		 long sent = h3Conn.sendResponse(streamId, headers, false);
		if (sent == Http3.ErrorCode.STREAM_BLOCKED) {
			// STREAM BLOCKED
			Log.i("QUIC","> stream " + streamId + " blocked");

			// STASH PARTIAL RESPONSE
			 PartialResponse part = new PartialResponse(headers, body, 0L);
			client.partialResponses.put(streamId, part);
			return;
		}

		if (sent < 0) {
			Log.i("QUIC","! h3.send response failed " + sent);
			return;
		}

		 long written = h3Conn.sendBody(streamId, body, true);
		if (written < 0) {
			Log.i("QUIC","! h3 send body failed " + written);
			return;
		}

		Log.i("QUIC","> send body " + written + " body");

		if (written < body.length) {
			// STASH PARTIAL RESPONSE
			 PartialResponse part = new PartialResponse(null, body, written);
			client.partialResponses.put(streamId, part);
		}
	}

	public  static void handleWritable(Client client, long streamId) {
		 PartialResponse resp = client.partialResponses.get(streamId);
		if (null == resp)
			return;

		 Http3Connection h3 = client.http3Connection();
		if (null != resp.headers) {
			 long sent = h3.sendResponse(streamId, resp.headers, false);
			if (sent == Http3.ErrorCode.STREAM_BLOCKED)
				return;
			if (sent < 0) {
				Log.i("QUIC","! h3.send response failed " + sent);
				return;
			}
		}

		resp.headers = null;

		 byte[] body = Arrays.copyOfRange(resp.body, (int) resp.written, resp.body.length);
		 long written = h3.sendBody(streamId, body, true);
		if (written < 0 && written != Quiche.ErrorCode.DONE) {
			Log.i("QUIC","! h3 send body failed " + written);
			return;
		}

		Log.i("QUIC","> send body " + written + " body");

		resp.written += written;
		if (resp.written < resp.body.length) {
			client.partialResponses.remove(streamId);
		}
	}
}

