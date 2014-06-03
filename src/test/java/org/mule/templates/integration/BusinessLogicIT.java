package org.mule.templates.integration;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.config.MuleProperties;
import org.mule.modules.siebel.api.model.response.CreateResult;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.NullPayload;
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
public class BusinessLogicIT extends FunctionalTestCase {

	private static final String KEY_ID = "Id";
	private static final String KEY_NAME = "Name";
	private static final String KEY_WEBSITE = "Website";
	private static final String KEY_PHONE = "Phone";
	private static final String KEY_NUMBER_OF_EMPLOYEES = "NumberOfEmployees";
	private static final String KEY_INDUSTRY = "Industry";
	private static final String KEY_CITY = "City";
	
	private static final String MAPPINGS_FOLDER_PATH = "./mappings";
	private static final String TEST_FLOWS_FOLDER_PATH = "./src/test/resources/flows/";
	private static final String MULE_DEPLOY_PROPERTIES_PATH = "./src/main/app/mule-deploy.properties";

	protected static final int TIMEOUT_SEC = 120;
	protected static final String TEMPLATE_NAME = "account-migration";

	protected SubflowInterceptingChainLifecycleWrapper retrieveAccountFromSalesforceFlow;
	private List<Map<String, Object>> createdAccountsInSiebel = new ArrayList<Map<String, Object>>();
	private BatchTestHelper helper;

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

//	@Ignore
	@Test
	public void testMainFlow() throws Exception {
		runFlow("mainFlow");
	
		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();
	
		Map<String, Object> payload0 = invokeRetrieveFlow(retrieveAccountFromSalesforceFlow, createdAccountsInSiebel.get(0));
		Assert.assertNotNull("The account 0 should have been sync but is null", payload0);
		Assert.assertEquals("The account 0 should have been sync (Website)", createdAccountsInSiebel.get(0).get(KEY_WEBSITE), payload0.get(KEY_WEBSITE));
		Assert.assertEquals("The account 0 should have been sync (Phone)", createdAccountsInSiebel.get(0).get(KEY_PHONE), payload0.get(KEY_PHONE));

		Map<String, Object>  payload1 = invokeRetrieveFlow(retrieveAccountFromSalesforceFlow, createdAccountsInSiebel.get(1));
		Assert.assertNotNull("The account 1 should have been sync but is null", payload1);
		Assert.assertEquals("The account 1 should have been sync (Website)", createdAccountsInSiebel.get(1).get(KEY_WEBSITE), payload1.get(KEY_WEBSITE));
		Assert.assertEquals("The account 1 should have been sync (Phone)", createdAccountsInSiebel.get(1).get(KEY_PHONE), payload1.get(KEY_PHONE));
		
		Map<String, Object>  payload2 = invokeRetrieveFlow(retrieveAccountFromSalesforceFlow, createdAccountsInSiebel.get(2));
		Assert.assertNull("The account 2 should have not been sync", payload2);
	}

