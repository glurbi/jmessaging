package test;
import java.util.concurrent.CountDownLatch;

import local.LocalMessageChannel;
import local.MessageChannel;
import local.MessageListener;


public class LocalMessageChannelTest {

	private static long MESSAGE_COUNT = 1000000;
	
	private static class MessageListenerTest implements MessageListener<Long> {
		private static long instanceCount = 0;
		private final long instanceNo = ++instanceCount;
		public void onMessage(Long message) {
			if (message % 100000 == 0) {
				System.out.println("Received " + message + " in MessageListener " + instanceNo);
			}
		}
	}
	
	private static class MessageProducerTest implements Runnable {
		private static long instanceCount = 0;
		private final MessageChannel<Long> messageChannel;
		private final long instanceNo = ++instanceCount;
		private final CountDownLatch latch = new CountDownLatch(1);
		public MessageProducerTest(MessageChannel<Long> messageChannel) {
			this.messageChannel = messageChannel;
		}
		public void run() {
			for (long i = 1; i <= MESSAGE_COUNT; i++) {
				messageChannel.publish(i);
				if (i % 100000 == 0) {
					System.out.println("Sent " + i + " in MessageProducerTest " + instanceNo);
				}
			}
			latch.countDown();
		}
		public void start() {
			new Thread(this).start();
		}
		public void awaitTermination() throws InterruptedException {
			latch.await();
		}
	}
	
	public static void main(String[] args) throws Exception {
		LocalMessageChannel<Long> messageChannel = new LocalMessageChannel<Long>();
		MessageProducerTest messageProducer = new MessageProducerTest(messageChannel);
		messageChannel.subscribe(new MessageListenerTest());
		messageChannel.subscribe(new MessageListenerTest());
		messageChannel.subscribe(new MessageListenerTest());
		messageChannel.subscribe(new MessageListenerTest());
		long t0 = System.currentTimeMillis();
		messageProducer.start();
		messageProducer.awaitTermination();
		long t1 = System.currentTimeMillis();
		System.out.println("***** Published " + MESSAGE_COUNT + " messages in " + (t1-t0) + "ms");
		messageChannel.stop();
		messageChannel.awaitTermination();
		long t2 = System.currentTimeMillis();
		System.out.println("***** Distributed " + MESSAGE_COUNT + " messages in " + (t2-t0) + "ms");
	}
	
}
