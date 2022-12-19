package br.ufc.great.caos.service.protocol.client.util.protocol.network;

import br.ufc.great.caos.service.protocol.client.model.services.ProtocolService;
import br.ufc.great.caos.service.protocol.core.offload.InvocableMethod;

public class IPv4 implements ProtocolService {
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

    @Override
    public Object executeOffload(InvocableMethod method) {
        return null;
    }

    @Override
    public String isInstanceOf() {
        return null;
    }
}
