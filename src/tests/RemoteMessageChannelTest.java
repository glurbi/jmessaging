package tests;

import implementations.BackgroundThreadMessageChannel;
import implementations.RemoteMessageChannelImpl;
import interfaces.MessageChannel;
import interfaces.RemoteMessageChannel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RemoteMessageChannelTest {
	
	public static int REGISTRY_PORT = 12345;
	
	public static void main(String[] args) throws Exception {
		System.out.println("RemoteMessageChannelTest");
		MessageChannel<String> messageChannel = new BackgroundThreadMessageChannel<String>();
		RemoteMessageChannel<String> remoteMessageChannel = new RemoteMessageChannelImpl<String>(messageChannel);
        Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
        registry.bind("TestChannel", remoteMessageChannel);
	}

}