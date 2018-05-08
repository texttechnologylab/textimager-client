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
mvn install
```

### Running

#### CLI API
A CLI of TextImager-Client will is in development and will be available soon.

#### Java Project

```java
TextImagerClient client = new TextImagerClient();
client.setConfigFile("src/main/resources/services.xml");
CAS output = client.process("Das ist ein Test. Das ist ein Test.",  new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTLemma", "MarMoTTagger", "FastTextDDC2LemmaNoPunctPOSNoFunctionwordsWithCategoriesService", "FastTextDDC3LemmaNoPunctPOSNoFunctionwordsWithCategoriesService", "FastTextDDCMulService"});
System.out.println(XmlFormatter.getPrettyString(output));
```
1. Instanciate an instance of TextImagerClient
2. Set the configFile. This configFile contains the location and prerequierments of each service.
3. process() input document with given pipeline. The names of the taggers must be defined in the previously set configFile.
4. get [CAS](https://uima.apache.org/downloads/releaseDocs/2.3.0-incubating/docs/api/org/apache/uima/cas/CAS.html) output.

### TODO
- [ ] Add input/output support for different formats
- [ ] CLI

