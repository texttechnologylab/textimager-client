package org.hucompute.textimager.client.rest.ducc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.xml.sax.SAXException;


/**
 * Erzeugt die Pipeline f�r die Orchestrierung der einzelnen webservices f�r das Prepreprocessing des Textimagers.
 * Dabei werden alle webservices, welche den selben Input ben�tigen aber unterscheidliche Outputs generieren parallel ausgef�hrt.
 * @author Wahed und Patrick.
 *
 */
public class Pipeline {
	final static Logger logger = Logger.getLogger(Pipeline.class);

	final String BABELTEXT = "BabelText";
	boolean containsBabelText = false;

	ConfigDataholder configDataholder;
	public Pipeline() throws JAXBException, IOException{
		configDataholder = new ConfigDataholder();
		containsBabelText = false;
	}


	
	/**
	 * Ermittelt die benoetigte Pipeline, wenn etwas parallel laufen kann, wird es ermittelt.
	 * @param options = Ausgew�hlte webservices des Benutzers mit key=language (HashMap).
	 * @param cas = unbearbeitete Texteingabe (JCas).
	 * @param configFile = Configuration (String).
	 * @return pipelineForLanguages = Komplette Pipeline f�r jede Sprache mit h�chst m�glichem Parallelisierungsgrad (HashMap).
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws NullPointerException 
	 * @throws XPathExpressionException 
	 * @throws MalformedURLException 
	 */	
	
/*Die Methode "constructPipeline": Input die Datenstruktur HashMap, welche aus den Datentypen String ("language" als Key) und geschachtelter Arraylist ("webservices" als Values) besteht.
								   Output die Variablen "options,JCas cas, String configFile" der bearbeiteten Strings (Jcas cas) durch die verschiedenen webservices (options).*/
	public HashMap<String, ArrayList<ArrayList<ServiceDataholder>>> constructPipeline(HashMap<String, String>options,JCas cas, String configFile) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
		if(configFile !=null)
			
			/*Aufruf der Klasse ConfigDataholder.java dort werden alle Metadaten aus der surls_remote.xml mit Hilfe von ServiceDataholder.java ausgelesen und
			 * in die Java Klasse ConfigDataholder.java importiert. Dabei werden die webservices auf Basis der Logik zu den XML elementen "language", "input" und "Output" ausgew�hlt.*/
			configDataholder = new ConfigDataholder(configFile);
		
//UPDATED:		
		Collection<String> pickedWebservices = options.values();
		System.out.println("Ausgewaehlte Webservices des Nutzers = " +pickedWebservices);
		
		//In der Variable "uniquePipeLine" stehen alle options (webservices) der pipeline ungeordnet.
		HashMap<String, ArrayList<ServiceDataholder>> uniquePipeLine =  HashMapgetPipeline(options);
		System.out.println("Die ungeordnete Pipeline = " +uniquePipeLine);
		
//DONE: Die Methode "getPipeline" auf Basis der Logik zu Input und Output umgeschrieben.
		
		/*Die Variable "pipelineForLanguages" enth�lt die pipelines f�r je eine Sprache. Dabei dient ServiceDataholder.java als JAXB f�r die Metainformationen �ber die webservices
		  aus der XML Datei surls_remote.xml. Die Variable "pipelineForLanguages" wird als HashMap, welche als Key "language" hat, welche in der Main Klasse ClientTest.java definiert wird.*/
		HashMap<String, ArrayList<ArrayList<ServiceDataholder>>>pipelineForLanguages = new HashMap<>();
		
//VON HIER NEU SCHREIBEN!!!
		
		/*Die Java-Methode Iterator liest die HashMap aus und speichert in die Variable "iter" den Inhalt der uniquePipeLine (ungeordnet) als Mapeintrag Objekt,
		  d.h. ein Objekt aus der HashMap mit Key (language) + dazugeh�rige Values (webservices) - ".entrySet".*/
		Iterator<Map.Entry<String, ArrayList<ServiceDataholder>>> iter = uniquePipeLine.entrySet().iterator();
		
		//Ungeordnete Pipeline wird geordnet, sodass bspw. ein tokenizer nicht nach einem pos steht, sondern davor.
		//Durchlaufe alle Mapeintr�ge (".entrySet") mit Hilfe der Variable "iter" komplett.
		
