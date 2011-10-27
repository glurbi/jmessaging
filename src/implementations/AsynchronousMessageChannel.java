package implementations;

import interfaces.MessageChannel;
import interfaces.MessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsynchronousMessageChannel<T> implements MessageChannel<T> {

	private final Object mutex = new Object();
	private final Set<MessageListener<T>> listeners = new HashSet<MessageListener<T>>();
	private final HashMap<Object, T> persistentMessages = new HashMap<Object, T>();
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

	public void publish(final Object id, final T message) {
		executor.submit(new Runnable() {
			public void run() {
				synchronized (mutex) {
					persistentMessages.put(id, message);
					for (MessageListener<T> listener : listeners) {
						listener.onMessage(message);
					}
				}
			}
		});
	}

	public void publish(final List<T> messages) {
		executor.submit(new Runnable() {
			public void run() {
				synchronized (mutex) {
					for (MessageListener<T> listener : listeners) {
						listener.onMessages(messages);
					}
				}
			}
		});
	}

	public void publish(final List<Object> ids, final List<T> messages) {
		executor.submit(new Runnable() {
			public void run() {
				synchronized (mutex) {
					for (int i = 0; i < ids.size(); i++) {
						Object id = ids.get(i);
						T message = messages.get(i);
						persistentMessages.put(id, message);
						for (MessageListener<T> listener : listeners) {
							listener.onMessage(message);
						}
					}
				}
			}
		});
	}
	
	public void subscribe(final MessageListener<T> listener) {
		synchronized (mutex) {
			if (listeners.contains(listener)) {
				throw new IllegalArgumentException("You cannot subscribe twice with the same listener.");
			}
			if (persistentMessages.size() > 0) {
				final List<T> previousMessages = new ArrayList<T>(persistentMessages.values());
				executor.submit(new Runnable() {
					public void run() {
						listener.onMessages(previousMessages);
					}
				});
			}
			listeners.add(listener);
		}
	}

	public void unsubscribe(MessageListener<T> listener) {
		synchronized (mutex) {
			listeners.remove(listener);
		}
	}

}
