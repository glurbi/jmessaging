// Copyright 2011 Vincent Gay
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package tests;

import implementations.BackgroundThreadMessageChannel;
import implementations.CurrentThreadMessageChannel;
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

	private static void performanceTestMessageChannel(final MessageChannel<String> messageChannel) throws Exception {
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
		
		//
		// one producer, one consumer
		//
		MessageListenerPerformanceMock messageListener = new MessageListenerPerformanceMock();
		messageChannel.subscribe(messageListener);
		final long max = 1000000;
		messageListener.reset(new CountDownLatch(1), max, new AtomicLong());
		long t0 = System.currentTimeMillis();
		for (long i = 0; i < max; i++) { messageChannel.publish(""+i); }
		messageListener.awaitLatch();
		long t1 = System.currentTimeMillis();
		System.out.println("Sending " + max + " messages to 1 listener took " + (t1-t0) + "ms");

		//
		// one producer, two consumer
		//
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

		//
		// one producer, two consumers, batched
		//
		messageListener.reset(new CountDownLatch(1), max, new AtomicLong());
		messageListener2.reset(new CountDownLatch(1), max, new AtomicLong());
		t0 = System.currentTimeMillis();
		for (long i = 0; i < 100; i++) {
			List<String> batch = new ArrayList<String>((int) (max / 100));
			for (long j = 0; j < max / 100; j++) { batch.add(""+i*j); }
			messageChannel.publish(batch);
		}
		messageListener.awaitLatch();
		messageListener2.awaitLatch();
		t1 = System.currentTimeMillis();
		System.out.println("Sending " + max + " messages in 100 batches to 2 listener took " + (t1-t0) + "ms");
	
		//
		// five producer, five consumers, persistent messages
		//
		messageChannel.unsubscribe(messageListener);
		messageChannel.unsubscribe(messageListener2);		
		List<MessageListenerPerformanceMock> listeners = new ArrayList<MessageListenerPerformanceMock>();
		for (int i = 0; i < 10; i++) {
			MessageListenerPerformanceMock mlpm = new MessageListenerPerformanceMock();
			listeners.add(mlpm);
			mlpm.reset(new CountDownLatch(1), 5 * max, new AtomicLong());
		}
		t0 = System.currentTimeMillis();
		for (int i = 0; i < 5; i++) {
			final int ii = i;
			Runnable r = new Runnable() {
				public void run() {
					for (long j = 0; j < max; j++) {
						int value = (int) (ii*max+j);
						messageChannel.publish(value, ""+value);
					}
				}
			};
			new Thread(r).start();
		}
		for (int i = 0; i < 10; i++) {
			messageChannel.subscribe(listeners.get(i));
			Thread.sleep(200);
		}
		for (int i = 0; i < 10; i++) { listeners.get(i).awaitLatch(); }
		t1 = System.currentTimeMillis();
		System.out.println("Sending " + max*5 + " persistent messages from 5 producers to 10 listener took " + (t1-t0) + "ms");
		
		System.out.println("Performance testing " + messageChannel.getClass().getName() + " --> SUCCESS!");
	}
	
	public static void main(String[] args) throws Exception {
		try {
			testMessageChannel(new CurrentThreadMessageChannel<String>());
			testMessageChannel(new BackgroundThreadMessageChannel<String>());
			testMessageChannel(new ThreadPerClientMessageChannel<String>());
			performanceTestMessageChannel(new CurrentThreadMessageChannel<String>());
			performanceTestMessageChannel(new BackgroundThreadMessageChannel<String>());
			performanceTestMessageChannel(new ThreadPerClientMessageChannel<String>());
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