		while (iter.hasNext()) {	

			// Wir befinden uns in einer Sprache

			//Die Variable "mapEintrag" ist eine Hilfsvariable zur Zwischenspeicherung des aktuellen entrySets der HashMap, d.h. Key (language) + dazugeh�rige Values (webservices).
			Map.Entry<String, ArrayList<ServiceDataholder>> mapEintrag = iter.next();
			
//UPDATED
//		    ServiceDataholder merger = configDataholder.getService(null, "LanguageMerger", null);
			
			//Der webservice "LanguageMerger" wird am Ende angef�gt , da dieser zum Schluss ausgef�hrt wird - "LanguageMerger" ist immer der webservice der zuletzt ausgef�hrt wird!
			ServiceDataholder merger = configDataholder.getService("LanguageMerger", null, null);
																	
			//Die Variable "processed" enth�lt alle bereits bearbeiteten webservices f�r eine Sprache.
			ArrayList<ServiceDataholder>processed = new ArrayList<ServiceDataholder>();

//UPDATED
//			ArrayList<String>groupProcessed = new ArrayList<>();

			//Die Variable "OutputProcessed" enth�lt alle Outputs, die schon zur Verf�gung stehen, welche als Inputs f�r einen webservice ben�tigt werden.
			HashSet<String>OutputProcessed = new HashSet<>();
			
			//Die Variable "pipeLine" enth�lt alle webservices die verwendet werden, d.h. fertige Pipeline.
			ArrayList<ArrayList<ServiceDataholder>>pipeLine = new ArrayList<ArrayList<ServiceDataholder>>();
			
			//Die Variable "iterations" enth�lt die Anzahl der values (webservices) der Variable "mapEintrag" des jeweiligen keys (language).
			int iterations = mapEintrag.getValue().size();

//RAUSLASSEN (Wahed)
			//						if(next.getValue().contains(merger)){
			//							merger = next.getValue().get(next.getValue().indexOf(merger));
			//							iterations--;
			//							next.getValue().remove(merger);
			//			//				containsMerger = true;
			//						}

			/*Falls die Anzahl der bereits bearbeiteten webservices ("procceded") kleiner ist als alle webservices des Mapeintrags ("iterations")
			  l�uft die Schleife weiter. Dabei die Variable "processed" bereits geordnet).*/
			while (processed.size()<iterations) {

				//Initialisierung der Variable "nonconflicting", um parallele Konfilkte zu l�sen.
				ArrayList<ArrayList<ServiceDataholder>>nonconflicting = new ArrayList<ArrayList<ServiceDataholder>>();

				//Initialisierung der Variable "thisStepProcess" f�r die Ermittlung der parallel lauff�higen webservices.
				ArrayList<ServiceDataholder>thisStepProcess = new ArrayList<ServiceDataholder>();
				
//UPDATED
//				ArrayList<String>thisStepGroupProcess = new ArrayList<String>();
				
				//Initialisierung der Variable "thisStepOutputProcess" f�r die Ermittlung der parallel lauff�higen webservices.
				HashSet<String>thisStepOutputProcess = new HashSet<>();
				
				//Potentiell parallel lauff�hige webservices werden ermittelt.	
				//Lese Wert (webservice) aus der HashMap "mapEintrag" und schreibe Wert (webservice) in die Variable "element".
				for (ServiceDataholder element : mapEintrag.getValue()) {

//RAUSLASSEN (Wahed)
//					System.out.println(cas);
//					if(alreadyTaggedWith(element, cas) && !processed.contains(element)){
//						thisStepProcess.add(element);
//						thisStepGroupProcess.add(element.getGroup());
//						continue;
//					}
//					else

//UPDATED					
//						if(!processed.contains(element)){	
						
						//Pr�ft, ob das element (webservice) noch nicht in der Arrayliste "processed" vorhanden ist, wenn nicht vorhanden, dann...
						if(!processed.contains(element)){

//DONE: Check des Inhalts des XML elements "pipeline", auf XML elemente "input" und "output" umgebogen.	
							
							//Wenn die Sprache nicht "unkown" ist, dann f�ge Output "language" bereits in das HashSet "OutputProcessed" ein.
							if(!mapEintrag.getKey().contains("unknown"))
								OutputProcessed.add("language");
							
//UPDATED
//							if(groupProcessed.containsAll(element.getPipeline()) || element.getPipeline().size() == 0){
							
							//Pr�ft, ob der Input des element (webservice) bereits in der OutputProcessed enthalten ist oder das element (webservice) keinen Input ben�tigt, wenn ja dann...
							if((OutputProcessed.containsAll(element.getInput())) || (element.getInput().size() == 0)){
								
								//F�ge element (webservice) zur Liste "thisStepProcess" hinzu.
								thisStepProcess.add(element);								
//UPDATED
//								thisStepGroupProcess.add(element.getGroup());
								
								//F�ge den Output des webservices dem HashSet "thisStepOutputProcess" hinzu.
								thisStepOutputProcess.addAll(element.getOutput());
								
//DONE: Falls Konflikt entsteht wird die parallel Information aus dem xml file gezogen - Umgebaut auf Input- und Output-Logik.
//DONE: addToNonConflicting von XML element "parallel" auf die Logik von den XML elementen "input" und "output" umgebogen.

//UPDATED								
//								addToNonConflicting(nonconflicting, element);
								
								//Aufruf der Methode "addToNonConflicting" zur Pr�fung der parallelen Lauff�higkeit der elemente (webservices).
								addToNonConflicting(nonconflicting, element, mapEintrag);
							}
						}
					}
				
//UPDATED
//				groupProcessed.addAll(thisStepGroupProcess);
				
				//Hinzuf�gen der Inputs aus dem HashSet "thisStepInputProcess" in das HashSet "InputProcessed".
				OutputProcessed.addAll(thisStepOutputProcess);
				//Hinzuf�gen der webservices aus der Liste "thisStepProcess" in die Liste "nonconflicting".
				processed.addAll(thisStepProcess);
				//Hinzuf�gen der parallel lauff�higen webservices in die variable "pipeLine".
				pipeLine.add(thisStepProcess);
			}
			
//VON HIER: Aufruf des LanguageMerger fest am Ende kodiert, da dieser webservice immer zum Schluss kommt. Und falls der LangauaeMerger schon irgendwo stand, wird dieser an der Stelle entfernt.
			ArrayList<ServiceDataholder>mergerList = new ArrayList<>();
			mergerList.add(merger);

			pipeLine.remove(mergerList);
			
			for(int i = pipeLine.size()-1;i > 0;i--){
//UPDATED
//				if(pipeLine.get(i).get(0).getPipeline().contains("languageSplitter")){
				
				if(pipeLine.get(i).get(0).getName().contains("LanguageSplitter")){
					pipeLine.add(i+1,mergerList);
					break;
				}
			}
//BIS HIER.
			
			//Falls der Nutzer nur eine Sprache benutzt hat, wird der LanguageSplitter und der LanguageMeger entfernt.
			if(options.size() == 1 && !options.containsValue("HucomputeLanguageDetectionPercentage"))
				removeSplitter(pipeLine);
			
//RAUSLASSEN (Wahed)	
			//			if(containsMerger){
			//				ArrayList<ServiceDataholder>mergerList = new ArrayList<>();
			//				mergerList.add(merger);
			//				pipeLine.add(mergerList);
			//			}
			//			if(containsBabelText)
		
			//Holt den Key, d.h die Sprache und die dazugeh�rige sortierte Pipeline und speichert diese in "pipelineForLanguages".
			pipelineForLanguages.put(mapEintrag.getKey(), pipeLine);
		}
		
//BIS HIER NEU SCHREIBEN!!!
		
