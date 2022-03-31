package br.ufc.great.caos.service.protocol.server.util.protocol.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.server.util.Utils;
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

					String encodedImage = JSONBody.getString("data");

					byte[] decodedImageByteArray = Base64.decode(encodedImage, Base64.DEFAULT);
					Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedImageByteArray, 0, decodedImageByteArray.length);
					Bitmap imagedWithBWFilter = Utils.convertImage(decodedImage);

					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					imagedWithBWFilter.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
					byte[] byteArray = byteStream.toByteArray();
					 encodedImage = Base64.encodeToString(byteArray,Base64.DEFAULT);

					elapsed = System.currentTimeMillis() - start;
					Log.i(isInstanceOf(), String.valueOf(elapsed));


					return newFixedLengthResponse(encodedImage);
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
