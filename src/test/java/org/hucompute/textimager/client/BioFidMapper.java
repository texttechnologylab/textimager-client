package org.hucompute.textimager.client;


import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.texttechnologylab.annotation.ocr.OCRParagraph;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;

/**
 * Stanford Part-of-Speech tagger component.
 *
 */
@TypeCapability(
		inputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
		outputs = {"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})
public class BioFidMapper
extends JCasAnnotator_ImplBase
{
	@Override
	public void process(JCas aJCas)
			throws AnalysisEngineProcessException
	{
		for (OCRParagraph ocrParagraph: JCasUtil.select(aJCas, OCRParagraph.class)) {
			Paragraph paragraph = new Paragraph(aJCas, ocrParagraph.getBegin(), ocrParagraph.getEnd());
			paragraph.addToIndexes();
		}
	}
}
