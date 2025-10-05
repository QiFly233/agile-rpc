package com.qifly.core.transport;

import com.qifly.core.protocol.frame.RpcFrame;
import io.netty.channel.Channel;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface TransportClient extends Closeable {

    void connect(String host, int port);

    default void connect(String endpoint) {
        String[] ss = endpoint.split(":");
        String host = ss[0];
        int port = Integer.parseInt(ss[1]);
        connect(host, port);
    }

    default void disconnect(String host, int port) {
        disconnect(host + ":" + port);
    }

    void disconnect(String endpoint);

	default Channel getChannel(String host, int port) {
        return getChannel(host + ":" + port);
    }

    Channel getChannel(String endpoint);

    CompletableFuture<RpcFrame> send(String endpoint, byte[] body, int protocolType);

	@Override
	void close();
}
