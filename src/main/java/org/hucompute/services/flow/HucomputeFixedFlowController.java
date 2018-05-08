/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.hucompute.services.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.fit.component.JCasFlowController_ImplBase;
import org.apache.uima.flow.FinalStep;
import org.apache.uima.flow.Flow;
import org.apache.uima.flow.FlowControllerContext;
import org.apache.uima.flow.JCasFlow_ImplBase;
import org.apache.uima.flow.ParallelStep;
import org.apache.uima.flow.SimpleStep;
import org.apache.uima.flow.Step;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Simple FlowController that invokes components in a fixed sequence.
 */
public class HucomputeFixedFlowController extends JCasFlowController_ImplBase{
	final static Logger logger = Logger.getLogger(HucomputeFixedFlowController.class);


	public static final String PARAM_ACTION_AFTER_CAS_MULTIPLIER = "ActionAfterCasMultiplier";

	public static final String PARAM_ALLOW_CONTINUE_ON_FAILURE = "AllowContinueOnFailure";

	public static final String PARAM_FLOW = "Flow";

	private static final int ACTION_CONTINUE = 0;

	private static final int ACTION_STOP = 1;

	private static final int ACTION_DROP = 2;

	private static final int ACTION_DROP_IF_NEW_CAS_PRODUCED = 3;

	//	private ArrayList mSequence;

	private int mActionAfterCasMultiplier;

	private Set mAEsAllowingContinueOnFailure = new HashSet();

	private HashMap<String,ArrayList>sequences = new HashMap<>();



