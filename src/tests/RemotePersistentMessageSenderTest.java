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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class RemotePersistentMessageSenderTest {

	private static long MESSAGE_COUNT = 1000000;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("RemotePersistentMessageSenderTest");
        Registry registry = LocateRegistry.getRegistry(RemoteMessageChannelTest.REGISTRY_PORT);
        RemoteMessageChannel<String> messageChannel = (RemoteMessageChannel<String>) registry.lookup("TestChannel");
		long t0 = System.currentTimeMillis();
		int messageCount = 0;
		messageChannel.publish(messageCount, ""+messageCount++);
		List<String> messages = new ArrayList<String>();
		List<Object> ids = new ArrayList<Object>();
		while (messageCount < MESSAGE_COUNT) {
			ids.add(messageCount);
			messages.add(""+messageCount++);
			try {
				if (messageCount % 100000 == 0) {
					messageChannel.publish(ids, messages);
					System.out.println("Published " + messageCount + " from RemoteVolatileMessageSenderTest.");
					messages.clear();
					ids.clear();
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
