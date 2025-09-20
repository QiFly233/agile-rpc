package com.qifly.core.transport;

import java.io.Closeable;

public interface TransportServer extends Closeable {

	void start() throws Exception;

	@Override
	void close();
}
