package br.ufc.great.caos.service.protocol.client.model.services;

import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;

public interface ProtocolService {
	public boolean init();
	public boolean connect(String ip, Integer port);
	public boolean disconnect();
	public Object executeOffload(InvocableMethod method);
	public String isInstanceOf();
}
