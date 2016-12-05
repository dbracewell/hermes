package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.Joiner;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

/**
 * The type Csv corpus.
 */
@MetaInfServices(CorpusFormat.class)
public class CSVCorpus extends DSVCorpus {
  private static final long serialVersionUID = 1L;
  public static final String CONFIG_PROPERTY = "CSVCorpus";

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
   * Instantiates a new Csv corpus.
   */
  public CSVCorpus() {
    super(CONFIG_PROPERTY, ',');
  }

  @Override
  public String name() {
    return "CSV";
  }

}
