package br.ufc.great.caos.service.protocol.server.util.protocol.application;

import java.io.IOException;
import java.util.HashMap;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import fi.iki.elonen.NanoHTTPD;

public class HTTP implements ProtocolService {

	HttpService server;

	@Override
	public boolean init() {
		return false;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			server = new HttpService(port);
			server.start();
			System.out.println("HTTP - Server started on port "+port);
			return true;
		} catch (IOException e) {
			System.out.println("HTTP - Start server error:" + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean disconnect() {
		try {
			server.start();
			System.out.println("HTTP - Server stopped");
			return true;
		} catch (IOException e) {
			System.out.println("HTTP - Finish connection error:" + e.getMessage());
			return false;
		}
	}


	public class HttpService extends NanoHTTPD {
		public HttpService(int port){
			super(port);
		}

		@Override
		public Response serve(IHTTPSession session) {

			String uri = session.getUri();
			final HashMap<String, String> body = new HashMap<String, String>();
			if (Method.POST.equals(session.getMethod())) {
				try {
					session.parseBody(body);
					return newFixedLengthResponse(body.get("postData"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ResponseException e) {
					e.printStackTrace();
				}

			}
			return NanoHTTPD.newFixedLengthResponse(uri);
		}
	}
}
