package br.ufc.great.caos.service.protocol.server.model.services;

public interface ProtocolService {
	public boolean init();
	public boolean connect(String ip, Integer port);
	public boolean disconnect();
}
