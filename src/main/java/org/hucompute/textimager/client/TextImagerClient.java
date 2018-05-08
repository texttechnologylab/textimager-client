package org.hucompute.textimager.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.aae.client.UimaASProcessStatus;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
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
import org.hucompute.textimager.util.XmlFormatter;
import org.xml.sax.SAXException;

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

	public static void main(String...args) throws Exception{
		TextImagerClient client = new TextImagerClient();
		client.setConfigFile("src/main/resources/services.xml");
		CAS output3 = client.process("Das ist ein Test vom 18. August 1990.",  "ParagraphSplitter,LanguageToolSegmenter,MarMoTTagger,MarMoTLemma,HeidelTime,MateParser,MateMorphTagger");
		//System.out.println(XmlFormatter.getPrettyString(output3));
//		List<CAS> output = client.processCollection(new File("/home/ahemati/can_be_deleted/testfiles"), IOFormat.TXT, "de", new String[]{"BreakIteratorSegmenter","HucomputeLanguageDetection"}, 10);
//		System.out.println(output.size());
//		System.out.println(XmlFormatter.getPrettyString(output.get(10)));
	}
	
	private String serverUrl = "tcp://alba.hucompute.org:61617";
	private String configFile;
	private int timeout = 100000;

	public TextImagerClient(){
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
	public CAS process(String inputString, String[] annotators) throws Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey("unknown"))
					options.put("unknown", options.get("unknown")+","+string);
				else
					options.put("unknown", string);
		}
		BaseUIMAAsynchronousEngine_impl uimaAsEngine = getUimaAsEngine(options);
		CAS output = uimaAsEngine.getCAS();
		output.setDocumentText(inputString);
		uimaAsEngine.sendAndReceiveCAS(output);
		uimaAsEngine.stop();
		return output;
	}

	public CAS process(String inputString, String annotators) throws Exception{
		ArrayList<String> pipeline = new ArrayList<String>(Arrays.asList(annotators.split(",")).stream().map(x->x.trim()).collect(Collectors.toSet()));
		return process(inputString,pipeline.toArray(new String[pipeline.size()]));
	}

	public CAS process(File inputFile, String ...annotators) throws Exception{
		HashMap<String, String> options = new HashMap<>();
		for (String string : annotators) {
			if(string.trim().length()>0)
				if(options.containsKey("unknown"))
					options.put("unknown", options.get("unknown")+","+string);
				else
					options.put("unknown", string);
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
		BaseUIMAAsynchronousEngine_impl uimaAsEngine = new BaseUIMAAsynchronousEngine_impl();
		Pipeline pipelineAPI = new Pipeline();
		HashMap<String, ArrayList<ArrayList<ServiceDataholder>>> pipeline = pipelineAPI.constructPipeline(options,null, configFile);
		
		Map<String, Object> clientCtx = new HashMap<String, Object>();

		//Falls die Pipeline nur aus einem Annotator besteht
		if (pipeline.size() == 1 && pipeline.values().iterator().next().get(0).size() == 1
				&& pipeline.values().iterator().next().size() == 1) {
			clientCtx.put(UimaAsynchronousEngine.ServerUri, pipeline.values().iterator().next().get(0).get(0).getBrokerURL());
			clientCtx.put(UimaAsynchronousEngine.ENDPOINT, pipeline.values().iterator().next().get(0).get(0).getName());
			clientCtx.put(UimaAsynchronousEngine.Timeout, 12000);
			clientCtx.put(UimaAsynchronousEngine.GetMetaTimeout, 5000);
			clientCtx.put(UimaAsynchronousEngine.CpcTimeout, 12000);
			clientCtx.put(UimaAsynchronousEngine.CasPoolSize, casPoolSize);
			clientCtx.put(UimaAsynchronousEngine.UimaEeDebug, true);
		}
		else{
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
			clientCtx.put(UimaAsynchronousEngine.DD2SpringXsltFilePath, ConfigDataholder.getDd2SpringPath());
			clientCtx.put(UimaAsynchronousEngine.SaxonClasspath, "file:" + ConfigDataholder.getSaxonPath());
			
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
		}

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


	private CAS parseInputText(CAS emptyCas, File inputFile) throws UIMAException, IOException, SAXException {
		String fileExtension = inputFile.getName().split("\\.")[inputFile.getName().split("\\.").length-1];
		String input = FileUtils.readFileToString(inputFile,"UTF-8");

		//TODO resourcecollectionbase umschreiben, damit generischer funktioniert.
		switch (fileExtension) {
		case "tei":
			try {
				return new org.hucompute.services.inputreader.TeiReader1().init(input).getCas();
			} catch (UIMAException | IOException | SAXException e) {
				e.printStackTrace();
			}
		case "pdf":
			return new org.hucompute.services.inputreader.PdfReader().init(input).getCas();
		case "json":
			return new org.hucompute.services.inputreader.JSONReader().init(input).getCas();
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
			//System.out.println(processed++);
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

}
