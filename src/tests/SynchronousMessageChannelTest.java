package tests;

import implementations.SynchronousMessageChannel;
import interfaces.MessageListener;

import java.util.ArrayList;
import java.util.List;


public class SynchronousMessageChannelTest {

	public static class MessageListenerMock implements MessageListener<String> {
		public List<String> recordedMessages = new ArrayList<String>();
		public void onMessage(String message) { recordedMessages.add(message); }
		public void onMessages(List<String> messages) { recordedMessages.addAll(messages); }
		public java.lang.String getName() { throw new UnsupportedOperationException(); }
	}
	
	public static void main(String[] args) throws Exception {
		SynchronousMessageChannel<String> messageChannel = new SynchronousMessageChannel<String>();
		MessageListenerMock messageListener = new MessageListenerMock();
		messageChannel.subscribe(messageListener);
		messageChannel.publish("Message one");
		assert messageListener.recordedMessages.size() == 1;
		assert messageListener.recordedMessages.get(0).equals("Message one");
		System.out.println("SUCCESS!");
	}

}
