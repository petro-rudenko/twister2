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
package edu.iu.dsc.tws.comms.mpi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.iu.dsc.tws.comms.api.MessageHeader;
import edu.iu.dsc.tws.comms.routing.DirectRouter;
import edu.iu.dsc.tws.comms.routing.IRouter;

/**
 * A direct data flow operation sends peer to peer messages
 */
public class MPIDirectDataFlowCommunication extends MPIDataFlowOperation {
  private Set<Integer> sources;
  private int destination;
  protected IRouter router;

  public MPIDirectDataFlowCommunication(TWSMPIChannel channel,
                                        Set<Integer> srcs, int dest) {
    super(channel);

    this.sources = srcs;
    this.destination = dest;
  }

  @Override
  protected void setupRouting() {
    this.router = new DirectRouter(instancePlan, sources, destination);
  }

  @Override
  protected boolean isLast(int source, int path, int taskIdentifier) {
    return router.isLastReceiver();
  }

  @Override
  protected void receiveMessage(MPIMessage currentMessage, Object object) {
    MessageHeader header = currentMessage.getHeader();
    // check weather this message is for a sub task
    finalReceiver.onMessage(header.getSourceId(), header.getPath(), destination, object);
  }

  @Override
  protected void routeReceivedMessage(MessageHeader message, List<Integer> routes) {
    throw new RuntimeException("We are not routing received messages");
  }

  @Override
  protected void receiveSendInternally(int source, int t, int path, Object message) {
    // we only have one destination in this case
    if (t != destination) {
      throw new RuntimeException("We only have one destination");
    }

    // okay this must be for the
    finalReceiver.onMessage(source, path, t, message);
  }

  @Override
  protected Set<Integer> receivingExecutors() {
    return router.receivingExecutors();
  }

  @Override
  protected Map<Integer, Map<Integer, List<Integer>>> receiveExpectedTaskIds() {
    return this.router.receiveExpectedTaskIds();
  }

  @Override
  public boolean injectPartialResult(int source, Object message) {
    throw new RuntimeException("This method is not used by direct communication");
  }

  @Override
  protected boolean isLastReceiver() {
    return router.isLastReceiver();
  }

  @Override
  protected RoutingParameters sendRoutingParameters(int source, int path) {
    RoutingParameters routingParameters = new RoutingParameters();
    // get the expected routes
    Map<Integer, Map<Integer, Set<Integer>>> internalRoutes = router.getInternalSendTasks(source);
    if (internalRoutes == null) {
      throw new RuntimeException("Un-expected message from source: " + source);
    }

    Map<Integer, Set<Integer>> internalSourceRouting = internalRoutes.get(source);
    if (internalSourceRouting != null) {
      // we always use path 0 because only one path
      routingParameters.addInternalRoutes(internalSourceRouting.get(0));
    }

    // get the expected routes
    Map<Integer, Map<Integer, Set<Integer>>> externalRouting = router.getExternalSendTasks(source);
    if (externalRouting == null) {
      throw new RuntimeException("Un-expected message from source: " + source);
    }

    Map<Integer, Set<Integer>> externalSourceRouting = externalRouting.get(source);
    if (externalSourceRouting != null) {
      // we always use path 0 because only one path
      routingParameters.addExternalRoutes(externalSourceRouting.get(0));
    }
    routingParameters.setDestinationId(destination);
    return routingParameters;
  }
}
