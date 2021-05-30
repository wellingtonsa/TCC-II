package util.protocol.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import model.entity.Protocol;

public class HTTP extends Protocol{
	private URL url;
	private HttpURLConnection con;
	
	@Override
	public boolean init() {
		return true;
	}
	
	@Override
	public boolean connect(String address) {
		try {
			url = new URL(address+"/offloading");
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			
			return true;
		} catch (IOException e) {
			System.out.println("Connection error:"+e.getMessage());
			return false;
		}
		
	}

	@Override
	public boolean desconnect() {
		return false;

	}

	@Override
	public String sendMessage(String message) {
		try {
			con.setDoOutput(true);
			
			byte[] out = message.getBytes(StandardCharsets.UTF_8);
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
			    
				return result.toString(StandardCharsets.UTF_8);
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
			return "";
		}
		
	}

}
