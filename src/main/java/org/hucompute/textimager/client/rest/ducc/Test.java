package org.hucompute.textimager.client.rest.ducc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Test {

	public static void main(String[] args) throws JAXBException, JsonSyntaxException, IOException, XPathExpressionException, SAXException, ParserConfigurationException {
		
//
//		ConfigDataholder holder = new ConfigDataholder("src/main/resources/surls_remote.xml");
//		System.out.println(holder.getWebservice(Arrays.asList(new String[]{"token"})));
//		holder.printWebservices();
//		
//		JAXBContext context = JAXBContext.newInstance(Services.class);
//		Unmarshaller um = context.createUnmarshaller();
//		Services services = (Services)um.unmarshal(new File("src/main/resources/surls_remote.xml"));
//		
//		for (Service service : services.getWebservices()) {
//			System.out.println(service);
//		}
		Gson gson = new Gson();
		Service[] services =  gson.fromJson(FileUtils.readFileToString(new File("src/main/resources/services.json")), Service[].class);
//		HashSet<String>annotations = new HashSet<>();
//		for (Service service : staff) {
//			System.out.println(annotations.addAll(service.getOutput()));
//		}
//		System.out.println(annotations);
		
		constructPipeline(services, "LanguageToolLemmatizer","StanfordSegmenter");
	}
	
	public static void constructPipeline(Service[]services, String language,String[]selected){
		
	}
	
	public static void constructPipeline(Service[]services, String...selected){
		System.out.println(findServicesByName(services, selected));
	}
	
	public static List<Service> findServicesByName(Service[]services, String... selected){
		HashSet<String>selectedList = new HashSet<>(Arrays.asList(selected));
		return Arrays.asList(services).stream().filter(x->selectedList.contains(x.getName())).collect(Collectors.toList());
	}
	
	

}
