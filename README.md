
# Anypoint Template: Siebel to Salesforce Account Migration

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Considerations](#considerations)
	* [Salesforce Considerations](#salesforceconsiderations)
	* [Siebel Considerations](#siebelconsiderations)
+ [Run it!](#runit)
	* [Running on premise](#runonopremise)
	* [Running on Studio](#runonstudio)
	* [Running on Mule ESB stand alone](#runonmuleesbstandalone)
	* [Running on CloudHub](#runoncloudhub)
	* [Deploying your Anypoint Template on CloudHub](#deployingyouranypointtemplateoncloudhub)
	* [Properties to be configured (With examples)](#propertiestobeconfigured)
+ [API Calls](#apicalls)
+ [Customize It!](#customizeit)
	* [config.xml](#configxml)
	* [businessLogic.xml](#businesslogicxml)
	* [endpoints.xml](#endpointsxml)
	* [errorHandling.xml](#errorhandlingxml)


# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>
I want to synchronize accounts from Oracle Siebel Business Objects to Salesforce instance.

This Template should serve as a foundation for the process of migrating accounts from Oracle Siebel Business Objects to Salesforce instance, being able to specify filtering criteria and desired behavior when an account already exists in the Salesforce. 

As implemented, this Template leverage the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing).

The batch job is divided into *Process* and *On Complete* stages.

The migration process starts with querying Siebel for all the existing Accounts that match the filter criteria.
During the *Process* stage, each Account will be filtered depending on existence of matching account in the Salesforce.
The last step of the *Process* stage will group the accounts and create/update them in Salesforce.
Finally during the *On Complete* stage the Template will both output statistics data into the console and send a notification email with the results of the batch execution.

# Considerations <a name="considerations"/>

To make this Anypoint Template run, there are certain preconditions that must be considered. All of them deal with the preparations in both, that must be made in order for all to run smoothly. **Failing to do so could lead to unexpected behavior of the template.**



## Salesforce Considerations <a name="salesforceconsiderations"/>

There may be a few things that you need to know regarding Salesforce for this template to work.

To have this template work as expected, you should be aware of your own Salesforce field configuration.

### FAQ

 - Where can I check that the field configuration for my Salesforce instance is the right one?

    [Salesforce: Checking Field Accessibility for a Particular Field][1]

- Can I modify the Field Access Settings? How?

    [Salesforce: Modifying Field Access Settings][2]


[1]: https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US
[2]: https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US


### As destination of data

There are no particular considerations for this Anypoint Template regarding Salesforce as data destination.
## Siebel Considerations <a name="siebelconsiderations"/>

There may be a few things that you need to know regarding Siebel for this template to work.

This Anypoint Template may be using date time/timestamp fields from the Siebel to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it cannot discover the time zone in which the Siebel instance is on.
It is up to the user of this template to provide such information. For more about Siebel time zones, see [link](http://docs.oracle.com/cd/B40099_02/books/Fundamentals/Fund_settingoptions3.html)


### As source of data

In order to make the Siebel connector work smoothly you have to provide the correct version of the Siebel jars that works with your Siebel installation. [See more](https://docs.mulesoft.com/connectors/siebel-connector#prerequisites).









# Run it! <a name="runit"/>
Simple steps to get Siebel to Salesforce Account Migration running.


## Running on premise <a name="runonopremise"/>
In this section we detail the way you should run your Anypoint Template on your computer.


### Where to Download Mule Studio and Mule ESB
First thing to know if you are a newcomer to Mule is where to get the tools.

+ You can download Anypoint Studio from this [location](https://www.mulesoft.com/lp/dl/studio)
+ You can download Mule runtime from this [location](https://www.mulesoft.com/platform/mule)


### Importing an Anypoint Template into Studio
Anypoint Studio offers several ways to import a project into the workspace, for instance: 

+ Anypoint Studio Project from a file system
+ Packaged Mule application (.jar)

You can find a detailed description on how to do so in this [documentation page](https://docs.mulesoft.com/).


### Running on Studio <a name="runonstudio"/>
Once you have imported you Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click the Anypoint Template project folder 
+ Hover your mouse over `"Run as"`
+ Click `"Mule Application (configure)"`
+ Inside the dialog, select Environment and set the variable `"mule.env"` to the value `"dev"`
+ Click `"Run"`


### Running on Mule ESB stand alone <a name="runonmuleesbstandalone"/>
Complete all properties in one of the property files, for example in [mule.prod.properties] (../master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`. 


## Running on CloudHub <a name="runoncloudhub"/>
While [creating your application on CloudHub](https://docs.mulesoft.com/runtime-manager/) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**.
Once your app is all set and started, supposing you choose as domain name `sieb2sfdcaccountmigration` to trigger the use case you just need to hit `http://sieb2sfdcaccountmigration.cloudhub.io/migrateaccounts` and report will be sent to the e-mails configured.

### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this [link](https://docs.mulesoft.com/runtime-manager/deployment-strategies)


## Properties to be configured (With examples) <a name="propertiestobeconfigured"/>
To use this Mule Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:
### Application configuration
+ http.port `9090` 
+ page.size `200`

**Salesforce Connector configuration**
+ sfdc.username `joan.baez@org`
+ sfdc.password `JoanBaez456`
+ sfdc.securityToken `ces56arl7apQs56XTddf34X`

**Oracle Siebel Business Objects Connector configuration**
+ sieb.user `SADMIN`
+ sieb.password `SADMIN`
+ sieb.server `192.168.10.8`
+ sieb.serverName `SBA_82`
+ sieb.objectManager `EAIObjMgr_enu`
+ sieb.port `2321`

**SMTP Services configuration**
+ smtp.host `some.host`
+ smtp.port `587`
+ smtp.user `email%40example.com`
+ smtp.password `password`

**E-Mail Details**
+ mail.from `batch.migrateaccounts.migration%40mulesoft.com`
+ mail.to `test@gmail.com`
+ mail.subject `Batch Job Finished Report`

# API Calls <a name="apicalls"/>
Salesforce imposes limits on the number of API Calls that can be made. Therefore calculating this amount may be an important factor to consider. The Anypoint Template calls to the API can be calculated using the formula:

***X + X / ${page.size}***

Being ***X*** the number of Accounts to be synchronized on each run. 

The division by ***${page.size}*** is because, by default, Accounts are gathered in groups of ${page.size} for each Upsert API Call in the aggregation step. 

For instance if 10 records are fetched from origin instance, then 11 api calls to Salesforce will be made (10 + 1).


# Customize It!<a name="customizeit"/>
This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page is organized by describing all the XML that conform the Anypoint Template.
Of course more files can be found such as test classes and Mule application files, but to keep it simple, we focus on the XMLs.

Here is a list of the main XML files you can find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Configuration Properties](https://docs.mulesoft.com/mule4-user-guide/v/4.1/configuring-properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor, they can be found on the *Global Element* tab.


## businessLogic.xml<a name="businesslogicxml"/>
The functional aspect of this template e is implemented on this XML, directed by one flow responsible of excecuting the logic.
For the pourpose of this template, the *mainFlow* excecutes a batch job that handles all its logic.
This flow has an exception strategy that basically consists on invoking the *defaultChoiseExceptionStrategy* defined in the *errorHandling.xml* file.



## endpoints.xml<a name="endpointsxml"/>
This is the file where you find the inbound and outbound sides of your integration app.
This template has only an HTTP Listener connector as the way to trigger the use case.
**HTTP Listener Connector** - Start Report Generation
+ `${http.port}` is set as a property to be defined either on a property file or in CloudHub environment variables.
+ The path configured by default is `migrateaccounts` that you are free to change for the one you prefer.
+ The host name for all endpoints in your CloudHub configuration is `localhost`. CloudHub routes requests from your application domain URL to the endpoint.
+ The endpoint is a *request-response* and a result of calling it is the response with the total records fetched by the criteria specified.



## errorHandling.xml<a name="errorhandlingxml"/>
This is the right place to handle how your integration will react depending on the different exceptions. 
This file provides [Error Handling](https://docs.mulesoft.com/mule4-user-guide/v/4.1/error-handling) that is referenced by the main flow in the business logic.



