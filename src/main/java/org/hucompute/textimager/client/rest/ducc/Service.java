package org.hucompute.textimager.client.rest.ducc;


import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.GsonBuilder;

public class Service {
	final static Logger logger = Logger.getLogger(Service.class);

	private String name;
	private String packageName;
	private String brokerURL;
	private List<String> supportedLanguages;
	private List<String> input;
	private List<String> output;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getBrokerURL() {
		return brokerURL;
	}

	public void setBrokerURL(String brokerURL) {
		this.brokerURL = brokerURL;
	}

	public List<String> getSupportedLanguages() {
		return supportedLanguages;
	}

	public void setSupportedLanguages(List<String> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}

	public List<String> getInput() {
		return input;
	}

	public void setInput(List<String> input) {
		this.input = input;
	}

	public List<String> getOutput() {
		return output;
	}

	public void setOutput(List<String> output) {
		this.output = output;
	}

	//	"name": "StanfordParser",
	//	"packageName": "de.tudarmstadt.ukp.dkpro.core.stanfordnlp",
	//	"brokerURL":"tcp://alba.hucompute.org:61617",
	//	"supportedLanguages":["ar","de","en","es","fr","zh"],
	//	"input":["pos","sentence","token"],
	//	"output":["constituent"]
	//
	//	public String getName() {
	//		return name;
	//	}
	//
	//	public void setName(String name) {
	//		this.name = name;
	//	}
	//	
	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}
}