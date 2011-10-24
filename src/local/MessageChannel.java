package local;

import java.util.List;

public interface MessageChannel<T> {
	public void publish(T message);
	public void publish(Object id, T message);
	public void publish(List<Object> ids, List<T> messages);
	public void subscribe(MessageListener<T> listener);
}
