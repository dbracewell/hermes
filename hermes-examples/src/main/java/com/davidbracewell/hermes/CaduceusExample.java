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

import com.davidbracewell.hermes.caduceus.CaduceusProgram;
import com.davidbracewell.io.Resources;

/**
 * @author David B. Bracewell
 */
public class CaduceusExample {

  public static void main(String[] args) throws Exception {
    //Initializes configuration settings
    Hermes.initializeApplication(args);

    //We use the example program
    CaduceusProgram program = CaduceusProgram.read(Resources.from("classpath:com/davidbracewell/hermes/example.yaml"));

    Document document = DocumentFactory.getInstance().create(
      "John Doe spooked his family while they were on vacation in St. George Falls. He was also spooked though."
    );
    Pipeline.process(document, Types.DEPENDENCY, Types.PHRASE_CHUNK);
    program.execute(document);

    RelationType eventRole = Types.relation("EVENT_ROLE");
    AnnotationType eventType = Types.annotation("EVENT");
    document.get(eventType).forEach(
      event -> {
        Annotation spooker = event.sources(eventRole, "SPOOKER").stream().findFirst().orElse(null);
        Annotation spookee = event.sources(eventRole, "SPOOKEE").stream().findFirst().orElse(null);
        System.out.println("EVENT := " + event);
        System.out.println("\tSPOOKER := " + spooker);
        System.out.println("\tSPOOKEE := " + spookee);
        System.out.println("==================================");
      }
    );

  }

}//END OF Sandbox
