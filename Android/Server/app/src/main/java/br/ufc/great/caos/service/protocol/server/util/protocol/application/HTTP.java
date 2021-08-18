package br.ufc.great.caos.service.protocol.server.util.protocol.application;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import fi.iki.elonen.NanoHTTPD;

public class HTTP implements ProtocolService {

	HttpService server;

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			server = new HttpService(port);
			server.start();
			Log.i("HTTP", "Server started on port " + port);
			return true;
		} catch (IOException e) {
			Log.i("HTTP", "Start server error:" + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean disconnect() {
		try {
			server.start();
			Log.i("HTTP", "Server stopped");
			return true;
		} catch (IOException e) {
			Log.i("HTTP", "Finish connection error:" + e.getMessage());
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

					JSONObject JSONBody = new JSONObject(body.get("postData"));

					return newFixedLengthResponse("Welcome "+JSONBody.getString("username"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ResponseException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
			return NanoHTTPD.newFixedLengthResponse(uri);
		}
	}

	@Override
	public String isInstanceOf() {
		return "HTTP";
	}
}
