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
import com.davidbracewell.hermes.preprocessing.TextNormalization;
import com.davidbracewell.hermes.preprocessing.TextNormalizer;
import com.davidbracewell.string.StringUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

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
public final class DocumentFactory implements Serializable {
   private static final long serialVersionUID = 1L;

   private static volatile DocumentFactory CONFIGURED_INSTANCE;
   private final TextNormalization normalizer;
   private final Language defaultLanguage;


   private DocumentFactory() {
      this.normalizer = TextNormalization.configuredInstance();
      this.defaultLanguage = Hermes.defaultLanguage();
   }


   @Builder
   private DocumentFactory(@Singular Set<? extends TextNormalizer> normalizers, Language defaultLanguage) {
      this.normalizer = TextNormalization.createInstance(normalizers);
      this.defaultLanguage = (defaultLanguage == null) ? Hermes.defaultLanguage() : defaultLanguage;
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
    * Creates a document with the given content.
    *
    * @param content the content
    * @return the document
    */
   public Document create(@NonNull String content) {
      return create(StringUtils.EMPTY, content, defaultLanguage, Collections.emptyMap());
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
    * Creates a document with the given content written in the given language.
    *
    * @param content  the content
    * @param language the language
    * @return the document
    */
   public Document create(@NonNull String content, @NonNull Language language) {
      return create(StringUtils.EMPTY, content, language, Collections.emptyMap());
   }

   /**
    * Creates a document with the given id and content written in the given language.
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
    * Creates a document with the given content written in the given language having the given set of attributes.
    *
    * @param content      the content
    * @param language     the language
    * @param attributeMap the attribute map
    * @return the document
    */
   public Document create(@NonNull String content, @NonNull Language language, @NonNull Map<AttributeType, ?> attributeMap) {
      return create("", content, language, attributeMap);
   }

   /**
    * Creates a document with the given id and content written in the given language having the given set of attributes.
    *
    * @param id           the id
    * @param content      the content
    * @param language     the language
    * @param attributeMap the attribute map
    * @return the document
    */
   public Document create(@NonNull String id, @NonNull String content, @NonNull Language language, @NonNull Map<AttributeType, ?> attributeMap) {
      Document document = new Document(id, normalizer.normalize(content, language), language);
      document.putAll(attributeMap);
      document.setLanguage(language);
      return document;
   }

   /**
    * Creates a document with the given id and content written in the given language having the given set of attributes.
    * This method does not apply any {@link TextNormalizer}
    *
    * @param id           the id
    * @param content      the content
    * @param language     the language
    * @param attributeMap the attribute map
    * @return the document
    */
   public Document createRaw(@NonNull String id, @NonNull String content, @NonNull Language language, @NonNull Map<AttributeType, ?> attributeMap) {
      Document document = new Document(id, content, language);
      document.putAll(attributeMap);
      document.setLanguage(language);
      return document;
   }

   /**
    * Creates a document with the given content written in the default language.  This method does not apply any {@link
    * TextNormalizer}
    *
    * @param content the content
    * @return the document
    */
   public Document createRaw(@NonNull String content) {
      return createRaw(StringUtils.EMPTY, content, defaultLanguage, Collections.emptyMap());
   }

   /**
    * Creates a document with the given id and content written in the default language. This method does not apply any
    * {@link TextNormalizer}
    *
    * @param id      the id
    * @param content the content
    * @return the document
    */
   public Document createRaw(@NonNull String id, @NonNull String content) {
      return createRaw(id, content, defaultLanguage, Collections.emptyMap());
   }

   /**
    * Creates a document with the given content written in the given language. This method does not apply any {@link
    * TextNormalizer}
    *
    * @param content  the content
    * @param language the language
    * @return the document
    */
   public Document createRaw(@NonNull String content, @NonNull Language language) {
      return createRaw("", content, language, Collections.emptyMap());
   }

   /**
    * Creates a document with the given id and content written in the given language. This method does not apply any
    * {@link TextNormalizer}
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
    * Creates a document with the given content written in the given language having the given set of attributes.
    * This method does not apply any {@link TextNormalizer}
    *
    * @param content      the content
    * @param language     the language
    * @param attributeMap the attribute map
    * @return the document
    */
   public Document createRaw(@NonNull String content, @NonNull Language language, @NonNull Map<AttributeType, ?> attributeMap) {
      return createRaw("", content, language, attributeMap);
   }

   /**
    * Creates a document from the given tokens. The language parameter controls how the content of the documents is
    * created. If the language has whitespace tokens are joined with a single space between them, otherwise no space is
    * inserted between tokens.
    *
    * @param tokens   the tokens making up the document
    * @param language the language of the document
    * @return the document with tokens provided.
    */
   public Document fromTokens(@NonNull Language language, @NonNull String... tokens) {
      return fromTokens(Arrays.asList(tokens), language);
   }

   /**
    * Creates a document from the given tokens using the default language.
    *
    * @param tokens the tokens
    * @return the document
    */
   public Document fromTokens(@NonNull String... tokens) {
      return fromTokens(Arrays.asList(tokens), getDefaultLanguage());
   }

   /**
    * Creates a document from the given tokens using the default language.
    *
    * @param tokens the tokens
    * @return the document
    */
   public Document fromTokens(@NonNull Iterable<String> tokens) {
      return fromTokens(tokens, getDefaultLanguage());
   }

   /**
    * Creates a document from the given tokens. The language parameter controls how the content of the documents is
    * created. If the language has whitespace tokens are joined with a single space between them, otherwise no space is
    * inserted between tokens.
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
      Document doc = new Document(null, content.toString().trim(), defaultLanguage);
      for (int idx = 0; idx < tokenSpans.size(); idx++) {
         doc.annotationBuilder()
            .type(Types.TOKEN)
            .bounds(tokenSpans.get(idx))
            .attribute(Types.INDEX, idx).createAttached();
      }
      doc.getAnnotationSet().setIsCompleted(Types.TOKEN, true, "PROVIDED");
      return doc;
   }

   /**
    * Gets the default language of the document factory.
    *
    * @return the default language
    */
   public Language getDefaultLanguage() {
      return defaultLanguage;
   }

}//END OF DocumentFactory
