/*
 * (c) 2005 David B. Bracewell
 *
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

package com.davidbracewell.hermes;

import com.davidbracewell.config.Config;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author David B. Bracewell
 */
enum Configurator {
  INSTANCE;

  private volatile AtomicBoolean done = new AtomicBoolean(false);


  public void configure(Config config) {
    if (!done.get()) {
      synchronized (this) {
        if (!done.get()) {
          Config.setInstance(config);
          done.set(true);
        }
      }
    }
  }

}//END OF Configurator
