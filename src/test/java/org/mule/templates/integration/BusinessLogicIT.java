/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.modules.siebel.api.model.response.CreateResult;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.util.UUID;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is to validate the correct behavior of the Mule Template that make calls to external systems.
 * 
 * The test will invoke the batch process and afterwards check that the accounts had been correctly created and that the ones that should be filtered are not in
 * the destination sand box.
 * 
 * The test validates that no account will get sync as result of the integration.
 * 
 * @author damiansima
 * @author MartinZdila
 */
public class BusinessLogicIT extends AbstractTemplatesTestCase {

	private static final Logger log = LogManager.getLogger(BusinessLogicIT.class);
	private static final String KEY_ID = "Id";
	private static final String KEY_NAME = "Name";
	private static final String KEY_WEBSITE = "Website";
	private static final String KEY_PHONE = "Phone";
	private static final String KEY_NUMBER_OF_EMPLOYEES = "NumberOfEmployees";
	private static final String KEY_CITY = "City";

	protected static final int TIMEOUT_SEC = 120;
	protected static final String TEMPLATE_NAME = "account-migration";

	protected SubflowInterceptingChainLifecycleWrapper retrieveAccountFromSalesforceFlow;
	private List<Map<String, Object>> createdAccountsInSiebel = new ArrayList<Map<String, Object>>();
	private BatchTestHelper helper;

	@Rule
	public DynamicPort port = new DynamicPort("http.port");
	
	@Before
	public void setUp() throws Exception {
		helper = new BatchTestHelper(muleContext);
		// Flow to retrieve accounts from target system after sync in g
		retrieveAccountFromSalesforceFlow = getSubFlow("retrieveAccountFromSalesforceFlow");
		retrieveAccountFromSalesforceFlow.initialise();
	
		createTestDataInSandBox();
	}

	@After
	public void tearDown() throws Exception {
		deleteTestAccountsFromSiebel(createdAccountsInSiebel);
		deleteTestAccountsFromSalesforce(createdAccountsInSiebel);
	}

