package br.ufc.great.caos.service.protocol.server.util.protocol.network;

import android.content.Context;

import br.ufc.great.caos.service.protocol.server.model.services.ProtocolService;

public class IPv4 implements ProtocolService {
    @Override
    public boolean init() {
        return false;
    }

    @Override
    public boolean connect(String ip, Integer port, Context context) {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public String isInstanceOf() {
        return null;
    }
}
