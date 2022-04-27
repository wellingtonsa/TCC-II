package br.ufc.great.caos.service.protocol.server.util.protocol.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;
import br.ufc.great.caos.service.protocol.core.offload.RemoteMethodExecutionService;
import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.server.util.Utils;
import fi.iki.elonen.NanoHTTPD;

public class HTTP implements ProtocolService {

	HttpService server;
	Context context;

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port, Context context) {
		try {
			this.context = context;
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
		server.stop();
		Log.i("HTTP", "Server stopped");
		return true;
	}


	public class HttpService extends NanoHTTPD {
		public HttpService(int port){
			super(port);
		}

		@Override
		public Response serve(IHTTPSession session) {

			Log.i("HTTP", "Received message");

			String uri = session.getUri();
			final HashMap<String, String> body = new HashMap<String, String>();
			if (Method.POST.equals(session.getMethod())) {
				try {
					session.parseBody(body);

					JSONObject JSONBody = new JSONObject(body.get("postData"));
					long start = System.currentTimeMillis();
					long elapsed = 0;

					InvocableMethod request = new Gson().fromJson(JSONBody.toString(), InvocableMethod.class);


					RemoteMethodExecutionService remoteMethodExecution = new RemoteMethodExecutionService(context);
					Object response = remoteMethodExecution.executeMethod(request);

					return newFixedLengthResponse(new Gson().toJson(response));
				} catch (IOException e) {
					Log.i("HTTP", "Receiving message error: "+e.getMessage());
					e.printStackTrace();
				} catch (ResponseException e) {
					Log.i("HTTP", "Receiving message error: "+e.getMessage());
					e.printStackTrace();
				} catch (JSONException e) {
					Log.i("HTTP", "Receiving message error: "+e.getMessage());
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