	@Test
	public void testMainFlow() throws Exception {
		runFlow("mainFlow");
	
		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();
	
		Map<String, Object> payload0 = invokeRetrieveFlow(retrieveAccountFromSalesforceFlow, createdAccountsInSiebel.get(0));
		Assert.assertNotNull("The account 0 should have been sync but is null", payload0);
		Assert.assertEquals("The account 0 should have been sync (Website)", createdAccountsInSiebel.get(0).get(KEY_WEBSITE), payload0.get(KEY_WEBSITE));
		Assert.assertEquals("The account 0 should have been sync (Number of Employees)", createdAccountsInSiebel.get(0).get(KEY_NUMBER_OF_EMPLOYEES).toString(), payload0.get(KEY_NUMBER_OF_EMPLOYEES).toString());
		Assert.assertEquals("The account 0 should have been sync (Phone)", createdAccountsInSiebel.get(0).get(KEY_PHONE), payload0.get(KEY_PHONE));
		
		Map<String, Object>  payload1 = invokeRetrieveFlow(retrieveAccountFromSalesforceFlow, createdAccountsInSiebel.get(1));
		Assert.assertNotNull("The account 1 should have been sync but is null", payload1);
		Assert.assertEquals("The account 1 should have been sync (Website)", createdAccountsInSiebel.get(1).get(KEY_WEBSITE), payload1.get(KEY_WEBSITE));
		Assert.assertEquals("The account 1 should have been sync (Number of Employees)", createdAccountsInSiebel.get(1).get(KEY_NUMBER_OF_EMPLOYEES).toString(), payload1.get(KEY_NUMBER_OF_EMPLOYEES).toString());
		Assert.assertEquals("The account 1 should have been sync (Phone)", createdAccountsInSiebel.get(1).get(KEY_PHONE), payload1.get(KEY_PHONE));
		
		Map<String, Object>  payload2 = invokeRetrieveFlow(retrieveAccountFromSalesforceFlow, createdAccountsInSiebel.get(2));
		Assert.assertNull("The account 2 should have not been sync", payload2);
	}

	
	private void createTestDataInSandBox() throws MuleException, Exception {
		
		
		String uniqueSuffix = "_" + TEMPLATE_NAME + "_" + UUID.getUUID();
		
		// Create object in target system to be updated
		Map<String, Object> sfdcAccountToBeUpdated = new HashMap<String, Object>();
		sfdcAccountToBeUpdated.put(KEY_NAME, "Test_AccToUpdate" + uniqueSuffix);
		sfdcAccountToBeUpdated.put(KEY_WEBSITE, "http://example.com");
		sfdcAccountToBeUpdated.put(KEY_PHONE, "112");
		sfdcAccountToBeUpdated.put(KEY_NUMBER_OF_EMPLOYEES, 72000);
		List<Map<String, Object>> createdAccountInSalesforce = new ArrayList<Map<String, Object>>();
		createdAccountInSalesforce.add(sfdcAccountToBeUpdated);
	
		// create SFDC account to be updated later
		SubflowInterceptingChainLifecycleWrapper createAccountInSalesforceFlow = getSubFlow("createAccountsInSalesforceFlow");
		createAccountInSalesforceFlow.initialise();
		createAccountInSalesforceFlow.process(getTestEvent(createdAccountInSalesforce, MessageExchangePattern.REQUEST_RESPONSE));
		Thread.sleep(1001); // this is here to prevent equal LastModifiedDate
		
		// Create accounts in source system to be or not to be synced
		// This account should be synced
		Map<String, Object> siebelAccountToInsert = new HashMap<String, Object>();
		siebelAccountToInsert.put(KEY_NAME, "Test_AccToInsert" + uniqueSuffix);
		siebelAccountToInsert.put(KEY_WEBSITE, "http://acme.org");
		siebelAccountToInsert.put(KEY_PHONE, "123");
		siebelAccountToInsert.put(KEY_NUMBER_OF_EMPLOYEES, 80000);
		siebelAccountToInsert.put(KEY_CITY, "Las Vegas");
		siebelAccountToInsert.put("Street", "street0A" + uniqueSuffix);
		createdAccountsInSiebel.add(siebelAccountToInsert);
				
		// This account should be synced (update)
		Map<String, Object> siebelAccountToUpdate = new HashMap<String, Object>();
		siebelAccountToUpdate.put(KEY_NAME,  sfdcAccountToBeUpdated.get(KEY_NAME));
		siebelAccountToUpdate.put(KEY_WEBSITE, "http://example.edu");
		siebelAccountToUpdate.put(KEY_PHONE, "911");
		siebelAccountToUpdate.put(KEY_NUMBER_OF_EMPLOYEES, 91000);
		siebelAccountToUpdate.put(KEY_CITY, "Jablonica");
		siebelAccountToUpdate.put("Street", "street1A" + uniqueSuffix);
		createdAccountsInSiebel.add(siebelAccountToUpdate);

		// This account should not be synced because of employees
		Map<String, Object> siebelAccountNotToSync = new HashMap<String, Object>();
		siebelAccountNotToSync.put(KEY_NAME, "Test_AccNotToInsert" + uniqueSuffix);
		siebelAccountNotToSync.put(KEY_WEBSITE, "http://energy.edu");
		siebelAccountNotToSync.put(KEY_PHONE, "333");
		siebelAccountNotToSync.put(KEY_NUMBER_OF_EMPLOYEES, 1400);
		siebelAccountNotToSync.put(KEY_CITY, "London");
		siebelAccountNotToSync.put("Street", "street2A" + uniqueSuffix);
		createdAccountsInSiebel.add(siebelAccountNotToSync);

		MuleEvent event = runFlow("createAccountsInSiebelFlow", createdAccountsInSiebel);
//		SubflowInterceptingChainLifecycleWrapper createAccountInSiebelFlow = getSubFlow("createAccountsInSiebelFlow");
//		createAccountInSiebelFlow.setFlowConstruct(getTestService());
//		createAccountInSiebelFlow.initialise();
//
//		MuleEvent event = createAccountInSiebelFlow.process(getTestEvent(createdAccountsInSiebel, MessageExchangePattern.REQUEST_RESPONSE));
		
		List<?> results = (List<?>) event.getMessage().getPayload();
		
		// assign Siebel-generated IDs
		for (int i = 0; i < createdAccountsInSiebel.size(); i++) {
			createdAccountsInSiebel.get(i).put(KEY_ID, ((CreateResult) results.get(i)).getCreatedObjects().get(0));
		}

		log.info("Results after adding: " + createdAccountsInSiebel.toString());
	}

	private void deleteTestAccountsFromSiebel(List<Map<String, Object>> createdAccountsInSiebel) throws Exception {
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromSiebelFlow = getSubFlow("deleteAccountsFromSiebelFlow");
		deleteAccountFromSiebelFlow.initialise();
		deleteTestEntityFromSandBox(deleteAccountFromSiebelFlow, createdAccountsInSiebel);
	}

	private void deleteTestAccountsFromSalesforce(List<Map<String, Object>> createdAccountsInA) throws Exception {
		List<Map<String, Object>> createdAccountsInSalesforce = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> c : createdAccountsInA) {
			Map<String, Object> account = invokeRetrieveFlow(retrieveAccountFromSalesforceFlow, c);
			if (account != null) {
				createdAccountsInSalesforce.add(account);
			}
		}
		SubflowInterceptingChainLifecycleWrapper deleteAccountFromSalesforceFlow = getSubFlow("deleteAccountsFromSalesforceFlow");
		deleteAccountFromSalesforceFlow.initialise();
		deleteTestEntityFromSandBox(deleteAccountFromSalesforceFlow, createdAccountsInSalesforce);
	}
	
	private MuleEvent deleteTestEntityFromSandBox(SubflowInterceptingChainLifecycleWrapper deleteFlow, List<Map<String, Object>> entitities) throws Exception {
		List<String> idList = new ArrayList<String>();
		for (Map<String, Object> c : entitities) {
			idList.add(c.get(KEY_ID).toString());
		}
		return deleteFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

}
