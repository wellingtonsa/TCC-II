package br.ufc.great.caos.service.protocol.client.util.protocol.application;


import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;

public class HTTP implements ProtocolService {

	private URL url;
	private HttpURLConnection con;

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			url = new URL("http://"+ip+":"+port+"/offloading");
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			Log.i("HTTP", "Connected to the server ("+ip+") at port: "+port);
			return true;
		} catch (IOException e) {
			Log.i("HTTP", "Connection error:"+e.getMessage());
			return false;
		}
		catch (Exception e) {
			Log.i("HTTP", "Connection error:"+e.getMessage());
			return false;
		}

	}

	@Override
	public boolean disconnect() {
		return false;

	}

	@Override
	public String sendMessage(String message) {
		try {

			con.setDoOutput(true);
			byte[] out = String.valueOf("{\"username\": "+message+"}").getBytes("UTF-8");
			int length = out.length;
			con.setFixedLengthStreamingMode(length);
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.connect();
			try(OutputStream os = con.getOutputStream()) {
				os.write(out);

				ByteArrayOutputStream result = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				for (int l; (l = con.getInputStream().read(buffer)) != -1; ) {
					result.write(buffer, 0, l);
				}

				return result.toString("UTF-8");
			}

		} catch (UnsupportedEncodingException e) {
			Log.i("HTTP", "Sending messsage error:"+e.getMessage());
			return "";

		} catch (IOException ioe) {
			Log.i("HTTP", "Sending messsage error:"+ioe.getMessage());
			return "";
		}

	}

	@Override
	public String isInstanceOf() {
		return "HTTP";
	}
}