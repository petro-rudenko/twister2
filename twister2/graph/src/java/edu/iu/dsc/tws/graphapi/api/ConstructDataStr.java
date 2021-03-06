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
package edu.iu.dsc.tws.graphapi.api;

import java.util.logging.Logger;

import edu.iu.dsc.tws.api.compute.TaskContext;
import edu.iu.dsc.tws.api.compute.nodes.BaseCompute;
import edu.iu.dsc.tws.api.config.Config;

public abstract  class ConstructDataStr extends BaseCompute {


  private static final Logger LOG = Logger.getLogger(ConstructDataStr.class.getName());

  /**
   * Edge name to write the partitoned datapoints
   */
  private String edgeName;

  /**
   * Task parallelism
   */
  private int parallel;

  /**
   * Data size
   */
  private int datasize;



  public ConstructDataStr(String edgename, int dsize, int parallel) {
    this.edgeName = edgename;
    this.parallel = parallel;
    this.datasize = dsize;
  }

  public int getDatasize() {
    return datasize;
  }

  public void setDatasize(int datasize) {
    this.datasize = datasize;
  }

  public int getParallel() {
    return parallel;
  }

  public void setParallel(int parallel) {
    this.parallel = parallel;
  }

  public String getEdgeName() {

    return edgeName;
  }

  public void setEdgeName(String edgeName) {

    this.edgeName = edgeName;
  }



  @Override
  public void prepare(Config cfg, TaskContext context) {
    super.prepare(cfg, context);
  }
}
