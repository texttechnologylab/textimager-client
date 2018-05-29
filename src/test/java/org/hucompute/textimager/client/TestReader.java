package org.hucompute.textimager.client;

import java.io.IOException;
import java.util.Arrays;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.hucompute.textimager.util.XmlFormatter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class TestReader extends JCasCollectionReader_ImplBase {

	/**
	 * Name of configuration parameter that contains the character encoding used by the input files.
	 */
	public static final String PARAM_MY_FANCY_PARAM = "ID_FOR_MY_FANCY_PARAM";
	@ConfigurationParameter(name = PARAM_MY_FANCY_PARAM, mandatory = true, defaultValue = "UTF-8")
	private String[] my_fancy_param;

	/**
	 * Name of configuration parameter that contains the character encoding used by the input files.
	 */
	public static final String PARAM_MY_FANCY_PARAM_2 = "ID_FOR_MY_FANCY_PARAM_2";
	@ConfigurationParameter(name = PARAM_MY_FANCY_PARAM_2, mandatory = true, defaultValue = "2")
	private int my_fancy_param_int;


	private int counter = 0;
	private int total = 2;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	};

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return counter < total;

	}

	@Override
	public Progress[] getProgress() {
		return new Progress[]{new ProgressImpl(counter, total, "documents")};
	}

	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
			counter++;
			//cas.reset();

			String lText = "Hallo ihr schönen Welten "+counter;

			DocumentMetaData docMetaData = DocumentMetaData.create(jCas);
			docMetaData.setDocumentTitle(Integer.toString(counter));
			docMetaData.setDocumentUri(Integer.toString(counter));
			docMetaData.setDocumentId(Integer.toString(counter));
			//		docMetaData.setLanguage("de");

			jCas.setDocumentLanguage("de");
			jCas.setDocumentText(lText);
	}

};
