package org.hucompute.textimager.client.rest.ducc;

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

//Wird von ConfigDataholder benutzt, um auf alle Metadaten aus surls_remote.xml zuzugreifen.

/**
 * JAXB Klasse die Java Objekte aus den xml nodes (webservices), der XML-Datei surls_remote.xml erzeugt.
 * @author Wahed und Patrick.
 *
 */
public class ServiceDataholder {
	final static Logger logger = Logger.getLogger(ServiceDataholder.class);
	
	public String packageName;
	
	public ServiceDataholder(){}

	public ServiceDataholder(Element node){
		name = (node.getAttribute("name").trim());

//UPADTED
//		group = 	(node.getAttribute("group").trim());
		
//UPADTED
//		if(node.getElementsByTagName("parallel").getLength()>0)
//			parallel = Boolean.getBoolean(node.getElementsByTagName("parallel").item(0).getTextContent());

		if(node.getElementsByTagName("casMultiplierPoolsize").getLength()>0)
			casMultiplierPoolsize= Integer.parseInt(node.getElementsByTagName("casMultiplierPoolsize").item(0).getTextContent());
		
//UPADTED
//		try{
//			addToPipeline(((Element)node.getElementsByTagName("pipeline").item(0)).getElementsByTagName("item"));
//		}catch(NullPointerException e){
//
//		}
		
//DONE: Einf�gen von Input und Output (Lanaguage ist bereits vorhanden). Pipeline und Parallel raus.	
//DONE: Methode schreiben entsprechend addToLanguages f�r Input und Output

//Methode f�r addToLanguages.
		try{
			addToLanguages(((Element)node.getElementsByTagName("supportedLanguages").item(0)).getElementsByTagName("language"));
			((Element)node.getElementsByTagName("supportedLanguages").item(0)).getElementsByTagName("language").item(0).getTextContent();
		}catch(NullPointerException e){

		}
		
//UPDATED: Methode f�r Input.
		try{
			addInput(((Element)node.getElementsByTagName("input").item(0)).getElementsByTagName("item"));
			((Element)node.getElementsByTagName("input").item(0)).getElementsByTagName("item").item(0).getTextContent();
		}catch(NullPointerException e){

		}
	
//UPDATED: Methode f�r Output.	
		try{
			addOutput(((Element)node.getElementsByTagName("output").item(0)).getElementsByTagName("item"));
			((Element)node.getElementsByTagName("output").item(0)).getElementsByTagName("item").item(0).getTextContent();
		}catch(NullPointerException e){

		}

//Methode f�r restliche Metadaten aus surls_remote.xml.
		try{
			Element remoteInfo = ((Element)node.getElementsByTagName("remoteDeployInfo").item(0));
			brokerURL = (remoteInfo.getElementsByTagName("brokerURL").item(0).getTextContent().trim());
			timeout = (remoteInfo.getElementsByTagName("timeout").item(0).getTextContent().trim());
			getmetatimeout = (remoteInfo.getElementsByTagName("getmetatimeout").item(0).getTextContent().trim());
			cpctimeout = (remoteInfo.getElementsByTagName("cpctimeout").item(0).getTextContent().trim());
		}catch(NullPointerException e){

		}

		packageName = (node.getAttribute("packageName").trim());

	}
	
//RAUSLASSEN (Wird nicht benutzt)
//	private String ElementToString(Element node) throws TransformerFactoryConfigurationError, TransformerException{
//		StringWriter writer = new StringWriter();
//		Transformer transformer = TransformerFactory.newInstance().newTransformer();
//		transformer.transform(new DOMSource(node), new StreamResult(writer));
//		String xml = writer.toString();
//		return xml;
//	}

//UPADTED
//	private void addToPipeline(NodeList nodeList){
//		for (int i = 0; i < nodeList.getLength(); i++){
//			String name = nodeList.item(i).getTextContent();
//			pipeline.add(name);
//		}
//	}
	
	
	//Beispiel Methode addToLanguages f�r Input und Output.
	/**
	 * Erzeugt das Java Objekt Language aus dem XML node Element Language eines webservices.
	 * @param nodeList.
	 */
	private void addToLanguages(NodeList nodeList){
		for (int i = 0; i < nodeList.getLength(); i++){
			String name = nodeList.item(i).getTextContent();
			supportedLanguages.add(name);
		}
	}
	
//UPDATED
	//Neue Methode f�r Input.
	/**
	 * Erzeugt das Java Objekt Input aus dem XML node Element Input eines webservices.
	 * @param nodeList.
	 */
	private void addInput(NodeList nodeList){
		for (int i = 0; i < nodeList.getLength(); i++){
			String name = nodeList.item(i).getTextContent();
			input.add(name);
		}
	}
	
//UPDATED
	//Neue Methode f�r Output.
	/**
	 * Erzeugt das Java Objekt Output aus dem XML node Element Output eines webservices.
	 * @param nodeList.
	 */
	private void addOutput(NodeList nodeList){
		for (int i = 0; i < nodeList.getLength(); i++){
			String name = nodeList.item(i).getTextContent();
			output.add(name);
		}
	}
	
	String name;
	
//UPADTED
//	List<String> pipeline = new ArrayList<String>();

	//Liste f�r Languages.
	List<String> supportedLanguages = new ArrayList<String>();

//UPDATED
	//Liste f�r Output.
	List<String> output = new ArrayList<String>();
	
//UPDATED
	//Liste f�r Input.
	List<String> input = new ArrayList<String>();
	
//UPADTED
//	String group;
	
	int casMultiplierPoolsize = 0;

	public int getCasMultiplierPoolsize(){
		return casMultiplierPoolsize;
	}
	
//UPADTED
//	boolean parallel = true;

//UPADTED
//	public boolean isParallel(){
//		return parallel;
//	}

//UPADTED
//	public String getGroup(){
//		return group;
//	}

	//Get Name.
	/**
	 * Gibt den namen eines webservices aus der XML-Datei surls_remote.xml zur�ck.
	 * @return name = webservicename (String).
	 */
	public String getName() {
		return name;
	}
	
	//Set Name.	
	/**
	 * Setzt den webservicenamen.
	 * @param name = webservicename (String).
	 */
	public void setName(String name) {
		this.name = name;
	}

//UPADTED
//	public List<String> getPipeline(){
//		return pipeline;
//	}

	//Get Language.
	/**
	 * Gibt die supportedLanguages eines webservices aus der XML-Datei surls_remote.xml zur�ck.
	 * @return supportedLanguages = unterst�tze languages des webservices (List<String>).
	 */
	public List<String> getSupportedLanguages(){
		return supportedLanguages;
	}
	
	//Get Output.
	/**
	 * Gibt den/die output/s eines webservices aus der XML-Datei surls_remote.xml zur�ck.
	 * @return output = output/s des webservices (List<String>).
	 */
	public List<String> getOutput(){
		return output;
	}
	
//UPDATED
	//Get Input.
	/**
	 * Gibt den/die input/s eines webservices aus der XML-Datei surls_remote.xml zur�ck.
	 * @return input = input/s des webservices (List<String>).
	 */
	public List<String> getInput(){
		return input;
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
//				append(this.getGroup()).
				toHashCode();
	}

	private String brokerURL;
	private String endpoint;
	private String timeout;
	private String getmetatimeout;
	private String cpctimeout;


	

	@Override
	public String toString(){
//		return getName();
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
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

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}


}