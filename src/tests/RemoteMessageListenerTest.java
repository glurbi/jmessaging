package tests;

import interfaces.RemoteMessageChannel;
import interfaces.RemoteMessageListener;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


public class RemoteMessageListenerTest implements RemoteMessageListener<String> {

	private static final long serialVersionUID = 1L;
	
	private final AtomicLong totalReceived = new AtomicLong();

	public void onMessages(List<String> messages) {
		int size = messages.size();
		totalReceived.addAndGet(size);
		System.out.println("Received " + totalReceived + " messages.");
	}

	public void onMessage(String message) throws RemoteException {
		totalReceived.incrementAndGet();
		System.out.println("Received " + totalReceived + " messages.");
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("RemoteMessageListenerTest");
        Registry registry = LocateRegistry.getRegistry(RemoteMessageChannelTest.REGISTRY_PORT);
        RemoteMessageChannel<String> messageChannel = (RemoteMessageChannel<String>) registry.lookup("TestChannel");
        RemoteMessageListenerTest messageListener = new RemoteMessageListenerTest();
        UnicastRemoteObject.exportObject(messageListener, 0);
        messageChannel.subscribe(messageListener);
        System.in.read();
	}
	
}
