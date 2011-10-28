package tests;

import interfaces.RemoteMessageChannel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class RemoteVolatileMessageSenderTest {

	private static long MESSAGE_COUNT = 1000000;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("RemoteVolatileMessageSenderTest");
        Registry registry = LocateRegistry.getRegistry(RemoteMessageChannelTest.REGISTRY_PORT);
        RemoteMessageChannel<String> messageChannel = (RemoteMessageChannel<String>) registry.lookup("TestChannel");
		long t0 = System.currentTimeMillis();
		int messageCount = 0;
		messageChannel.publish(""+messageCount++);
		List<String> messages = new ArrayList<String>();
		while (messageCount < MESSAGE_COUNT) {
			messages.add(""+messageCount++);
			try {
				if (messageCount % 100000 == 0) {
					messageChannel.publish(messages);
					System.out.println("Published " + messageCount + " from RemoteVolatileMessageSenderTest.");
					messages.clear();
				}
			} catch (RemoteException e) {
				System.out.println("Failed at " + messageCount + ", reason: " + e.getMessage());
				System.exit(0);
			}
		}
		long t1 = System.currentTimeMillis();
		System.out.println("Published " + MESSAGE_COUNT + " messages in " + (t1-t0) + "ms");
	}
	
}
