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

package com.davidbracewell.hermes.corpus;

import com.davidbracewell.collection.NormalizedStringMap;
import com.davidbracewell.hermes.corpus.spi.OnePerLineFormat;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author David B. Bracewell
 */
public final class DocumentFormats {
  private static final Map<String, DocumentFormat> formats = new NormalizedStringMap<>();

  static {
    for (DocumentFormat df : ServiceLoader.load(DocumentFormat.class)) {
      formats.put(df.name(), df);
    }
  }

  public static DocumentFormat forName(@NonNull String name) {
    String format = StringUtils.trim(name).toUpperCase();
    boolean isOPL = format.endsWith("_OPL");
    final String normFormat = format.replaceAll("_OPL$", "").trim();
    if (formats.containsKey(normFormat)) {
      return isOPL ? new OnePerLineFormat(formats.get(normFormat)) : formats.get(normFormat);
    }
    throw new IllegalArgumentException(name + " is an unknown format.");
  }

  /**
   * JSON format created by using write or toJson from a Document
   */
  public static final String JSON = "JSON";
  /**
   * XML format created by using write from a Document
   */
  public static final String XML = "XML";
  /**
   * CONLL tab delimited format
   */
  public static final String CONLL = "CONLL";
  /**
   * Plain text
   */
  public static final String PLAIN_TEXT = "TEXT";
  /**
   * One per line JSON format
   */
  public static final String JSON_OPL = "JSON_OPL";
  /**
   * One per line plain text format
   */
  public static final String PLAIN_TEXT_OPL = "TEXT_OPL";
  /**
   * CSV Format
   */
  public static final String CSV = "CSV";
  /**
   * TSV Format
   */
  public static final String TSV = "TSV";

  private DocumentFormats() {
    throw new IllegalAccessError();
  }

}//END OF DocumentFormats
