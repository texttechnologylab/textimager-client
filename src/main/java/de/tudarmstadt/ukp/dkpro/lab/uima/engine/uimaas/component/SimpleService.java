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
package de.tudarmstadt.ukp.dkpro.lab.uima.engine.uimaas.component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.textimager.config.ConfigDataholder;

import de.tudarmstadt.ukp.dkpro.lab.uima.engine.uimaas.AsDeploymentDescription;


public class SimpleService 
{
	private final Log log = LogFactory.getLog(getClass());

	private UimaAsynchronousEngine uimaAsEngine;

	private String endpoint;
	private AnalysisEngineDescription aeDesc;
	private String serviceId;
	private String brokerUrl;

	public SimpleService(final String aEndpoint, final AnalysisEngineDescription aAeDesc, String brokerUrl)
	{
		endpoint = aEndpoint;
		aeDesc = aAeDesc;
		setBrokerUrl(brokerUrl);
	}

	public SimpleService(final String aEndpoint)
	{
		endpoint = aEndpoint;
	}

	public String getEndpointName(){
		return endpoint;
	}

	/**
	 * Initialize the UIMA-AS client.
	 */
	public String start(int numberOfInstances,boolean async)
			throws ResourceInitializationException
	{
		uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

		Map<String, Object> serviceCtx = new HashMap<String, Object>();
		File deploymentDescriptionFile;

		try {
			// Create service descriptor
			AsDeploymentDescription deploymentDescription = new AsDeploymentDescription(
					aeDesc, endpoint, getBrokerUrl(),null);
			deploymentDescription.setNumberOfInstances(numberOfInstances);
			deploymentDescriptionFile = File.createTempFile(getClass().getSimpleName(), ".xml");
			deploymentDescriptionFile.deleteOnExit();
			deploymentDescription.toXML(deploymentDescriptionFile);
			//deploymentDescription.toXML(System.out);

			serviceCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,  ConfigDataholder.getDd2SpringPath());
			serviceCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + ConfigDataholder.getSaxonPath());
			
//			serviceCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,  getClass().getClassLoader().getResource("dd2spring.xsl").getPath());
//			serviceCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + getClass().getClassLoader().getResource("saxon8.jar").getPath());
			
//			serviceCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,  "/home/FB/fb09/ahemati/server_configs/apache-uima-as-2.6.0/bin/dd2spring.xsl");
//			serviceCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + "/home/FB/fb09/ahemati/server_configs/apache-uima-as-2.6.0/saxon/saxon8.jar");

		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}

		try {
			serviceId = uimaAsEngine.deploy(deploymentDescriptionFile.getAbsolutePath(), serviceCtx);
			log.info("UIMA AS service deployed: [" + serviceId + "]");
			
			return serviceId;
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	public void start(String deploymentResourceFile){
		uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();

		Map<String, Object> serviceCtx = new HashMap<String, Object>();

		// Create service descriptor
		serviceCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,  ConfigDataholder.getDd2SpringPath());
		serviceCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + ConfigDataholder.getSaxonPath());
		
//		serviceCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath,  "/home/FB/fb09/ahemati/server_configs/apache-uima-as-2.6.0/bin/dd2spring.xsl");
//		serviceCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + "/home/FB/fb09/ahemati/server_configs/apache-uima-as-2.6.0/saxon/saxon8.jar");

		try {
			serviceId = uimaAsEngine.deploy(deploymentResourceFile, serviceCtx);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public void stop() throws ResourceInitializationException
	{
		try {
			uimaAsEngine.undeploy(serviceId);
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	public String getServiceId()
	{
		return serviceId;
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}
}
