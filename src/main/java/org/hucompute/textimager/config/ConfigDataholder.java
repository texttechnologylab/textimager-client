package org.hucompute.textimager.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class ConfigDataholder {
	final static Logger logger = Logger.getLogger(ConfigDataholder.class);
	private static String localFile = "/home/ahemati/workspace/services/services-server/src/main/resources/surls.xml";
	private final static String remoteFile= "http://service.hucompute.org/urls_v2.xml";

	private static Document getPropertiesDoc() throws MalformedURLException, SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc;
		try{
			doc = db.parse(localFile);
		}catch(FileNotFoundException e){
			doc = db.parse(new URL(remoteFile).openStream());
		}
		return doc;
	}

	public ConfigDataholder() {
	}
	
	public ConfigDataholder(String configFile) {
		this.localFile = configFile;
	}

	@XmlElementWrapper( name="webserviceGroups" )
	@XmlElement( name="webserviceGroup" )
	List<ServicegroupDataHolder> webservices = new ArrayList<ServicegroupDataHolder>();


	public List<ServicegroupDataHolder> getWebservices(){
		return webservices;
	}

	public ServiceDataholder getService(String groupName,String classname,String language) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, XPathExpressionException,NullPointerException {
//		try {
			if(classname == null)
				return getDefaultWebserviceByGroup(groupName,language);
			Document doc = getPropertiesDoc();
			doc.getDocumentElement().normalize();
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//webservice[@name='" + classname + "']");

			Element node = (Element)  ((NodeList)expr.evaluate(doc, XPathConstants.NODESET)).item(0);
			ServiceDataholder holder = new ServiceDataholder(node);
			return holder;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
	}
	

	public ArrayList<ServiceDataholder> getWebserviceGroup(String groupName){
		ArrayList<ServiceDataholder> output = new ArrayList<ServiceDataholder>();
		try {
			Document doc = getPropertiesDoc();
			doc.getDocumentElement().normalize();
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//webserviceGroup[@name='" + groupName + "']");
			Element node = (Element)  ((NodeList)expr.evaluate(doc, XPathConstants.NODESET)).item(0);
			NodeList webservices = node.getElementsByTagName("webservice");
			for(int i = 0; i< webservices.getLength(); i++)
				output.add(new ServiceDataholder((Element)webservices.item(i)));
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ServiceDataholder getDefaultWebserviceByGroup(String groupname,String language){
		if(language.equals("unknown"))
			return getWebserviceGroup(groupname).get(0);

		for (ServiceDataholder webserviceHolder : getWebserviceGroup(groupname)) {
			if(webserviceHolder.supportedLanguages.contains(language) || webserviceHolder.supportedLanguages.contains("all"))
				return webserviceHolder;
		}
		
		return null;
	}

	public String getStringFromDocument(Document doc)
	{
		try
		{
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		}
		catch(TransformerException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static String getSaxonPath(){
		File dest = new File(System.getProperty("java.io.tmpdir")+"/saxon8.jar");
		if(!dest.exists()) { 
			URL inputUrl = ConfigDataholder.class.getClassLoader().getResource("saxon8.jar");
			try {
				FileUtils.copyURLToFile(inputUrl, dest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		return dest.getAbsolutePath();
	}
	
	public static String getDd2SpringPath(){
		File dest = new File(System.getProperty("java.io.tmpdir")+"/dd2spring.xsl");
		if(!dest.exists()) { 
			URL inputUrl = ConfigDataholder.class.getClassLoader().getResource("dd2spring.xsl");
			try {
				FileUtils.copyURLToFile(inputUrl, dest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		return dest.getAbsolutePath();
	}
}
