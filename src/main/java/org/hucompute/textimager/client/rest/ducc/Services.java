package org.hucompute.textimager.client.rest.ducc;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "webservices")
public class Services { 
    private List<Service> webservices;

    @XmlElement(name="webservice")
    public List<Service> getWebservices() {
        return webservices;
    }

    public void setWebservices(List<Service> webservices) {
        this.webservices = webservices;
    }
}