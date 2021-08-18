package br.ufc.great.caos.service.protocol.server.model.entity;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;

import io.quiche4j.Connection;
import io.quiche4j.http3.Http3Connection;
import io.quiche4j.http3.Http3Header;

public class Client {

    protected final static class PartialResponse {
        protected List<Http3Header> headers;
        protected byte[] body;
        protected long written;

        PartialResponse(List<Http3Header> headers, byte[] body, long written) {
            this.headers = headers;
            this.body = body;
            this.written = written;
        }
    }

    private final Connection conn;
    private Http3Connection h3Conn;
    private HashMap<Long, PartialResponse> partialResponses;
    private SocketAddress sender;

    public Client(Connection conn, SocketAddress sender) {
        this.conn = conn;
        this.sender = sender;
        this.h3Conn = null;
        this.partialResponses = new HashMap<>();
    }

    public final Connection connection() {
        return this.conn;
    }

    public final SocketAddress sender() {
        return this.sender;
    }

    public final Http3Connection http3Connection() {
        return this.h3Conn;
    }

    public final void setHttp3Connection(Http3Connection conn) {
        this.h3Conn = conn;
    }
}

