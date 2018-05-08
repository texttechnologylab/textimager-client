package org.hucompute.textimager.client;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;

import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.services.type.CategoryCoveredTagged;
import org.hucompute.textimager.client.TextImagerClient;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
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
		

		TextImagerClient client = new TextImagerClient();
		client.setConfigFile("src/main/resources/services.xml");
		CAS output = client.process("Das ist ein Test. Das ist ein Test.",  new String[]{"LanguageToolSegmenter", "ParagraphSplitter", "MarMoTLemma", "MarMoTTagger", "FastTextDDC2LemmaNoPunctPOSNoFunctionwordsWithCategoriesService", "FastTextDDC3LemmaNoPunctPOSNoFunctionwordsWithCategoriesService", "FastTextDDCMulService"});
		System.out.println(XmlFormatter.getPrettyString(output));
	}

}
