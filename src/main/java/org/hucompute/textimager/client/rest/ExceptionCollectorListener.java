package org.hucompute.textimager.client.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.aae.UimaASApplicationEvent.EventTrigger;
import org.apache.uima.aae.client.UimaASProcessStatus;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.EntityProcessStatus;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.codehaus.plexus.util.ExceptionUtils;


public class ExceptionCollectorListener extends UimaAsBaseCallbackListener{
		private Map<String, CasError> errors;
		
		public boolean hasErrors() {
			return !errors.isEmpty();
		}
		
		public Map<String, CasError> getErrors() {
			return errors;
		}
		
		public ExceptionCollectorListener() {
			super();
			errors = new HashMap<>();
		}
		
		@Override
		public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
			super.entityProcessComplete(aCas, aStatus);
			
			if (aStatus.isException()) {
				String docId = DocumentMetaData.get(aCas).getDocumentId();
				CasError casError = new CasError(docId, aStatus.getStatusMessage());
				
				for (Exception ex : aStatus.getExceptions()) {
					String name = "Unknown Error";
					String fullst = ExceptionUtils.getFullStackTrace(ex);
					
					ex.printStackTrace();
					
					// Provide specific error names on known problems...
					if (fullst.contains("Unable to load resource")) {
						name = "Service Language Error";
					}
					else if (fullst.contains("org.apache.uima.aae.error.UimaASProcessCasTimeout")) {
						name = "Service Unavailable Error";
					}
					
					casError.exceptions.put(name, fullst);
				}
				
				errors.put(docId, casError);
			}
		}

		@Override
		public void collectionProcessComplete(EntityProcessStatus aStatus) {
			super.collectionProcessComplete(aStatus);
			//System.out.println("collectionProcessComplete");
		};
		
		@Override
		public void onBeforeMessageSend(UimaASProcessStatus status) {
			super.onBeforeMessageSend(status);
			//System.out.println("onBeforeMessageSend");
		}

		@Override
		public void onBeforeProcessCAS(UimaASProcessStatus status, String nodeIP, String pid) {
			super.onBeforeProcessCAS(status, nodeIP, pid);
			//System.out.println("onBeforeProcessCAS");
		}

		@Override
		public void onBeforeProcessMeta(String nodeIP, String pid) {
			super.onBeforeProcessMeta(nodeIP, pid);
			//System.out.println("onBeforeProcessMeta");
		}

		@Override
		public void onUimaAsServiceExit(EventTrigger cause) {
			super.onUimaAsServiceExit(cause);
			//System.out.println("onUimaAsServiceExit");
		}

		@Override
		public void initializationComplete(EntityProcessStatus aStatus) {
			super.initializationComplete(aStatus);
			System.out.println(aStatus);
			//System.out.println("initializationComplete");
		}
		
	}