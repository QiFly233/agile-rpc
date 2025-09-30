package com.qifly.core.transport;

import com.google.protobuf.Any;
import com.qifly.core.protocol.data.RpcBody;
import io.netty.channel.Channel;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface TransportClient extends Closeable {

    void connect(String host, int port) throws InterruptedException;

    default void connect(String endpoint) throws InterruptedException {
        String[] ss = endpoint.split(":");
        String host = ss[0];
        int port = Integer.parseInt(ss[1]);
        connect(host, port);
    }

    default void disconnect(String host, int port) throws InterruptedException {
        disconnect(host + ":" + port);
    }

    void disconnect(String endpoint) throws InterruptedException;

	default Channel getChannel(String host, int port) {
        return getChannel(host + ":" + port);
    }

    Channel getChannel(String endpoint);

    CompletableFuture<Any> send(String endpoint, RpcBody body);

	@Override
	void close();
}
