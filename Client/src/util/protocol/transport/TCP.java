package util.protocol.transport;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import model.services.ProtocolService;

public class TCP implements ProtocolService{
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
		Socket s;
		try {
			s = new Socket(ip,port);
			din=new DataInputStream(s.getInputStream());  
			dout=new DataOutputStream(s.getOutputStream());  
			br=new BufferedReader(new InputStreamReader(System.in));  
			return true;
		} catch (UnknownHostException e) {
			System.out.println("TCP - Connection error: " + e.getMessage());
			return false;
		} catch (IOException e) {
			System.out.println("TCP - Connection error: " + e.getMessage());
			return false;
		}  

	}

	@Override
	public boolean disconnect() {
		try {
			din.close();
			s.close();
			return true;
		} catch (IOException e) {
			System.out.println("TCP - Error to disconnect:" + e.getMessage());
			return false;
		}
	}

	@Override
	public String sendMessage(String message) {
		try {
			dout.writeUTF(message);
			dout.flush();  
			return din.readUTF();  
		} catch (IOException e) {
			System.out.println("TCP - Error to send a message:"+e.getMessage());
			return "";
		}  

	}


}
