package br.ufc.great.caos.service.protocol.client.util.protocol.transport;

import java.util.ArrayList;
import java.util.List;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import io.quiche4j.Config;
import io.quiche4j.ConfigBuilder;
import io.quiche4j.Connection;
import io.quiche4j.ConnectionFailureException;
import io.quiche4j.Quiche;
import io.quiche4j.http3.Http3;
import io.quiche4j.http3.Http3Config;
import io.quiche4j.http3.Http3ConfigBuilder;
import io.quiche4j.http3.Http3Connection;
import io.quiche4j.http3.Http3EventListener;
import io.quiche4j.http3.Http3Header;

public class QUIC implements ProtocolService {

	Config config;
	Connection conn;
	byte[] connId;
	long streamId;
	byte[] response;
	Http3Connection h3Conn;
	Http3Config h3Config;

	@Override
	public boolean init() {
		h3Config = new Http3ConfigBuilder().build();
		config = new ConfigBuilder(Quiche.PROTOCOL_VERSION)
				.build();
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		connId = Quiche.newConnectionId();
		try {
			h3Conn = Http3Connection.withTransport(conn, h3Config);
			conn = Quiche.connect("http://"+ip+":"+port, connId, config);
			return true;
		} catch (ConnectionFailureException e) {
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

		List<Http3Header> req = new ArrayList<>();
		req.add(new Http3Header(":method", "POST"));
		req.add(new Http3Header(":scheme", "http"));
		req.add(new Http3Header(":authority", "offloading"));
		req.add(new Http3Header(":path", "/offloading"));

		h3Conn.sendBody(streamId, message.getBytes(), true);

		streamId = h3Conn.poll(new Http3EventListener() {


			public void onHeaders(long streamId, List<Http3Header> headers) {
			}

			@Override
			public void onHeaders(long l, List<Http3Header> list, boolean b) {

			}

			public void onData(long streamId) {
				final byte[] body = new byte[1_000_000];
				final int len = h3Conn.recvBody(streamId, body);
				response = body;

			}

			public void onFinished(long streamId) {
				conn.close(true, 0x00, response);
			}
		});

		if(Quiche.ErrorCode.DONE == streamId) {

			return response.toString();
		}

		return response.toString();
	}


	@Override
	public String isInstanceOf() {
		return "QUIC";
	}

}
