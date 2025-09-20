package com.qifly.core.transport;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.netty.channel.Channel;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface TransportClient extends Closeable {

	void connect(String host, int port) throws Exception;

	Channel getChannel(String host, int port);

    CompletableFuture<Any> send(int rpcId, Message req);

	@Override
	void close();
}
