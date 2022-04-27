package br.ufc.great.caos.service.protocol.client.util.protocol.transport;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;
import io.quiche4j.Config;
import io.quiche4j.ConfigBuilder;
import io.quiche4j.Connection;
import io.quiche4j.ConnectionFailureException;
import io.quiche4j.Quiche;
import io.quiche4j.QuicheSocketAddress;
import io.quiche4j.Utils;
import io.quiche4j.http3.Http3;

public class QUIC implements ProtocolService {

	public static final int MAX_DATAGRAM_SIZE = 1350;
	public static final String CLIENT_NAME = "Quiche4j";
	Config config;

	String url;
	URI uri;

	int addressPort;
	InetAddress address;
	byte[] connId;
	QuicheSocketAddress socketAddress;
	Connection conn;
	DatagramSocket socket;

	@Override
	public boolean init() {
		try {
			config = new ConfigBuilder(Quiche.PROTOCOL_VERSION)
					.withApplicationProtos(Http3.APPLICATION_PROTOCOL)
					// CAUTION: this should not be set to `false` in production
					.withVerifyPeer(true)
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
					.build();
			return true;
		} catch (IOException ioException) {
			ioException.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean connect(String ip, Integer port) {

		try {
			uri = new URI("http://"+ip+":"+port);
			Log.i("QUIC", "> sending request to " + uri);

			addressPort = uri.getPort();
			address = InetAddress.getByName(uri.getHost());


			connId = Quiche.newConnectionId();
			socketAddress = new QuicheSocketAddress(address.getHostAddress(), port);
			conn = Quiche.connect(uri.getHost(), connId, config, socketAddress);

		} catch (URISyntaxException e) {
			Log.i("QUIC", "Failed to parse URL " + url);
			return false;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (ConnectionFailureException e) {
			e.printStackTrace();
			return false;
		}


		return true;
	}

	@Override
	public boolean disconnect() {
		Log.i("QUIC", "> conn is closed");
		Log.i("QUIC", ""+conn.stats());
		socket.close();
		return true;
	}

	@Override
	public Object executeOffload(InvocableMethod method) {
		/*int len = 0;
		final byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
		len = conn.send(buffer);
		if (len < 0 && len != Quiche.ErrorCode.DONE) {
			Log.i("QUIC", "! handshake init problem " + len);
			return "";
		}
		Log.i("QUIC", "> handshake size: " + len);

		final DatagramPacket handshakePacket = new DatagramPacket(buffer, len, address, addressPort);
		try {
			socket = new DatagramSocket(0);
			socket.setSoTimeout(200);
			socket.send(handshakePacket);



		Long streamId = null;
		final AtomicBoolean reading = new AtomicBoolean(true);
		final Http3Config h3Config = new Http3ConfigBuilder().build();
		DatagramPacket packet;
		Http3Connection h3Conn = null;

		while (!conn.isClosed()) {
			// READING LOOP
			while (reading.get()) {
				packet = new DatagramPacket(buffer, buffer.length);
				try {
					socket.receive(packet);
					final int recvBytes = packet.getLength();

					Log.i("QUIC", "> socket.recieve " + recvBytes + " bytes");

					// xxx(okachaiev): if we extend `recv` API to deal with optional buf len,
					// we could avoid Arrays.copy here
					final int read = conn.recv(Arrays.copyOfRange(packet.getData(), packet.getOffset(), recvBytes), socketAddress);
					if (read < 0 && read != Quiche.ErrorCode.DONE) {
						Log.i("QUIC", "> conn.recv failed " + read);

						reading.set(false);
					} else {
						Log.i("QUIC", "> conn.recv " + read + " bytes");
					}
				} catch (SocketTimeoutException e) {
					conn.onTimeout();
					reading.set(false);
				}

				// POLL
				if (null != h3Conn) {
					final Http3Connection h3c = h3Conn;
					streamId = h3c.poll(new Http3EventListener() {
						@RequiresApi(api = Build.VERSION_CODES.N)
						public void onHeaders(long streamId, List<Http3Header> headers, boolean hasBody) {
							headers.forEach(header -> {
								Log.i("QUIC", header.name() + ": " + header.value());
							});
						}

						@RequiresApi(api = Build.VERSION_CODES.KITKAT)
						public void onData(long streamId) {
							final int bodyLength = h3c.recvBody(streamId, buffer);
							if (bodyLength < 0 && bodyLength != Quiche.ErrorCode.DONE) {
								Log.i("QUIC", "! recv body failed " + bodyLength);
							} else {
								Log.i("QUIC", "< got body " + bodyLength + " bytes for " + streamId);
								final byte[] body = Arrays.copyOfRange(buffer, 0, bodyLength);
								Log.i("QUIC", new String(body, StandardCharsets.UTF_8));
							}
						}

						public void onFinished(long streamId) {
							Log.i("QUIC", "> response finished");
							Log.i("QUIC", "> close code " + conn.close(true, 0x00, "kthxbye"));
							reading.set(false);
						}
					});

					if (streamId < 0 && streamId != Quiche.ErrorCode.DONE) {
						Log.i("QUIC", "> poll failed " + streamId);
						reading.set(false);
						break;
					}

					if (Quiche.ErrorCode.DONE == streamId)
						reading.set(false);
				}
			}

			if (conn.isClosed()) {
				Log.i("QUIC", "! conn is closed " + conn.stats());

				socket.close();
				System.exit(1);
				return "";
			}

			if (conn.isEstablished() && null == h3Conn) {
				h3Conn = Http3Connection.withTransport(conn, h3Config);

				Log.i("QUIC", "! h3 conn is established");

				List<Http3Header> req = new ArrayList<>();
				req.add(new Http3Header(":method", "GET"));
				req.add(new Http3Header(":scheme", uri.getScheme()));
				req.add(new Http3Header(":authority", uri.getAuthority()));
				req.add(new Http3Header(":path", uri.getPath()));
				req.add(new Http3Header("user-agent", CLIENT_NAME));
				req.add(new Http3Header("content-length", "0"));

				streamId = h3Conn.sendRequest(req, false);
				h3Conn.sendBody(streamId, message.getBytes(), true);
			}

			// WRITING LOOP
			while (true) {
				len = conn.send(buffer);
				if (len < 0 && len != Quiche.ErrorCode.DONE) {
					Log.i("QUIC", "! conn.send failed " + len);
					break;
				}
				if (len <= 0)
					break;
				Log.i("QUIC", "> conn.send " + len + " bytes");
				packet = new DatagramPacket(buffer, len, address, addressPort);
				socket.send(packet);
			}

			if (conn.isClosed()) {
				Log.i("QUIC", "! conn is closed " + conn.stats());

				socket.close();
				System.exit(1);
				return "";
			}

			reading.set(true);
		}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
*/
		return null;
	}


	@Override
	public String isInstanceOf() {
		return "QUIC";
	}


}
