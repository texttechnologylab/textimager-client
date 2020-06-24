package org.hucompute.textimager.client.rest.ducc;//Klasse um Informationen aus surls.xml zu extrahieren mit Aufruf von ServiceDataholder

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
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


/**
 * Liest die JAXB Klasse ServiceDataholder.java auf Basis von uebergabewerten aus der Klasse Pipeline.java aus
 * und stellt die gesuchten webservices auf Basis einer Logik ueber die xml elemente "language", "input" und "output" zur Verfuegung.
 * @author Wahed und Patrick.
 *
 */
public class ConfigDataholder {
	final static Logger logger = Logger.getLogger(ConfigDataholder.class);
	private static String localFile = "ll";
//	private static String localFile = "/home/ducc/workspace/DuccRest/src/main/resources/surls_remote.xml";
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

	//UPDATED	
	//	@XmlElementWrapper( name="webserviceGroups" )
	//	@XmlElement( name="webserviceGroup" )
	//	List<ServicegroupDataHolder> webservices = new ArrayList<ServicegroupDataHolder>();
	@XmlElementWrapper( name="webservices" )
	@XmlElement( name="webservice" )

	//Aufruf von ServiceDataholder, um auf alle Metadaten der webservices in surls_remote.xml zuzugreifen.
	List<ServiceDataholder> webservices = new ArrayList<ServiceDataholder>();


	//UPDATED	
	//	public List<ServicegroupDataHolder> getWebservices(){
	//		return webservices;
	//	}
	/**     
	 * Holt mit Hilfe der JAXB Klasse ServiceDataholder.java den gesuchten webservice aus der surls_remote.xml auf Basis der Logik ber die xml elemente "language", "input" und "output".
	 * @return webservices = gesuchter webservice (List<ServiceDataholder>).
	 */
	public List<ServiceDataholder> getWebservices(){
		return webservices;
	}

	/*DONE: Input von Methode abndern: classname (entspricht dem Attribut "name" des node/element "webservice") beibehalten,
	  groupName (entspricht dem Attribut "name" des node/element "webserviceGroup") entfernt, Input (child node/element) und Output (child node/element) eingefgt.*/

	//UPDATED
	//	public ServiceDataholder getService(String groupName,String classname,String language) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, XPathExpressionException,NullPointerException {
	/**
	 * Sucht anhand des bergebenen webservices den webservice und gibt ihn zurck,
	 * oder sucht anhand der Sprache und dem Output den webservice und gibt ihn zurck.
	 * Hierzu ruft er zur weiteren Verarbeitung die methode "getDefaultWebserviceByLanguageandOutput" auf.
	 * @param classname = webservice, der gesucht und zrckgegeben wird (String).
	 * @param language = supporttedLangauge, welche vom gesuchten webservice untersttzt werden muss (String).
	 * @param output = bentigter output des gesuchten webservice,der als Input von einem webservice bentigt wird (bergabe des bentigten inputs des webservices) (List<String>).
	 * @return 
	 * @throws MalformedURLException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 * @throws NullPointerException
	 */
	public ServiceDataholder getService(String classname,String language, List<String> output) throws MalformedURLException, SAXException, IOException, ParserConfigurationException, XPathExpressionException,NullPointerException {

		//RAUSLASSEN
		//		try {

		if(classname == null){

			//UPDATED
			//				return getDefaultWebserviceByGroup(groupName,language);

			System.out.println("Es wird der webservice gesucht, welcher den Output " +output +" generiert, welcher als Input des webservices benoetigt wird.");
			return getDefaultWebserviceByLanguageandOutput(language, output);
		}

		Document doc = getPropertiesDoc();
		doc.getDocumentElement().normalize();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//webservice[@name='" + classname + "']");

		Element node = (Element)  ((NodeList)expr.evaluate(doc, XPathConstants.NODESET)).item(0);
		ServiceDataholder holder = new ServiceDataholder(node);
		return holder;

		//RAUSLASSEN			
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		return null;

	}

	//DONE: Logik auf Basis der Methode "getWebserviceGroup" entfernt und Logik fr Input und Output mit der Methode "getWebservice" eingefgt.

