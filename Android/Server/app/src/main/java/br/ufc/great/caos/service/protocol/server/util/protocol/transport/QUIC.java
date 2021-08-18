package br.ufc.great.caos.service.protocol.server.util.protocol.transport;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.quiche4j.Config;
import io.quiche4j.ConfigBuilder;
import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import io.quiche4j.Connection;
import io.quiche4j.ConnectionFailureException;
import io.quiche4j.PacketHeader;
import io.quiche4j.Quiche;
import io.quiche4j.http3.Http3;
import io.quiche4j.http3.Http3Config;
import io.quiche4j.http3.Http3Connection;
import io.quiche4j.http3.Http3EventListener;
import io.quiche4j.http3.Http3Header;



public class QUIC implements ProtocolService {

	static final int MAX_DATAGRAM_SIZE = 1350;
	final byte[] buf = new byte[65535];
	final byte[] out = new byte[MAX_DATAGRAM_SIZE];

	Config config;
	Http3Config h3Config;
	Connection conn;
	Http3Connection h3Conn;

	DatagramSocket socket;
	byte[] connIdSeed;
	AtomicBoolean running;


	@Override
	public boolean init() {
		config= new ConfigBuilder(Quiche.PROTOCOL_VERSION)
				.withApplicationProtos(Http3.APPLICATION_PROTOCOL)
				.loadCertChainFromPemFile("/br/ufc/great/caos/service/protocol/server/util/cert/cert.pem")
				.loadPrivKeyFromPemFile("/br/ufc/great/caos/service/protocol/server/util/cert/key.pem")
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
		//h3Config = new Http3ConfigBuilder().build();
		//connIdSeed = Quiche.newConnectionIdSeed();
		//running = new AtomicBoolean(true);
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			socket = new DatagramSocket(port, InetAddress.getByName(ip));

			while (running.get()) {
				while (true) {
					final DatagramPacket packet = new DatagramPacket(buf, buf.length);
					try {
						socket.receive(packet);
					} catch (SocketTimeoutException e) {
						break;
					} catch (IOException e) {
						e.printStackTrace();
					}

					final int offset = packet.getOffset();
					final int len = packet.getLength();
					final byte[] packetBuf = Arrays.copyOfRange(packet.getData(), offset, len);

					final PacketHeader hdr;
					try {
						hdr = PacketHeader.parse(packetBuf, Quiche.MAX_CONN_ID_LEN);
					} catch (Exception e) {
						continue;
					}

					final byte[] connId = Quiche.signConnectionId(connIdSeed, hdr.destinationConnectionId());

					conn = Quiche.accept(connId, hdr.destinationConnectionId(), config);
					h3Conn = Http3Connection.withTransport(conn, h3Config);

					long streamId = h3Conn.poll(new Http3EventListener() {
						@Override
						public void onHeaders(long l, List<Http3Header> list, boolean b) {

						}

						public void onData(long streamId) {
							final byte[] body = new byte[MAX_DATAGRAM_SIZE];
							final int len = h3Conn.recvBody(streamId, body);
						}

						public void onFinished(long streamId) {
							conn.close(true, 0x00, "Bye! :)".getBytes());
						}
					});

					if(Quiche.ErrorCode.DONE == streamId) {
						// this means no event was emitted
						// it would take more packets to proceed with new events
					}			}

		}
		return true;
	} catch (UnknownHostException e) {
			Log.i("QUIC", "Connection Error (UnknownHost): "+e.getMessage());
			e.printStackTrace();
		} catch (ConnectionFailureException e) {
			Log.i("QUIC", "Connection Error (ConnectionFailure): "+e.getMessage());
			e.printStackTrace();
		} catch (SocketException e) {
			Log.i("QUIC", "Connection Error (Socket): "+e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
		@Override
	public boolean disconnect() {
		return false;
	}


	@Override
	public String isInstanceOf() {
		return "QUIC";
	}
}