	public void initialize(FlowControllerContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		JSONObject json = new JSONObject(((String)aContext.getConfigParameterValue(PARAM_FLOW)).replace("=", ":"));

		Iterator<?> keys = json.keys();
		while(keys.hasNext()){
			String key = (String)keys.next();
			JSONArray objects = json.getJSONArray(key);
			ArrayList<Step> sequence = new ArrayList<>();
			for(int i = 0; i < objects.length(); i++){
				JSONArray array = objects.getJSONArray(i);
				if(array.length() == 1){
					sequence.add(new SimpleStep(array.getString(0)));
				}else{

					List<String> myList = new ArrayList<>();
					for(int j = 0; j<array.length();j++){
						myList.add(array.getString(j));
					}
					sequence.add(new ParallelStep(myList));
				}
			}
			sequences.put(key, sequence);
		}


		String[] aeKeysAllowingContinue = (String[])aContext
				.getConfigParameterValue(PARAM_ALLOW_CONTINUE_ON_FAILURE);
		if (aeKeysAllowingContinue != null) {
			mAEsAllowingContinueOnFailure.addAll(Arrays.asList(aeKeysAllowingContinue));
		}



	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.uima.flow.CasFlowController_ImplBase#computeFlow(org.apache.uima.cas.CAS)
	 */
	public Flow computeFlow(JCas aCAS) throws AnalysisEngineProcessException {
		logger.info("inner");
		return new FixedFlowObject(0,aCAS.getDocumentLanguage());
	}

	//	/* (non-Javadoc)
	//	 * @see org.apache.uima.flow.FlowController_ImplBase#addAnalysisEngines(java.util.Collection)
	//	 */
	//	public synchronized void addAnalysisEngines(Collection aKeys) {
	//		// Append new keys as a ParallelStep at end of Sequence
	//		// This is just an example of what could be done.
	//		// Note that in general, a "Collection" is unordered
	//		mSequence.add(new ParallelStep(new ArrayList(aKeys)));
	//	}

	//	/* (non-Javadoc)
	//	 * @see org.apache.uima.flow.FlowController_ImplBase#removeAnalysisEngines(java.util.Collection)
	//	 */
	//	public synchronized void removeAnalysisEngines(Collection aKeys) throws AnalysisEngineProcessException {
	//		// Remove keys from Sequence ... replace with null so step indices are still valid
	//		for (int i = 0; i < mSequence.size(); ++i) {
	//			Step step = (Step)mSequence.get(i);
	//			if (step instanceof SimpleStep && aKeys.contains(((SimpleStep)step).getAnalysisEngineKey())) {
	//				mSequence.set(i, null);
	//			}
	//			else if (step instanceof ParallelStep) {
	//				Collection keys = new ArrayList(((ParallelStep)step).getAnalysisEngineKeys());
	//				keys.removeAll(aKeys);
	//				if (keys.isEmpty()) {
	//					mSequence.set(i, null);
	//				}
	//				else {
	//					mSequence.set(i, new ParallelStep(keys));
	//				}
	//			}
	//		}
	//	}

	class FixedFlowObject extends JCasFlow_ImplBase{
		private int currentStep;

		private boolean wasPassedToCasMultiplier = false;

		private String language;

		/**
		 * Create a new fixed flow starting at step <code>startStep</code> of the fixed sequence.
		 * 
		 * @param startStep
		 *          index of mSequence to start at
		 */
		//		public FixedFlowObject(int startStep) {
		//			this(startStep, false,null);
		//		}

		/**
		 * Create a new fixed flow starting at step <code>startStep</code> of the fixed sequence.
		 * 
		 * @param startStep
		 *          index of mSequence to start at
		 * @param internallyCreatedCas
		 *          true to indicate that this Flow object is for a CAS that was produced by a
		 *          CasMultiplier within this aggregate. Such CASes area allowed to be dropped and not
		 *          output from the aggregate.
		 * 
		 */
		public FixedFlowObject(int startStep, String language) {
			currentStep = startStep;
			this.language = language;
		}



		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.uima.flow.Flow#next()
		 */
		public Step next() throws AnalysisEngineProcessException {

			if (wasPassedToCasMultiplier) {
				return new FinalStep(true);
			}

			Step nextStep;
			synchronized (HucomputeFixedFlowController.this) {
				if(!language.equals("x-unspecified")){
					//Heist letztes element in der pipe deswegen kann ausgegeben werden.
					if (currentStep >= sequences.get(language).size()) {
						return new FinalStep(); // this CAS has finished the sequence
					}

					nextStep = (Step) sequences.get(language).get(currentStep++);
				}
				else
				{
					if (currentStep >= sequences.entrySet().iterator().next().getValue().size()) {
						return new FinalStep(); // this CAS has finished the sequence
					}
					nextStep = (Step) sequences.entrySet().iterator().next().getValue().get(currentStep++);
				}


			}

			if (stepContainsCasMultiplier(nextStep)){
				wasPassedToCasMultiplier = true;
			}

			try{
				logger.info(language+" - "+((SimpleStep)nextStep).getAnalysisEngineKey());
			}catch (Exception e) {
				logger.info(language+" - "+((ParallelStep)nextStep).getAnalysisEngineKeys());
			}

			// now send the CAS to the next AE(s) in sequence.
			return nextStep;
		}

		/**
		 * @param nextStep
		 * @return
		 */
		private boolean stepContainsCasMultiplier(Step nextStep) {
			if (nextStep instanceof SimpleStep) {
				AnalysisEngineMetaData md = (AnalysisEngineMetaData) getContext()
						.getAnalysisEngineMetaDataMap().get(((SimpleStep)nextStep).getAnalysisEngineKey());
				return md != null && md.getOperationalProperties() != null &&
						md.getOperationalProperties().getOutputsNewCASes();
			}
			else if (nextStep instanceof ParallelStep) {
				Iterator iter = ((ParallelStep)nextStep).getAnalysisEngineKeys().iterator();
				while (iter.hasNext()) {
					String key = (String)iter.next();
					AnalysisEngineMetaData md = (AnalysisEngineMetaData) getContext()
							.getAnalysisEngineMetaDataMap().get(key);
					if (md != null && md.getOperationalProperties() != null &&
							md.getOperationalProperties().getOutputsNewCASes())
						return true;
				}
				return false;
			}
			else
				return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.uima.flow.CasFlow_ImplBase#newCasProduced(CAS, String)
		 */
		@Override
		public synchronized Flow newCasProduced(JCas newCas, String producedBy) throws AnalysisEngineProcessException {
			// start the new output CAS from the next node after the CasMultiplier that produced it
			int i = 0;
			if(sequences.containsKey(newCas.getDocumentLanguage())){
				while (!stepContains((Step)sequences.get(newCas.getDocumentLanguage()).get(i), producedBy))
					i++;
				return new FixedFlowObject(i + 1,newCas.getDocumentLanguage());
			}
			else{
				while (!stepContains((Step)sequences.get(sequences.keySet().iterator().next()).get(i), producedBy))
					i++;
				return new FixedFlowObject(i + 1,sequences.keySet().iterator().next());
			}
		}

		/**
		 * @param object
		 * @param producedBy
		 * @return
		 */
		private boolean stepContains(Step step, String producedBy) {
			if (step instanceof SimpleStep) {
				return ((SimpleStep)step).getAnalysisEngineKey().equals(producedBy);
			}
			else if (step instanceof ParallelStep) {
				Iterator iter = ((ParallelStep)step).getAnalysisEngineKeys().iterator();
				while (iter.hasNext()) {
					String key = (String)iter.next();
					if (key.equals(producedBy))
						return true;
				}
				return false;
			}
			else
				return false;
		}

		/* (non-Javadoc)
		 * @see org.apache.uima.flow.CasFlow_ImplBase#continueOnFailure(java.lang.String, java.lang.Exception)
		 */
		public boolean continueOnFailure(String failedAeKey, Exception failure) {
			return mAEsAllowingContinueOnFailure.contains(failedAeKey);
		}
	}

	//	@Override
	//	public Flow computeFlow(JCas aJCas) throws AnalysisEngineProcessException {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}
}