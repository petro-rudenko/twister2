//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
package edu.iu.dsc.tws.api.compute.executor;

/**
 * Sync interface, this method is called when barriers come and tasks sync
 */
public interface ISync {
  /**
   * Callback for sync
   * @param edge edge name
   * @param value value, can be null
   * @return if we handled the sync, return true
   */
  boolean sync(String edge, byte[] value);
}
