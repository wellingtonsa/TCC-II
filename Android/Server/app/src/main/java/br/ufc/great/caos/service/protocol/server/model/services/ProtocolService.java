package br.ufc.great.caos.service.protocol.server.model.services;

import android.content.Context;

public interface ProtocolService {
	public boolean init();
	public boolean connect(String ip, Integer port, Context context);
	public boolean disconnect();
	public String isInstanceOf();
}
