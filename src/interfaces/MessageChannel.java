package interfaces;

import java.util.List;

public interface MessageChannel<T> {
	public void publish(T message);
	public void publish(List<T> messages);
	public void publish(Object id, T message);
	public void publish(List<Object> ids, List<T> messages);
	public void subscribe(MessageListener<T> listener);
	public void unsubscribe(MessageListener<T> listener);
}
