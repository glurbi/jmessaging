package tests;

import implementations.RmiMessageChannel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RmiMessageChannelTest1 {
	
	public static void main(String[] args) throws Exception {
		System.out.println("RmiMessageChannelTest");
		RmiMessageChannel<String> messageChannel = new RmiMessageChannel<String>();
        Registry registry = LocateRegistry.createRegistry(12345);
        registry.bind("TestChannel", messageChannel);
	}

}
