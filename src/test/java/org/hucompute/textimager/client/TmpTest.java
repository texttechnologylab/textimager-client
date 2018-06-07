package org.hucompute.textimager.client;

import static org.dom4j.DocumentHelper.createDocument;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.services.type.CategoryCoveredTagged;
import org.hucompute.textimager.client.TextImagerClient;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.TextImagerOptions.Language;
import org.hucompute.textimager.config.ConfigDataholder;
import org.hucompute.textimager.config.ServiceDataholder;
import org.hucompute.textimager.uima.io.mediawiki.MediawikiWriter;
import org.hucompute.textimager.util.XmlFormatter;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2000Writer;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.lab.uima.engine.uimaas.AsAnalysisEngineDescription;
import de.tudarmstadt.ukp.dkpro.lab.uima.engine.uimaas.AsDeploymentDescription;

public class TmpTest {

	public static void main(String[] args) throws ResourceInitializationException, Exception {
		//		TextImagerClient client = new TextImagerClient();
		//		client.setConfigFile("src/main/resources/services.xml");
		//		client.processCollection(
		//				new File("src/test/resources/collectionTest"),
		//				IOFormat.TXT,
		//				TextImagerOptions.Language.de,
		//				new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTLemma", "MarMoTTagger","FastTextDDC3LemmaNoPunctPOSNoFunctionwordsWithCategoriesService"},
		//				10,
		//				new UimaAsBaseCallbackListener() {
		//					@Override
		//					public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
		//						// TODO Auto-generated method stub
		//						super.entityProcessComplete(aCas, aStatus);
		//						try {
		//							Collection<CategoryCoveredTagged> ddcs = JCasUtil.select(aCas.getJCas(), CategoryCoveredTagged.class);
		//							System.out.println(ddcs.size());
		//
		//						} catch (CASException e) {
		//							// TODO Auto-generated catch block
		//							e.printStackTrace();
		//						}
		//					}
		//
		//					@Override
		//					public void collectionProcessComplete(EntityProcessStatus aStatus) {
		//						super.collectionProcessComplete(aStatus);
		//						System.out.println("collection complete");
		//					};
		//				});

		File outputFolder = new File("src/test/resources/wikiOutput");

		outputFolder.mkdirs();
		AnalysisEngineDescription casConsumer = AnalysisEngineFactory.createEngineDescription(
				MediawikiWriter.class,
				MediawikiWriter.PARAM_TARGET_LOCATION,outputFolder.getPath()
				);
		TextImagerClient client = new TextImagerClient();
		client.setConfigFile("src/main/resources/services.xml");
//		client.processCollection(TextImagerOptions.getReader(IOFormat.TXT, "src/test/resources/collectionTest", TextImagerOptions.Language.de),
//				Language.de, 
//				new String[]{"LanguageToolSegmenter","LanguageToolLemmatizer","StanfordPosTagger"}, 
//				casConsumer);
		client.processCollection(new File("src/test/resources/collectionTestConll"), IOFormat.CONLL2009, Language.de, new String[]{"LanguageToolSegmenter"}, IOFormat.CONLL2000, "src/test/resources/collectionTestConllOutput");
	}
}
