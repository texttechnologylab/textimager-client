package org.hucompute.textimager.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.hucompute.textimager.config.ConfigDataholder;
import org.hucompute.textimager.config.ServiceDataholder;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;



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
	 * Ermittelt die benoetigte Pipeline. Wenn etwas parallel laufen kann, wird
	 * es ermittelt.
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws NullPointerException 
	 * @throws XPathExpressionException 
	 * @throws MalformedURLException 
	 */
	public HashMap<String, ArrayList<ArrayList<ServiceDataholder>>> constructPipeline(HashMap<String, String>options,JCas cas, String configFile) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
		if(configFile !=null)
			configDataholder = new ConfigDataholder(configFile);

		//alle services in der pipeline. ungeordnet
		HashMap<String, ArrayList<ServiceDataholder>> uniquePipeLine =  getPipeline(options);

		//holds the pipeline for each language
		HashMap<String, ArrayList<ArrayList<ServiceDataholder>>>pipelineForLanguages = new HashMap<>();


		Iterator<Map.Entry<String, ArrayList<ServiceDataholder>>> iter = uniquePipeLine.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry<String, ArrayList<ServiceDataholder>> next = iter.next();
			ServiceDataholder merger = configDataholder.getService(null, "LanguageMerger", null);

			//die bearbeiteten services.
			ArrayList<ServiceDataholder>processed = new ArrayList<ServiceDataholder>();

			//gruppen die schon bearbeitet wurden
			ArrayList<String>groupProcessed = new ArrayList<>();

			//ausgabe
			ArrayList<ArrayList<ServiceDataholder>>pipeLine = new ArrayList<ArrayList<ServiceDataholder>>();

			int iterations = next.getValue().size();
			//						if(next.getValue().contains(merger)){
			//							merger = next.getValue().get(next.getValue().indexOf(merger));
			//							iterations--;
			//							next.getValue().remove(merger);
			//			//				containsMerger = true;
			//						}

			//falls die Anzahl der bearbeiteten kleiner ist als alle services in der pipeline.
			while (processed.size()<iterations) {
				ArrayList<ArrayList<ServiceDataholder>>nonconflicting = new ArrayList<ArrayList<ServiceDataholder>>();
				ArrayList<ServiceDataholder>thisStepProcess = new ArrayList<ServiceDataholder>();
				ArrayList<String>thisStepGroupProcess = new ArrayList<String>();

				//potentiell parallel laufende werden ermittelt
				for (ServiceDataholder element : next.getValue()) {
//					if(alreadyTaggedWith(element, cas) && !processed.contains(element)){
//						thisStepProcess.add(element);
//						thisStepGroupProcess.add(element.getGroup());
//						continue;
//					}
//					else
						if(!processed.contains(element)){
							if(groupProcessed.containsAll(element.getPipeline()) || element.getPipeline().size() == 0){
								thisStepProcess.add(element);
								thisStepGroupProcess.add(element.getGroup());
								addToNonConflicting(nonconflicting, element);
							}
						}
				}	


				groupProcessed.addAll(thisStepGroupProcess);
				processed.addAll(thisStepProcess);
				pipeLine.addAll(nonconflicting);
			}
			ArrayList<ServiceDataholder>mergerList = new ArrayList<>();
			mergerList.add(merger);

			pipeLine.remove(mergerList);

			for(int i = pipeLine.size()-1;i > 0;i--){
				if(pipeLine.get(i).get(0).getPipeline().contains("languageSplitter")){
					pipeLine.add(i+1,mergerList);
					break;
				}
			}

			if(options.size() == 1 && !options.containsValue("HucomputeLanguageDetectionPercentage"))
				removeSplitter(pipeLine);
			//			if(containsMerger){
			//				ArrayList<ServiceDataholder>mergerList = new ArrayList<>();
			//				mergerList.add(merger);
			//				pipeLine.add(mergerList);
			//			}
			//			if(containsBabelText)

			pipelineForLanguages.put(next.getKey(), pipeLine);
		}


		return pipelineForLanguages;
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

	private boolean alreadyTaggedWith(ServiceDataholder element, JCas cas){

		if(cas.getAnnotationIndex(Token.type).size()>0 && element.getGroup().equals("tokenizer")){			
			return true;
		}

		if(cas.getDocumentLanguage() !=null &&!cas.getDocumentLanguage().equals("x-unspecified") && element.getGroup().equals("languageDetection")){			
			return true;
		}


		if(cas.getAnnotationIndex(POS.type).size()>0 && element.getGroup().equals("pos")){			
			return true;
		}

		if(cas.getAnnotationIndex(Lemma.type).size()>0 && element.getGroup().equals("lemmatizer")){			
			return true;
		}

		return false;
	}


	private void removeSplitter(ArrayList<ArrayList<ServiceDataholder>>pipeLine) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
		ServiceDataholder merger = configDataholder.getService(null, "LanguageMerger", null);
		ServiceDataholder splitter = configDataholder.getService(null, "LanguageSplitter", null);
		ServiceDataholder advancedLangdetet = configDataholder.getService(null, "HucomputeLanguageDetectionPercentage", null);

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
	 * @param nonconflicting Services, die parallel laufen koennen.
	 * @param newServiceDataholder neue service.
	 */
	private void addToNonConflicting(ArrayList<ArrayList<ServiceDataholder>>nonconflicting, ServiceDataholder newServiceDataholder){
		if(newServiceDataholder.isParallel())
			for (ArrayList<ServiceDataholder> arrayList : nonconflicting) {
				boolean cont = false;
				if(!arrayList.get(0).isParallel())
					continue;
				ArrayList<String>outputs = new ArrayList<String>();
				for (ServiceDataholder serviceDataholder : arrayList) {
					outputs.addAll(serviceDataholder.getOutput());
					if(serviceDataholder.getPipeline().contains("languageSplitter") != newServiceDataholder.getPipeline().contains("languageSplitter"))
						cont=true;
				}
				if(cont)
					continue;
				outputs.retainAll(newServiceDataholder.getOutput());
				if(outputs.size()==0){
					arrayList.add(newServiceDataholder);
					return;
				}
			}
		ArrayList<ServiceDataholder> tmp = new ArrayList<ServiceDataholder>();
		tmp.add(newServiceDataholder);
		nonconflicting.add(tmp);
	}

	/**
	 * Bestimmt alle NLP-Verfahren die im Laufe des Preprocesses benötigt werden. 
	 * @param uniquePipeline	Die NLP-Verfahren die benötigt werden
	 * @param pipeline			Die Pipeline des NLP-Verfahrens, die die Preprocess-Funktion aufruft
	 * @return	uniquePipeline
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws NullPointerException 
	 * @throws XPathExpressionException 
	 * @throws MalformedURLException 
	 */
	private ArrayList<ServiceDataholder> getUniqueNLPinPipeline(ArrayList<ServiceDataholder> uniquePipeline, ArrayList<ServiceDataholder> pipeline,HashMap<String, String> options,String language) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
		//rekursiver Aufruf. Geht alle NLPs durch und fuegt sie zur lieste uniquePipeline hinzu, falls noch nicht vorhanden.
		for (ServiceDataholder abstractNLP : pipeline) {
			if(!uniquePipeline.contains(abstractNLP))
				uniquePipeline.add(abstractNLP);
//			if(abstractNLP !=null)
				getUniqueNLPinPipeline(uniquePipeline,getPipeline(abstractNLP.getPipeline(),options,language),options,language);
		}
		return uniquePipeline;
	}

	public ArrayList<ServiceDataholder> getPipeline(List<String> pipelineStrings,HashMap<String, String> options,String language) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{
		ArrayList<ServiceDataholder> pipeline = new ArrayList<ServiceDataholder>();
		for (String string : pipelineStrings) {
			boolean added = false;

			for (String pipelineService : options.get(language).split(",")) {
				if(!added && configDataholder.getService(null, pipelineService, null).getGroup().equals(string)){
					pipeline.add(configDataholder.getService(null, pipelineService, null));
					added = true;
				}
			}

			if(!added)
				pipeline.add(configDataholder.getService(string, null,language));
		}
		return pipeline;
	}


	public HashMap<String,ArrayList<ServiceDataholder>> getPipeline(HashMap<String, String> options) throws MalformedURLException, XPathExpressionException, NullPointerException, SAXException, IOException, ParserConfigurationException{

		HashMap<String,ArrayList<ServiceDataholder>> output = new HashMap<>();

		//		ArrayList<ServiceDataholder> pipeline = new ArrayList<ServiceDataholder>();
		Iterator<Map.Entry<String, String>> iter = options.entrySet().iterator();
		//
		//		//hier werden die vordefinierten services aus den Optionen der pipeline hinzugefuegt.
		while (iter.hasNext()) {
			Map.Entry<String, String> next = iter.next();
			//			output.put(next.getKey(), new ArrayList<>());
			ArrayList<ServiceDataholder> tmp = new ArrayList<>();
			String[]services = next.getValue().split(",");
			for (String string : services) {
				if(!string.equals(BABELTEXT))
					tmp.add(configDataholder.getService(null, string,next.getKey()));
				else
					containsBabelText = true;
			}
			output.put(next.getKey(), getUniqueNLPinPipeline(new ArrayList<>(), tmp, options,next.getKey()));
			//			pipeline.add(configDataholder.getService(next.getKey(),next.getValue()));
		}
		return output;
		//		
		//		return getUniqueNLPinPipeline(new ArrayList<>(), pipeline, options);
	}
	
	public String constructPipelineName(HashMap<String, ArrayList<ArrayList<ServiceDataholder>>> pipeline) {
		String pipelineName = "";

		Iterator<Map.Entry<String, ArrayList<ArrayList<ServiceDataholder>>>> iter = pipeline.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, ArrayList<ArrayList<ServiceDataholder>>> next = iter.next();
			if (pipelineName.length() > 0)
				pipelineName += "____";
			pipelineName += next.getKey() + "___";
			for (ArrayList<ServiceDataholder> arrayList : next.getValue()) {
				if (pipelineName.length() > 0)
					pipelineName += "__";
				for (ServiceDataholder serviceDataholder : arrayList) {
					if (pipelineName.length() != 0)
						pipelineName += "_";
					pipelineName += serviceDataholder.getName();
				}
			}
		}

		return pipelineName;
	};


}
