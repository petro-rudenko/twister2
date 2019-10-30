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
package org.apache.beam.runners.twister2.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.beam.runners.core.InMemoryMultimapSideInputView;
import org.apache.beam.runners.core.SideInputReader;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.transforms.Materializations;
import org.apache.beam.sdk.transforms.ViewFn;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.util.WindowedValue;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.WindowingStrategy;

import edu.iu.dsc.tws.api.dataset.DataPartition;
import edu.iu.dsc.tws.api.dataset.DataPartitionConsumer;
import edu.iu.dsc.tws.api.tset.TSetContext;
import static org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions.checkArgument;
import static org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions.checkNotNull;

public class Twister2SideInputReader implements SideInputReader {
  private static final Materializations.MultimapView EMPTY_MULTMAP_VIEW
      = o -> Collections.EMPTY_LIST;

  private final TSetContext runtimeContext;
  private final Map<TupleTag<?>, WindowingStrategy<?, ?>> sideInputs;

  public Twister2SideInputReader(Map<PCollectionView<?>, WindowingStrategy<?, ?>> indexByView,
                                 TSetContext context) {
    this.sideInputs = new HashMap<>();

    for (PCollectionView<?> view : indexByView.keySet()) {
      checkArgument(
          Materializations.MULTIMAP_MATERIALIZATION_URN.equals(
              view.getViewFn().getMaterialization().getUrn()),
          "This handler is only capable of dealing with %s materializations "
              + "but was asked to handle %s for PCollectionView with tag %s.",
          Materializations.MULTIMAP_MATERIALIZATION_URN,
          view.getViewFn().getMaterialization().getUrn(),
          view.getTagInternal().getId());
    }
    for (Map.Entry<PCollectionView<?>, WindowingStrategy<?, ?>> entry : indexByView.entrySet()) {
      sideInputs.put(entry.getKey().getTagInternal(), entry.getValue());
    }
    this.runtimeContext = context;
  }

  @Nullable
  @Override
  public <T> T get(PCollectionView<T> view, BoundedWindow window) {
    checkNotNull(view, "View passed to sideInput cannot be null");
    TupleTag<?> tag = view.getTagInternal();
    checkNotNull(sideInputs.get(tag), "Side input for " + view + " not available.");
    return getSideInput(view, window);
  }

  @Override
  public <T> boolean contains(PCollectionView<T> view) {
    return sideInputs.containsKey(view.getTagInternal());
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  private <T> T getSideInput(PCollectionView<T> view, BoundedWindow window) {
    Map<BoundedWindow, List<WindowedValue<KV<?, ?>>>> partitionedElements = new HashMap<>();
    DataPartition<?> sideInput = runtimeContext.getInput(view.getTagInternal().getId());
    DataPartitionConsumer<?> dataPartitionConsumer = sideInput.getConsumer();
    while (dataPartitionConsumer.hasNext()) {
      WindowedValue<KV<?, ?>> winValue = (WindowedValue<KV<?, ?>>) dataPartitionConsumer.next();
      for (BoundedWindow tbw : winValue.getWindows()) {
        List<WindowedValue<KV<?, ?>>> windowedValues =
            partitionedElements.computeIfAbsent(tbw, k -> new ArrayList<>());
        windowedValues.add(winValue);
      }

    }

    Map<BoundedWindow, T> resultMap = new HashMap<>();

    for (Map.Entry<BoundedWindow, List<WindowedValue<KV<?, ?>>>> elements
        : partitionedElements.entrySet()) {

      ViewFn<Materializations.MultimapView, T> viewFn
          = (ViewFn<Materializations.MultimapView, T>) view.getViewFn();
      Coder keyCoder = ((KvCoder<?, ?>) view.getCoderInternal()).getKeyCoder();
      resultMap.put(
          elements.getKey(),
          (T)
              viewFn.apply(
                  InMemoryMultimapSideInputView.fromIterable(
                      keyCoder,
                      (Iterable)
                          elements.getValue().stream()
                              .map(WindowedValue::getValue)
                              .collect(Collectors.toList()))));
    }
    T result = resultMap.get(window);
    if (result == null) {
      ViewFn<Materializations.MultimapView, T> viewFn
          = (ViewFn<Materializations.MultimapView, T>) view.getViewFn();
      result = viewFn.apply(EMPTY_MULTMAP_VIEW);
    }
    return result;
  }
}
