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

import java.util.List;

public interface MessageChannel<T> {
	public void publish(T message);
	public void publish(List<T> messages);
	public void publish(Object id, T message);
	public void publish(List<Object> ids, List<T> messages);
	public void subscribe(MessageListener<T> listener);
	public void unsubscribe(MessageListener<T> listener);
}
