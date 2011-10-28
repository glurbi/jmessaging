package implementations;

import interfaces.MessageChannel;
import interfaces.MessageListener;
import interfaces.RemoteMessageChannel;
import interfaces.RemoteMessageListener;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteMessageChannelImpl<T> extends UnicastRemoteObject implements RemoteMessageChannel<T>, Serializable {

	private static final long serialVersionUID = 1L;

	private final Object mutex = new Object();
	private final MessageChannel<T> messageChannel;
	private final Set<RemoteMessageListener<T>> listeners = new HashSet<RemoteMessageListener<T>>();

	public RemoteMessageChannelImpl(MessageChannel<T> messageChannel) throws RemoteException {
		super();
		this.messageChannel = messageChannel;
	}
	
	public void publish(List<T> messages) throws RemoteException {
		messageChannel.publish(messages);
	}
	
	public void publish(Object id, T message) throws RemoteException {
		messageChannel.publish(id, message);
	}

	public void subscribe(RemoteMessageListener<T> listener) throws RemoteException {
		synchronized (mutex) {
			if (listeners.contains(listener)) {
				throw new IllegalArgumentException("You cannot subscribe twice the same listener.");
			}
			listeners.add(listener);
			messageChannel.subscribe(new MessageListenerProxy(listener));
		}
	}

	private class MessageListenerProxy implements MessageListener<T> {
		private final RemoteMessageListener<T> remoteMessageListener;
		private final ExecutorService executor = Executors.newSingleThreadExecutor();
		public MessageListenerProxy(RemoteMessageListener<T> remoteMessageListener) {
			this.remoteMessageListener = remoteMessageListener;
		}
		public void onMessage(final T message) {
			executor.submit(new Runnable() {
				public void run() {
					try {
						remoteMessageListener.onMessage(message);
					} catch (RemoteException e) {
						messageChannel.unsubscribe(MessageListenerProxy.this);
					}
				}
			});
		}
		public void onMessages(final List<T> messages) {
			executor.submit(new Runnable() {
				public void run() {
					try {
						remoteMessageListener.onMessages(messages);
					} catch (RemoteException e) {
						messageChannel.unsubscribe(MessageListenerProxy.this);
					}
				}
			});
		}
		public String getName() {
			return null;
		}
	}
	
}
