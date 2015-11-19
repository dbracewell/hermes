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
import com.davidbracewell.collection.Collect;
import com.davidbracewell.hermes.preprocessing.TextNormalization;
import com.davidbracewell.hermes.preprocessing.TextNormalizer;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;

/**
 * <p>A document factory facilitates the creation of  document objects. It handles performing any predefined
 * preprocessing and helps in determining the type of document being read. A default factory can be obtained by calling
 * {@link #getInstance()} or a factory can be built using a {@link Builder} constructed using {@link #builder()}.
 * </p>
 * <p>
 * The default factory uses configuration settings to determine the default language and preprocessing normalizers.
 * The default language is defined using the <code>hermes.DefaultLanguage</code> configuration property and the
 * normalizers are defined using the <code>hermes.preprocessing.normalizers</code> configuration property.
 * </p>
 *
 * @author David B. Bracewell
 */
public class DocumentFactory implements Serializable {
  private static final long serialVersionUID = 1L;

  private static volatile DocumentFactory CONFIGURED_INSTANCE;
  private final TextNormalization normalizer;
  private final Language defaultLanguage;


  private DocumentFactory() {
    this.normalizer = TextNormalization.configuredInstance();
    this.defaultLanguage = Hermes.defaultLanguage();
  }

  private DocumentFactory(Set<? extends TextNormalizer> normalizers, Language defaultLanguage) {
    this.normalizer = TextNormalization.createInstance(normalizers);
    this.defaultLanguage = (defaultLanguage == null) ? Hermes.defaultLanguage() : defaultLanguage;
  }

  /**
   * Creates a builder for constructing custom document factories.
   *
   * @return A Builder to create a DocumentFactory
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets instance of the document factory configured using configuration settings.
   *
   * @return A document factory whose preprocessors are set via configuration options
   */
  public static DocumentFactory getInstance() {
    if (CONFIGURED_INSTANCE == null) {
      synchronized (DocumentFactory.class) {
        if (CONFIGURED_INSTANCE == null) {
          CONFIGURED_INSTANCE = new DocumentFactory();
        }
      }
    }
    return CONFIGURED_INSTANCE;
  }

  /**
   * From tokens document.
   *
   * @param id       the id
   * @param tokens   the tokens
   * @param language the language
   * @return the document
   */
  public Document fromTokens(String id, @NonNull String[] tokens, @NonNull Language language) {
    int delta = language.usesWhitespace() ? 1 : 0;
    String content = Joiner.on(language.usesWhitespace() ? " " : "").join(tokens);
    Document doc = new Document(id, content, language);
    int charStart = 0;
    for (int i = 0; i < tokens.length; i++) {
      int charEnd = charStart + tokens[i].length();
      doc.createAnnotation(Types.TOKEN, charStart, charEnd);
      charStart = charEnd + delta;
    }
    doc.getAnnotationSet().setIsCompleted(Types.TOKEN, true, "PROVIDED");
    return doc;
  }

  /**
   * Creates a document with the given content.
   *
   * @param content the content
   * @return the document
   */
  public Document create(@NonNull String content) {
    return create("", content, defaultLanguage, Collections.emptyMap());
  }

  /**
   * Creates a document with the given id and content
   *
   * @param id      the id
   * @param content the content
   * @return the document
   */
  public Document create(@NonNull String id, @NonNull String content) {
    return create(id, content, defaultLanguage, Collections.emptyMap());
  }

  /**
   * Create document.
   *
   * @param content  the content
   * @param language the language
   * @return the document
   */
  public Document create(@NonNull String content, @NonNull Language language) {
    return create("", content, language, Collections.emptyMap());
  }

  /**
   * Create document.
   *
   * @param id       the id
   * @param content  the content
   * @param language the language
   * @return the document
   */
  public Document create(@NonNull String id, @NonNull String content, @NonNull Language language) {
    return create(id, content, language, Collections.emptyMap());
  }

  /**
   * Create document.
   *
   * @param content      the content
   * @param language     the language
   * @param attributeMap the attribute map
   * @return the document
   */
  public Document create(@NonNull String content, @NonNull Language language, @NonNull Map<Attribute, ?> attributeMap) {
    return create("", content, language, attributeMap);
  }

  /**
   * Create document.
   *
   * @param id           the id
   * @param content      the content
   * @param language     the language
   * @param attributeMap the attribute map
   * @return the document
   */
  public Document create(@NonNull String id, @NonNull String content, @NonNull Language language, @NonNull Map<Attribute, ?> attributeMap) {
    Document document = new Document(id, normalizer.normalize(content, language), language);
    document.putAll(attributeMap);
    document.setLanguage(language);
    return document;
  }

