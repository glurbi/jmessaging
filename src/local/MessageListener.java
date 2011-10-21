package local;

import java.util.List;

public interface MessageListener<T> {
	public void onMessage(T message);
	public void onMessages(List<T> messages);
}
