package implementations;

import interfaces.MessageChannel;
import interfaces.MessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForegroundThreadMessageChannel<T> implements MessageChannel<T> {

	private final Object mutex = new Object();
	private final Set<MessageListener<T>> listeners = new HashSet<MessageListener<T>>();
	private final HashMap<Object, T> persistentMessages = new HashMap<Object, T>();

	public void publish(final T message) {
		synchronized (mutex) {
			for (MessageListener<T> listener : listeners) {
				listener.onMessage(message);
			}
		}
	}

	public void publish(final Object id, final T message) {
		synchronized (mutex) {
			persistentMessages.put(id, message);
			for (MessageListener<T> listener : listeners) {
				listener.onMessage(message);
			}
		}
	}

	public void publish(List<T> messages) {
		synchronized (mutex) {
			for (MessageListener<T> listener : listeners) {
				listener.onMessages(messages);
			}
		}
	}

	public void publish(final List<Object> ids, final List<T> messages) {
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
	
	public void subscribe(final MessageListener<T> listener) {
		synchronized (mutex) {
			if (listeners.contains(listener)) {
				throw new IllegalArgumentException("You cannot subscribe twice with the same listener.");
			}
			if (persistentMessages.size() > 0) {
				List<T> previousMessages = new ArrayList<T>(persistentMessages.values());
				listener.onMessages(previousMessages);
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