		System.out.println("Die geordnete Pipeline fuer je eine Sprache = " +pipelineForLanguages);
		return pipelineForLanguages;
		
//RAUSLASSEN (Wahed)
		//		//die bearbeiteten services.
		//		ArrayList<ServiceDataholder>processed = new ArrayList<ServiceDataholder>();
		//
		//		//gruppen die schon bearbeitet wurden
		//		ArrayList<String>groupProcessed = new ArrayList<>();
		//
		//		//ausgabe
		//		ArrayList<ArrayList<ServiceDataholder>>pipeLine = new ArrayList<ArrayList<ServiceDataholder>>();
		//
		//		//falls die Anzahl der bearbeiteten kleiner ist als alle services in der pipeline.
		//		while (processed.size()<uniquePipeLine.size()) {
		//			ArrayList<ArrayList<ServiceDataholder>>nonconflicting = new ArrayList<ArrayList<ServiceDataholder>>();
		//			ArrayList<ServiceDataholder>thisStepProcess = new ArrayList<ServiceDataholder>();
		//			ArrayList<String>thisStepGroupProcess = new ArrayList<String>();
		//
		//			//potentiell parallel laufende werden ermittelt
		//			for (ServiceDataholder element : uniquePipeLine) {
		//				if(!processed.contains(element)){
		//					if(groupProcessed.containsAll(element.getPipeline()) || element.getPipeline().size() == 0){
		//						thisStepProcess.add(element);
		//						thisStepGroupProcess.add(element.getGroup());
		//						addToNonConflicting(nonconflicting, element);
		//					}
		//				}
		//			}	
		//
		//
		//			groupProcessed.addAll(thisStepGroupProcess);
		//			processed.addAll(thisStepProcess);
		//			pipeLine.addAll(nonconflicting);
		//
		////			//ausgabe wird generiert
		////			for (ArrayList<ServiceDataholder> array : nonconflicting) {
		////				String tmp = "";
		////				for (ServiceDataholder serviceDataholder : array) {
		////					tmp+=serviceDataholder.getName()+",";
		////				}
		////				pipeLine.add(tmp.substring(0, tmp.length()-1));
		////			}
		////			System.out.println(pipeLine);
		//		}
		//		return pipeLine;
	}
	
