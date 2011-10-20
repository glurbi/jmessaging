package test;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import remote.RmiMessageChannel;

public class RmiMessageChannelTest {
	
	public static void main(String[] args) throws Exception {
		System.out.println("RmiMessageChannelTest");
		RmiMessageChannel<String> messageChannel = new RmiMessageChannel<String>();
        Registry registry = LocateRegistry.createRegistry(12345);
        registry.bind("TestChannel", messageChannel);
	}

}
