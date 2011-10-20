package local;

public interface MessageListener<T> {
	public void onMessage(T message);
}
