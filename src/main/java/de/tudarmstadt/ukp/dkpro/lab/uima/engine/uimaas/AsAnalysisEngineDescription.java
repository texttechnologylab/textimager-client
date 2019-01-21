/*******************************************************************************
 * Copyright 2011
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.lab.uima.engine.uimaas;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.hucompute.textimager.config.ServiceDataholder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;

public class AsAnalysisEngineDescription
{



	public static void toXML(File file,HashMap<String, ArrayList<ArrayList<ServiceDataholder>>> pipeline) throws IOException, ParserConfigurationException, SAXException, TransformerException{
		//		AsAnalysisEngineDescription ae  = AnalysisEngineFactory.create


		File dest = new File(System.getProperty("java.io.tmpdir")+"/HucomputeFixedFlowController.xml");
		if(!dest.exists()){
			URL inputUrl = AsAnalysisEngineDescription.class.getClassLoader().getResource("HucomputeFixedFlowController.xml");
			FileUtils.copyURLToFile(inputUrl, dest);
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(AsAnalysisEngineDescription.class.getClassLoader().getResourceAsStream("emptyAnalysisEngine.xml"));

		Element delegatesList = (Element) doc.getElementsByTagName("delegateAnalysisEngineSpecifiers").item(0);
		((Element)doc.getElementsByTagName("configurationParameterSettings").item(0)).getElementsByTagName("string").item(0).setTextContent(pipeline.toString());;

		HashSet<ServiceDataholder> uniquePipeline = new HashSet<>();
		Iterator<Entry<String, ArrayList<ArrayList<ServiceDataholder>>>> iter = pipeline.entrySet().iterator();
		while(iter.hasNext()){
			for (ArrayList<ServiceDataholder> serviceDataholder : iter.next().getValue()) {
				for (ServiceDataholder serviceDataholder2 : serviceDataholder) {
					uniquePipeline.add(serviceDataholder2);
				}
			}
		}

		for (ServiceDataholder serviceDataholder : uniquePipeline) {
			Element delegateAnalysisEngine = doc.createElement("delegateAnalysisEngine");
			delegateAnalysisEngine.setAttribute("key", serviceDataholder.getName());

			File remoteDescriptor = new File(System.getProperty("java.io.tmpdir")+"/"+serviceDataholder.getName()+".xml");
			AsRemoteDescription.toXML(remoteDescriptor, serviceDataholder);


			Element importNode = doc.createElement("import");
			if(SystemUtils.IS_OS_WINDOWS)
				importNode.setAttribute("location", "file:/"+remoteDescriptor.getPath().replace("\\", "/"));
			else
				importNode.setAttribute("location", remoteDescriptor.getPath());

			delegateAnalysisEngine.appendChild(importNode);
			delegatesList.appendChild(delegateAnalysisEngine);
			if(serviceDataholder.getCasMultiplierPoolsize()>0)
				((Element)doc.getElementsByTagName("outputsNewCASes").item(0)).setTextContent("true");;


		}

		//		for (ArrayList<ServiceDataholder> delegateName : uniquePipeline) {
		//			String pipelineLine = "";
		//			for (ServiceDataholder service : delegateName) {
		//				if(pipelineLine.length()!=0)
		//					pipelineLine+=",";
		//				pipelineLine+=service.getName();
		//				Element delegateAnalysisEngine = doc.createElement("delegateAnalysisEngine");
		//				delegateAnalysisEngine.setAttribute("key", service.getName());
		//
		//				File remoteDescriptor = new File(System.getProperty("java.io.tmpdir")+"/"+service.getName()+".xml");
		//				AsRemoteDescription.toXML(remoteDescriptor, service);
		//
		//
		//				Element importNode = doc.createElement("import");
		//				importNode.setAttribute("location", remoteDescriptor.getPath());
		//
		//				delegateAnalysisEngine.appendChild(importNode);
		//				delegatesList.appendChild(delegateAnalysisEngine);
		//			}
		//
		//
		//
		//			Element pipelineString = doc.createElement("string");
		//			pipelineString.setTextContent(pipelineLine);
		//			pipelineArray.appendChild(pipelineString);
		//		}
		FileOutputStream os = new FileOutputStream(file);
		printDocument(doc, os);
	}

	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), 
				new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	/**
	 * Close a {@link Closeable} object. This method is best used in {@code finally}
	 * sections.
	 *
	 * @param object the object to close.
	 */
	public static void close(final Closeable object)
	{
		if (object == null) {
			return;
		}

		try {
			object.close();
		}
		catch (IOException e) {
			// Ignore exceptions happening while closing.
		}
	}
}
