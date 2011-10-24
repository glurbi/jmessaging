package remote;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RmiMessageChannel<T> extends UnicastRemoteObject implements RemoteMessageChannel<T>, Serializable {

	public RmiMessageChannel() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = 1L;
	
	private final Object mutex = new Object();
	private final HashMap<RemoteMessageListener<T>, ExecutorService> executors = new HashMap<RemoteMessageListener<T>, ExecutorService>();

	public void publish(final List<T> messages) throws RemoteException {
		synchronized (mutex) {
			for (Entry<RemoteMessageListener<T>, ExecutorService> e : executors.entrySet()) {
				final ExecutorService executor = e.getValue();
				final RemoteMessageListener<T> listener = e.getKey();
				executor.submit(new Runnable() {
					public void run() {
						try {
							listener.onMessages(messages);
						} catch (Exception re) {
							re.printStackTrace();
							executors.remove(listener);
						}
					}
				});
			}
		}
	}

	public void subscribe(RemoteMessageListener<T> listener) throws RemoteException {
		synchronized (mutex) {
			if (executors.containsKey(listener)) {
				throw new IllegalArgumentException("You cannot subscribe twice the same listener.");
			}
			executors.put(listener, Executors.newSingleThreadExecutor());
		}
	}

	public void stop() {
		synchronized (mutex) {
			for (ExecutorService executor : executors.values()) {
				executor.shutdown();
			}
		}
	}
	
	public void awaitTermination() throws InterruptedException {
		synchronized (mutex) {
			for (ExecutorService executor : executors.values()) {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			}
		}
	}
	
}
