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
import org.hucompute.textimager.util.XmlFormatter;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2000Writer;
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


		AnalysisEngineDescription casConsumer = AnalysisEngineFactory.createEngineDescription(
				Conll2000Writer.class,
				Conll2000Writer.PARAM_TARGET_LOCATION,"/home/ahemati/workspaceGit/textimager-client/test",
				Conll2000Writer.PARAM_OVERWRITE,true
				);
		TextImagerClient client = new TextImagerClient();
		client.setConfigFile("src/main/resources/services.xml");
//		client.processCollection(CollectionReaderFactory.createCollectionReader(
//				TestReader.class, 
//				TestReader.PARAM_MY_FANCY_PARAM, new String[]{"das ist ein param","hier noch einer"},
//				TestReader.PARAM_MY_FANCY_PARAM_2,42),
//				Language.de, 
//				new String[]{"LanguageToolSegmenter"}, 
//				casConsumer);
		client.processCollection(new File("src/test/resources/collectionTestConll"), IOFormat.CONLL2009, Language.de, new String[]{"LanguageToolSegmenter"}, IOFormat.CONLL2000, "src/test/resources/collectionTestConllOutput");

		//		System.out.println(XmlFormatter.getPrettyString(output));

		//		BaseUIMAAsynchronousEngine_impl uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
		//
		//		Map<String, Object> clientCtx = new HashMap<String, Object>();
		//
		//
		//		File deployFile = new File("/tmp/deployFile2073420664057259862.xml");
		//		//			deploymentDescription.toXML(deployFile);
		//		//			deploymentDescription.toXML(System.out);
		//
		//		// preparing map for use in deploying services
		//		clientCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath, ConfigDataholder.getDd2SpringPath());
		//		clientCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + ConfigDataholder.getSaxonPath());
		//
		//		//		addAnalysisEngine(deployFile,XmiWriter.class);
		//
		//		// creating aggregate analysis engine
		//		uimaAsEngine.deploy(deployFile.getAbsolutePath(), clientCtx);
		//		//System.out.println(FileUtils.readFileToString(deployFile.getAbsoluteFile(),"UTF-8"));
		//
		//		// preparing map for use in a UIMA client for submitting text to
		//		clientCtx.put(UimaAsynchronousEngine.ServerUri, "tcp://alba.hucompute.org:61617");
		//		clientCtx.put(UimaAsynchronousEngine.ENDPOINT, "unknown______HucomputeLanguageDetection___ParagraphSplitter___LanguageToolSegmenter");
		//		clientCtx.put(UimaAsynchronousEngine.Timeout, 50000 );
		//		clientCtx.put(UimaAsynchronousEngine.GetMetaTimeout, 50000 );
		//		clientCtx.put(UimaAsynchronousEngine.CpcTimeout, 50000);
		//		clientCtx.put(UimaAsynchronousEngine.CasPoolSize, 5);
		//		uimaAsEngine.setCollectionReader(CollectionReaderFactory.createCollectionReader(
		//				TestReader.class, 
		//				TestReader.PARAM_MY_FANCY_PARAM, new String[]{"das ist ein param","hier noch einer"},
		//				TestReader.PARAM_MY_FANCY_PARAM_2,42));
		//		uimaAsEngine.addStatusCallbackListener(new UimaAsBaseCallbackListener() {
		//			@Override
		//			public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
		//				// TODO Auto-generated method stub
		//				super.entityProcessComplete(aCas, aStatus);
		////				System.out.println(aStatus);
		////				System.out.println(XmlFormatter.getPrettyString(aCas));
		//			}
		//		});
		//
		//		// Initialize the client
		//		uimaAsEngine.initialize(clientCtx);	
		//		uimaAsEngine.process();
		//		uimaAsEngine.undeploy();
		//		uimaAsEngine.stop();
	}


	private static void addAnalysisEngine(File deploymentDescription,AnalysisEngineDescription desciption) throws ParserConfigurationException, SAXException, IOException, ResourceInitializationException{
		//adding to delegets list of remoteDeploymentDescriptor
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(deploymentDescription);
		Element delegates = (Element)doc.getElementsByTagName("delegates").item(0);
		Element casConsumerElement = doc.createElement("analysisEngine");
		String newAnnotatorName = desciption.getAnnotatorImplementationName().replaceAll(".*\\.", "");
		casConsumerElement.setAttribute("key", newAnnotatorName);
		delegates.appendChild(casConsumerElement);

		String locationDescriptor = ((Element)((Element)doc.getElementsByTagName("topDescriptor").item(0)).getElementsByTagName("import").item(0)).getAttribute("location");

		File casConsumerFile = new File("/tmp/" + newAnnotatorName + ".xml");
		desciption.toXML(new FileWriter(casConsumerFile));
		DocumentBuilderFactory dbFactory1 = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder1 = dbFactory1.newDocumentBuilder();
		Document doc1 = dBuilder1.parse(new File(locationDescriptor));
		Element delegateAnalysisEngineSpecifiers = (Element)doc1.getElementsByTagName("delegateAnalysisEngineSpecifiers").item(0);
		Element delegateAnalysisEngine = doc1.createElement("delegateAnalysisEngine");
		delegateAnalysisEngine.setAttribute("key", newAnnotatorName);
		Element importElement = doc1.createElement("import");
		importElement.setAttribute("location", casConsumerFile.getAbsolutePath());
		delegateAnalysisEngine.appendChild(importElement);
		delegateAnalysisEngineSpecifiers.appendChild(delegateAnalysisEngine);

		NodeList configurationParameterSettings = doc1.getElementsByTagName("nameValuePair");
		for (int i = 0; i < configurationParameterSettings.getLength(); i++) {
			Element nameValuePair = (Element)configurationParameterSettings.item(i);
			String name = nameValuePair.getElementsByTagName("name").item(0).getTextContent().trim();
			if(name.equals("Flow")){
				String stringValue = ((Element)nameValuePair.getElementsByTagName("string").item(0)).getTextContent().trim();
				JSONObject pipeline = new JSONObject(stringValue.replace("=", ":"));
				for (String object : pipeline.keySet()) {
					pipeline.getJSONArray(object).put(Arrays.asList(new String[]{newAnnotatorName}));
				}
				((Element)nameValuePair.getElementsByTagName("string").item(0)).setTextContent(pipeline.toString().replace(":", "="));
			}
		}

		//		FileUtils.writeStringToFile(new File(locationDescriptor), asString(doc1));
		//		FileUtils.writeStringToFile(deploymentDescription, asString(doc));

		//		delegates1.appendChild(casConsumerElement1);
		//		((Element)doc.getElementsByTagName("configurationParameterSettings").item(0)).getElementsByTagName("string").item(0).setTextContent(pipeline.toString());

	}

	private static String asString(Node node) {
		StringWriter writer = new StringWriter();
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			// @checkstyle MultipleStringLiterals (1 line)
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.VERSION, "1.0");
			if (!(node instanceof Document)) {
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}
			trans.transform(new DOMSource(node), new StreamResult(writer));
		} catch (final TransformerConfigurationException ex) {
			throw new IllegalStateException(ex);
		} catch (final TransformerException ex) {
			throw new IllegalArgumentException(ex);
		}
		return writer.toString();
	}

}
