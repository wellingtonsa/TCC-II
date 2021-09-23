package br.ufc.great.caos.service.protocol.client.util.protocol.transport;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import io.quiche4j.Config;
import io.quiche4j.ConfigBuilder;
import io.quiche4j.Connection;
import io.quiche4j.ConnectionFailureException;
import io.quiche4j.Quiche;
import io.quiche4j.Utils;
import io.quiche4j.http3.Http3;
import io.quiche4j.http3.Http3Config;
import io.quiche4j.http3.Http3ConfigBuilder;
import io.quiche4j.http3.Http3Connection;
import io.quiche4j.http3.Http3EventListener;
import io.quiche4j.http3.Http3Header;

public class QUIC implements ProtocolService {
	static final int MAX_DATAGRAM_SIZE = 1350;

	public static final String CLIENT_NAME = "Quiche4j";

	Config config;
	Connection conn;
	byte[] connId;
	byte[] buffer;
	DatagramPacket handshakePacket;
	int len;

	URI uri;

	@Override
	public boolean init() {
		try {
			config = new ConfigBuilder(Quiche.PROTOCOL_VERSION)
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
					.build();
			return true;
		}catch (Exception e){
			return false;
		}
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			connId = Quiche.newConnectionId();
			conn = Quiche.connect(ip, connId, config);

			 uri = new URI("http://"+ip+":"+port);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean disconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String sendMessage(String message) {
		try {
			int len = 0;
			buffer = new byte[MAX_DATAGRAM_SIZE];
			len = conn.send(buffer);
			if (len < 0 && len != Quiche.ErrorCode.DONE) {
				Log.i("QUIC","! handshake init problem " + len);
				System.exit(1);
				return "";
			}
			Log.i("QUIC", "> handshake size: " + len);

			handshakePacket = new DatagramPacket(buffer, len, InetAddress.getByName(uri.getHost()), uri.getPort());

			 DatagramSocket socket = new DatagramSocket(0);
			socket.setSoTimeout(200);
			socket.send(handshakePacket);

			Long streamId = null;
			 AtomicBoolean reading = new AtomicBoolean(true);
			 Http3Config h3Config = new Http3ConfigBuilder().build();
			DatagramPacket packet;
			Http3Connection h3Conn = null;

			while (!conn.isClosed()) {
				// READING LOOP
				while (reading.get()) {
					packet = new DatagramPacket(buffer, buffer.length);
					try {
						socket.receive(packet);
						 int recvBytes = packet.getLength();

						Log.i("QUIC","> socket.recieve " + recvBytes + " bytes");
						// xxx(okachaiev): if we extend `recv` API to deal with optional buf len,
						// we could avoid Arrays.copy here
						 int read = conn.recv(Arrays.copyOfRange(packet.getData(), packet.getOffset(), recvBytes));
						if (read < 0 && read != Quiche.ErrorCode.DONE) {
							Log.i("QUIC","> conn.recv failed " + read);

							reading.set(false);
						} else {
							Log.i("QUIC","> conn.recv " + read + " bytes");
						}
					} catch (SocketTimeoutException e) {
						conn.onTimeout();
						reading.set(false);
					}

					// POLL
					if (null != h3Conn) {
						 Http3Connection h3c = h3Conn;
						streamId = h3c.poll(new Http3EventListener() {
							@RequiresApi(api = Build.VERSION_CODES.N)
							public void onHeaders(long streamId, List<Http3Header> headers, boolean hasBody) {
								headers.forEach(header -> {
									Log.i("QUIC", header.name() + ": " + header.value());
								});
							}

							@RequiresApi(api = Build.VERSION_CODES.KITKAT)
							public void onData(long streamId) {
								 int bodyLength = h3c.recvBody(streamId, buffer);
								if (bodyLength < 0 && bodyLength != Quiche.ErrorCode.DONE) {
									Log.i("QUIC", "! recv body failed " + bodyLength);
								} else {
									Log.i("QUIC", "< got body " + bodyLength + " bytes for " + streamId);
									 byte[] body = Arrays.copyOfRange(buffer, 0, bodyLength);
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
					h3Conn.sendRequest(req, true);
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
					packet = new DatagramPacket(buffer, len, InetAddress.getByName(uri.getHost()), uri.getPort());
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

			Log.i("QUIC", "> conn is closed");
			Log.i("QUIC", conn.stats()+"");
			socket.close();
		}catch(Exception e){
			return "";
		}

		return "";
	}


	@Override
	public String isInstanceOf() {
		return "QUIC";
	}

}
