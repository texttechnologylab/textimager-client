package org.hucompute.textimager.client;

import java.io.File;

import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.TextImagerOptions.Language;
import org.hucompute.textimager.util.XmlFormatter;
public class TmpTest {

	public static void main(String[] args) throws ResourceInitializationException, Exception {
		TextImagerClient client = new TextImagerClient();
		System.out.println(XmlFormatter.getPrettyString(client.process("This is a test by Barack Obama.", new String[]{"EuroWordNetTagger"},"en")));
		
	}
}
