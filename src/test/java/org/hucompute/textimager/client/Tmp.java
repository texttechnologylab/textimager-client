package org.hucompute.textimager.client;

import java.io.File;

import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.textimager.util.XmlFormatter;

public class Tmp {

	public static void main(String[] args) throws ResourceInitializationException, Exception {
		TextImagerClient client = new TextImagerClient();
		client.processCollection(
				new File("/home/wahed/Downloads/teis"), 
				TextImagerOptions.IOFormat.TEI, 
				TextImagerOptions.Language.de, 
				"LanguageToolSegmenter".split(","), 
				TextImagerOptions.IOFormat.XMI, 
				"/tmp/output/",
				false,
				"tei",
				"UTF-8");

	}

}
