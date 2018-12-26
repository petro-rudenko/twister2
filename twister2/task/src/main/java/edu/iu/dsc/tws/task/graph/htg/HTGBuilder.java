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
package edu.iu.dsc.tws.task.graph.htg;

import edu.iu.dsc.tws.data.api.DataType;
import edu.iu.dsc.tws.task.api.IFunction;
import edu.iu.dsc.tws.task.graph.DataFlowTaskGraph;
import edu.iu.dsc.tws.task.graph.Edge;
import edu.iu.dsc.tws.task.graph.OperationMode;

/**
 * This class is responsible for building the task graph and the configuration values required for
 * each task in the task graph.
 */
public final class HTGBuilder {

  private HierarchicalTaskGraph graph;

  private HTGBuilder() {
    graph = new HierarchicalTaskGraph();
  }

  public static HTGBuilder newBuilder() {
    return new HTGBuilder();
  }

  public HTGBuilder connect(String t1, String t2, String name) {
    DataFlowTaskGraph v1 = graph.dataFlowTaskGraph(name);
    if (v1 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t1);
    }

    DataFlowTaskGraph v2 = graph.dataFlowTaskGraph(name);
    if (v2 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t2);
    }
    graph.addTaskGraphEdge(v1, v2, new Edge(name));
    return this;
  }

  public HTGBuilder connect(String t1, String t2, String name, String operation) {
    DataFlowTaskGraph v1 = graph.dataFlowTaskGraph(name);
    if (v1 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t1);
    }

    DataFlowTaskGraph v2 = graph.dataFlowTaskGraph(name);
    if (v2 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t2);
    }
    graph.addTaskGraphEdge(v1, v2, new Edge(name, operation));
    return this;
  }

  public HTGBuilder connect(String t1, String t2, String name, String operation,
                            IFunction task) {
    DataFlowTaskGraph v1 = graph.dataFlowTaskGraph(name);
    if (v1 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t1);
    }

    DataFlowTaskGraph v2 = graph.dataFlowTaskGraph(name);
    if (v2 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t2);
    }
    graph.addTaskGraphEdge(v1, v2, new Edge(name, operation, task));
    return this;
  }

  public HTGBuilder connect(String t1, String t2, String name, String operation,
                            DataType dataType, DataType keyType) {
    DataFlowTaskGraph v1 = graph.dataFlowTaskGraph(name);
    if (v1 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t1);
    }

    DataFlowTaskGraph v2 = graph.dataFlowTaskGraph(name);
    if (v2 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t2);
    }
    graph.addTaskGraphEdge(v1, v2, new Edge(name, operation, dataType, keyType));
    return this;
  }

  public HTGBuilder connect(String t1, String t2, String name, String operation,
                            IFunction function, DataType dataType,
                            DataType keyType) {
    DataFlowTaskGraph v1 = graph.dataFlowTaskGraph(name);
    if (v1 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t1);
    }

    DataFlowTaskGraph v2 = graph.dataFlowTaskGraph(name);
    if (v2 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t2);
    }
    graph.addTaskGraphEdge(v1, v2, new Edge(name, operation, dataType, keyType, function));
    return this;
  }

  public HTGBuilder connect(String t1, String t2, String name, String operation,
                            DataType dataType) {
    DataFlowTaskGraph v1 = graph.dataFlowTaskGraph(name);
    if (v1 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t1);
    }

    DataFlowTaskGraph v2 = graph.dataFlowTaskGraph(name);
    if (v2 == null) {
      throw new RuntimeException("Failed to connect non-existing graph: " + t2);
    }
    graph.addTaskGraphEdge(v1, v2, new Edge(name, operation, dataType));
    return this;
  }

  public HierarchicalTaskGraph build() {
    //graph.validate();
    graph.build();
    return graph;
  }

  /**
   * Set the operation mode of the graph, default is set to stream
   */
  public HierarchicalTaskGraph operationMode(OperationMode mode) {
    graph.setOperationMode(mode);
    return graph;
  }
}
