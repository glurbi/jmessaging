package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteMessageListener<T> extends Remote {
	public void onMessages(List<T> messages) throws RemoteException;
}
