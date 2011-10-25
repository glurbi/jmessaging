package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteMessageListener<T> extends Remote {
	public void onMessage(T message) throws RemoteException;
	public void onMessages(List<T> messages) throws RemoteException;
}
