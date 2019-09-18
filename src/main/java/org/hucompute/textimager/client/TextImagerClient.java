package org.hucompute.textimager.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.aae.client.UimaASProcessStatus;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.TextImagerOptions.Language;
import org.hucompute.textimager.config.ConfigDataholder;
import org.hucompute.textimager.config.ServiceDataholder;
import org.hucompute.textimager.uima.io.json.JSONReader;
import org.hucompute.textimager.uima.io.pdf.PdfReader;
import org.hucompute.textimager.uima.io.tei.TeiReader1;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.lab.uima.engine.uimaas.AsAnalysisEngineDescription;
import de.tudarmstadt.ukp.dkpro.lab.uima.engine.uimaas.AsDeploymentDescription;


/**
 * @author ahemati
 *
 */
/**
 * @author ahemati
 *
 */
public class TextImagerClient {

	final static Logger logger = Logger.getLogger(TextImagerClient.class);

	private String serverUrl = "tcp://alba.hucompute.org:61617";
	private String configFile;
	private int timeout = 100000;

	public TextImagerClient(){
		System.setProperty("org.apache.uima.logger.class","org.apache.uima.util.impl.Log4jLogger_impl");
		System.setProperty("dontKill", "true"); 
	}

	public void setConfigFile(String configFile){
		this.configFile = configFile;
	}

