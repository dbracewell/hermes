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

import com.davidbracewell.DynamicEnum;
import com.davidbracewell.annotation.Preload;
import com.davidbracewell.hermes.attribute.AttributeType;
import com.davidbracewell.hermes.attribute.AttributeValueType;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

/**
 * <p>Common Annotation Types. The predefined types are pre-loaded on initialization.</p>
 *
 * @author David B. Bracewell
 */
@Preload
public interface Types {

   /**
    * Document author
    */
   AttributeType AUTHOR = AttributeType.create("AUTHOR", AttributeValueType.STRING);
   /**
    * Document CATEGORY
    */
   AttributeType CATEGORY = AttributeType.create("CATEGORY", AttributeValueType.STRING);
   /**
    * Confidence value associated with an annotation
    */
   AttributeType CONFIDENCE = AttributeType.create("CONFIDENCE", AttributeValueType.DOUBLE);
   /**
    * The constant DEPENDENCY.
    */
   RelationType DEPENDENCY = RelationType.create("DEPENDENCY");
   /**
    * Entity annotation type
    */
   AnnotationType ENTITY = AnnotationType.create("ENTITY");
   /**
    * The constant ENTITY_TYPE.
    */
   AttributeType ENTITY_TYPE = AttributeType.create("ENTITY_TYPE", AttributeValueType.ENTITY_TYPE);
   /**
    * File used to create the document
    */
   AttributeType FILE = AttributeType.create("FILE", AttributeValueType.STRING);
   /**
    * The index of a span with regards to a document
    */
   AttributeType INDEX = AttributeType.create("INDEX", AttributeValueType.INTEGER);
   /**
    * The Language associated with a span
    */
   AttributeType LANGUAGE = AttributeType.create("LANGUAGE", AttributeValueType.LANGUAGE);
   /**
    * The lemma version of a span
    */
   AttributeType LEMMA = AttributeType.create("LEMMA", AttributeValueType.STRING);
   /**
    * lexicon match annotation type
    */
   AnnotationType LEXICON_MATCH = AnnotationType.create("LEXICON_MATCH");
   /**
    * The constant CADUCEUS_RULE.
    */
   AttributeType CADUCEUS_RULE = AttributeType.create("CADUCEUS_RULE", AttributeValueType.STRING);
   /**
    * The constant MATCHED_STRING.
    */
   AttributeType MATCHED_STRING = AttributeType.create("MATCHED_STRING", AttributeValueType.STRING);
   /**
    * The part-of-speech assocaited with a span
    */
   AttributeType PART_OF_SPEECH = AttributeType.create("PART_OF_SPEECH", AttributeValueType.PART_OF_SPEECH);
   /**
    * phrase chunk annotation type
    */
   AnnotationType PHRASE_CHUNK = AnnotationType.create("PHRASE_CHUNK");
   /**
    * Date content was published
    */
   AttributeType PUBLICATION_DATE = AttributeType.create("PUBLICATION_DATE", AttributeValueType.DATE);
   /**
    * The constant SENSE.
    */
   AttributeType SENSE = AttributeType.create("SENSE", AttributeValueType.STRING);
   /**
    * sentence annotation type
    */
   AnnotationType SENTENCE = AnnotationType.create("SENTENCE");
   /**
    * Document source
    */
   AttributeType SOURCE = AttributeType.create("SOURCE", AttributeValueType.STRING);
   /**
    * The STEM.
    */
   AttributeType STEM = AttributeType.create("STEM", AttributeValueType.STRING);
   /**
    * The tag associated with a span
    */
   AttributeType TAG = AttributeType.create("TAG", AttributeValueType.STRING_TAG);
   /**
    * Document title
    */
   AttributeType TITLE = AttributeType.create("TITLE", AttributeValueType.STRING);
   /**
    * token annotation type
    */
   AnnotationType TOKEN = AnnotationType.create("TOKEN");
   /**
    * The type of token
    */
   AttributeType TOKEN_TYPE = AttributeType.create("TOKEN_TYPE", AttributeValueType.TOKEN_TYPE);
   /**
    * The constant TOKEN_TYPE_ENTITY.
    */
   AnnotationType TOKEN_TYPE_ENTITY = AnnotationType.create("TOKEN_TYPE_ENTITY", ENTITY);
   /**
    * The TRANSLITERATION.
    */
   AttributeType TRANSLITERATION = AttributeType.create("TRANSLITERATION", AttributeValueType.STRING);
   /**
    * The constant WORD_SENSE.
    */
   AnnotationType WORD_SENSE = AnnotationType.create("WORD_SENSE");

