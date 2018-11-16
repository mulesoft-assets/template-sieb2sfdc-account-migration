
# Anypoint Template: Siebel to Salesforce Account Migration

# License Agreement
Note that using this template is subject to the conditions of this [License Agreement](https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf).
Review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
I want to synchronize accounts from Oracle Siebel Business Objects to Salesforce instance.

This template serves as a foundation for the process of migrating accounts from Oracle Siebel Business Objects to Salesforce instance, and specifying filtering criteria and desired behavior when an account already exists in the Salesforce. 

As implemented, this template leverages the Mule batch module.

The batch job is divided into *Process* and *On Complete* stages.

The migration process starts with querying Siebel for all the existing Accounts that match the filter criteria.
During the *Process* stage, each Account is filtered depending on whether there is a matching account in Salesforce.
The last step of the *Process* stage groups the accounts and creates or updates them in Salesforce.
Finally during the *On Complete* stage the template outputs statistics data to the console and sends a notification email with the results of the batch execution.

# Considerations

To make this template run, there are certain preconditions that must be considered. All of them deal with the preparations in both, that must be made for all to run smoothly. Failing to do so could lead to unexpected behavior of the template.

## Salesforce Considerations

There may be a few things that you need to know regarding Salesforce for this template to work.

To have this template work as expected, you should be aware of your own Salesforce field configuration.

### FAQ

 - Where can I check that the field configuration for my Salesforce instance is the right one?

    [Salesforce: Checking Field Accessibility for a Particular Field][1]

- Can I modify the Field Access Settings? How?

    [Salesforce: Modifying Field Access Settings][2]


[1]: https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US
[2]: https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US

### As a Destination of Data

There are no particular considerations for this template regarding Salesforce as data destination.
## Siebel Considerations

This template may use date time/timestamp fields from Siebel to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it cannot discover the time zone in which the Siebel instance is on.
It is up to the user of this template to provide such information. For more about Siebel time zones, see [link](http://docs.oracle.com/cd/B40099_02/books/Fundamentals/Fund_settingoptions3.html)

### As a Source of Data

To make the Siebel connector work smoothly, you have to provide the correct version of the Siebel JAR files that work with your Siebel installation. [See more](https://docs.mulesoft.com/connectors/siebel-connector#prerequisites).

# Run it!
Simple steps to get Siebel to Salesforce Account Migration running.


## Running on premise <a name="runonopremise"/>
In this section we detail the way you should run your template on your computer.

### Where to Download Anypoint Studio and Mule Runtime
First thing to know if you are a newcomer to Mule is where to get the tools:

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule Runtime](https://www.mulesoft.com/platform/mule)

### Import an Anypoint Template into Studio

1. In Anypoint Studio, click the Exchange icon in the Studio task bar.
2. Click Login in Anypoint Exchange and supply your Anypoint Platform username and password.
3. Search for the connector and click Install.
4. Follow the prompts to install the connector.

### Run in Studio
After importing the template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Once that is done, right click the template project folder.
+ Hover your mouse over `Run as`.
+ Click `Mule Application (configure)`.
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
+ Click `Run`.

### Running on a Standalone Mule Runtime 
Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable to use it. To follow the example, use `mule.env=prod`. 

## Running on CloudHub
While [creating your application on CloudHub](https://docs.mulesoft.com/runtime-manager/) (or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in "Properties to Configure" as well as the **mule.env**.
Once your app is all set and started, if you choose as domain name `sieb2sfdcaccountmigration` to trigger the use case you just need to hit `http://sieb2sfdcaccountmigration.cloudhub.io/migrateaccounts` and report will be sent to the emails you  configured.

### Deploying your Template on CloudHub
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this [link](https://docs.mulesoft.com/runtime-manager/deployment-strategies)

## Properties to Configure
To use this template you need to configure properties (credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:

### Application Configuration
+ http.port `9090` 
+ page.size `200`

**Salesforce Connector Configuration**
+ sfdc.username `joan.baez@org`
+ sfdc.password `JoanBaez456`
+ sfdc.securityToken `ces56arl7apQs56XTddf34X`

**Oracle Siebel Business Objects Connector Configuration**
+ sieb.user `SADMIN`
+ sieb.password `SADMIN`
+ sieb.server `192.168.10.8`
+ sieb.serverName `SBA_82`
+ sieb.objectManager `EAIObjMgr_enu`
+ sieb.port `2321`

**SMTP Services Configuration**
+ smtp.host `some.host`
+ smtp.port `587`
+ smtp.user `email%40example.com`
+ smtp.password `password`

**EMail Details**
+ mail.from `batch.migrateaccounts.migration%40mulesoft.com`
+ mail.to `test@gmail.com`
+ mail.subject `Batch Job Finished Report`

# API Calls
Salesforce imposes limits on the number of API Calls that can be made. Therefore calculating this amount may be an important factor to consider. The template calls to the API can be calculated using the formula:

***X + X / ${page.size}***

***X*** the number of accounts to be synchronized on each run. 

Divide by ***${page.size}*** because, by default, accounts are gathered in groups of ${page.size} for each Upsert API Call in the aggregation step. 

For instance if 10 records are fetched from origin instance, then 11 API calls are made to Salesforce (10 + 1).


# Customize It!
This brief guide intends to give a high level idea of how this template is built and how you can 
change it according to your needs. As Mule applications are based on XML files, this page is 
organized by describing all the XML that affect this template.

More files can be found such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml

## config.xml
Configuration for Connectors and [Configuration Properties](https://docs.mulesoft.com/mule4-user-guide/v/4.1/configuring-properties) are set in this file. Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so. Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor, they can be found on the *Global Element* tab.

## businessLogic.xml
The functional aspect of this template is implemented on this XML, directed by one flow responsible of executing the logic.
For the purpose of this template, the *mainFlow* executes a batch job that handles all its logic.
This flow has an exception strategy that basically consists on invoking the *defaultChoiseExceptionStrategy* defined in the *errorHandling.xml* file.

## endpoints.xml
This is the file where you find the inbound and outbound sides of your integration app.
This template has only an HTTP Listener connector as the way to trigger the use case.
**HTTP Listener Connector** - Start Report Generation
+ `${http.port}` is set as a property to be defined either on a property file or in CloudHub environment variables.
+ The path configured by default is `migrateaccounts` that you are free to change for the one you prefer.
+ The host name for all endpoints in your CloudHub configuration is `localhost`. CloudHub routes requests from your application domain URL to the endpoint.
+ The endpoint is a *request-response* and a result of calling it is the response with the total records fetched by the criteria specified.

## errorHandling.xml
This is the right place to handle how your integration will react depending on the different exceptions. 
This file provides [Error Handling](https://docs.mulesoft.com/mule4-user-guide/v/4.1/error-handling) that is referenced by the main flow in the business logic.
