package org.hucompute.textimager.client;

import org.apache.uima.cas.CAS;
import org.hucompute.textimager.util.XmlFormatter;

public class SimpleLocalhostTest {

	public static void main(String[] args) throws Exception {
		TextImagerClient client = new TextImagerClient();
		client.setConfigFile("src/main/resources/services_localhost.xml");
		CAS output = client.process("Das ist ein Test.", "BreakIteratorSegmenter","de");
		System.out.println(XmlFormatter.getPrettyString(output));
	}

}