//UPDATED: WIRD NICHT MEHR BEN�TIGT	
//	private boolean alreadyTaggedWith(ServiceDataholder element, JCas cas){
//
//		if(cas.getAnnotationIndex(Token.type).size()>0 && element.getGroup().equals("tokenizer")){		
//			return true;
//		}
//		
//		if(cas.getDocumentLanguage() !=null &&!cas.getDocumentLanguage().equals("x-unspecified") && element.getGroup().equals("languageDetection")){	
//			return true;
//		}
//
//		if(cas.getAnnotationIndex(POS.type).size()>0 && element.getGroup().equals("pos")){	
//			return true;
//		}
//	
//		if(cas.getAnnotationIndex(Lemma.type).size()>0 && element.getGroup().equals("lemmatizer")){	
//			return true;
//		}
//
//		return false;
//	}

	/**
	 * Entfernt die languageMerger, languageSplitter sowie die advancedLanguageDetection, falls diese nicht ben�tigt werden, z.B., wenn nur eine Sprache ausgew�hlt wurde.
	 * @param pipeLine = fertige Pipeline wird �bergeben (ArrayList<ArrayList<ServiceDataholder>>)
	 * @throws MalformedURLException
	 * @throws XPathExpressionException
	 * @throws NullPointerException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private void removeSplitter(ArrayList<ArrayList<ServiceDataholder>>pipeLine) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{

//UPDATED
//		ServiceDataholder merger = configDataholder.getService(null, "LanguageMerger", null);
		
		ServiceDataholder merger = configDataholder.getService("LanguageMerger", null, null);

//UPDATED
//		ServiceDataholder splitter = configDataholder.getService(null, "LanguageSplitter", null);
		
		ServiceDataholder splitter = configDataholder.getService("LanguageSplitter", null, null);
		
//UPDATED
//		ServiceDataholder advancedLangdetet = configDataholder.getService(null, "HucomputeLanguageDetectionPercentage", null);
		
		ServiceDataholder advancedLangdetet = configDataholder.getService("HucomputeLanguageDetectionPercentage", null, null);

		ArrayList<ServiceDataholder>mergerList = new ArrayList<>();
		mergerList.add(merger);
		ArrayList<ServiceDataholder>splitterList = new ArrayList<>();
		splitterList.add(splitter);
		ArrayList<ServiceDataholder>advancedLangdetetList = new ArrayList<>();
		advancedLangdetetList.add(advancedLangdetet);

		pipeLine.remove(mergerList);
		pipeLine.remove(splitterList);
		pipeLine.remove(advancedLangdetetList);
	}

/**
 * Parallel laufende Services duerfen nicht die selben annotationen bearbeiten. Hier wird determiniert,
 * ob parallel laufende services auch wirklich parallel laufen koennen.
 * @param nonconflicting = webservices, die parallel laufen koennen (ArrayList<ArrayList<ServiceDataholder>>).
 * @param newWebservice = neuer webservice, der auf paralle Lauff�higkeit gegen alle anderen webservices in der "uniquePipeLine" gepr�ft wird (ServiceDataholder).
 * @param mapEintrag = der aktuelle MapEintrga der Sprache (Map.Entry<String, ArrayList<ServiceDataholder>>).
 */

