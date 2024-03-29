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

package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteMessageChannel<T> extends Remote {
	public void publish(T message) throws RemoteException;
	public void publish(List<T> messages) throws RemoteException;
	public void publish(Object id, T message) throws RemoteException;
	public void publish(List<Object> ids, List<T> messages) throws RemoteException;
	public void subscribe(RemoteMessageListener<T> listener) throws RemoteException;
	public void unsubscribe(RemoteMessageListener<T> listener) throws RemoteException;
}
