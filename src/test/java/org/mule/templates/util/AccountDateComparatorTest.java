/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author MartinZdila
 *
 */
public class AccountDateComparatorTest {
	
	private static final String KEY_LAST_MODIFIED_DATE = "LastModifiedDate";
	
	private static final String SALESFORCE_DATETIME = "2013-12-09T22:15:33.001Z";

	private static final String SIEBEL_DATETIME = "12/09/2013 15:15:33";
	
	private static final String SIEBEL_DATETIME2 = "12/10/2013 15:15:33";
	
	private static final String SIEBEL_TIME_OFFSET = "-7";

	@Test(expected = IllegalArgumentException.class)
	public void nullSiebelAccount() {
		Map<String, Object> siebelAccount = null;

		Map<String, Object> salesforceAccount = new HashMap<String, Object>();
		salesforceAccount.put(KEY_LAST_MODIFIED_DATE, SALESFORCE_DATETIME);

		AccountDateComparator.isAfter(siebelAccount, salesforceAccount, SIEBEL_TIME_OFFSET);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullSalesforceAccount() {
		Map<String, Object> siebelAccount = new HashMap<String, Object>();
		siebelAccount.put(KEY_LAST_MODIFIED_DATE, SIEBEL_DATETIME);

		Map<String, Object> salesforceAccount = null;

		AccountDateComparator.isAfter(siebelAccount, salesforceAccount, SIEBEL_TIME_OFFSET);
	}

	@Test(expected = IllegalArgumentException.class)
	public void malFormedSiebelAccount() {
		Map<String, Object> siebelAccount = new HashMap<String, Object>();

		Map<String, Object> salesforceAccount = new HashMap<String, Object>();
		salesforceAccount.put(KEY_LAST_MODIFIED_DATE, SALESFORCE_DATETIME);

		AccountDateComparator.isAfter(siebelAccount, salesforceAccount, SIEBEL_TIME_OFFSET);
	}

	public void emptySalesforceAccount() {
		Map<String, Object> siebelAccount = new HashMap<String, Object>();
		siebelAccount.put(KEY_LAST_MODIFIED_DATE, SIEBEL_DATETIME);

		Map<String, Object> salesforceAccount = new HashMap<String, Object>();

		Assert.assertTrue("Siebel account should be after Salesforce account",
				AccountDateComparator.isAfter(siebelAccount, salesforceAccount, SIEBEL_TIME_OFFSET));
	}

	@Test
	public void siebelAccountIsAfterSalesforceAccount() {
		Map<String, Object> siebelAccount = new HashMap<String, Object>();
		siebelAccount.put(KEY_LAST_MODIFIED_DATE, SIEBEL_DATETIME2);

		Map<String, Object> salesforceAccount = new HashMap<String, Object>();
		salesforceAccount.put(KEY_LAST_MODIFIED_DATE, SALESFORCE_DATETIME);

		Assert.assertTrue("Siebel account should be after Salesforce account",
				AccountDateComparator.isAfter(siebelAccount, salesforceAccount, SIEBEL_TIME_OFFSET));
	}

	@Test
	public void siebelAccountIsNotAfterSalesforceAccount() {
		Map<String, Object> siebelAccount = new HashMap<String, Object>();
		siebelAccount.put(KEY_LAST_MODIFIED_DATE, SIEBEL_DATETIME);

		Map<String, Object> salesforceAccount = new HashMap<String, Object>();
		salesforceAccount.put(KEY_LAST_MODIFIED_DATE, SALESFORCE_DATETIME);

		Assert.assertFalse("Siebel account should not be after Salesforce account",
				AccountDateComparator.isAfter(siebelAccount, salesforceAccount, SIEBEL_TIME_OFFSET));
	}

	@Test
	public void siebelAccountIsTheSameThatSalesforceAccount() {
		Map<String, Object> siebelAccount = new HashMap<String, Object>();
		siebelAccount.put(KEY_LAST_MODIFIED_DATE, SIEBEL_DATETIME);

		Map<String, Object> salesforceAccount = new HashMap<String, Object>();
		salesforceAccount.put(KEY_LAST_MODIFIED_DATE, SALESFORCE_DATETIME);

		Assert.assertFalse("Siebel account should not be after Salesforce account",
				AccountDateComparator.isAfter(siebelAccount, salesforceAccount, SIEBEL_TIME_OFFSET));
	}

}