//DONE: Die Logik der Methode "addToNonConflicting" von "isParallel" zur Logik basierend auf "getInput" und "getOutput" aus ServiceDataholder umgebogen.	
	
//UPDATED
//	private void addToNonConflicting(ArrayList<ArrayList<ServiceDataholder>>nonconflicting, ServiceDataholder newServiceDataholder){
//		if(newServiceDataholder.isParallel())
//			for (ArrayList<ServiceDataholder> arrayList : nonconflicting) {
//				boolean cont = false;
//				if(!arrayList.get(0).isParallel())
//					continue;
//				ArrayList<String>outputs = new ArrayList<String>();
//				for (ServiceDataholder serviceDataholder : arrayList) {
//					outputs.addAll(serviceDataholder.getOutput());
//					if(serviceDataholder.getPipeline().contains("languageSplitter") != newServiceDataholder.getPipeline().contains("languageSplitter"))
//						cont=true;
//				}
//				if(cont)
//					continue;
//				outputs.retainAll(newServiceDataholder.getOutput());
//				if(outputs.size()==0){
//					arrayList.add(newServiceDataholder);
//					return;
//				}
//			}
//		ArrayList<ServiceDataholder> tmp = new ArrayList<ServiceDataholder>();
//		tmp.add(newServiceDataholder);
//		nonconflicting.add(tmp);
//	}

	/*Die Methode "addToNonConflicting" pr�ft, welche webservices auf Basis der XML elemente "input" und "output" der surls_remote.xml parallel lauff�hig sind.
	  Dabei ist ausschlaggebend, ob "input" von currentWebservice == "input" von newWebservice UND, ob "output" von currentWebservice != "output" von newWebservice ist.
	  Wenn dies wahr ist, dann sind die webservices currentWebservice und newWebservice parallel lauff�hig.*/
	private void addToNonConflicting(ArrayList<ArrayList<ServiceDataholder>>nonconflicting, ServiceDataholder newWebservice, Map.Entry<String, ArrayList<ServiceDataholder>>mapEintrag){
				
				//Hier werden die Values (webservices) des Mapeintrags des aktuellen Keys (language) durchlaufen und je Durchlauf wir ein Value in currentWebservice gespeichert.
				for (ServiceDataholder currentWebservice : mapEintrag.getValue()) {
					
					ArrayList<String>inputs_new = new ArrayList<String>();
					ArrayList<String>inputs_current = new ArrayList<String>();
					ArrayList<String>outputs_new = new ArrayList<String>();
					ArrayList<String>outputs_current = new ArrayList<String>();
					
					inputs_new.addAll(newWebservice.getInput());
					inputs_current.addAll(currentWebservice.getInput());
					outputs_new.addAll(newWebservice.getOutput());
					outputs_current.addAll(currentWebservice.getOutput());
					
					//Vergleich der Inputs und Outputs von currentWebservice und newWebservice.
					//Wenn Inputs gleich und Outputs ungleich von newWebservice und currentWebservice dann ist der newWebservice mit dem parallel lauff�hig.
					if((!(outputs_new.retainAll(outputs_current)))){
						
						//F�ge newServiceDataholder mit Hilfe der Variable tmp der Variable "nonconflicting" hinzu, wenn er noch nicht in der Variable "nonconflicting" vorhanden ist
						if(!nonconflicting.contains(newWebservice)){
						ArrayList<ServiceDataholder> tmp = new ArrayList<ServiceDataholder>();
						tmp.add(newWebservice);
						nonconflicting.add(tmp);
						}
					}
				}
	}
	
	/**
	 * Bestimmt alle NLP-Verfahren die im Laufe des Preprocesses ben�tigt werden. Komplementiert somit die Auswahl der webservices des Benutzers mit noch fehlenden webservices.
	 * @param uniquePipeline = Die NLP-Verfahren die ben�tigt werden (ArrayList<ServiceDataholder>).
	 * @param pipeline = Die Pipeline des NLP-Verfahrens, die die Preprocess-Funktion aufruft und vom Benutzer ausgew�hlt wurden (ArrayList<ServiceDataholder>).
	 * @return	uniquePipeline = Komplettierte pipeline mit allen ben�tigten webservies (ArrayList<ServiceDataholder>).
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws NullPointerException 
	 * @throws XPathExpressionException 
	 * @throws MalformedURLException 
	 */
	