	/**
	 * Process CAS with defined annotatores in a pipeline. 
	 * @param inputCAS 
	 * @param annotators
	 * @return
	 * @throws Exception
	 */
	public CAS process(CAS inputCAS, String[]annotators) throws Exception{
		//TODO Skip already processed steps
		String language = inputCAS.getDocumentLanguage()==null?"unknown":inputCAS.getDocumentLanguage();
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(language))
					options.put(language, options.get(language)+","+string);
				else
					options.put(language, string);
		}
		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options);
		uimaAsEngine.sendAndReceiveCAS(inputCAS);
		uimaAsEngine.stop();
		return inputCAS;
	}

	/**
	 * Process inputString with defined annotatores in a simple pipeline. The language is detected automaticully. 
	 * The inputString is handled as single-language. 
	 * @param inputString String to be processed
	 * @param annotators The processing pipeline
	 * @return Processed CAS object.
	 * @throws Exception
	 */
	public CAS process(String inputString, String[] annotators, String language) throws Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0){
				if(language == null)
					language = "unknown";
				if(options.containsKey(language))
					options.put(language, options.get(language)+","+string);
				else
					options.put(language, string);
			}
		}
		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options);
		CAS output = uimaAsEngine.getCAS();
		DocumentMetaData.create(output).setDocumentId("Inline");
		output.setDocumentText(inputString);
		if(language.equals("unknown"))
			output.setDocumentLanguage("x-unspecified");
		else
			output.setDocumentLanguage(language);
		uimaAsEngine.sendAndReceiveCAS(output);
		uimaAsEngine.stop();
		return output;
	}

	public CAS process(String inputString, String[] annotators) throws Exception{
		return process(inputString,annotators,null);
	}

	public CAS process(String inputString, String annotators) throws Exception{
		ArrayList<String> pipeline = new ArrayList<String>(Arrays.asList(annotators.split(",")).stream().map(x->x.trim()).collect(Collectors.toSet()));
		return process(inputString,pipeline.toArray(new String[pipeline.size()]),null);
	}

	public CAS process(String inputString, String annotators,String language) throws Exception{
		ArrayList<String> pipeline = new ArrayList<String>(Arrays.asList(annotators.split(",")).stream().map(x->x.trim()).collect(Collectors.toSet()));
		return process(inputString,pipeline.toArray(new String[pipeline.size()]),language);
	}

	public CAS process(File inputFile, String ...annotators) throws Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0){
				if(options.containsKey("unknown"))
					options.put("unknown", options.get("unknown")+","+string);
				else
					options.put("unknown", string);
			}
		}
		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options);

		CAS emptyCas = uimaAsEngine.getCAS();
		emptyCas = parseInputText(emptyCas, inputFile);

		uimaAsEngine.sendAndReceiveCAS(emptyCas);
		uimaAsEngine.stop();
		return emptyCas;
	}

	public CAS process(File inputFile, String annotators) throws Exception{
		ArrayList<String> pipeline = new ArrayList<String>(Arrays.asList(annotators.split(",")).stream().map(x->x.trim()).collect(Collectors.toSet()));
		return process(inputFile,pipeline.toArray(new String[pipeline.size()]));
	}

	private BaseUIMAAsynchronousEngine_impl getUimaAsEngine(HashMap<String, String> options) throws Exception{
		return getUimaAsEngine(options, 1);
	}

	private BaseUIMAAsynchronousEngine_impl getUimaAsEngine(HashMap<String, String> options, int casPoolSize) throws Exception{
		return getUimaAsEngine(options, casPoolSize,null,null);
	}

	private BaseUIMAAsynchronousEngine_impl getUimaAsEngine(HashMap<String, String> options, int casPoolSize,CollectionReader collectionReader,UimaAsBaseCallbackListener listener) throws Exception{
		return getUimaAsEngine(options, casPoolSize,collectionReader,listener,null);
	}

	private BaseUIMAAsynchronousEngine_impl getUimaAsEngine(HashMap<String, String> options, int casPoolSize,CollectionReader collectionReader,UimaAsBaseCallbackListener listener,AnalysisEngineDescription casConsumer) throws Exception{
		return getUimaAsEngine(options, casPoolSize, collectionReader, listener, casConsumer, false,false);
	}

	private BaseUIMAAsynchronousEngine_impl getUimaAsEngine(HashMap<String, String> options, int casPoolSize,CollectionReader collectionReader,UimaAsBaseCallbackListener listener,AnalysisEngineDescription casConsumer,boolean languageDefined, boolean forcePipeline) throws Exception{
		BaseUIMAAsynchronousEngine_impl uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
		Pipeline pipelineAPI = new Pipeline();
		HashMap<String, ArrayList<ArrayList<ServiceDataholder>>> pipeline = null;
		if(forcePipeline){
			// TODO Mehr testen, und schönere Lösung?
			pipelineAPI.configDataholder = new ConfigDataholder(configFile);
			
			ArrayList<ArrayList<ServiceDataholder>> top = new ArrayList<>();
			ArrayList<ServiceDataholder> annotators = new ArrayList<>();
			for (Entry<String, String> option : options.entrySet()) {
				annotators.add(pipelineAPI.configDataholder.getService(null, option.getValue(), option.getKey()));
			}
			top.add(annotators);
			pipeline = new HashMap<>();
			pipeline.put(options.entrySet().iterator().next().getKey(), top);
		}
		else{
			pipeline = pipelineAPI.constructPipeline(options,null, configFile,languageDefined);
		}
		
		System.out.println(pipeline);
		Map<String, Object> clientCtx = new HashMap<String, Object>();
		//		System.out.println(pipeline);
		//Falls die Pipeline nur aus einem Annotator besteht
//		if (pipeline.size() == 1 && pipeline.values().iterator().next().get(0).size() == 1
//				&& pipeline.values().iterator().next().size() == 1) {
//			clientCtx.put(UimaAsynchronousEngine.ServerUri, pipeline.values().iterator().next().get(0).get(0).getBrokerURL());
//			clientCtx.put(UimaAsynchronousEngine.ENDPOINT, pipeline.values().iterator().next().get(0).get(0).getName());
//			clientCtx.put(UimaAsynchronousEngine.Timeout, 12000);
//			clientCtx.put(UimaAsynchronousEngine.GetMetaTimeout, 5000);
//			clientCtx.put(UimaAsynchronousEngine.CpcTimeout, 12000);
//			clientCtx.put(UimaAsynchronousEngine.CasPoolSize, casPoolSize);
//			clientCtx.put(UimaAsynchronousEngine.UimaEeDebug, true);
//		}
//		else{
			String pipelineName = pipelineAPI.constructPipelineName(pipeline);

			File aeDescriptionFile = File.createTempFile("aed", ".xml");
			aeDescriptionFile.deleteOnExit();
			AsAnalysisEngineDescription.toXML(aeDescriptionFile, pipeline);

			AsDeploymentDescription deploymentDescription = new AsDeploymentDescription(
					AnalysisEngineFactory
					.createEngineDescriptionFromPath(aeDescriptionFile.getAbsolutePath()),
					pipelineName, serverUrl, pipeline);

			deploymentDescription.setName("tmp"+System.currentTimeMillis());

			File deployFile = File.createTempFile("deployFile", ".xml");
			deployFile.deleteOnExit();

			deploymentDescription.toXML(deployFile);
			deploymentDescription.toXML(System.out);

			// preparing map for use in deploying services
			clientCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath, ConfigDataholder.getDd2SpringPath().replace("\\", "/"));
			clientCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + ConfigDataholder.getSaxonPath().replace("\\", "/"));
			if(casConsumer != null){
				addAnalysisEngine(deployFile,casConsumer);
			}

			// creating aggregate analysis engine
			uimaAsEngine.deploy(deployFile.getAbsolutePath(), clientCtx);
			//System.out.println(FileUtils.readFileToString(deployFile.getAbsoluteFile(),"UTF-8"));

			// preparing map for use in a UIMA client for submitting text to
			clientCtx.put(UimaAsynchronousEngine.ServerUri, serverUrl);
			clientCtx.put(UimaAsynchronousEngine.ENDPOINT, pipelineName);
			clientCtx.put(UimaAsynchronousEngine.Timeout, 500 * timeout);
			clientCtx.put(UimaAsynchronousEngine.GetMetaTimeout, 500 * timeout);
			clientCtx.put(UimaAsynchronousEngine.CpcTimeout, 500 * timeout);
			clientCtx.put(UimaAsynchronousEngine.CasPoolSize, casPoolSize);
