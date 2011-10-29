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
import implementations.RemoteMessageChannelImpl;
import interfaces.MessageChannel;
import interfaces.RemoteMessageChannel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RemoteMessageChannelTest {
	
	public static int REGISTRY_PORT = 12345;
	
	public static void main(String[] args) throws Exception {
		System.out.println("RemoteMessageChannelTest");
		MessageChannel<String> messageChannel = new BackgroundThreadMessageChannel<String>();
		RemoteMessageChannel<String> remoteMessageChannel = new RemoteMessageChannelImpl<String>(messageChannel);
        Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
        registry.bind("TestChannel", remoteMessageChannel);
	}

}
