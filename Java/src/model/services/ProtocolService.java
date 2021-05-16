package model.services;

public interface ProtocolService {
	public boolean connectServer();
	public boolean connectClient();
	public boolean disconnectServer();
	public boolean disconnectClient();
	public void sendMessage();
}
