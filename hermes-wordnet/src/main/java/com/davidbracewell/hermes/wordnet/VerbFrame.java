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

package com.davidbracewell.hermes.wordnet;

import java.io.Serializable;

/**
 * The type Verb frame.
 *
 * @author David B. Bracewell
 */
public class VerbFrame implements Serializable {
  static final VerbFrame[] verbFrames = {
      new VerbFrame("Something----s"),
      new VerbFrame("Somebody----s"),
      new VerbFrame("It is----ing"),
      new VerbFrame("Something is----ing PP"),
      new VerbFrame("Something----s something Adjective / Noun"),
      new VerbFrame("Something----s Adjective / Noun"),
      new VerbFrame("Somebody----s Adjective"),
      new VerbFrame("Somebody----s something"),
      new VerbFrame("Somebody----s somebody"),
      new VerbFrame("Something----s somebody"),
      new VerbFrame("Something----s something"),
      new VerbFrame("Something----s to somebody"),
      new VerbFrame("Somebody----s on something"),
      new VerbFrame("Somebody----s somebody something"),
      new VerbFrame("Somebody----s something to somebody"),
      new VerbFrame("Somebody----s something from somebody"),
      new VerbFrame("Somebody----s somebody with something"),
      new VerbFrame("Somebody----s somebody of something"),
      new VerbFrame("Somebody----s something on somebody"),
      new VerbFrame("Somebody----s somebody PP"),
      new VerbFrame("Somebody----s something PP"),
      new VerbFrame("Somebody----s PP"),
      new VerbFrame("Somebody's (body part) ----s"),
      new VerbFrame("Somebody----s somebody to INFINITIVE"),
      new VerbFrame("Somebody----s somebody INFINITIVE"),
      new VerbFrame("Somebody----s that CLAUSE"),
      new VerbFrame("Somebody----s to somebody"),
      new VerbFrame("Somebody----s to INFINITIVE"),
      new VerbFrame("Somebody----s whether INFINITIVE"),
      new VerbFrame("Somebody----s somebody into V - ing something"),
      new VerbFrame("Somebody----s something with something"),
      new VerbFrame("Somebody----s INFINITIVE"),
      new VerbFrame("Somebody----s VERB - ing"),
      new VerbFrame("It----s that CLAUSE"),
      new VerbFrame("Something----s INFINITIVE")
  };
  private static final long serialVersionUID = 8804812496868698814L;

  private final String frame;

  private VerbFrame(String frame) {
    this.frame = frame;
  }

  /**
   * For id.
   *
   * @param id the id
   * @return the verb frame
   */
  public static VerbFrame forId(int id) {
    return verbFrames[id];
  }

  /**
   * Gets frame.
   *
   * @return the frame
   */
  public String getFrame() {
    return frame;
  }

  /**
   * Resolve string.
   *
   * @param sense the sense
   * @return the string
   */
  public String resolve(Sense sense) {
    return frame.replace("----", " " + sense.getLemma());
  }

  @Override
  public String toString() {
    return frame;
  }

}//END OF VerbFrame
