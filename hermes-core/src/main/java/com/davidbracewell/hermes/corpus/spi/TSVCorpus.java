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
  public static final String CONFIG_PROPERTY = "TSVCorpus";

  public static void setCommentCharacter(char commentCharacter) {
    Config.setProperty(
      CONFIG_PROPERTY + ".comment",
      Character.toString(commentCharacter)
    );
  }

  public static void setHasHeader(boolean hasHeader) {
    Config.setProperty(
      CONFIG_PROPERTY + ".hasHeader",
      Boolean.toString(hasHeader)
    );
  }

  public static void setFieldNames(@NonNull String... names) {
    Config.setProperty(
      CONFIG_PROPERTY + ".fields",
      Joiner.on(',').join(names)
    );
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