//AB HIER: AUF INPUT UND OUTPUT UMBIEGEN!!!

//DONE: Logik auf der Basis von XML elment "pipeline" auf  die Logik der XML elemente "input" und "output" umgebogen.	

//UPDATED
//	private ArrayList<ServiceDataholder> getUniqueNLPinPipeline(ArrayList<ServiceDataholder> uniquePipeline, ArrayList<ServiceDataholder> pipeline,HashMap<String, String> options,String language) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
//		
//		
//		//rekursiver Aufruf. Geht alle NLPs durch und f�gt sie zur Liste uniquePipeline hinzu, falls diese noch nicht vorhanden waren, d.h. von Benutzer ausgew�hlt wurden.
//		
//		//Durchlaufe alle Stellen der Arrayliste "pipeline" und schreibe aktuellen Wert bei jedem Durchlauf in die Variable "abstractNLP". 
//		for (ServiceDataholder abstractNLP : pipeline) {
//			//Wenn die Variable uniquePipeline den webservice aus der Variable abstractNLP nicht enth�lt, dann f�ge diesen zu uniquePipeline hinzu.
//			if(!uniquePipeline.contains(abstractNLP))
//				uniquePipeline.add(abstractNLP);
//		
//RAUSLASSEN (Wahed)
//			if(abstractNLP !=null)	
//			
//				getUniqueNLPinPipeline(uniquePipeline,getPipeline(abstractNLP.getPipeline(),options,language),options,language);
//		}
//		return uniquePipeline;
//	}
	
private ArrayList<ServiceDataholder> getUniqueNLPinPipeline(ArrayList<ServiceDataholder> uniquePipeline, ArrayList<ServiceDataholder> pipeline,HashMap<String, String> options,String language) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
	
	HashSet<String>OutputsAvaiable = new HashSet<>();
	
		//Durchlaufe alle Stellen der Arrayliste "pipeline" (alle gew�hlten webservices) und schreibe aktuellen Wert bei jedem Durchlauf in die Variable "abstractNLP". 
		//Schreibe alle Outputs der gew�hlten webservices des Benutzers in das HashSet "OutputsAvaiable".
		for(ServiceDataholder abstractNLP : pipeline){
			OutputsAvaiable.addAll(abstractNLP.getOutput());
		}
		
		//Wenn die Sprache nicht "unkown" ist, dann f�ge Output "language" bereits in das HashSet "OutputsAvaiable" ein.
		if(!language.contains("unknown"))
			OutputsAvaiable.add("language");
		
		//Schreibt alle gew�hlten webservices des Benutzers in die Liste uniquePipeline, falls diese noch nicht vorhanden waren.
		//Durchlaufe alle Stellen der Arrayliste "pipeline" (alle gew�hlten webservices) und schreibe aktuellen Wert bei jedem Durchlauf in die Variable "abstractNLP". 
		for (ServiceDataholder abstractNLP : pipeline) {
			//Wenn die Variable uniquePipeline den webservices aus der Variable abstractNLP nicht enth�lt, dann f�ge diesen zu uniquePipeline hinzu.
			if(!uniquePipeline.contains(abstractNLP))
				uniquePipeline.add(abstractNLP);
			
			/*Wenn das HashSet "OutputsAvaiable" den Input des webservices aus der Variable abstractNLP nicht enth�lt,
			  dann suche nach einem webservice mit dem Output, welcher dem Input des webservices entspricht und f�ge diesen webservice der Variable uniquePipeline hinzu.
			  Des Weiteren f�ge anschliesend den Output dieses webservices dem HashSet "OutputsAvaiable" hinzu.*
			  */
			if(!OutputsAvaiable.containsAll(abstractNLP.getInput())){
				uniquePipeline.add(configDataholder.getService(null, language, abstractNLP.getInput()));
				OutputsAvaiable.addAll((configDataholder.getService(null, language, abstractNLP.getInput())).getOutput());
			}
			
//RAUSLASSEN (Wahed)
//			if(abstractNLP !=null)
		}
		
		return uniquePipeline;
	}

//BIS HIER.
	
