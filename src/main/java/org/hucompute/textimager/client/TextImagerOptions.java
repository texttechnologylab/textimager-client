package org.hucompute.textimager.client;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
//import org.hucompute.textimager.uima.io.html.EnhancedHtmlReader;

import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
//
import org.dkpro.core.io.bincas.BinaryCasReader;
import org.dkpro.core.io.bincas.BinaryCasWriter;
import org.dkpro.core.io.conll.Conll2000Reader;
import org.dkpro.core.io.conll.Conll2000Writer;
import org.dkpro.core.io.conll.Conll2002Reader;
import org.dkpro.core.io.conll.Conll2002Writer;
import org.dkpro.core.io.conll.Conll2006Reader;
import org.dkpro.core.io.conll.Conll2006Writer;
import org.dkpro.core.io.conll.Conll2009Reader;
import org.dkpro.core.io.conll.Conll2009Writer;
import org.dkpro.core.io.conll.Conll2012Reader;
import org.dkpro.core.io.conll.Conll2012Writer;
//import org.dkpro.core.io.conll.ConllUReader;
//import org.dkpro.core.io.conll.ConllUWriter;
import org.dkpro.core.io.tcf.TcfReader;
import org.dkpro.core.io.tcf.TcfWriter;
import org.dkpro.core.io.tei.TeiReader;
import org.dkpro.core.io.tei.TeiWriter;
//import org.dkpro.core.io.tei.TeiReader;
//import org.dkpro.core.io.tei.TeiWriter;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.io.xmi.XmiReader;
import org.dkpro.core.io.xmi.XmiWriter;
import org.hucompute.textimager.uima.io.mediawiki.MediawikiWriter;

public class TextImagerOptions {
	public enum Language{
		unknown,
		de,
		la,
		en,
		fr,
		es,
		zh,
		da,
		nl,
		el,
		it,
		ja,
		lt,
		nb,
		pl,
		pt,
		ro,
		ru,
		ca,
		mk,
		af,
		grc,
		ar,
		hy,
		eu,
		be,
		bg,
		bxr,
		zh_hans,
		zh_hant,
		lzh,
		cop,
		hr,
		cs,
		et,
		fi,
		gl,
		got,
		he,
		hi,
		hu,
		id,
		ga,
		kk,
		ko,
		kmr,
		lv,
		olo,
		mt,
		mr,
		sme,
		no,
		nn,
		cu,
		fro,
		orv,
		fa,
		gd,
		sr,
		sk,
		sl,
		sv,
		swl,
		ta,
		te,
		tr,
		uk,
		hsb,
		ur,
		ug,
		vi,
		wo
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
		CONLLU,
		TEI,
		BINARYCAS,
		TXT,
		MEDIAWIKI,
		HTML,
		WIKIDRAGON,
		TEI_TTLAB
	}

	public static CollectionReader getReader(IOFormat format,String inputDir,Language language, String fileSuffix, String sourceEncoding) throws ResourceInitializationException{
		Class<? extends CollectionReader> reader = null;
		String pattern = "[+]**/*.";
		switch (format) {
			case TCF:
				reader = TcfReader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "tcf" : fileSuffix);
				break;
			case XMI:
				reader = XmiReader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "xmi" : fileSuffix);
				break;
			case TXT:
				reader = TextReader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "txt" : fileSuffix);
				break;
			case TEI:
				reader = TeiReader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "xml" : fileSuffix);
				break;
			case CONLL2000:
				reader = Conll2000Reader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "conll" : fileSuffix);
				break;
			case CONLL2002:
				reader = Conll2002Reader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "conll" : fileSuffix);
				break;
			case CONLL2006:
				reader = Conll2006Reader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "conll" : fileSuffix);
				break;
			case CONLL2009:
				reader = Conll2009Reader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "conll" : fileSuffix);
				break;
			case CONLL2012:
				reader = Conll2012Reader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "conll" : fileSuffix);
				break;
