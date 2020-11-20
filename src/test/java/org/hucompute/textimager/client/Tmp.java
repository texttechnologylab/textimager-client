package org.hucompute.textimager.client;

import java.io.IOException;

import org.hucompute.textimager.client.rest.ducc.DUCCAPI;

public class Tmp {
	public static void main(String[] args) throws IOException {
		DUCCAPI duccapi = new DUCCAPI("src/main/resources/config_local.prop");
		duccapi.cancel(5525);
	}
}