  /**
   * Create raw.
   *
   * @param id           the id
   * @param content      the content
   * @param language     the language
   * @param attributeMap the attribute map
   * @return the document
   */
  public Document createRaw(@NonNull String id, @NonNull String content, @NonNull Language language, @NonNull Map<Attribute, ?> attributeMap) {
    Document document = new Document(id, content, language);
    document.putAll(attributeMap);
    document.setLanguage(language);
    return document;
  }

  /**
   * From tokens document.
   *
   * @param tokens the tokens
   * @return the document
   */
  public Document fromTokens(@NonNull Iterable<String> tokens) {
    return fromTokens(tokens, getDefaultLanguage());
  }

  /**
   * Creates a document from an iterable of strings that represent tokens.
   *
   * @param tokens   the tokens
   * @param language the language
   * @return the document
   */
  public Document fromTokens(@NonNull Iterable<String> tokens, @NonNull Language language) {
    StringBuilder content = new StringBuilder();
    List<Span> tokenSpans = new ArrayList<>();
    for (String token : tokens) {
      tokenSpans.add(new Span(content.length(), content.length() + token.length()));
      content.append(token);
      if (language.usesWhitespace()) {
        content.append(" ");
      }
    }
    Document doc = new Document("", content.toString().trim(), defaultLanguage);
    for (int idx = 0; idx < tokenSpans.size(); idx++) {
      doc.createAnnotation(Types.TOKEN, tokenSpans.get(idx));
      Collect.map(Attrs.INDEX, idx);
    }
    doc.getAnnotationSet().setIsCompleted(Types.TOKEN, true, "PROVIDED");
    return doc;
  }

  /**
   * Create document.
   *
   * @param content the content
   * @return the document
   */
  public Document createRaw(@NonNull String content) {
    return createRaw("", content, defaultLanguage, Collections.emptyMap());
  }

  /**
   * Create document.
   *
   * @param id      the id
   * @param content the content
   * @return the document
   */
  public Document createRaw(@NonNull String id, @NonNull String content) {
    return createRaw(id, content, defaultLanguage, Collections.emptyMap());
  }

  /**
   * Create document.
   *
   * @param content  the content
   * @param language the language
   * @return the document
   */
  public Document createRaw(@NonNull String content, @NonNull Language language) {
    return createRaw("", content, language, Collections.emptyMap());
  }

  /**
   * Create document.
   *
   * @param id       the id
   * @param content  the content
   * @param language the language
   * @return the document
   */
  public Document createRaw(@NonNull String id, @NonNull String content, @NonNull Language language) {
    return createRaw(id, content, language, Collections.emptyMap());
  }

  /**
   * Create document.
   *
   * @param content      the content
   * @param language     the language
   * @param attributeMap the attribute map
   * @return the document
   */
  public Document createRaw(@NonNull String content, @NonNull Language language, @NonNull Map<Attribute, ?> attributeMap) {
    return createRaw("", content, language, attributeMap);
  }

  /**
   * Gets default language.
   *
   * @return the default language
   */
  public Language getDefaultLanguage() {
    return defaultLanguage;
  }

  /**
   * <p>Builder for DocumentFactory</p>
   *
   * @author David B. Bracewell
   */
  public static class Builder {

    private final Set<TextNormalizer> preprocessors = Sets.newHashSet();
    private Language defaultLanguage;

    private Builder() {

    }

    /**
     * Adds a Text preprocessor
     *
     * @param preprocessor The text preprocessor to add
     * @return This instance of the DocumentFactoryBuilder
     */
    public Builder add(TextNormalizer preprocessor) {
      if (preprocessor != null) {
        preprocessors.add(preprocessor);
      }
      return this;
    }

    /**
     * Build document factory.
     *
     * @return A DocumentFactory
     */
    public DocumentFactory build() {
      return new DocumentFactory(preprocessors, defaultLanguage);
    }

    /**
     * Sets the default language for new documents
     *
     * @param language the default language
     * @return This instance of the DocumentFactoryBuilder
     */
    public Builder defaultLanguage(Language language) {
      this.defaultLanguage = language == null ? Hermes.defaultLanguage() : language;
      return this;
    }

  }//END OF DocumentFactory$Builder

}//END OF DocumentFactory
