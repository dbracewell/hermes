package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.Joiner;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

/**
 * The type Tsv corpus.
 */
@MetaInfServices(CorpusFormat.class)
public class TSVCorpus extends DSVCorpus {
   private static final long serialVersionUID = 1L;
   /**
    * Prefix to use when setting options via configuration
    */
   public static final String CONFIG_PROPERTY = "TSVCorpus";

   /**
    * Sets the comment character.
    *
    * @param commentCharacter the comment character
    */
   public static void setCommentCharacter(char commentCharacter) {
      Config.setProperty(CONFIG_PROPERTY + ".comment",
                         Character.toString(commentCharacter));
   }

   /**
    * Sets whether the tsv file has a header
    *
    * @param hasHeader true - has header, false no header
    */
   public static void setHasHeader(boolean hasHeader) {
      Config.setProperty(CONFIG_PROPERTY + ".hasHeader",
                         Boolean.toString(hasHeader));
   }

   /**
    * Sets the field names.
    *
    * @param names the field names
    */
   public static void setFieldNames(@NonNull String... names) {
      Config.setProperty(CONFIG_PROPERTY + ".fields",
                         Joiner.on(',').join(names));
   }

   /**
    * Instantiates a new Tsv corpus.
    */
   public TSVCorpus() {
      super(CONFIG_PROPERTY, '\t');
   }

   @Override
   public String name() {
      return "TSV";
   }

}//END OF TSVCorpus
