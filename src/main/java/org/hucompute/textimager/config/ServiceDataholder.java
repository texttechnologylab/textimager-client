package org.hucompute.textimager.config;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.GsonBuilder;

public class ServiceDataholder {
	final static Logger logger = Logger.getLogger(ServiceDataholder.class);

	public ServiceDataholder(){}

	public ServiceDataholder(Element node){
		name = (node.getAttribute("name").trim());

		group = 	(node.getAttribute("group").trim());

		if(node.getElementsByTagName("parallel").getLength()>0)
			parallel = Boolean.getBoolean(node.getElementsByTagName("parallel").item(0).getTextContent());

		if(node.getElementsByTagName("casMultiplierPoolsize").getLength()>0)
			casMultiplierPoolsize= Integer.parseInt(node.getElementsByTagName("casMultiplierPoolsize").item(0).getTextContent());
		
		try{
			addToPipeline(((Element)node.getElementsByTagName("pipeline").item(0)).getElementsByTagName("item"));
		}catch(NullPointerException e){

		}

		try{
			addToLanguages(((Element)node.getElementsByTagName("supportedLanguages").item(0)).getElementsByTagName("language"));
			((Element)node.getElementsByTagName("supportedLanguages").item(0)).getElementsByTagName("language").item(0).getTextContent();
		}catch(NullPointerException e){

		}

		try{
			Element remoteInfo = ((Element)node.getElementsByTagName("remoteDeployInfo").item(0));
			brokerURL = (remoteInfo.getElementsByTagName("brokerURL").item(0).getTextContent().trim());
			timeout = (remoteInfo.getElementsByTagName("timeout").item(0).getTextContent().trim());
			getmetatimeout = (remoteInfo.getElementsByTagName("getmetatimeout").item(0).getTextContent().trim());
			cpctimeout = (remoteInfo.getElementsByTagName("cpctimeout").item(0).getTextContent().trim());
		}catch(NullPointerException e){

		}


	}

	private String ElementToString(Element node) throws TransformerFactoryConfigurationError, TransformerException{
		StringWriter writer = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(node), new StreamResult(writer));
		String xml = writer.toString();
		return xml;
	}

	private void addToPipeline(NodeList nodeList){
		for (int i = 0; i < nodeList.getLength(); i++){
			String name = nodeList.item(i).getTextContent();
			pipeline.add(name);
		}
	}

	private void addToLanguages(NodeList nodeList){
		for (int i = 0; i < nodeList.getLength(); i++){
			String name = nodeList.item(i).getTextContent();
			supportedLanguages.add(name);
		}
	}

	String name;

	List<String> pipeline = new ArrayList<String>();

	List<String> supportedLanguages = new ArrayList<String>();

	List<String> output = new ArrayList<String>();

	String group;
	
	int casMultiplierPoolsize = 0;

	public int getCasMultiplierPoolsize(){
		return casMultiplierPoolsize;
	}

	boolean parallel = true;

	public boolean isParallel(){
		return parallel;
	}

	public String getGroup(){
		return group;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPipeline(){
		return pipeline;
	}

	public List<String> getSupportedLanguages(){
		return supportedLanguages;
	}

	public List<String> getOutput(){
		return output;
	}
	

	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other instanceof ServiceDataholder){
			ServiceDataholder otherMyClass = (ServiceDataholder)other;
			return otherMyClass.getName().equals(this.getName());
		}
		else return false;
	}

	@Override
	public int hashCode() {
		return new org.apache.commons.lang.builder.HashCodeBuilder(17, 31).
				append(this.getName()).
				append(this.getGroup()).
				toHashCode();
	}

	private String brokerURL;
	private String endpoint;
	private String timeout;
	private String getmetatimeout;
	private String cpctimeout;


	

	@Override
	public String toString(){
		return getName();
//		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public String getBrokerURL() {
		return brokerURL;
	}

	public void setBrokerURL(String brokerURL) {
		this.brokerURL = brokerURL;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getGetmetatimeout() {
		return getmetatimeout;
	}

	public void setGetmetatimeout(String getmetatimeout) {
		this.getmetatimeout = getmetatimeout;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getCpctimeout() {
		return cpctimeout;
	}

	public void setCpctimeout(String cpctimeout) {
		this.cpctimeout = cpctimeout;
	}


}