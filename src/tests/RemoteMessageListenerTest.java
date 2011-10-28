package tests;

import interfaces.RemoteMessageChannel;
import interfaces.RemoteMessageListener;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;


public class RemoteMessageListenerTest implements RemoteMessageListener<String> {

	private static final long serialVersionUID = 1L;

	public void onMessages(List<String> messages) {
		int size = messages.size();
		System.out.println("Received " + size + " messages. Last is " + messages.get(size-1));
	}

	public void onMessage(String message) throws RemoteException {
		System.out.println("Received " + message);
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("RemoteMessageListenerTest");
        Registry registry = LocateRegistry.getRegistry(12345);
        RemoteMessageChannel<String> messageChannel = (RemoteMessageChannel<String>) registry.lookup("TestChannel");
        RemoteMessageListenerTest messageListener = new RemoteMessageListenerTest();
        UnicastRemoteObject.exportObject(messageListener, 0);
        messageChannel.subscribe(messageListener);
        System.in.read();
	}
	
}
