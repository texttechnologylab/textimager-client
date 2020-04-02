# textimager-client
The TextImager-Client is the API for communicating with [TextImager-Servers](https://github.com/texttechnologylab/textimager-server).

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites
In order to run the TextImager Client you need
* Java 8
* Maven

### Installing
Clone and star this repository
```
git clone https://github.com/texttechnologylab/textimager-client
```
Navigate to the directory and build project
```
cd ../some/dir/textimager-client
mvn install -DskipTests -P CLI
```

### Running

#### CLI API
After ```mvn install``` the CLI script will be generated in ```target/textimager-CLI.jar```.

Instructions and help will be printed with:  ```java -jar target/textimager-CLI.jar -h```

Example client call:
```
java -Dfile.encoding=UTF-8 -jar target/textimager-CLI.jar -I "This is a test." -p "CoreNlpSegmenter,CoreNlpPosTagger" -o "test.xmi"
```

The above example will parse the input document defined in ```-I``` with the pipeline defined in ```-p``` and writes the output as XMI defined in ```-o```

#### Java Project

```java
TextImagerClient client = new TextImagerClient();
client.setConfigFile("src/main/resources/services.xml");
CAS output = client.process("Das ist ein Test. Das ist ein Test.",  new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTLemma", "MarMoTTagger"});
System.out.println(XmlFormatter.getPrettyString(output));
```
1. Instanciate an instance of TextImagerClient
2. Set the configFile. This configFile contains the location and prerequierments of each service.
3. process() input document with given pipeline. The names of the taggers must be defined in the previously set configFile.
4. get [CAS](https://uima.apache.org/downloads/releaseDocs/2.3.0-incubating/docs/api/org/apache/uima/cas/CAS.html) output.

### TODO
- [ ] Documentation
- [x] Add input/output support for different formats
- [x] CLI
- [x] Support for Windows

