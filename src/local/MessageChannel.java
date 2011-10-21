package local;

public interface MessageChannel<T> {
	public void publish(T message);
	public void publish(Object id, T message);
	public void subscribe(MessageListener<T> listener);
}
