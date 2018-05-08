package org.hucompute.textimager.client;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CasConsumer;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
//import org.hucompute.services.inputreader.TeiReader;
import org.hucompute.services.uima.database.mongo.MongoWriter;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
//
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2000Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2000Writer;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Writer;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Writer;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2009Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2009Writer;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2012Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2012Writer;
import de.tudarmstadt.ukp.dkpro.core.io.tcf.TcfReader;
import de.tudarmstadt.ukp.dkpro.core.io.tcf.TcfWriter;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiWriter;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;

public class TextImagerOptions {
	public enum Language{
		unknown,
		de,
		la,
		en,
		fr,
		es,
		zh
	}

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

	public enum IOFormat{
		TCF,
		XMI,
		CONLL2000,
		CONLL2002,
		CONLL2006,
		CONLL2009,
		CONLL2012,
		TEI,
		BINARYCAS,
		TXT
	}

	protected static CollectionReader getReader(IOFormat format,String inputDir,Language language) throws ResourceInitializationException{
		Class<? extends CollectionReader> reader = null;
		String pattern = "[+]**/*.";
		switch (format) {
		case TCF:
			reader = TcfReader.class;
			pattern = pattern + "tcf";
			break;
		case XMI:
			reader = XmiReader.class;
			pattern = pattern + "xmi";
			break;
		case TXT:
			reader = TextReader.class;
			pattern = pattern + "txt";
			break;
//		case TEI:
//			reader = TeiReader.class;
//			pattern = pattern + "xml";
//			break;
		case CONLL2000:
			reader = Conll2000Reader.class;
			pattern = pattern + "conll";
			break;
		case CONLL2002:
			reader = Conll2002Reader.class;
			pattern = pattern + "conll";
			break;
		case CONLL2006:
			reader = Conll2006Reader.class;
			pattern = pattern + "conll";
			break;
		case CONLL2009:
			reader = Conll2009Reader.class;
			pattern = pattern + "conll";
			break;
		case CONLL2012:
			reader = Conll2012Reader.class;
			pattern = pattern + "conll";
			break;
		case BINARYCAS:
			reader = BinaryCasReader.class;
			pattern = pattern + "bin";
			break;
		default:
			throw new UnsupportedOperationException("Input format not supported. Supported output formats are TCF, XMI, TXT, TEI, CONLL2000, CONLL2002, CONLL2006, CONLL2009, BINARYCAS");
		}
		return CollectionReaderFactory.createReader(reader,
				ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,inputDir,
				ResourceCollectionReaderBase.PARAM_PATTERNS,pattern,
				ResourceCollectionReaderBase.PARAM_LANGUAGE,language);
	}
//
	protected static AnalysisEngineDescription getWriter(IOFormat format,String outputDir) throws ResourceInitializationException{
		Class<? extends JCasConsumer_ImplBase> consumer = null;
		switch (format) {
		case TCF:
			consumer = TcfWriter.class;
			break;
		case XMI:
			consumer = XmiWriter.class;
			break;
		case TEI:
			consumer = TeiWriter.class;
			break;
		case CONLL2000:
			consumer = Conll2000Writer.class;
			break;
		case CONLL2002:
			consumer = Conll2002Writer.class;
			break;
		case CONLL2006:
			consumer = Conll2006Writer.class;
			break;
		case CONLL2009:
			consumer = Conll2009Writer.class;
			break;
		case CONLL2012:
			consumer = Conll2012Writer.class;
			break;
		case BINARYCAS:
			consumer = BinaryCasWriter.class;
			break;
		default:
			throw new UnsupportedOperationException("Output format not supported. Supported output formats are TCF, XMI, TEI, CONLL2000, CONLL2002, CONLL2006, CONLL2009, BINARYCAS");
		}
		return createEngineDescription(consumer,XmiWriter.PARAM_TARGET_LOCATION,outputDir,XmiWriter.PARAM_OVERWRITE,true);
	}



}
