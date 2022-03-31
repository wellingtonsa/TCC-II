package br.ufc.great.caos.service.protocol.server.util.protocol.transport;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.server.util.Utils;


public class TCP implements ProtocolService {

	ServerSocket ss;
	Socket s;
	DataInputStream din;
	DataOutputStream dout;
	BufferedReader br;

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		try {
			ss = new ServerSocket(port);
			s = ss.accept();
			din = new DataInputStream(s.getInputStream());
			dout = new DataOutputStream(s.getOutputStream());
			br = new BufferedReader(new InputStreamReader(System.in));
			new RequestHandler().execute();
			Log.i("TCP", "Server started on port " + port);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("TCP", "Connection error: " + e.getMessage());
			return false;
		}

	}

	@Override
	public boolean disconnect() {
		try {
			din.close();
			s.close();
			ss.close();
			return true;
		} catch (IOException e) {
			Log.i("TCP", "Error to disconnect:" + e.getMessage());
			return false;
		}

	}

	private class RequestHandler extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... voids) {
			String request = "";

			try {

				while (true) {

					request = din.readUTF();

					if(!request.isEmpty()) {
						String encodedImage = request;

						byte[] decodedImageByteArray = Base64.decode(encodedImage, Base64.DEFAULT);
						Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedImageByteArray, 0, decodedImageByteArray.length);
						Bitmap imagedWithBWFilter = Utils.convertImage(decodedImage);

						ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
						imagedWithBWFilter.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
						byte[] byteArray = byteStream.toByteArray();
						encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

						dout.writeUTF(encodedImage);
						dout.flush();

						return encodedImage;
					}
				}
			} catch (IOException e) {
				Log.i("TCP", "Error to proccess:" + e.getMessage());
				e.printStackTrace();
				return "";
			}


		}
	}

	@Override
	public String isInstanceOf() {
		return "TCP";
	}
}
