package util.protocol.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import model.entity.Protocol;

public class HTTP extends Protocol {
	private HttpServer server;

	@Override
	public boolean init() {
		try {
			server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
			return true;
		} catch (IOException e) {
			System.out.println("HTTP - Connection error:" + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean connect(String address) {
		try {

			server.createContext("/offloading", new Handler());
			server.setExecutor(null);
			server.start();
			System.out.println("HTTP - Server started on port 8001");
			return true;
		} catch (Exception e) {
			System.out.println("HTTP - Start server error:" + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean disconnect() {
		try {
			server.stop(0);
			System.out.println("HTTP - Server stopped");
			return true;
		} catch (Exception e) {
			System.out.println("HTTP - Finish connection error:" + e.getMessage());
			return false;
		}
	}

	class Handler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			System.out.println("HTTP - Serving the request");

			if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
				try {
					Headers requestHeaders = exchange.getRequestHeaders();
					Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();

					InputStream is = exchange.getRequestBody();
					
					ByteArrayOutputStream body = new ByteArrayOutputStream();
				    byte[] buffer = new byte[1024];
				    for (int l; (l = is.read(buffer)) != -1; ) {
				        body.write(buffer, 0, l);
				    }
				    
				    String response = "Welcome "+body.toString(StandardCharsets.UTF_8);


					exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
											
					OutputStream os = exchange.getResponseBody();

					os.write(response.getBytes());

					exchange.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

}
