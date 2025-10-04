package com.qifly.core.transport;

import com.qifly.core.exception.TransportException;

import java.io.Closeable;

public interface TransportServer extends Closeable {

	void start() throws TransportException, InterruptedException;

	@Override
	void close();
}