//UPDATED: WIRD NICHT MEHR BEN�TIGT.	
//	public ArrayList<ServiceDataholder> getPipeline(List<String> pipelineStrings,HashMap<String, String> options,String language) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
//		ArrayList<ServiceDataholder> pipeline = new ArrayList<ServiceDataholder>();
//
//		for (String string : pipelineStrings) {	
//			boolean added = false;
//			
//			for (String pipelineService : options.get(language).split(",")) {
//				if(!added && configDataholder.getService(null, pipelineService, null).getGroup().equals(string)){	
//				pipeline.add(configDataholder.getService(null, pipelineService, null));	
//					pipeline.add(configDataholder.getService(pipelineService, null, null));
//					
//
//				added = true;
//				}
//			}
//			
//
//		if(!added)
//				pipeline.add(configDataholder.getService(string, null,language));
//			
//	}
//		return pipeline;
//	}
	
	/**
	 * Die Methode "HashMapgetPipeline(options)" holt sich alle aktivierten webservices aus ClientTest.java.
	 * Options sind die ausgew�hlten webservices des Benutzers in der ClientTest.java, die �bergabe erfolgt als HashMap mit Key = langauage und Values = webservices.
	 * Des Weiteren werden alle webservices die als Zulieferer ben�tigt werden durch Aufruf der Methode "getUniqueNLPinPipeline auf Basis der Input- und Output-Logik gesucht
	 * und anschlie�end in die Variable "uniquePipeLine", welche als HashMap erzeugt wird, eingef�gt.
	 * @param options = ausgew�hlte webservices des Benutzers (HashMap)
	 * @return alle webservices, welche in der "uniquePipeline" ben�tigt werden (HashMap).
	 * @throws MalformedURLException
	 * @throws XPathExpressionException
	 * @throws NullPointerException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public HashMap<String,ArrayList<ServiceDataholder>> HashMapgetPipeline(HashMap<String, String> options) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
		
		HashMap<String,ArrayList<ServiceDataholder>> output = new HashMap<>();
		
//DONE: Abge�ndert von "webserviceGroup" und "pipeline" XML elemente auf "input" und "output" xml elemente in surls_remote.xml.
//DONE: Demenstprechend �nderungen in ConfigDataholder und ServiceDataholder durchgef�hrt.

//RAUSLASSEN (Wahed)
//		ArrayList<ServiceDataholder> pipeline = new ArrayList<ServiceDataholder>();
		
		//Durchl�uft die HashMap und nutz die Variable iter als Zwischenspeicher f�r jeden Mapeintrag zu einem Key (language).
		Iterator<Map.Entry<String, String>> iter = options.entrySet().iterator();
		
		//Hier werden die vordefinierten webservices aus den Optionen der Pipeline hinzugefuegt.
		//Durchlaufe alle Mapeintr�ge der Hashmap.
		while (iter.hasNext()) {
			Map.Entry<String, String> next = iter.next();

//RAUSLASSEN (Wahed)
//			output.put(next.getKey(), new ArrayList<>());
			
			//Hilfsvariable tmp.
			ArrayList<ServiceDataholder> tmp = new ArrayList<>();
			
			//Splittet die webservices auf.
			String[]services = next.getValue().split(",");	
			
			for (String string : services) {
				if(!string.equals(BABELTEXT))
					//string = alle webservices einer Sprache und next.getKey() = language (Sprache).
					tmp.add(configDataholder.getService(string,next.getKey(), null));
				else
					containsBabelText = true;
			}
			
			//getUniqueNLPinPipeline erzeugt f�r die n�chste Sprache die Pipeline.
			output.put(next.getKey(), getUniqueNLPinPipeline(new ArrayList<>(), tmp, options,next.getKey()));
//DONE: getUniqueNLPinPipeline abge�ndert, da diese auf pipieline-Logik xml element aus surls_remote.xml basiert - umgebogen auf Input- und Output-Logik.			 
			
//			pipeline.add(configDataholder.getService(next.getKey(),next.getValue()));
			
		}
		
		//R�ckgabe der manipulierten UniqueNLPinPipeline, d.h. logisches zusammensetzen aller ben�tigten webservices f�r die getroffene Auswahl der webservices	durch den Benutzer.
		return output;
			
//		return getUniqueNLPinPipeline(new ArrayList<>(), pipeline, options);	
	}
}