	//UPDATED
	//	public ArrayList<ServiceDataholder> getWebserviceGroup(String groupName){
	//		ArrayList<ServiceDataholder> output = new ArrayList<ServiceDataholder>();
	//		try {
	//			Document doc = getPropertiesDoc();
	//			doc.getDocumentElement().normalize();
	//			XPathFactory xPathfactory = XPathFactory.newInstance();
	//			XPath xpath = xPathfactory.newXPath();
	//			XPathExpression expr = xpath.compile("//webserviceGroup[@name='" + groupName + "']");
	//			Element node = (Element)  ((NodeList)expr.evaluate(doc, XPathConstants.NODESET)).item(0);
	//			NodeList webservices = node.getElementsByTagName("webservice");
	//			for(int i = 0; i< webservices.getLength(); i++)
	//				output.add(new ServiceDataholder((Element)webservices.item(i)));
	//			return output;
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}


	/**
	 * Sucht fr die bergebene Liste "output" den entsprechenden webservice,
	 * welcher als xml items in dem node element "output" alle bergebenen Elemente aus der "output" Liste enthlt.
	 * @param output = gesuchte bergebene Liste "output" (List<String>).
	 * @return Output = webservice, der alle bergebenen Elemente aus der "output" Liste enthlt im xml node element "output" enthlt (ArrayList<ServiceDataholder>).
	 * (die "output" Liste entspricht den bentigten inputs eines webservices)
	 */
	public ArrayList<ServiceDataholder> getWebservice (List<String> output){
		HashSet<String>outputSet = new HashSet<>(output);
		ArrayList<ServiceDataholder> Output = new ArrayList<ServiceDataholder>();

		try {
			Document doc = getPropertiesDoc();
			doc.getDocumentElement().normalize();
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("//webservices/webservice");
			NodeList tmp = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);

			//Durchlaufe alle webservices
			for(int i = 0; i< tmp.getLength();i++){
				System.out.println("output laenge = ");
				NodeList outputTag = ((Element)  tmp.item(i)).getElementsByTagName("output");
				HashSet<String>tmpOutput = new HashSet<>();
				//Wenn ein webservice das xml node element "output" besitzt, dann
				if(outputTag.getLength() > 0){
					NodeList outputItems =  ((Element)outputTag.item(0)).getElementsByTagName("item");
					System.out.println(outputItems.getLength());
					//Durchlaufe alle items des xml node elements "output" und schreibe sie in das HashSet "tmpOutput"
					for(int j = 0;j<outputItems.getLength();j++){
						tmpOutput.add(outputItems.item(j).getTextContent());
						System.out.println("output inhalt = ");
						System.out.println(outputItems.item(j).getTextContent());
					}	
				}
				if (outputSet.containsAll(tmpOutput)){
					System.out.println("Outputs stimmen mit benoetigtem Inputs des webservices ueberein = " +outputSet.containsAll(tmpOutput));

					Output.add(new ServiceDataholder((Element)tmp.item(i)));
					return Output;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void printWebservices() throws MalformedURLException, SAXException, IOException, ParserConfigurationException, XPathExpressionException{
		Document doc = getPropertiesDoc();
		doc.getDocumentElement().normalize();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//webservices/webservice");
		NodeList tmp = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);

		//Durchlaufe alle webservices
		for(int i = 0; i< tmp.getLength();i++){
			System.out.println(new ServiceDataholder((Element)tmp.item(i))+",");
		}
	}
	//DONE: Logik auf Basis der Methode "getDefaultWebserviceByGroup" entfernt und Logik fr Input und Output mit der Methode "getDefaultWebserviceByLanguageandInputandOutput" eingefgt	

	//UPDATED	
	//	public ServiceDataholder getDefaultWebserviceByGroup(String groupname,String language){
	//		if(language.equals("unknown"))
	//			return getWebserviceGroup(groupname).get(0);
	//
	//		for (ServiceDataholder webserviceHolder : getWebserviceGroup(groupname)) {
	//			if(webserviceHolder.supportedLanguages.contains(language) || webserviceHolder.supportedLanguages.contains("all"))
	//				return webserviceHolder;
	//		}
	//		
	//		return null;
	//	}

	/**
	 * Prft, ob die Sprache (language) "unkown" entspricht, falls ja wir ein Mal die Methode getWebservice
	 * mit dem gesuchten output zur weiteren Verarbeitung aufgerufen,um einen passenden webservice zu finden.
	 * Ansonsten wird fr alle webservices mit dem gesuchten output geprft, ob die gesuchte Sprache (language)
	 * oder "all" als item im xml node element "supportedLanguages" enthalten ist, um einen passenden webservice zu finden.
	 * @param language = xml node element "supportedLanguage" des gesuchten webservices (String).
	 * @param output = xml node element "output" des gesuchten webservices (List<String>).
	 * @return webserviceHolder = gesuchter webservice (ServiceDataholder).
	 */
	public ServiceDataholder getDefaultWebserviceByLanguageandOutput (String language, List<String> output){
		if(language.equals("unknown"))
			return getWebservice(output).get(0);

		for (ServiceDataholder webserviceHolder : getWebservice(output)) {
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