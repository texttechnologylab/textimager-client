package org.hucompute.textimager.client;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.TextImagerOptions.Language;
import org.hucompute.textimager.uima.io.StringReader;
import org.hucompute.textimager.util.XmlFormatter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TextImagerClientTest {
	
	@Test
	public void testStringArrayCasCallback() throws ResourceInitializationException, Exception{
		String[] inputDocuments = new String[]{"Das ist ein Test.", "This is a test."};
		TextImagerClient client = new TextImagerClient();
		List<CAS> output = client.processCollection(CollectionReaderFactory.createCollectionReader(StringReader.class, StringReader.PARAM_DOCUMENT_TEXT,inputDocuments),TextImagerOptions.Language.unknown, new String[]{"LanguageIdentification"}, 2);
		assertEquals(output.get(0).getDocumentLanguage(), "de");
		assertEquals(output.get(1).getDocumentLanguage(), "en");
	}
	

//	@Test
//	public void testCallbackListener() throws ResourceInitializationException, Exception{
//		TextImagerClient client = new TextImagerClient();
//		client.setConfigFile("src/main/resources/services.xml");
//		client.processCollection(
//				new File("src/test/resources/collectionTest"),
//				IOFormat.TXT,
//				TextImagerOptions.Language.de,
//				new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTLemma"},
//				10,
//				new UimaAsBaseCallbackListener() {
//					@Override
//					public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
//						// TODO Auto-generated method stub
//						super.entityProcessComplete(aCas, aStatus);
//						try {
//							Collection<CategoryCoveredTagged> ddcs = JCasUtil.select(aCas.getJCas(), CategoryCoveredTagged.class);
//							//System.out.println(ddcs.size());
//
//						} catch (CASException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//
//					@Override
//					public void collectionProcessComplete(EntityProcessStatus aStatus) {
//						super.collectionProcessComplete(aStatus);
//						//System.out.println("collection complete");
//					};
//				}
//				);
//	}

//	@Test
//	public void testCallbackListenerCustomReader() throws ResourceInitializationException, Exception{
//		TextImagerClient client = new TextImagerClient();
//		client.setConfigFile("src/main/resources/services.xml");
//		// CollectionReader reader,String inputLanguage,String []annotators, int numberOfCases, UimaAsBaseCallbackListener callbackListener
//		client.processCollection(
//				CollectionReaderFactory.createCollectionReader(
//						TestReader.class, 
//						TestReader.PARAM_MY_FANCY_PARAM, new String[]{"das ist ein param","hier noch einer"},
//						TestReader.PARAM_MY_FANCY_PARAM_2,42),
//				Language.de,
//				new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTLemma", "MarMoTTagger"},
//				10,
//				new UimaAsBaseCallbackListener() {
//					@Override
//					public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
//						super.entityProcessComplete(aCas, aStatus);
//						System.out.println(XmlFormatter.getPrettyString(aCas));
//						try {
//							Collection<CategoryCoveredTagged> ddcs = JCasUtil.select(aCas.getJCas(), CategoryCoveredTagged.class);
//							Iterator<CategoryCoveredTagged> i = ddcs.iterator();
//							if (i.hasNext()) {
//								CategoryCoveredTagged lCategoryCoveredTagged = i.next();
//								System.out.println(lCategoryCoveredTagged.getBegin()+"-"+lCategoryCoveredTagged.getEnd()+"\t"+lCategoryCoveredTagged.getValue());
//							}
//
//						} catch (CASException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//
//					@Override
//					public void collectionProcessComplete(EntityProcessStatus aStatus) {
//						super.collectionProcessComplete(aStatus);
//						System.out.println("collection complete");
//					};
//				});
//	}


//	@Test
	public void testDDC() throws Exception{
		CAS inputCas = JCasFactory.createJCas().getCas();
		inputCas.setDocumentLanguage("de");
		inputCas.setDocumentText("Das ist kein Test.");

		TextImagerClient client = new TextImagerClient();
		client.setConfigFile("src/main/resources/services.xml");
		CAS output = client.process(inputCas, new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTTagger", "MarMoTLemma"});
		assertEquals("de", output.getDocumentLanguage());
		assertEquals("Das", JCasUtil.select(output.getJCas(), Token.class).iterator().next().getCoveredText());
		System.out.println(XmlFormatter.getPrettyString(output));
	}


//	@Test
	public void testCASInput() throws Exception{
		CAS inputCas = JCasFactory.createJCas().getCas();
		inputCas.setDocumentLanguage("de");
		inputCas.setDocumentText("Das ist ein Test.");

		TextImagerClient client = new TextImagerClient();
		CAS output = client.process(inputCas, new String[]{"BreakIteratorSegmenter"});
		assertEquals("de", output.getDocumentLanguage());
		assertEquals("Das", JCasUtil.select(output.getJCas(), Token.class).iterator().next().getCoveredText());
	}

	@Test
	public void testSingleAnnotator() throws Exception{
		TextImagerClient client = new TextImagerClient();
		CAS output = client.process("Das ist ein Test.", new String[]{"LanguageIdentification"});
		assertEquals("de", output.getDocumentLanguage());
	}

	@Test
	public void testWrtonAnnotator() throws Exception{
		TextImagerClient client = new TextImagerClient();
		CAS output = client.process("Das ist ein Test.", new String[]{"LanguageIdentification"});
		assertEquals("de", output.getDocumentLanguage());
	}

//	@Test
	public void testSimplePipeline() throws Exception{
		TextImagerClient client = new TextImagerClient();
		CAS output = client.process("Das ist ein Test.", new String[]{"BreakIteratorSegmenter"});
		assertEquals("de", output.getDocumentLanguage());
		assertEquals(5, JCasUtil.select(output.getJCas(), Token.class).size());
		assertEquals("Das", JCasUtil.select(output.getJCas(), Token.class).iterator().next().getCoveredText());

		CAS output3 = client.process("Das ist ein Test.",  new String[]{"BreakIteratorSegmenter","LanguageIdentification"});
		assertEquals("de", output3.getDocumentLanguage());
		assertEquals(5, JCasUtil.select(output3.getJCas(), Token.class).size());
		assertEquals("Das", JCasUtil.select(output3.getJCas(), Token.class).iterator().next().getCoveredText());

		CAS output1 = client.process("Das ist ein Test.", "HucomputeLanguageDetection,   BreakIteratorSegmenter,    ");
		assertEquals("de", output1.getDocumentLanguage());
		assertEquals(5, JCasUtil.select(output1.getJCas(), Token.class).size());
		assertEquals("Das", JCasUtil.select(output1.getJCas(), Token.class).iterator().next().getCoveredText());

		CAS output2 = client.process("Das ist ein Test.", "BreakIteratorSegmenter,LanguageIdentification");
		assertEquals("de", output2.getDocumentLanguage());
		assertEquals(5, JCasUtil.select(output2.getJCas(), Token.class).size());
		assertEquals("Das", JCasUtil.select(output2.getJCas(), Token.class).iterator().next().getCoveredText());
	}

	@Test
	public void testSingleFileAnnotator() throws Exception{
		TextImagerClient client = new TextImagerClient();
		CAS output = client.process(new File("src/test/resources/testfile.txt"), new String[]{"LanguageIdentification"});
		assertEquals("de", output.getDocumentLanguage());
	}

//	@Test
	public void testSimpleFilePipeline() throws Exception{
		TextImagerClient client = new TextImagerClient();
		CAS output = client.process(new File("src/test/resources/testfile.txt"), new String[]{"BreakIteratorSegmenter"});
		assertEquals("de", output.getDocumentLanguage());
		assertEquals(5, JCasUtil.select(output.getJCas(), Token.class).size());
		assertEquals("Das", JCasUtil.select(output.getJCas(), Token.class).iterator().next().getCoveredText());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSimpleFileExtensionException() throws Exception{
		TextImagerClient client = new TextImagerClient();
		client.process(new File("src/test/resources/exception.xy"), new String[]{"BreakIteratorSegmenter"});
	}

	@Test
	public void testCollectionProcess() throws Exception{
		TextImagerClient client = new TextImagerClient();
		List<CAS> output = client.processCollection(new File("src/test/resources/collectionTest"), IOFormat.TXT, TextImagerOptions.Language.de, new String[]{"BreakIteratorSegmenter","HucomputeLanguageDetection"}, 10);
		assertEquals(14, output.size());
		assertEquals("de", output.get(1).getDocumentLanguage());
		assertEquals(5, JCasUtil.select(output.get(1).getJCas(), Token.class).size());
		assertEquals("Das", JCasUtil.select(output.get(1).getJCas(), Token.class).iterator().next().getCoveredText());
	}

	@Test
	public void testCollectionProcessWithOutput() throws Exception{
		TextImagerClient client = new TextImagerClient();
		File outputFolder = new File("src/test/resources/collectionTxtTestOutput");
		FileUtils.deleteDirectory(outputFolder);
		client.processCollection(new File("src/test/resources/collectionTxtTest"), IOFormat.TXT, TextImagerOptions.Language.de, new String[]{"BreakIteratorSegmenter","HucomputeLanguageDetection"}, IOFormat.CONLL2000,outputFolder.getPath());
		File[]outputFiles = outputFolder.listFiles();
		assertEquals(2, outputFiles.length);
		assertEquals("ein _ O", FileUtils.readLines(new File(outputFolder,"test1.txt.conll")).get(2));

		client.processCollection(new File("src/test/resources/collectionTxtTest"), IOFormat.TXT, TextImagerOptions.Language.de, new String[]{"BreakIteratorSegmenter","HucomputeLanguageDetection"}, IOFormat.XMI,outputFolder.getPath()+"XMI");
		File[]outputFilesXMI = new File(outputFolder.getAbsolutePath()+"XMI").listFiles();
		assertEquals(3, outputFilesXMI.length);
		
		FileUtils.deleteDirectory(outputFolder);
		FileUtils.deleteDirectory(new File(outputFolder.getAbsolutePath()+"XMI"));
	}

	//	@Test
	//	public void testCollectionProcessCustomReader() throws Exception{
	//		//Test txt collection
	//		TextImagerClient client = new TextImagerClient();
	//		client.processCollection(
	//				CollectionReaderFactory.createCollectionReader(TextReader.class, TextReader.PARAM_PATH,"src/test/resources/collectionTest",TextReader.PARAM_LANGUAGE,Language.de,TextReader.PARAM_PATTERNS,"[+]**/*.txt"),
	//				Language.de,
	//				new String[]{"BreakIteratorSegmenter","HucomputeLanguageDetection"},10,new UimaAsBaseCallbackListener() {
	//				});
	//
	//		//		client.processCollection(
	//		////				new Resoucecollectionre,
	//		//				Language.de,
	//		//				new String[]{"BreakIteratorSegmenter","HucomputeLanguageDetection"},10,new UimaAsBaseCallbackListener() {
	//		//				});
	//		//		assertEquals(14, output.size());
	//		//		assertEquals("de", output.get(1).getDocumentLanguage());
	//		//		assertEquals(5, JCasUtil.select(output.get(1).getJCas(), Token.class).size());
	//		//		assertEquals("Das", JCasUtil.select(output.get(1).getJCas(), Token.class).iterator().next().getCoveredText());	
	//
	//
	//	}
}
