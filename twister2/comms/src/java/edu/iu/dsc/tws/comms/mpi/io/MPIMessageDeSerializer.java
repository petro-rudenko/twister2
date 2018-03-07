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
package edu.iu.dsc.tws.comms.mpi.io;

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.comms.api.MessageHeader;
import edu.iu.dsc.tws.comms.api.MessageType;
import edu.iu.dsc.tws.comms.mpi.MPIBuffer;
import edu.iu.dsc.tws.comms.mpi.MPIMessage;
import edu.iu.dsc.tws.comms.mpi.io.types.DataDeserializer;
import edu.iu.dsc.tws.comms.mpi.io.types.KeyDeserializer;
import edu.iu.dsc.tws.comms.utils.KryoSerializer;
import edu.iu.dsc.tws.comms.utils.MessageTypeUtils;

public class MPIMessageDeSerializer implements MessageDeSerializer {
  private static final Logger LOG = Logger.getLogger(MPIMessageDeSerializer.class.getName());

  private KryoSerializer serializer;

  private boolean keyed;

  public MPIMessageDeSerializer(KryoSerializer kryoSerializer) {
    this.serializer = kryoSerializer;
  }

  @Override
  public void init(Config cfg, boolean k) {
    this.keyed = k;
  }

  @Override
  public Object build(Object partialObject, int edge) {
    MPIMessage currentMessage = (MPIMessage) partialObject;
    return buildMessage(currentMessage);
  }

  public MessageHeader buildHeader(MPIBuffer buffer, int edge) {
    int sourceId = buffer.getByteBuffer().getInt();
    int flags = buffer.getByteBuffer().getInt();
    int subEdge = buffer.getByteBuffer().getInt();
    int length = buffer.getByteBuffer().getInt();

    MessageHeader.Builder headerBuilder = MessageHeader.newBuilder(
        sourceId, edge, length);
    // set the path
    headerBuilder.flags(flags);
    headerBuilder.destination(subEdge);

    // first build the header
    return headerBuilder.build();
  }

  @Override
  public Object getDataBuffers(Object partialObject, int edge) {
    MPIMessage currentMessage = (MPIMessage) partialObject;
    MessageType type = currentMessage.getType();

    if (!keyed) {
      return DataDeserializer.getAsByteBuffer(currentMessage.getBuffers(),
          currentMessage.getHeader().getLength(), type);
    } else {

      Pair<Integer, byte[]> keyPair = KeyDeserializer.
          getKeyAsByteBuffer(currentMessage.getKeyType(),
              currentMessage.getBuffers());
      MessageType keyType = currentMessage.getKeyType();
      byte[] data;
      if (!MessageTypeUtils.isPrimitiveType(keyType)) {
        data = DataDeserializer.getAsByteBuffer(currentMessage.getBuffers(),
            currentMessage.getHeader().getLength() - keyPair.getKey() - 4, type);
      } else {
        data = DataDeserializer.getAsByteBuffer(currentMessage.getBuffers(),
            currentMessage.getHeader().getLength() - keyPair.getKey(), type);
      }

      return new ImmutablePair<>(keyPair.getValue(), data);
    }
  }

  private Object buildMessage(MPIMessage message) {
    MessageType type = message.getType();

    if (!keyed) {
      return DataDeserializer.deserializeData(message.getBuffers(),
          message.getHeader().getLength(), serializer, type);
    } else {
      Pair<Integer, Object> keyPair = KeyDeserializer.deserializeKey(message.getKeyType(),
          message.getBuffers(), serializer);
      MessageType keyType = message.getKeyType();

      if (MessageTypeUtils.isMultiMessageType(message.getKeyType())) {
        return DataDeserializer.deserializeData(message.getBuffers(),
            message.getHeader().getLength() - keyPair.getKey() - 4 - 4, serializer, type,
            ((List) keyPair.getValue()).size());
      } else if (!MessageTypeUtils.isPrimitiveType(keyType)) {
        return DataDeserializer.deserializeData(message.getBuffers(),
            message.getHeader().getLength() - keyPair.getKey() - 4, serializer, type);
      } else {
        return DataDeserializer.deserializeData(message.getBuffers(),
            message.getHeader().getLength() - keyPair.getKey(), serializer, type);
      }
    }
  }
}
