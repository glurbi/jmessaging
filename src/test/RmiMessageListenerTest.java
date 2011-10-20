package test;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import remote.RemoteMessageChannel;
import remote.RemoteMessageListener;

public class RmiMessageListenerTest implements RemoteMessageListener<String> {

	private static final long serialVersionUID = 1L;

	public void onMessages(List<String> messages) {
		System.out.println("Received " + messages.get(messages.size()-1));
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("RemoteMessageListenerTest");
        Registry registry = LocateRegistry.getRegistry(12345);
        RemoteMessageChannel<String> messageChannel = (RemoteMessageChannel<String>) registry.lookup("TestChannel");
        RmiMessageListenerTest messageListener = new RmiMessageListenerTest();
        UnicastRemoteObject.exportObject(messageListener, 0);
        messageChannel.subscribe(messageListener);
        System.in.read();
	}
	
}
