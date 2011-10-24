package test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import local.AsynchronousMessageChannel;
import local.MessageListener;

public class LocalMessageChannelTest1 {

	private static long MESSAGE_COUNT = 1000000;
	private static CountDownLatch receivedLatch = new CountDownLatch(1);
	private static AsynchronousMessageChannel<Long> messageChannel = new AsynchronousMessageChannel<Long>();
	
	private static class MessageListenerTest1 implements MessageListener<Long> {
		private long messageCount = 0;
		public void onMessage(Long message) {
			messageCount++;
			checkFinished();
		}
		public void onMessages(List<Long> messages) {
			messageCount += messages.size();
			checkFinished();
		}
		private void checkFinished() {
			if (messageCount == MESSAGE_COUNT) {
				receivedLatch.countDown();
			}
		}
		public String getName() {
			return "MessageListenerTest1";
		}
	}
	
	public static void main(String[] args) throws Exception {
		messageChannel.subscribe(new MessageListenerTest1());
		long t0 = System.currentTimeMillis();
		for (long i = 1; i <= MESSAGE_COUNT; i++) {
			messageChannel.publish(i);
		}
		long t1 = System.currentTimeMillis();
		System.out.println("Published " + MESSAGE_COUNT + " messages in " + (t1-t0) + "ms");
		receivedLatch.await();
		long t2 = System.currentTimeMillis();
		System.out.println("Distributed " + MESSAGE_COUNT + " messages in " + (t2-t0) + "ms");
		System.exit(0);
	}
	
}
