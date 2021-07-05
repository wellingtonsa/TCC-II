package br.ufc.great.caos.service.protocol.server.util.protocol.application;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;


public class MQTT implements ProtocolService {


	@Override
	public boolean init() {
		return false;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		return false;
	}

	@Override
	public boolean disconnect() {
		return false;
	}
}
