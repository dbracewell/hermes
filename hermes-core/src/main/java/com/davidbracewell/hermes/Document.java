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

import com.davidbracewell.Language;
import com.davidbracewell.string.StringUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class Document implements HString {

  private static final long serialVersionUID = 1L;
  private final Map<Attribute, Object> attributes = new HashMap<>(5);
  private final String content;
  private String id;

  public Document(@Nonnull String content) {
    this.content = content;
    setId(null);
  }

  @Override
  public char charAt(int index) {
    return content.charAt(index);
  }

  @Override
  public Document document() {
    return this;
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

  /**
   * Gets the id of the document
   *
   * @return The id of the document
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id of the document. If a null or blank id is given a random id will generated.
   *
   * @param id The new id of the document
   */
  public void setId(String id) {
    if (StringUtils.isNullOrBlank(id)) {
      this.id = StringUtils.randomHexString(10);
    } else {
      this.id = id;
    }
  }

  @Override
  public String toString() {
    return content;
  }

  @Override
  public boolean isDocument() {
    return true;
  }

  @Override
  public Language getLanguage() {
    if (containsAttribute(Attrs.LANGUAGE)) {
      return getAttribute(Attrs.LANGUAGE);
    }
    return Language.ENGLISH;
  }

  List<Annotation> getStartingAt(AnnotationType type, int start) {
    return null;
  }

  public List<Annotation> getOverlapping(AnnotationType type, CharSpan span) {
    return null;
  }

  public List<Annotation> getContaining(AnnotationType type, CharSpan span) {
    return null;
  }

  public List<Annotation> getDuring(AnnotationType type, CharSpan span) {
    return null;
  }

  @Override
  public List<Annotation> getOverlapping(AnnotationType type) {
    return null;
  }

  @Override
  public List<Annotation> getContaining(AnnotationType type) {
    return null;
  }

  @Override
  public List<Annotation> getDuring(AnnotationType type) {
    return null;
  }

}//END OF Document
