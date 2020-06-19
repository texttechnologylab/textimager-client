package org.hucompute.textimager.client;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.metadata.FixedFlow;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.CustomResourceSpecifier;
import org.apache.uima.resource.Parameter;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.apache.uima.resource.impl.Parameter_impl;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.io.xmi.XmiReader;
import org.dkpro.core.io.xmi.XmiWriter;

public class SimplePipelineTest {

	public static void main(String[] args) throws IOException, UIMAException {

		CollectionReader reader = CollectionReaderFactory.createReader(
				XmiReader.class, 
				XmiReader.PARAM_SOURCE_LOCATION,"/resources/public/stoeckel/BIOfid/BIOfid_XMI_27.01.2020",
				XmiReader.PARAM_PATTERNS,"*.xmi",
				XmiReader.PARAM_LANGUAGE,"de");

		AnalysisEngineDescription aggDesc = UIMAFramework.getResourceSpecifierFactory()
				.createAnalysisEngineDescription();
		aggDesc.setPrimitive(false);
		//		aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("BreakIteratorSegmenter", primitiveEngine("BreakIteratorSegmenter"));
		aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("CoreNlpPosTagger", primitiveEngine("CoreNlpPosTagger"));
		aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("LanguageToolLemmatizer", primitiveEngine("LanguageToolLemmatizer"));
		aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("CoreNlpNamedEntityRecognizer", primitiveEngine("CoreNlpNamedEntityRecognizer"));
		aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("HeidelTime", primitiveEngine("HeidelTime"));
		aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("TagMeLocalAnnotator", primitiveEngine("TagMeLocalAnnotator"));
//		aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("WikidataHyponyms", primitiveEngine("WikidataHyponyms"));
//		aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("EuroWordNetTagger", primitiveEngine("EuroWordNetTagger"));
		FixedFlow flow = UIMAFramework.getResourceSpecifierFactory().createFixedFlow();      
		flow.setFixedFlow(new String[] {"CoreNlpPosTagger","LanguageToolLemmatizer","CoreNlpNamedEntityRecognizer","HeidelTime","TagMeLocalAnnotator"});
		aggDesc.getAnalysisEngineMetaData().setFlowConstraints(flow);
		//				de______ParagraphSplitter___BreakIteratorSegmenter_TagMeLocalAnnotator___MateLemmatizer___WikidataHyponyms___CoreNlpPosTagger___BIOfidGazetteer___HeidelTime___EuroWordNetTagger

		AggregateBuilder builder = new AggregateBuilder();
		builder.add(createEngineDescription(BioFidMapper.class));


		SimplePipeline.runPipeline(
				reader, 
				builder.createAggregateDescription(),
				aggDesc,
				AnalysisEngineFactory.createEngineDescription(
						XmiWriter.class,
						XmiWriter.PARAM_TARGET_LOCATION,"/home/ahemati/xmi/output",
						XmiWriter.PARAM_OVERWRITE,true));
		//		SimplePipeline.runPipeline(
		//				reader, 
		//				builder.createAggregateDescription(),
		//				AnalysisEngineFactory.createEngineDescription(
		//						XmiWriter.class,
		//						XmiWriter.PARAM_TARGET_LOCATION,"/home/ahemati/xmi/output",
		//						XmiWriter.PARAM_OVERWRITE,true));

	}

	public static CustomResourceSpecifier primitiveEngine(String name){
		// read AE descriptor from file
		CustomResourceSpecifier aeSpecifier = new CustomResourceSpecifier_impl();
		aeSpecifier.setResourceClassName("org.apache.uima.aae.jms_adapter.JmsAnalysisEngineServiceAdapter");
		ArrayList<Parameter>parameters = new ArrayList<>();
		parameters.add(new Parameter_impl("brokerURL", "tcp://alba:61617"));
		parameters.add(new Parameter_impl("endpoint", name));
		parameters.add(new Parameter_impl("timeout", "51111"));
		parameters.add(new Parameter_impl("getmetatimeout", "51111"));
		parameters.add(new Parameter_impl("cpctimeout", "51111"));
		aeSpecifier.setParameters(parameters.toArray(new Parameter[parameters.size()]));
		return aeSpecifier;
	}


}
