package org.hucompute.textimager.client;

import java.io.File;

import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.TextImagerOptions.Language;

public class SimpleMediawikiExporter {

	public static void main(String[] args) throws ResourceInitializationException, Exception {
		TextImagerClient client = new TextImagerClient();
		client.processCollection(
				new File("inputTexte"), 
				IOFormat.TXT, 
				Language.de, 
				new String[]{
						"LanguageToolSegmenter",
						"LanguageToolLemmatizer",
						"CoreNlpPosTagger",
						"SpaCyNER",
						"FastTextDDCMulLemmaNoPunctPOSNoFunctionwordsWithCategoriesTextImagerService",
						"TagMeLocalAnnotator",
						"MateMorphTagger" 
				},
				IOFormat.XMI, "output");
	}

}
