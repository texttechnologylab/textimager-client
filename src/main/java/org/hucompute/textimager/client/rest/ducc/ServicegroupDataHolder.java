package org.hucompute.textimager.client.rest.ducc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement(name="webserviceGroup")
public class ServicegroupDataHolder {
	
	@XmlAttribute
	String name;

	
	@XmlElement( name="webservice" )
	List<ServiceDataholder> webservices = new ArrayList<ServiceDataholder>();

//	public List<ServiceDataholder> getSWebservices(){
//		return webservices;
//	}
//	
//	public void setSWebservices(List<ServiceDataholder> webservices){
//		this.webservices = webservices;
//	}
//
//	public String getSName() {
//		return name;
//	}
//	
//	public void setSName(String name){
//		this.name = name;
//	}

}
