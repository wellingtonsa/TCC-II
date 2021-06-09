package model.services;

public interface ProtocolService {
	public boolean init();
	public boolean connect(String address);
	public boolean disconnect();
}