//		}

		if(collectionReader != null){
			uimaAsEngine.setCollectionReader(collectionReader);
		}

		if(listener != null){
			uimaAsEngine.addStatusCallbackListener(listener);
		}


		// Initialize the client
		uimaAsEngine.initialize(clientCtx);

		return uimaAsEngine;
	}


	private static void addAnalysisEngine(File deploymentDescription,AnalysisEngineDescription desciption) throws ParserConfigurationException, SAXException, IOException, ResourceInitializationException{
		//adding to delegets list of remoteDeploymentDescriptor
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(deploymentDescription);
		Element delegates = (Element)doc.getElementsByTagName("delegates").item(0);
		Element casConsumerElement = doc.createElement("analysisEngine");
		String newAnnotatorName = desciption.getAnnotatorImplementationName().replaceAll(".*\\.", "");
		casConsumerElement.setAttribute("key", newAnnotatorName);
		delegates.appendChild(casConsumerElement);

		String locationDescriptor = ((Element)((Element)doc.getElementsByTagName("topDescriptor").item(0)).getElementsByTagName("import").item(0)).getAttribute("location");

		File casConsumerFile = File.createTempFile(newAnnotatorName, ".xml");
		desciption.toXML(new FileOutputStream(casConsumerFile));
		DocumentBuilderFactory dbFactory1 = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder1 = dbFactory1.newDocumentBuilder();

		Document doc1 = null;
		if(org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS){
			try {
				//				System.out.println(new File(new URI(locationDescriptor)));
				doc1 = dBuilder1.parse(new File(new URI(locationDescriptor)));
				locationDescriptor = new File(new URI(locationDescriptor)).getPath();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			doc1 = dBuilder1.parse(new File(locationDescriptor));
		}
		Element delegateAnalysisEngineSpecifiers = (Element)doc1.getElementsByTagName("delegateAnalysisEngineSpecifiers").item(0);
		Element delegateAnalysisEngine = doc1.createElement("delegateAnalysisEngine");
		delegateAnalysisEngine.setAttribute("key", newAnnotatorName);
		Element importElement = doc1.createElement("import");
		if(SystemUtils.IS_OS_WINDOWS){
			importElement.setAttribute("location", "file:/"+casConsumerFile.getAbsolutePath().replace("\\", "/"));
		}
		else{
			importElement.setAttribute("location", casConsumerFile.getAbsolutePath().replace("\\", "/"));
		}
		delegateAnalysisEngine.appendChild(importElement);
		delegateAnalysisEngineSpecifiers.appendChild(delegateAnalysisEngine);

		NodeList configurationParameterSettings = doc1.getElementsByTagName("nameValuePair");
		for (int i = 0; i < configurationParameterSettings.getLength(); i++) {
			Element nameValuePair = (Element)configurationParameterSettings.item(i);
			String name = nameValuePair.getElementsByTagName("name").item(0).getTextContent().trim();
			if(name.equals("Flow")){
				String stringValue = ((Element)nameValuePair.getElementsByTagName("string").item(0)).getTextContent().trim();
				JSONObject pipeline = new JSONObject(stringValue.replace("=", ":"));
				for (String object : pipeline.keySet()) {
					pipeline.getJSONArray(object).put(Arrays.asList(new String[]{newAnnotatorName}));
				}
				((Element)nameValuePair.getElementsByTagName("string").item(0)).setTextContent(pipeline.toString().replace(":", "="));
			}
		}
		FileUtils.writeStringToFile(new File(locationDescriptor), asString(doc1));
		FileUtils.writeStringToFile(deploymentDescription, asString(doc));
	}

	private static String asString(Node node) {
		StringWriter writer = new StringWriter();
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			// @checkstyle MultipleStringLiterals (1 line)
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty(OutputKeys.VERSION, "1.0");
			if (!(node instanceof Document)) {
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}
			trans.transform(new DOMSource(node), new StreamResult(writer));
		} catch (final TransformerConfigurationException ex) {
			throw new IllegalStateException(ex);
		} catch (final TransformerException ex) {
			throw new IllegalArgumentException(ex);
		}
		return writer.toString();
	}

	private CAS parseInputText(CAS emptyCas, File inputFile) throws UIMAException, IOException, SAXException {
		String fileExtension = inputFile.getName().split("\\.")[inputFile.getName().split("\\.").length-1];
		String input = FileUtils.readFileToString(inputFile,"UTF-8");

		//TODO resourcecollectionbase umschreiben, damit generischer funktioniert.
		switch (fileExtension) {
		case "tei":
			try {
				return new TeiReader1().init(input).getCas();
			} catch (UIMAException | IOException | SAXException e) {
				e.printStackTrace();
			}
		case "pdf":
			return new PdfReader().init(input).getCas();
		case "json":
			return new JSONReader().init(input).getCas();
			//		case "becalm":
			//			return new org.hucompute.services.inputreader.BecalmReader().init(input).getCas();
		case "txt":
			emptyCas.setDocumentText(FileUtils.readFileToString(inputFile,"UTF-8"));
			return emptyCas;
		default:
			throw new UnsupportedOperationException("Filetype not supported. Supported types are :\nTEI (.tei)\nTXT(.txt)");
		}
	}

	private class CallbackListenerCollection extends UimaAsBaseCallbackListener{
		List<CAS>output = new ArrayList<>();

		int processed = 0;

		private HashMap<String, Integer> processedBy = new HashMap<>();

		/**
		 * This will be called once the text is processed.
		 */
		@Override
		public void entityProcessComplete(CAS output, EntityProcessStatus aStatus) {
			JCas cas = null;
			try {
				cas = JCasFactory.createJCas();
			} catch (UIMAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CasCopier.copyCas(output, cas.getCas(),true,true);
			this.output.add(cas.getCas());
			//			if(!aStatus.getStatusMessage().equals("success")){
			//				System.out.println(DocumentMetaData.get(output).getDocumentTitle()+" - "+aStatus.getStatusMessage());
			//				System.out.println(aStatus);
			//			}
			//			System.out.println(getPrettyString(output));
			//
			//			//TODO
			//			try {
			//				SimplePipeline.runPipeline(output, TextImagerOptions.getWriter(outputFormat, outputDir));
			//				DocElement element = new DocElement(output.getJCas(), 0, output.getDocumentText().length());
			//				element.setName(DocumentMetaData.get(output).getDocumentTitle());
			//				element.addToIndexes();
			//			} catch (AnalysisEngineProcessException | ResourceInitializationException e) {
			//				e.printStackTrace();
			//			} catch (CASException e) {
			//				e.printStackTrace();
			//			}
			//
			//			if(processed++%100==0){
			//				System.out.println(processed+" , "+ (System.currentTimeMillis()-start));
			//				System.out.println(processedBy);
			//			}
			//			output.reset();
		}

		@Override
		public void onBeforeProcessCAS(UimaASProcessStatus status,
				String nodeIP, String pid) {
			if(!processedBy.containsKey(nodeIP))
				processedBy.put(nodeIP, 1);
			else
				processedBy.put(nodeIP, processedBy.get(nodeIP)+1);
			super.onBeforeProcessCAS(status, nodeIP, pid);
		}

		@Override
		public void collectionProcessComplete(EntityProcessStatus aStatus) {
			super.collectionProcessComplete(aStatus);
		}
	};


	/**
	 * Process collection
	 * @param collectionPath Path to the Collection
	 * @param inputFormant 
	 * @param inputLanguage
	 * @param annotators
	 * @param numberOfCases
	 * @return List of CASes of processed input collection 
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public List<CAS> processCollection(File collectionPath, IOFormat inputFormant, Language inputLanguage,String []annotators, int numberOfCases) throws ResourceInitializationException, Exception {
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}
		CallbackListenerCollection asyncListener = new CallbackListenerCollection();

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,numberOfCases,
				TextImagerOptions.getReader(inputFormant, collectionPath.getPath(), inputLanguage)
				,asyncListener);
		uimaAsEngine.process();
		uimaAsEngine.stop();
		return asyncListener.output;
	}


	/**
	 * Process collection with callback listener
	 * @param collectionPath
	 * @param inputFormant
	 * @param inputLanguage
	 * @param annotators
	 * @param numberOfCases
	 * @param callbackListener
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(File collectionPath, IOFormat inputFormant, Language inputLanguage,String []annotators, int numberOfCases, UimaAsBaseCallbackListener callbackListener) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,numberOfCases,
				TextImagerOptions.getReader(inputFormant, collectionPath.getPath(), inputLanguage)
				,callbackListener);

		uimaAsEngine.process();
		uimaAsEngine.stop();
	}

	/**
	 * Process collection with into outputformat
	 * @param collectionPath Collection base directory
	 * @param inputFormant
	 * @param inputLanguage
	 * @param annotators
	 * @param outputFormat
	 * @param outputLocation
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(File collectionPath, IOFormat inputFormant, Language inputLanguage,String []annotators,IOFormat outputFormat, String outputLocation) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,2,
				TextImagerOptions.getReader(inputFormant, collectionPath.getPath(), inputLanguage),
				null,TextImagerOptions.getWriter(outputFormat, outputLocation));
		uimaAsEngine.process();
		uimaAsEngine.stop();
	}
	
	/**
	 * Process collection with into outputformat
	 * @param collectionPath Collection base directory
	 * @param inputFormant
	 * @param inputLanguage
	 * @param annotators
	 * @param outputFormat
	 * @param outputLocation
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(File collectionPath, IOFormat inputFormant, Language inputLanguage,String []annotators,IOFormat outputFormat, String outputLocation, boolean forcePipeline) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,2,
				TextImagerOptions.getReader(inputFormant, collectionPath.getPath(), inputLanguage),
				null,TextImagerOptions.getWriter(outputFormat, outputLocation), false, forcePipeline);
		uimaAsEngine.process();
		uimaAsEngine.stop();
	}

	/**
	 * Process collection with into outputformat
	 * @param collectionPath Collection base directory
	 * @param inputFormant
	 * @param inputLanguage
	 * @param annotators
	 * @param outputFormat
	 * @param outputLocation
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(File collectionPath, IOFormat inputFormant, Language inputLanguage,String []annotators,IOFormat outputFormat, String outputLocation, boolean forcePipeline, String fileSuffix, String sourceEncoding) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,2,
				TextImagerOptions.getReader(inputFormant, collectionPath.getPath(), inputLanguage, fileSuffix, sourceEncoding),
				null,TextImagerOptions.getWriter(outputFormat, outputLocation), false, forcePipeline);
		uimaAsEngine.process();
		uimaAsEngine.stop();
	}

	/**
	 * Process collection with callback listener and custom collection reader.
	 * @param reader
	 * @param inputLanguage
	 * @param annotators
	 * @param numberOfCases
	 * @param callbackListener
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(CollectionReader reader,Language inputLanguage,String []annotators, int numberOfCases, UimaAsBaseCallbackListener callbackListener) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,numberOfCases,reader,callbackListener);
		uimaAsEngine.process();
		uimaAsEngine.stop();
	}

	/**
	 * Process collection with cas consumer and custom collection reader.
	 * @param reader
	 * @param inputLanguage
	 * @param annotators
	 * @param numberOfCases
	 * @param casConsumer
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(CollectionReader reader,Language inputLanguage,String []annotators, int numberOfCases, AnalysisEngineDescription casConsumer) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,numberOfCases,reader,null,casConsumer,inputLanguage == Language.unknown?false:true,false);
		uimaAsEngine.process();
		uimaAsEngine.stop();
	}
	
	/**
	 * Process collection with cas consumer and custom collection reader.
	 * @param reader
	 * @param inputLanguage
	 * @param annotators
	 * @param numberOfCases
	 * @param casConsumer
	 * @param callbackListener
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(CollectionReader reader,Language inputLanguage,String []annotators, int numberOfCases, AnalysisEngineDescription casConsumer, UimaAsBaseCallbackListener callbackListener) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,numberOfCases,reader,callbackListener,casConsumer,inputLanguage == Language.unknown?false:true,false);
		uimaAsEngine.process();
		uimaAsEngine.stop();
	}
	
	/**
	 * Process collection with cas consumer and custom collection reader.
	 * @param reader
	 * @param inputLanguage
	 * @param annotators
	 * @param numberOfCases
	 * @param casConsumer
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(CollectionReader reader,Language inputLanguage,String []annotators, int numberOfCases, AnalysisEngineDescription casConsumer, boolean forcePipeline) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,numberOfCases,reader,null,casConsumer,inputLanguage == Language.unknown?false:true,forcePipeline);
		uimaAsEngine.process();
		uimaAsEngine.stop();
	}

	/**
	 * Process collection with cas consumer and custom collection reader.
	 * @param reader
	 * @param inputLanguage
	 * @param annotators
	 * @param casConsumer
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public void processCollection(CollectionReader reader,Language inputLanguage,String []annotators, AnalysisEngineDescription casConsumer) throws ResourceInitializationException, Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,2,reader,null,casConsumer);
		uimaAsEngine.process();
		uimaAsEngine.stop();
	}

	/**
	 * Process collection
	 * @param reader Collection Reader
	 * @param inputLanguage
	 * @param annotators
	 * @param numberOfCases
	 * @return List of CASes of processed input collection 
	 * @throws ResourceInitializationException
	 * @throws Exception
	 */
	public List<CAS> processCollection(CollectionReader reader, Language inputLanguage,String []annotators, int numberOfCases) throws ResourceInitializationException, Exception {
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey(inputLanguage.name()))
					options.put(inputLanguage.name(), options.get(inputLanguage.name())+","+string);
				else
					options.put(inputLanguage.name(), string);
		}

		CallbackListenerCollection asyncListener = new CallbackListenerCollection();


		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options,numberOfCases,
				reader
				,asyncListener);
		uimaAsEngine.process();
		uimaAsEngine.stop();
		return asyncListener.output;
	}

}
