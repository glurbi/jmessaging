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

import interfaces.RemoteMessageChannel;
import interfaces.RemoteMessageListener;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


public class RemoteMessageListenerTest implements RemoteMessageListener<String> {

	private static final long serialVersionUID = 1L;
	
	private final AtomicLong totalReceived = new AtomicLong();

	public void onMessages(List<String> messages) {
		int size = messages.size();
		totalReceived.addAndGet(size);
		System.out.println("Received " + totalReceived + " messages.");
	}

	public void onMessage(String message) throws RemoteException {
		totalReceived.incrementAndGet();
		System.out.println("Received " + totalReceived + " messages.");
	}

	public String getName() {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("RemoteMessageListenerTest");
        Registry registry = LocateRegistry.getRegistry(RemoteMessageChannelTest.REGISTRY_PORT);
        RemoteMessageChannel<String> messageChannel = (RemoteMessageChannel<String>) registry.lookup("TestChannel");
        RemoteMessageListenerTest messageListener = new RemoteMessageListenerTest();
        UnicastRemoteObject.exportObject(messageListener, 0);
        messageChannel.subscribe(messageListener);
        System.in.read();
	}

}
