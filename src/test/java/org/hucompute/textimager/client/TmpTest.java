package org.hucompute.textimager.client;

import java.io.File;

import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.TextImagerOptions.Language;
import org.hucompute.textimager.util.XmlFormatter;
public class TmpTest {

	public static void main(String[] args) throws ResourceInitializationException, Exception {
		//		TextImagerClient client = new TextImagerClient();
		//		client.setConfigFile("src/main/resources/services.xml");
		//		client.processCollection(
		//				new File("src/test/resources/collectionTest"),
		//				IOFormat.TXT,
		//				TextImagerOptions.Language.de,
		//				new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTLemma", "MarMoTTagger","FastTextDDC3LemmaNoPunctPOSNoFunctionwordsWithCategoriesService"},
		//				10,
		//				new UimaAsBaseCallbackListener() {
		//					@Override
		//					public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
		//						// TODO Auto-generated method stub
		//						super.entityProcessComplete(aCas, aStatus);
		//						try {
		//							Collection<CategoryCoveredTagged> ddcs = JCasUtil.select(aCas.getJCas(), CategoryCoveredTagged.class);
		//							System.out.println(ddcs.size());
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

//		File outputFolder = new File("src/test/resources/wikiOutput");

		//		outputFolder.mkdirs();
		//		AnalysisEngineDescription casConsumer = AnalysisEngineFactory.createEngineDescription(
		//				MediawikiWriter.class,
		//				MediawikiWriter.PARAM_TARGET_LOCATION,outputFolder.getPath()
		//				);
		//		TextImagerClient client = new TextImagerClient();
		//		CAS output = client.process("Das ist ein Test.", new String[]{"LanguageToolSegmenter","LanguageToolLemmatizer"});
		//		System.out.println(XmlFormatter.getPrettyString(output));
//
		TextImagerClient client = new TextImagerClient();
//		client.setConfigFile("src/main/resources/services.xml");
//		client.process("Nos Petrus Dei et apostolice sedis gracia episcopus ecclesie Osiliensis, iudex et executor causarum causeque et causis ac partibus infrascriptis a gloriosissimo, serenissimo et invictissimo principe et domino, domino Frederico, divina favente clemencia Romanorum imperatore, semper Augusto etc., unacum reverendo patre, domino abbate in Padyß, Cisterciensis ordinis.", "CLTKSegmenter");
//		CAS cas = client.process("Das ist ein Test aus Frankfurt am Main.","LanguageToolSegmenter,StanfordPosTagger,ParagraphSplitter,MarMoTLemma,EuroWordNetTagger");
//		System.out.println(XmlFormatter.getPrettyString(cas));
		
		System.out.println(XmlFormatter.getPrettyString(client.process("Das ist ein Test.", new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTLemma", "MarMoTTagger", "FastTextDDC2LemmaNoPunctPOSNoFunctionwordsWithCategoriesTextImagerService"},"de")));
		
//		client.processCollection(
//				new File("/resources/public/stoeckel/BIOfid/BIOfid_XMI_27.01.2020"), 
//				IOFormat.XMI, 
//				Language.de, 
//				new String[]{"CoreNlpPosTagger"}, 
//				IOFormat.XMI, 
//				"/home/ahemati/xmi/output",
//				true);
//		de______ParagraphSplitter___BreakIteratorSegmenter_TagMeLocalAnnotator___MateLemmatizer___WikidataHyponyms___CoreNlpPosTagger___BIOfidGazetteer___HeidelTime___EuroWordNetTagger
		
		
		//		System.out.println(XmlFormatter.getPrettyString(client.process("This is a test.", new String[]{"StanfordSegmenter","StanfordPosTagger"})));		
//		client.process(new File("/home/ahemati/Downloads/CoNLL Files/UD_German/de-ud-dev.conllu"), "LanguageToolLemmatizer,MarMoTTagger");
//		client.processCollection(new File("/home/ahemati/Downloads/CoNLL Files/UD_German"), IOFormat.CONLLU, Language.de, new String[]{"HeidelTime"}, IOFormat.CONLLU,"test2");
//		System.out.println(cases.size());
//		for (CAS cas : cases) {
//			System.out.println(XmlFormatter.getPrettyString(cas));
//		}
		//		client.processCollection(TextImagerOptions.getReader(IOFormat.TXT, "src/test/resources/test", TextImagerOptions.Language.de),
		//				Language.de, 
		//				new String[]{"LanguageToolSegmenter","MarMoTLemma","MarMoTTagger"}, 
		//				TextImagerOptions.getWriter(IOFormat.CONLL2012, "test"));
		//		client.processCollection(new File("src/test/resources/collectionTestConll"), IOFormat.CONLL2009, Language.de, new String[]{"LanguageToolSegmenter"}, IOFormat.CONLL2000, "src/test/resources/collectionTestConllOutput");
		//		CAS cas = client.process("Das ist ein Test.", "SpaCySentenceSegmenter,SpaCyTokenizer,MarMoTLemma,MarMoTTagger");
		//		JCas cas = JCasFactory.createJCas();
		//		cas.setDocumentLanguage("en");
		//		cas.setDocumentText("This is a simple test.");
		//		AggregateBuilder builder = new AggregateBuilder();
		//		builder.add(createEngineDescription(UDPipeSegmenter.class));
		//		SimplePipeline.runPipeline(cas,builder.createAggregate());
		//		System.out.println(XmlFormatter.getPrettyString(cas));
		//		client.processCollection(new File("src/test/resources/collectionTestConll"), IOFormat.CONLL2009, Language.de, new String[]{"LanguageToolSegmenter"}, IOFormat.CONLL2002, "test");

		//		args = new String[]{
		//				"-I","This is a test.",
		//				"-p","StanfordSegmenter,StanfordPosTagger", 
		//				"-o","testOutput.conll",
		//				"--output-format","CONLL2009"
		//		};
		//		TextImagerClientCLI.main(args);

		//		String outputConll = FileUtils.readFileToString(new File("testOutput.xmi"),"UTF-8");
		//		String outputGoldConll = FileUtils.readFileToString(new File("src/test/resources/testCLI/outputProcessText.xmi"),"UTF-8");
		//		
		//		System.out.println(outputConll.trim().equals(outputGoldConll.trim()));
		//		System.out.println(outputGoldConll.trim().length());
		//		System.out.println(outputConll.trim().length());
		
//		String[]argsConll = new String[]{
//				"-i","src/test/resources/de-ud-train.txt",
//				"-p","LanguageToolSegmenter,StanfordPosTagger,LanguageToolSegmenter", 
//				"-o","our-de-ud-train2.conll",
//				"--output-format","CONLL2012"
//		};
//		TextImagerClientCLI.main(argsConll);
		
	}
}
