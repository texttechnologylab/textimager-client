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

import static org.dom4j.DocumentHelper.createDocument;
import static org.dom4j.DocumentHelper.createElement;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hucompute.textimager.config.ServiceDataholder;


/**
 * Each deployment descriptor describes one service, associated with a single
 * UIMA descriptor (aggregate or primitive), and describes the deployment of
 * those UIMA components that are co-located, together with specifications of
 * connections to those subcomponents that are remote.
 * <li>
 * The deployment descriptor is used to augment information contained in an
 * analysis engine descriptor. It adds information concerning
 * <li>
 * which components are managed using AS
 * <li>
 * queue names for connecting components
 * <li>
 * error thresholds and recovery / terminate action specifications
 * <li>
 * error handling routine specifications
 * <p>
 * The application can include both Java and non-Java components; the deployment
 * descriptors are slightly different for non-Java components.
 */
public class AsRemoteDescription
{
	final static Logger logger = Logger.getLogger(AsRemoteDescription.class);

	public static final Namespace NS = Namespace.get("http://uima.apache.org/resourceSpecifier");
	public static final QName E_ROOT = QName.get("customResourceSpecifier", NS);


	public static void toXML(File aOutput,ServiceDataholder simpleService) throws IOException
	{
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(aOutput);
			toXML(os,simpleService);
		}
		finally {
			close(os);
		}
	}

	public static void toXML(OutputStream aOutput,ServiceDataholder simpleService) throws IOException
	{
		Element root = createElement(E_ROOT);
		root.addElement("resourceClassName").addText("org.apache.uima.aae.jms_adapter.JmsAnalysisEngineServiceAdapter");
		Element parameters = root.addElement("parameters");
		parameters.addElement("parameter").addAttribute("name", "brokerURL").addAttribute("value", simpleService.getBrokerURL());
		parameters.addElement("parameter").addAttribute("name", "endpoint").addAttribute("value", simpleService.getEndpoint());
		parameters.addElement("parameter").addAttribute("name", "timeout").addAttribute("value", simpleService.getTimeout());
		parameters.addElement("parameter").addAttribute("name", "getmetatimeout").addAttribute("value", simpleService.getGetmetatimeout());
		parameters.addElement("parameter").addAttribute("name", "cpctimeout").addAttribute("value", simpleService.getCpctimeout());

		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding("UTF-8");
		XMLWriter writer = new XMLWriter(aOutput, outformat);
		writer.write(createDocument(root));
		writer.flush();
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
