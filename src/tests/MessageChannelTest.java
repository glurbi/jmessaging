package tests;

import implementations.AsynchronousMessageChannel;
import implementations.SynchronousMessageChannel;
import interfaces.MessageChannel;
import interfaces.MessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MessageChannelTest {

	private static class MessageListenerMock implements MessageListener<String> {
		public CountDownLatch latch;
		public List<String> recordedMessages = new ArrayList<String>();
		public void onMessage(String message) { recordedMessages.add(message); latch.countDown();  }
		public void onMessages(List<String> messages) { recordedMessages.addAll(messages); for (int i=0; i < recordedMessages.size(); i++) latch.countDown(); }
		public java.lang.String getName() { throw new UnsupportedOperationException(); }
		public void awaitLatch() throws Exception { latch.await(); }
	}
	
	private static void testMessageChannel(MessageChannel<String> messageChannel) throws Exception {
		MessageListenerMock messageListener = new MessageListenerMock();
		
		messageListener.latch = new CountDownLatch(1);
		messageChannel.subscribe(messageListener);
		messageChannel.publish("Message one");
		messageListener.awaitLatch();
		assert messageListener.recordedMessages.size() == 1;
		assert messageListener.recordedMessages.get(0).equals("Message one");
		
		messageListener.latch = new CountDownLatch(2);
		messageChannel.publish(2, "Message two");
		messageChannel.publish(3, "Message three");
		messageListener.awaitLatch();
		assert messageListener.recordedMessages.size() == 3;
		assert messageListener.recordedMessages.get(2).equals("Message three");
		
		messageListener.latch = new CountDownLatch(3);
		messageChannel.unsubscribe(messageListener);
		messageChannel.publish(4, "Message four");
		messageChannel.publish("Message five");
		Thread.sleep(100); // make sure messages 4 and 5 finished published
		assert messageListener.recordedMessages.size() == 3;
		messageListener.recordedMessages.clear();
		messageChannel.subscribe(messageListener);
		messageListener.awaitLatch();
		assert messageListener.recordedMessages.size() == 3;
		assert messageListener.recordedMessages.contains("Message two");
		assert messageListener.recordedMessages.contains("Message three");
		assert messageListener.recordedMessages.contains("Message four");
		
		System.out.println(messageChannel.getClass().getName() + " --> SUCCESS!");
	}
	
	public static void main(String[] args) throws Exception {
		try {
			testMessageChannel(new SynchronousMessageChannel<String>());
			testMessageChannel(new AsynchronousMessageChannel<String>());
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
