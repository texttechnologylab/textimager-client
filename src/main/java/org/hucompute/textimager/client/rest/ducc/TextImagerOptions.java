package org.hucompute.textimager.client.rest.ducc;


public class TextImagerOptions {
	public final static String LANGUAGE_UNKNOWN = "unknown";
	public final static String LANGUAGE_GERMAN = "de";
	public final static String LANGUAGE_LATIN = "la";
	public final static String LANGUAGE_ENGLISH = "en";
	public final static String LANGUAGE_FRENCH = "fr";
	public final static String LANGUAGE_SPANISH = "es";
	public final static String LANGUAGE_CHINESE = "zh";


	public final static String PARAGRAPHSPLITTER = "ParagraphSplitter";


	public final static String SEGMENTER_BREAKITERATOR = "BreakIteratorSegmenter";
	public final static String SEGMENTER_STANFORD = "StanfordSegmenter";
	public final static String SEGMENTER_LANGUAGETOOL = "LanguageToolSegmenter";
	public final static String SEGMENTER_OPENNLP = "OpenNlpSegmenter";


	public final static String LANGUAGE_DETECTION = "HucomputeLanguageDetection";


	public final static String POS_MARMOT = "MarMoTTagger";
	public final static String POS_STANFORD = "StanfordPosTagger";
	public final static String POS_HUNPOSTAGGER = "HunPosTagger";
	public final static String POS_MATEPOSTAGGER = "MatePosTagger";
	public final static String POS_OPENNLPPOSTAGGER = "OpenNlpPosTagger";
	public final static String POS_RFTAGGER = "RfTagger";
	public final static String POS_TREETAGGERPOSTAGGER = "TreeTaggerPosTagger";
	//	public final static String POS_SFSTANNOTATOR = "SfstAnnotator";


	public final static String CHUNKER_TREETAGGERCHUNKER = "TreeTaggerChunker";
	public final static String MORPH_MATE = "MateMorphTagger";


	public final static String LEMMATIZER_STANFORD = "StanfordLemmatizer";
	public final static String LEMMATIZER_LANGUAGETOOLLEMMATIZER = "LanguageToolLemmatizer";
	public final static String LEMMATIZER_MATELEMMATIZER = "MateLemmatizer";
	public final static String LEMMATIZER_MARMOT = "MarMoTLemma";


	public final static String PARSER_STANFORD = "StanfordParser";
	public final static String PARSER_BERKELEYPARSER = "BerkeleyParser";
	public final static String PARSER_MATEPARSER = "MateParser";
	public final static String PARSER_MALTPARSER = "MaltParser";


	public final static String TIME_HEIDELTIME = "HeidelTime";


	public final static String NER_STANFORD = "StanfordNamedEntityRecognizer";
	public final static String NER_OPENNLP = "OpenNlpNamedEntityRecognizer";


	public final static String SENTIMENT_SENTIWS = "Sentiws";


	public final static String SIMILARITY_COSINE = "CosineSimilarity";
	public final static String SIMILARITY_WORDNGRAMJACCARDMEASURE = "WordNGramJaccardMeasure";

	public final static String COREF_CORZU = "CorZuAnnotator";
	public final static String COREF_STANFORD = "StanfordCoreferenceResolver";

	public final static String WIKIFY_IXA = "IXAWikify";
	public final static String WIKIFY_WIKIDATAHYPONYMS = "WikiDataHyponyms";

}
