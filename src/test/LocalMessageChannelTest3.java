package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import local.LocalMessageChannel;
import local.MessageChannel;
import local.MessageListener;

public class LocalMessageChannelTest3 {

	private static long MESSAGE_COUNT = 1000000;
	private static int LISTENER_COUNT = 4;
	private static CountDownLatch receivedLatch = new CountDownLatch(LISTENER_COUNT);
	private static LocalMessageChannel<Long> messageChannel = new LocalMessageChannel<Long>();
	
	private static class MessageListenerTest2 implements MessageListener<Long> {
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
			return "MessageListenerTest2";
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
			List<Object> ids = new ArrayList<Object>();
			List<Long> messages = new ArrayList<Long>();
			for (long i = 1; i <= MESSAGE_COUNT; i++) {
				ids.add(i);
				messages.add(i);
			}
			messageChannel.publish(ids, messages);
			long t1 = System.currentTimeMillis();
			System.out.println("Published " + MESSAGE_COUNT + " messages in " + (t1-t0) + "ms");
		}
	}
	
	public static void main(String[] args) throws Exception {
		new MessageProducerTest(messageChannel);
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < LISTENER_COUNT; i++) {
			messageChannel.subscribe(new MessageListenerTest2());
			Thread.sleep(200);
		}
		receivedLatch.await();
		long t1 = System.currentTimeMillis();
		System.out.println("Received " + MESSAGE_COUNT + " messages in " + LISTENER_COUNT + " listeners in " + (t1-t0) + "ms");
		System.exit(0);
	}
	
}
