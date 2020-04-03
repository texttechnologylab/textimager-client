package org.hucompute.textimager.client;

import java.nio.file.Files;

import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.io.xmi.XmiWriter;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.rest.ExceptionCollectorListener;
import org.hucompute.textimager.uima.io.StringReader;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
public class TmpTest {

	public static void main(String[] args) throws ResourceInitializationException, Exception {
		TextImagerClient client = new TextImagerClient();
		
//		CAS output = client.process("This is a test by Barack Obama.", new String[]{"ParagraphSplitter"},"en");
//		SimplePipeline.runPipeline(output, AnalysisEngineFactory.createEngineDescription(XmiWriter.class,XmiWriter.PARAM_TARGET_LOCATION,"output",XmiWriter.PARAM_OVERWRITE,true));
		
//		System.out.println(XmlFormatter.getPrettyString(client.process("This is a test by Barack Obama.", new String[]{"CoreNlpSegmenter"},"en")));
		
		
		java.nio.file.Path tmpFolder = Files.createTempDirectory("textImager");
		ExceptionCollectorListener listener = new ExceptionCollectorListener();
		System.out.println(tmpFolder);
//
		client.processCollection(
				CollectionReaderFactory.createCollectionReader(StringReader.class, StringReader.PARAM_DOCUMENT_TEXT,"Das ist ein Test.",StringReader.PARAM_LANGUAGE,"en"),
				TextImagerOptions.Language.en, 
				new String[]{"BreakIteratorSegmenter"}, 
				2,
				TextImagerOptions.getWriter(IOFormat.valueOf("XMI"), tmpFolder.toFile().toString()),
				listener
			);
		System.out.println(listener.getErrors().get("Document0"));
	}
}
