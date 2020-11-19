package org.hucompute.textimager.client.rest.ducc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.xml.sax.SAXException;


public class PipelineTest {
	private static HashMap<String, String> options = new HashMap<>();
	
	public static ArrayList<ServiceDataholder> constructPipeline(HashMap<String, String> options) throws JAXBException, IOException, XPathExpressionException, NullPointerException, UIMAException, SAXException, ParserConfigurationException{
		Pipeline pipeObject = new Pipeline();
		HashMap<String, ArrayList<ArrayList<ServiceDataholder>>> pipeline = pipeObject.constructPipeline(options,JCasFactory.createJCas(), null);
		Iterator<Entry<String, ArrayList<ArrayList<ServiceDataholder>>>> iter = pipeline.entrySet().iterator();
		ArrayList<ServiceDataholder>pipelineStream = new ArrayList<>();
		while(iter.hasNext()){
			Entry<String, ArrayList<ArrayList<ServiceDataholder>>> next = iter.next();
			for (ArrayList<ServiceDataholder> step: next.getValue()) {
				pipelineStream.addAll(step);
			}
		}
		return pipelineStream;
	}

	public static void main(String...args) throws MalformedURLException, XPathExpressionException, NullPointerException, UIMAException, SAXException, IOException, ParserConfigurationException, JAXBException{
		Pipeline pipeObject = new Pipeline();

		addOption(TextImagerOptions.PARAGRAPHSPLITTER, TextImagerOptions.LANGUAGE_ENGLISH); //PARALLEL mit SEGMENTER_STANFORD.
		addOption(TextImagerOptions.POS_MARMOT, TextImagerOptions.LANGUAGE_ENGLISH);//PARALLEL mit PARAGRAPHSPLITTER.
		HashMap<String, ArrayList<ArrayList<ServiceDataholder>>> pipeline = pipeObject.constructPipeline(options,JCasFactory.createJCas(), "src/main/resources/surls_remote.xml");
		System.out.println(pipeline);
		Iterator<Entry<String, ArrayList<ArrayList<ServiceDataholder>>>> iter = pipeline.entrySet().iterator();
		ArrayList<ServiceDataholder>pipelineStream = new ArrayList<>();
		while(iter.hasNext()){
			Entry<String, ArrayList<ArrayList<ServiceDataholder>>> next = iter.next();
			for (ArrayList<ServiceDataholder> step: next.getValue()) {
				pipelineStream.addAll(step);
			}
		}
//		for (ServiceDataholder serviceDataholder : pipelineStream) {
//			System.out.println(serviceDataholder.getPackageName()+"."+serviceDataholder.getName());
//		}
		System.out.println(getDelegateAnalysisEngineSpecifiers(pipelineStream));
		System.out.println(getFixedFlow(pipelineStream));
	}

	public static String getDelegateAnalysisEngineSpecifiers(ArrayList<ServiceDataholder>pipelineStream){
		StringBuilder sb = new StringBuilder();
		for (ServiceDataholder serviceDataholder : pipelineStream) {
			sb.append("<delegateAnalysisEngine key=\"" + serviceDataholder.getName() + "\">").append(System.lineSeparator())
			.append("<import location=\""+Paths.get(DUCCAPI.DUCC_SERVICE_SCRIPTS,serviceDataholder.getName(),  serviceDataholder.getName() + ".xml").toString()+"\"/>").append(System.lineSeparator())
			.append("</delegateAnalysisEngine>").append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	public static String getFixedFlow(ArrayList<ServiceDataholder>pipelineStream){
		StringBuilder sb = new StringBuilder();
		for (ServiceDataholder serviceDataholder : pipelineStream) {
			sb.append("<node>" + serviceDataholder.getName() + "</node> ").append(System.lineSeparator());
		}
		return sb.toString();
	}

	public static  void addOption(String annotator, String language){
		if(options.containsKey(language))
			options.put(language, options.get(language)+","+annotator);
		else
			options.put(language, annotator);
	}
}

