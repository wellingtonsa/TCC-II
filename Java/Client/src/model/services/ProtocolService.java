package model.services;

public interface ProtocolService {
	public boolean init();
	public boolean connect(String ip, Integer port);
	public boolean disconnect();
	public String sendMessage(String message);
}
