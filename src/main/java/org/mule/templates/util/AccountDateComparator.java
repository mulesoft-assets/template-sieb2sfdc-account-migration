package org.mule.templates.util;

import java.text.ParseException;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * The function of this class is to establish a relation happens before between two maps - first representing Siebel and second Salesforce account.
 * 
 * It's assumed that these maps are well formed maps thus they both contain an entry with the expected key. Never the less validations are being done.
 * 
 * @author martin.zdila
 */
public class AccountDateComparator {
	private static final DateTimeFormatter SFDC_DATETIME_FORMATTER = ISODateTimeFormat.dateTimeParser();
	private static final PeriodFormatter OFFSET_FORMATTER = new PeriodFormatterBuilder()
			.rejectSignedValues(true)
			.appendHours().appendSeparator(":")
			.appendMinutes().appendSeparator(":")
			.appendSecondsWithOptionalMillis().toFormatter();		
	
	private static final String LAST_MODIFIED_DATE = "LastModifiedDate";

	/**
	 * Validate which account has the latest last referenced date.
	 * 
	 * @param siebelAccount
	 *            Siebel account map
	 * @param salesforceAccount
	 *            Salesforce account map
	 * @param siebelTimeOffset
	 *            optional Siebel time offset parameter in format [+|-]HH[:MM[:SS[.mmm]]] 
	 * @return true if the last activity date from accountA is after the one from accountB
	 * @throws ParseException 
	 */
	public static boolean isAfter(Map<String, Object> siebelAccount, Map<String, Object> salesforceAccount, String siebelTimeOffset) {
		DateTimeFormatter siebelDatetimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
		
		if (siebelTimeOffset != null && !siebelTimeOffset.isEmpty()) {
			char sign = siebelTimeOffset.charAt(0);
			Period period = OFFSET_FORMATTER.parsePeriod(sign == '-' || sign == '+' ? siebelTimeOffset.substring(1) : siebelTimeOffset);
			int millis = (sign == '-' ? -1 : 1) * (int) period.toStandardDuration().getMillis();
			siebelDatetimeFormatter = siebelDatetimeFormatter.withZone(DateTimeZone.forOffsetMillis(millis));
		}

		Validate.notNull(siebelAccount, "Siebel account should not be null");
		Validate.notNull(salesforceAccount, "Salesforce account should not be null");

		Validate.isTrue(siebelAccount.containsKey(LAST_MODIFIED_DATE), "Siebel Account should contain key " + LAST_MODIFIED_DATE);
		
		if (salesforceAccount.get(LAST_MODIFIED_DATE) == null) {
			return true;
		}
		
		Object siebelDateObject = siebelAccount.get(LAST_MODIFIED_DATE);
		
		Validate.isTrue(siebelDateObject instanceof String, "LastModifiedDate of Siebel Account must be String");
		DateTime LastModifiedDateOfA = siebelDatetimeFormatter.parseDateTime((String) siebelDateObject);
		
		Object salesforceDateObject = salesforceAccount.get(LAST_MODIFIED_DATE);
		Validate.isTrue(salesforceDateObject instanceof String, "LastModifiedDate of Salesforce Account must be String");
		DateTime LastModifiedDateOfB = SFDC_DATETIME_FORMATTER.parseDateTime((String) salesforceDateObject);
		
		return LastModifiedDateOfA.isAfter(LastModifiedDateOfB);
	}
	
}
