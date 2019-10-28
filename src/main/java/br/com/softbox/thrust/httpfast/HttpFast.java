package br.com.softbox.thrust.httpfast;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import br.com.softbox.thrust.api.thread.LocalWorkerThreadPool;

/**
 * Specialization of the SelectSockets class which uses a thread pool to service
 * channels. The thread pool is an ad-hoc implementation quicky lashed togther
 * in a few hours for demonstration purposes. It's definitely not production
 * quality.
 *
 * @author Ron Hitchens (ron@ronsoft.com)
 */
public class HttpFast extends SelectSockets {

	private final LocalWorkerThreadPool pool;
	private final HttpFastWorkerBuilder threadBuilder;

	private static HttpFast instance;

	private HttpFast(int minThreads, int maxThreads, String rootPath, String routesFilePath, String middlewaresFilePath,
			String afterRequestFnFilePath) throws Exception {
		this.threadBuilder = new HttpFastWorkerBuilder(routesFilePath, middlewaresFilePath, afterRequestFnFilePath);
		this.pool = new LocalWorkerThreadPool(minThreads, maxThreads, rootPath, this.threadBuilder);
	}

	public static synchronized HttpFast startServer(int minThreads, int maxThreads, String rootPath,
			String routesFilePath, String middlewaresFilePath, String afterRequestFnFilePath) throws Exception {
		if (instance != null) {
			throw new RuntimeException("Server already started");
		}
		instance = new HttpFast(minThreads, maxThreads, rootPath, routesFilePath, middlewaresFilePath,
				afterRequestFnFilePath);
		return instance;
	}
	
	public static synchronized HttpFast getInstance() {
		return instance;
	}

	/**
	 * Sample data handler method for a channel with data ready to read. This method
	 * is invoked from the go( ) method in the parent class. This handler delegates
	 * to a worker thread in a thread pool to service the channel, then returns
	 * immediately.
	 *
	 * @param key A SelectionKey object representing a channel determined by the
	 *            selector to be ready for reading. If the channel returns an EOF
	 *            condition, it is closed here, which automatically invalidates the
	 *            associated key. The selector will then de-register the channel on
	 *            the next select call.
	 */
	protected void readDataFromSocket(SelectionKey key) throws IOException {
		HttpFastWorkerThread worker = (HttpFastWorkerThread) pool.getThrustWorkerThread();
		if (worker != null) {
			worker.serviceChannel(key);
		}
	}
}