//		case CONLLU:
//			reader = ConllUReader.class;
//			pattern = pattern + (fileSuffix.isEmpty() ? "conll" : fileSuffix);
//			break;
			case BINARYCAS:
				reader = BinaryCasReader.class;
				pattern = pattern + (fileSuffix.isEmpty() ? "bin" : fileSuffix);
				break;
//			case HTML:
//				reader = EnhancedHtmlReader.class;
//				pattern = pattern + (fileSuffix.isEmpty() ? "html" : fileSuffix);
//				return getEnhancedHtmlReader(language, inputDir, pattern, sourceEncoding);
			default:
				throw new UnsupportedOperationException("Input format not supported. Supported output formats are TCF, XMI, TXT, TEI, CONLL2000, CONLL2002, CONLL2006, CONLL2009, BINARYCAS");
		}
		if(language == Language.unknown)
			return CollectionReaderFactory.createReader(reader,
					ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,inputDir,
					ResourceCollectionReaderBase.PARAM_PATTERNS,pattern
//					ConllUReader.PARAM_READ_CPOS,false,
//					ConllUReader.PARAM_READ_DEPENDENCY,false,
//					ConllUReader.PARAM_READ_LEMMA,false,
//					ConllUReader.PARAM_READ_MORPH,false,
//					ConllUReader.PARAM_READ_POS,false
			);
		else
			return CollectionReaderFactory.createReader(reader,
					ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,inputDir,
					ResourceCollectionReaderBase.PARAM_PATTERNS,pattern,
					ResourceCollectionReaderBase.PARAM_LANGUAGE,language
//					ConllUReader.PARAM_READ_CPOS,false,
//					ConllUReader.PARAM_READ_DEPENDENCY,false,
//					ConllUReader.PARAM_READ_LEMMA,false,
//					ConllUReader.PARAM_READ_MORPH,false,
//					ConllUReader.PARAM_READ_POS,false
			);
	}

	public static CollectionReader getReader(IOFormat format,String inputDir,Language language) throws ResourceInitializationException{
		// empty suffix to use defaults
		return getReader(format, inputDir, language, "", "");
	}
	//
	
//	private static CollectionReader getEnhancedHtmlReader(Language language, String inputDir, String pattern, String sourceEncoding) throws ResourceInitializationException {
//		if (sourceEncoding == null || sourceEncoding.isEmpty()) {
//			sourceEncoding = "auto";
//		}
//		if(language == Language.unknown)
//			return CollectionReaderFactory.createReader(EnhancedHtmlReader.class,
//					ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,inputDir,
//					ResourceCollectionReaderBase.PARAM_PATTERNS,pattern,
//					EnhancedHtmlReader.PARAM_SOURCE_ENCODING, sourceEncoding
//			);
//		else
//			return CollectionReaderFactory.createReader(EnhancedHtmlReader.class,
//					ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,inputDir,
//					ResourceCollectionReaderBase.PARAM_PATTERNS,pattern,
//					ResourceCollectionReaderBase.PARAM_LANGUAGE,language,
//					EnhancedHtmlReader.PARAM_SOURCE_ENCODING, sourceEncoding
//			);
//	}

	public static AnalysisEngineDescription getWriter(IOFormat format,String outputDir) throws ResourceInitializationException{
		return getWriter(format, outputDir, false);
	}

	protected static AnalysisEngineDescription getWriter(IOFormat format,String outputDir, boolean singularTarget) throws ResourceInitializationException{
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
//		case CONLLU:
//			consumer = ConllUWriter.class;
//			break;
		case BINARYCAS:
			consumer = BinaryCasWriter.class;
			break;
		case MEDIAWIKI:
			consumer = MediawikiWriter.class;
			break;
		default:
			throw new UnsupportedOperationException("Output format not supported. Supported output formats are TCF, XMI, TEI, CONLL2000, CONLL2002, CONLL2006, CONLL2009, BINARYCAS");
		}
		return createEngineDescription(consumer,XmiWriter.PARAM_TARGET_LOCATION,outputDir,XmiWriter.PARAM_OVERWRITE,true, JCasFileWriter_ImplBase.PARAM_SINGULAR_TARGET,singularTarget);
	}

}