   AnnotationType ML_ENTITY = AnnotationType.create("ML_ENTITY", ENTITY);

   /**
    * Annotation annotation type.
    *
    * @param name the name
    * @return the annotation type
    */
   static AnnotationType annotation(String name) {
      return AnnotationType.create(name);
   }

   /**
    * Attribute attribute type.
    *
    * @param name the name
    * @return the attribute type
    */
   static AttributeType attribute(String name) {
      return AttributeType.create(name);
   }

   /**
    * Relation relation type.
    *
    * @param name the name
    * @return the relation type
    */
   static RelationType relation(String name) {
      return RelationType.create(name);
   }


   /**
    * Creates the appropriate annotatable from the given name.
    *
    * @param string The type and name separated by a period, e.g. Annotation.ENTITY
    * @return The appropriate annotatable
    * @throws IllegalArgumentException Invalid type or no type given.
    */
   static AnnotatableType from(@NonNull String string) {
      int lastIdx = string.lastIndexOf('.');
      if (lastIdx >= 0) {
         String type = string.substring(0, lastIdx);
         String name = string.substring(lastIdx + 1);
         return from(type, name);
      } else {
         if (DynamicEnum.isDefined(AnnotationType.class, string)) {
            return AnnotationType.valueOf(string);
         } else if (DynamicEnum.isDefined(AttributeType.class, string)) {
            return AttributeType.valueOf(string);
         } else if (DynamicEnum.isDefined(RelationType.class, string)) {
            return RelationType.valueOf(string);
         }
      }
      throw new IllegalArgumentException("No type specified.");
   }

   /**
    * From annotatable type.
    *
    * @param type the type
    * @param name the name
    * @return the annotatable type
    */
   static AnnotatableType from(@NonNull String type, @NonNull String name) {
      if (type.equalsIgnoreCase("annotation") || type.equalsIgnoreCase(AnnotationType.class.getCanonicalName())) {
         return AnnotationType.create(name);
      } else if (type.equalsIgnoreCase("attribute") || type.equalsIgnoreCase(AttributeType.class.getCanonicalName())) {
         return AttributeType.create(name);
      } else if (type.equalsIgnoreCase("relation") || type.equalsIgnoreCase(RelationType.class.getCanonicalName())) {
         return RelationType.create(name);
      }
      throw new IllegalArgumentException(type + " is and invalid type.");
   }

   /**
    * To name string.
    *
    * @param type the type
    * @param name the name
    * @return the string
    */
   static String toName(@NonNull String type, @NonNull String name) {
      type = type.toLowerCase();
      if (!type.endsWith(".")) {
         type = type + ".";
      }
      return name.toLowerCase().startsWith(type) ? name.substring(type.length()) : name;
   }

   /**
    * To type name string.
    *
    * @param type the type
    * @param name the name
    * @return the string
    */
   static String toTypeName(@NonNull String type, @NonNull String name) {
      int dot = name.indexOf('.');
      if (dot < 0) {
         return StringUtils.toTitleCase(type) + "." + name;
      }

      String sub = name.substring(0, dot);
      if (sub.equalsIgnoreCase(type)) {
         return StringUtils.toTitleCase(type) + name.substring(dot);
      }

      return StringUtils.toTitleCase(type) + "." + name;
   }
}//END OF AnnotationTypes
