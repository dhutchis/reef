/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.reef.io.storage.ram;

import org.apache.reef.io.Accumulator;
import org.apache.reef.io.Spool;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * A SpoolFile implementation that is backed by RAM.
 * <p>
 * It uses an ArrayList to store the objects in.
 */
public final class RamSpool<T> implements Spool<T> {

  private final List<T> backingStore = new ArrayList<>();
  private boolean canAppend = true;
  private boolean canGetAccumulator = true;

  @Inject
  public RamSpool(final RamStorageService ramStore) {
  }

  @Override
  public Iterator<T> iterator() {
    canAppend = false;
    return backingStore.iterator();
  }

  @Override
  public Accumulator<T> accumulator() {
    if (!canGetAccumulator) {
      throw new UnsupportedOperationException("Can only getAccumulator() once!");
    }
    canGetAccumulator = false;
    return new Accumulator<T>() {
      @Override
      public void add(final T datum) {
        if (!canAppend) {
          throw new ConcurrentModificationException("Attempt to append after creating iterator!");
        }
        backingStore.add(datum);
      }

      @Override
      public void close() {
        canAppend = false;
      }
    };
  }
}
