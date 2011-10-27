package tests;

import implementations.AsynchronousMessageChannel;
import implementations.SynchronousMessageChannel;
import implementations.ThreadPerClientMessageChannel;
import interfaces.MessageChannel;
import interfaces.MessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class MessageChannelTest {

	private static void testMessageChannel(MessageChannel<String> messageChannel) throws Exception {
		System.out.println("Testing " + messageChannel.getClass().getName());
		
		class MessageListenerMock implements MessageListener<String> {
			public CountDownLatch latch;
			public List<String> recordedMessages = new ArrayList<String>();
			public void onMessage(String message) { recordedMessages.add(message); latch.countDown();  }
			public void onMessages(List<String> messages) { recordedMessages.addAll(messages); for (int i=0; i < recordedMessages.size(); i++) latch.countDown(); }
			public java.lang.String getName() { throw new UnsupportedOperationException(); }
			public void awaitLatch() throws Exception { latch.await(); }
		}
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
		
		messageListener.latch = new CountDownLatch(2);
		List<Object> ids = Arrays.<Object>asList(6, 7);
		List<String> messages = Arrays.asList("Message six", "Message seven");
		messageChannel.publish(ids, messages);
		messageListener.awaitLatch();
		assert messageListener.recordedMessages.size() == 5;
		assert messageListener.recordedMessages.get(3).equals("Message six");
		assert messageListener.recordedMessages.get(4).equals("Message seven");
		
		System.out.println("Testing " + messageChannel.getClass().getName() + " --> SUCCESS!");
	}

	private static void performanceTestMessageChannel(MessageChannel<String> messageChannel) throws Exception {
		System.out.println("Performance testing " + messageChannel.getClass().getName());
		
		class MessageListenerPerformanceMock implements MessageListener<String> {
			public CountDownLatch latch;
			public long expectedMessageCount;
			public AtomicLong messagesCount;
			public void onMessage(String message) { messagesCount.incrementAndGet(); check(); }
			public void onMessages(List<String> messages) { messagesCount.addAndGet(messages.size()); check(); }
			public java.lang.String getName() { throw new UnsupportedOperationException(); }
			private void check() { if (messagesCount.get() == expectedMessageCount) latch.countDown(); }
			public void awaitLatch() throws Exception { latch.await(); }
			private void reset(CountDownLatch latch, long expectedMessageCount, AtomicLong messagesCount) {
				this.latch = latch; this.expectedMessageCount = expectedMessageCount; this.messagesCount = messagesCount;
			}
		}
		
		MessageListenerPerformanceMock messageListener = new MessageListenerPerformanceMock();
		messageChannel.subscribe(messageListener);
		long max = 1000000;
		messageListener.reset(new CountDownLatch(1), max, new AtomicLong());
		long t0 = System.currentTimeMillis();
		for (long i = 0; i < max; i++) { messageChannel.publish(""+i); }
		messageListener.awaitLatch();
		long t1 = System.currentTimeMillis();
		System.out.println("Sending " + max + " messages to 1 listener took " + (t1-t0) + "ms");

		MessageListenerPerformanceMock messageListener2 = new MessageListenerPerformanceMock();
		messageChannel.subscribe(messageListener2);
		messageListener.reset(new CountDownLatch(1), max, new AtomicLong());
		messageListener2.reset(new CountDownLatch(1), max, new AtomicLong());
		t0 = System.currentTimeMillis();
		for (long i = 0; i < max; i++) { messageChannel.publish(""+i); }
		messageListener.awaitLatch();
		messageListener2.awaitLatch();
		t1 = System.currentTimeMillis();
		System.out.println("Sending " + max + " messages to 2 listener took " + (t1-t0) + "ms");
		
		System.out.println("Performance testing " + messageChannel.getClass().getName() + " --> SUCCESS!");
	}
	
	public static void main(String[] args) throws Exception {
		try {
			testMessageChannel(new SynchronousMessageChannel<String>());
			testMessageChannel(new AsynchronousMessageChannel<String>());
			testMessageChannel(new ThreadPerClientMessageChannel<String>());
			performanceTestMessageChannel(new SynchronousMessageChannel<String>());
			performanceTestMessageChannel(new AsynchronousMessageChannel<String>());
			performanceTestMessageChannel(new ThreadPerClientMessageChannel<String>());
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
