package implementations;

import interfaces.MessageChannel;
import interfaces.MessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPerClientMessageChannel<T> implements MessageChannel<T> {

	private final Object mutex = new Object();
	private final Map<MessageListener<T>, ExecutorService> listeners = new HashMap<MessageListener<T>, ExecutorService>();
	private final HashMap<Object, T> persistentMessages = new HashMap<Object, T>();

	public void publish(final T message) {
		synchronized (mutex) {
			for (Entry<MessageListener<T>, ExecutorService> entry : listeners.entrySet()) {
				final MessageListener<T> listener = entry.getKey();
				final ExecutorService executor = entry.getValue();
				executor.submit(new Runnable() {
					public void run() {
						listener.onMessage(message);
					}
				});
			}
		}
	}

	public void publish(Object id, final T message) {
		synchronized (mutex) {
			persistentMessages.put(id, message);
			for (Entry<MessageListener<T>, ExecutorService> entry : listeners.entrySet()) {
				final MessageListener<T> listener = entry.getKey();
				ExecutorService executor = entry.getValue();
				executor.submit(new Runnable() {
					public void run() {
						listener.onMessage(message);
					}
				});
			}
		}
	}

	public void publish(final List<Object> ids, final List<T> messages) {
		synchronized (mutex) {
			for (Entry<MessageListener<T>, ExecutorService> entry : listeners.entrySet()) {
				final MessageListener<T> listener = entry.getKey();
				ExecutorService executor = entry.getValue();
				for (int i = 0; i < ids.size(); i++) {
					Object id = ids.get(i);
					T message = messages.get(i);
					persistentMessages.put(id, message);
				}
				executor.submit(new Runnable() {
					public void run() {
						listener.onMessages(messages);
					}
				});
			}
		}
	}
	
	public void subscribe(final MessageListener<T> listener) {
		synchronized (mutex) {
			if (listeners.containsKey(listener)) {
				throw new IllegalArgumentException("You cannot subscribe twice with the same listener.");
			}
			ExecutorService executor = Executors.newSingleThreadExecutor();
			listeners.put(listener, executor);
			if (persistentMessages.size() > 0) {
				final List<T> previousMessages = new ArrayList<T>(persistentMessages.values());
				executor.submit(new Runnable() {
					public void run() {
						listener.onMessages(previousMessages);
					}
				});
			}
		}
	}

	public void unsubscribe(MessageListener<T> listener) {
		synchronized (mutex) {
			listeners.remove(listener);
		}
	}

}
