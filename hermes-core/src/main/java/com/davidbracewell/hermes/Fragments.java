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

import com.davidbracewell.string.StringUtils;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public final class Fragments {

  private Fragments() {
    throw new IllegalAccessError();
  }


  public static HString emptyOrphan() {
    return ORPHANED_EMPTY.INSTANCE;
  }

  public static HString orphan(@Nonnull String content) {
    return new ORPHANED(content);
  }

  public static HString empty(@Nonnull Document document) {
    return new Fragment(document, 0, 0);
  }

  public static HString empty(@Nonnull HString string) {
    if (string.document() == null) {
      return emptyOrphan();
    }
    return new Fragment(string.document(), 0, 0);
  }


  private static class ORPHANED implements HString, Serializable {
    private static final long serialVersionUID = 1L;

    private final String content;
    private final Map<Attribute, Object> attributes = new HashMap<>(5);

    private ORPHANED(String content) {
      this.content = content;
    }

    @Override
    public char charAt(int index) {
      return content.charAt(index);
    }

    @Override
    public Document document() {
      return null;
    }

    @Override
    public Map<Attribute, Object> getAttributes() {
      return attributes;
    }

    @Override
    public int start() {
      return 0;
    }

    @Override
    public int end() {
      return content.length();
    }
  }

  private enum ORPHANED_EMPTY implements HString {
    INSTANCE {
      @Override
      public char charAt(int index) {
        throw new IndexOutOfBoundsException();
      }

      @Override
      public int start() {
        return 0;
      }

      @Override
      public int end() {
        return 0;
      }

      @Override
      public Map<Attribute, Object> getAttributes() {
        return Collections.emptyMap();
      }

      @Override
      public Document document() {
        return null;
      }

      @Override
      public String toString() {
        return StringUtils.EMPTY;
      }
    }
  }


}//END OF Fragments
