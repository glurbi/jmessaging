package test;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import local.LocalMessageChannel;
import local.MessageChannel;
import local.MessageListener;


public class LocalMessageChannelTest2 {

	private static long MESSAGE_COUNT = 2000000;
	private static int LISTENER_COUNT = 4;
	private static CountDownLatch latch = new CountDownLatch(LISTENER_COUNT);
	
	private static class MessageListenerTest implements MessageListener<Long> {
		private static long instanceCount = 0;
		private final long instanceNo = ++instanceCount;
		private long messageCount = 0;
		public void onMessage(Long message) {
			messageCount++;
			checkFinished();
		}
		public void onMessages(List<Long> messages) {
			messageCount += messages.size();
			System.out.println("Received once " + messages.size() + " in MessageListener " + instanceNo);
			checkFinished();
		}
		private void checkFinished() {
			if (messageCount == MESSAGE_COUNT) {
				System.out.println("Received " + messageCount + " in MessageListener " + instanceNo);
				latch.countDown();
			}
		}
	}
	
	private static class MessageProducerTest implements Runnable {
		private final MessageChannel<Long> messageChannel;
		public MessageProducerTest(MessageChannel<Long> messageChannel) {
			this.messageChannel = messageChannel;
			new Thread(this).start();
		}
		public void run() {
			long t0 = System.currentTimeMillis();
			for (long i = 1; i <= MESSAGE_COUNT; i++) {
				messageChannel.publish(i, i);
			}
			long t1 = System.currentTimeMillis();
			System.out.println("Published " + MESSAGE_COUNT + " messages in " + (t1-t0) + "ms");
		}
	}
	
	public static void main(String[] args) throws Exception {
		LocalMessageChannel<Long> messageChannel = new LocalMessageChannel<Long>();
		new MessageProducerTest(messageChannel);
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < LISTENER_COUNT; i++) {
			messageChannel.subscribe(new MessageListenerTest());
			Thread.sleep(200);
		}
		latch.await();
		long t1 = System.currentTimeMillis();
		System.out.println("Received " + MESSAGE_COUNT + " messages in " + (t1-t0) + "ms");
		System.exit(0);
	}
	
}
