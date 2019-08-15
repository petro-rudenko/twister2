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

package edu.iu.dsc.tws.api.tset.link.batch;

import edu.iu.dsc.tws.api.compute.OperationNames;
import edu.iu.dsc.tws.api.compute.graph.Edge;
import edu.iu.dsc.tws.api.tset.TSetUtils;
import edu.iu.dsc.tws.api.tset.env.BatchTSetEnvironment;

public class ReplicateTLink<T> extends BIteratorLink<T> {

  public ReplicateTLink(BatchTSetEnvironment tSetEnv, int reps) {
    super(tSetEnv, TSetUtils.generateName("replicate"), 1, reps);
  }

  @Override
  public Edge getEdge() {
    return new Edge(getId(), OperationNames.BROADCAST, getMessageType());
  }

  @Override
  public ReplicateTLink<T> setName(String n) {
    rename(n);
    return this;
  }
}