	@Override
	protected String getConfigResources() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(MULE_DEPLOY_PROPERTIES_PATH));
		} catch (IOException e) {
			throw new IllegalStateException(
					"Could not find mule-deploy.properties file on classpath. " +
					"Please add any of those files or override the getConfigResources() method to provide the resources by your own.");
		}

		return props.getProperty("config.resources") + getTestFlows();
	}


	private void createTestDataInSandBox() throws MuleException, Exception {
		// Create object in target system to be updated
		
		String uniqueSuffix = "_" + TEMPLATE_NAME + "_" + UUID.getUUID();
		
		Map<String, Object> salesforceAccount3 = new HashMap<String, Object>();
		salesforceAccount3.put(KEY_NAME, "Name_3_SFDC" + uniqueSuffix);
		salesforceAccount3.put(KEY_WEBSITE, "http://example.com");
		salesforceAccount3.put(KEY_PHONE, "112");
		List<Map<String, Object>> createdAccountInSalesforce = new ArrayList<Map<String, Object>>();
		createdAccountInSalesforce.add(salesforceAccount3);
	
		SubflowInterceptingChainLifecycleWrapper createAccountInSalesforceFlow = getSubFlow("createAccountsInSalesforceFlow");
		createAccountInSalesforceFlow.initialise();
		createAccountInSalesforceFlow.process(getTestEvent(createdAccountInSalesforce, MessageExchangePattern.REQUEST_RESPONSE));
	
		Thread.sleep(1001); // this is here to prevent equal LastModifiedDate
		
		// Create accounts in source system to be or not to be synced
	
		// This account should be synced
		Map<String, Object> siebelAccount0 = new HashMap<String, Object>();
		siebelAccount0.put(KEY_NAME, "Name_0_SIEB" + uniqueSuffix);
		siebelAccount0.put(KEY_WEBSITE, "http://acme.org");
		siebelAccount0.put(KEY_PHONE, "123");
		siebelAccount0.put(KEY_NUMBER_OF_EMPLOYEES, 6000);
		siebelAccount0.put(KEY_CITY, "Las Vegas");
		siebelAccount0.put("Street", "street0A" + uniqueSuffix);
//		siebelAccount0.put(KEY_INDUSTRY, "Education");
		createdAccountsInSiebel.add(siebelAccount0);
				
		// This account should be synced (update)
		Map<String, Object> siebelAccount1 = new HashMap<String, Object>();
		siebelAccount1.put(KEY_NAME,  salesforceAccount3.get(KEY_NAME));
		siebelAccount1.put(KEY_WEBSITE, "http://example.edu");
		siebelAccount1.put(KEY_PHONE, "911");
		siebelAccount1.put(KEY_NUMBER_OF_EMPLOYEES, 7100);
		siebelAccount1.put(KEY_CITY, "Jablonica");
		siebelAccount1.put("Street", "street1A" + uniqueSuffix);
//		siebelAccount1.put(KEY_INDUSTRY, "Government");
		createdAccountsInSiebel.add(siebelAccount1);

		// This account should not be synced because of employees// was: industry
		Map<String, Object> siebelAccount2 = new HashMap<String, Object>();
		siebelAccount2.put(KEY_NAME, "Name_2_SIEB" + uniqueSuffix);
		siebelAccount2.put(KEY_WEBSITE, "http://energy.edu");
		siebelAccount2.put(KEY_PHONE, "333");
		siebelAccount2.put(KEY_NUMBER_OF_EMPLOYEES, 204);
		siebelAccount2.put(KEY_CITY, "London");
		siebelAccount2.put("Street", "street2A" + uniqueSuffix);
//		siebelAccount2.put(KEY_INDUSTRY, "Energetic");
		createdAccountsInSiebel.add(siebelAccount2);

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

		System.out.println("Results after adding: " + createdAccountsInSiebel.toString());
	}

	private String getTestFlows() {
		File[] listOfFiles = new File(TEST_FLOWS_FOLDER_PATH).listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().endsWith(".xml");
			}
		});
		
		if (listOfFiles == null) {
			return "";
		}
		
		StringBuilder resources = new StringBuilder();
		for (File f : listOfFiles) {
			resources.append(",").append(TEST_FLOWS_FOLDER_PATH).append(f.getName());
		}
		return resources.toString();
	}

	@Override
	protected Properties getStartUpProperties() {
		Properties properties = new Properties(super.getStartUpProperties());
		properties.put(
				MuleProperties.APP_HOME_DIRECTORY_PROPERTY,
				new File(MAPPINGS_FOLDER_PATH).getAbsolutePath());
		return properties;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> invokeRetrieveFlow(SubflowInterceptingChainLifecycleWrapper flow, Map<String, Object> payload) throws Exception {
		MuleEvent event = flow.process(getTestEvent(payload, MessageExchangePattern.REQUEST_RESPONSE));
		Object resultPayload = event.getMessage().getPayload();
		return resultPayload instanceof NullPayload ? null : (Map<String, Object>) resultPayload;
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
