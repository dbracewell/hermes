![Hermes](https://github.com/dbracewell/hermes/blob/gh-pages/images/hermes.png)

A Natural Language Processing framework for Java based on the [Tipster Architecture](http://cs.nyu.edu/cs/faculty/grishman/tipster.html). Check out the [Wiki](https://github.com/dbracewell/hermes/wiki) for more information and a Quickstart example.

## License
Copyright 2015 David B. Bracewell

Hermes is [Apache License, Version 2.0 ](LICENSE) licensed making it free for all uses.

## Maven
If you use maven, you can get the latest release using the following dependency:

### Hermes Core
The core sub-module contains the basic api.
```
 <dependency>
     <groupId>com.davidbracewell</groupId>
     <artifactId>hermes-core</artifactId>
     <version>0.1</version>
 </dependency>
```

### OpenNLP Backend
A set of wrappers around the [OpenNLP](https://opennlp.apache.org/) framework for tokenization, sentence segmentation, part-of-speech tagging, shallow parsing, named entity recognition, and syntactic parsing (tbd).
```
 <dependency>
     <groupId>com.davidbracewell</groupId>
     <artifactId>hermes-opennlp</artifactId>
     <version>0.1</version>
 </dependency>
```

### MaltParser Backend
A wrapper around [MaltParser](http://www.maltparser.org/) provides dependency relations between tokens.
```
 <dependency>
     <groupId>com.davidbracewell</groupId>
     <artifactId>hermes-maltparser</artifactId>
     <version>0.1</version>
 </dependency>
```

