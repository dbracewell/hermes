package com.davidbracewell.hermes.tag;

import com.davidbracewell.annotation.DynamicEnumeration;


/**
 * @author David B. Bracewell
 */
@DynamicEnumeration(
  className = "RelationType",
  configPrefix = "RelationType"
)
public interface Relations {

  RelationType DEPENDENCY = RelationType.create("DEPENDENCY");

  RelationType EVENT_ROLE = RelationType.create("EVENT_ROLE");

}//END OF Relations
