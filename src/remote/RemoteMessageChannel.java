package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteMessageChannel<T> extends Remote {
	public void publish(List<T> messages) throws RemoteException;
	public void subscribe(RemoteMessageListener<T> listener) throws RemoteException;
}
