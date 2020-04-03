package org.hucompute.textimager.client.rest;

import java.util.HashMap;
import java.util.Map;

public class CasError {
	String docId;
	String status;
	Map<String, String> exceptions;

	public CasError(String docId, String status) {
		this.docId = docId;
		this.status = status;
		this.exceptions = new HashMap<>();
	}
}
