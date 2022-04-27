package br.ufc.great.caos.service.protocol.client.util.protocol.application;


import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;

public class HTTP implements ProtocolService {

	private URL url;
	private HttpURLConnection con;
	long start = System.currentTimeMillis();
	long elapsed = 0;

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			url = new URL("http://"+ip+":"+port+"/offloading");

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
		if(con != null) con.disconnect();
		return true;

	}

	@Override
	public Object executeOffload(InvocableMethod method) {
		/*start = System.currentTimeMillis();
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			byte[] out = String.valueOf("{\"data\": \""+message+"\"}").getBytes();
			int length = out.length;
			con.setFixedLengthStreamingMode(length);
			con.connect();

			try(OutputStream os = con.getOutputStream()) {
				os.write(out);

				ByteArrayOutputStream result = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				for (int l; (l = con.getInputStream().read(buffer)) != -1; ) {
					result.write(buffer, 0, l);
				}
				elapsed = System.currentTimeMillis() - start;
				Log.i(isInstanceOf(), String.valueOf(elapsed));
				return result.toString("UTF-8");
			}

		} catch (UnsupportedEncodingException e) {
			Log.i("HTTP", "Sending messsage error:"+e.getMessage());
			return "";

		} catch (IOException ioe) {
			Log.i("HTTP", "Sending messsage error:"+ioe.getMessage());
			return "";
		}
	*/
		return null;
	}

	@Override
	public String isInstanceOf() {
		return "HTTP";
	}
}
