package local;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class LocalMessageChannel<T> implements MessageChannel<T> {

	private final Object mutex = new Object();
	private final Set<MessageListener<T>> listeners = new HashSet<MessageListener<T>>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public void publish(final T message) {
		executor.submit(new Runnable() {
			public void run() {
				synchronized (mutex) {
					for (MessageListener<T> listener : listeners) {
						listener.onMessage(message);
					}
				}
			}
		});
	}

	public void subscribe(MessageListener<T> listener) {
		synchronized (mutex) {
			if (listeners.contains(listener)) {
				throw new IllegalArgumentException("You cannot subscribe twice the same listener.");
			}
			listeners.add(listener);
		}
	}
	
	public void stop() {
		executor.shutdown();
	}
	
	public void awaitTermination() throws InterruptedException {
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
	}
	
}
