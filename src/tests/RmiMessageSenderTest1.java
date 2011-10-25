package tests;

import interfaces.RemoteMessageChannel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;


public class RmiMessageSenderTest1 {

	private static long MESSAGE_COUNT = 1000000;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("RmiMessageSenderTest");
        Registry registry = LocateRegistry.getRegistry(12345);
        RemoteMessageChannel<String> messageChannel = (RemoteMessageChannel<String>) registry.lookup("TestChannel");
		long t0 = System.currentTimeMillis();
		List<String> messages = new ArrayList<String>(100000);
		for (long i = 1; i <= MESSAGE_COUNT; i++) {
			messages.add("" + i);
			if (i % 100000 == 0) {
				try {
					System.out.println("Published " + messages.get(messages.size()-1) + " from MessageProducerTest.");
					messageChannel.publish(messages);
					messageChannel.publish(i, messages.get(messages.size()-1));
					messages.clear();
				} catch (RemoteException e) {
					System.out.println("Failed at " + i);
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		long t1 = System.currentTimeMillis();
		System.out.println("Published " + MESSAGE_COUNT + " messages in " + (t1-t0) + "ms");
	}
	
}
