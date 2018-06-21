package org.hucompute.textimager.client;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

public class TextImagerClientCLITest {

	@Test
	public void testProcessText() throws ResourceInitializationException, Exception{
		//Test simple CLI
		String[]args = new String[]{
				"-I","This is a test.",
				"-p","StanfordSegmenter,StanfordPosTagger", 
				"-o","testOutput.xmi"
		};
		TextImagerClientCLI.main(args);
		String output = FileUtils.readFileToString(new File("testOutput.xmi"),"UTF-8");
		String outputGold = FileUtils.readFileToString(new File("src/test/resources/testCLI/outputProcessText.xmi"),"UTF-8");
		assertEquals(output, outputGold);

		//Test if file exists
		try{
			TextImagerClientCLI.main(args);
		}catch(Exception e){
			assertEquals(e.getMessage(), "output already exists. If you want to overwrite file add the option: --output-overwrite");
		}

		args = new String[]{
				"-I","This is a test.",
				"-p","StanfordSegmenter,StanfordPosTagger", 
				"-o","testOutput.xmi",
				"--output-overwrite"
		};		
		TextImagerClientCLI.main(args);
		new File("testOutput.xmi").delete();

		
	}
	
	@Test
	public void testProcessTextConll() throws Exception{
		//Test conll output
		String[]argsConll = new String[]{
				"-I","This is a test.",
				"-p","StanfordSegmenter,StanfordPosTagger", 
				"-o","testOutput.conll",
				"--output-format","CONLL2009"
		};
		TextImagerClientCLI.main(argsConll);
		String outputConll = FileUtils.readFileToString(new File("testOutput.conll"),"UTF-8");
		String outputGoldConll = FileUtils.readFileToString(new File("src/test/resources/testCLI/outputProcessText.conll"),"UTF-8");
		assertEquals(outputConll.trim(), outputGoldConll.trim());
		new File("testOutput.conll").delete();
	}
}
