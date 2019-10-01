# mod-source-record-manager

Copyright (C) 2018-2019 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

<!-- ../../okapi/doc/md2toc -l 2 -h 4 README.md -->
* [Introduction](#introduction)
* [Compiling](#compiling)
* [Docker](#docker)
* [Installing the module](#installing-the-module)
* [Deploying the module](#deploying-the-module)
* [REST Client](#rest-client)

## Introduction

FOLIO source record manager module.

## Compiling

```
   mvn install
```

See that it says "BUILD SUCCESS" near the end.

## Docker

Build the docker container with:

```
   docker build -t mod-source-record-manager .
```

Test that it runs with:

```
   docker run -t -i -p 8081:8081 mod-source-record-manager
```

## Installing the module

Follow the guide of
[Deploying Modules](https://github.com/folio-org/okapi/blob/master/doc/guide.md#example-1-deploying-and-using-a-simple-module)
sections of the Okapi Guide and Reference, which describe the process in detail.

First of all you need a running Okapi instance.
(Note that [specifying](../README.md#setting-things-up) an explicit 'okapiurl' might be needed.)

```
   cd .../okapi
   java -jar okapi-core/target/okapi-core-fat.jar dev
```

We need to declare the module to Okapi:

```
curl -w '\n' -X POST -D -   \
   -H "Content-type: application/json"   \
   -d @target/ModuleDescriptor.json \
   http://localhost:9130/_/proxy/modules
```

That ModuleDescriptor tells Okapi what the module is called, what services it
provides, and how to deploy it.

## Deploying the module

Next we need to deploy the module. There is a deployment descriptor in
`target/DeploymentDescriptor.json`. It tells Okapi to start the module on 'localhost'.

Deploy it via Okapi discovery:

```
curl -w '\n' -D - -s \
  -X POST \
  -H "Content-type: application/json" \
  -d @target/DeploymentDescriptor.json  \
  http://localhost:9130/_/discovery/modules
```

Then we need to enable the module for the tenant:

```
curl -w '\n' -X POST -D -   \
    -H "Content-type: application/json"   \
    -d @target/TenantModuleDescriptor.json \
    http://localhost:9130/_/proxy/tenants/<tenant_name>/modules
```

## REST Client

Provides RMB generated Client to call the module's endpoints. The Client is packaged into the lightweight jar.

### Maven dependency 

```xml
    <dependency>
      <groupId>org.folio</groupId>
      <artifactId>mod-source-record-manager-client</artifactId>
      <version>x.y.z</version>
      <type>jar</type>
    </dependency>
```
Where x.y.z - version of mod-source-record-manager.

### Usage

ChangeManagerClient is generated by RMB and provides methods for all endpoints described in the RAML file
```java
    // create client object with okapi url, tenant id and token
    ChangeManagerClient client = new ChangeManagerClient("localhost", "diku", "token");
```
Clients methods work with RMB generated data classes based on json schemas. 
mod-source-record-manager-client jar contains only generated by RMB DTOs and clients. 

Example of sending a request to the mod-source-record-manager
```
    // send request to mod-source-record-manager
    client.getChangeManagerJobExecutionsById("334508c6-e85d-407b-971d-a59f790cba30", null, response->{
      // processing response
      if (response.statusCode() == 200){
        System.out.println("Call is successful");
      }
    });
```
### Issue tracker

See project [MODSOURMAN](https://issues.folio.org/browse/MODSOURMAN)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).


## Data import workflow
There are two ways to import records into source-record-storage via source-record-manager.
* The first way is basically used by UI application - user has to upload file and start file processing, mod-data-import provides API for this functionality, see [FileUploadApi](https://github.com/folio-org/mod-data-import/blob/master/FileUploadApi.md) and [File processing API](https://github.com/folio-org/mod-data-import/blob/master/FileProcessingApi.md).
* The second way is intended to import records using CLI tools - Postman, curl, SoapUI. This option is preferred if user wants to process records directly without uploading files, and mod-source-record-manager provides API for this.

In both ways mod-source-record-manager maps MARC records to Inventory instances and sends instances to mod-inventory. For more details see [RuleProcessorApi](RuleProcessorApi.md).

Next up listed  details of how to import records using second way. In this case user has to follow steps below:
1. Create JobExecution containing: jobProfileInfo, sourceType="ONLINE" and empty files list.
2. Send RawRecordsDto containing records list and field last=false.
3. Complete data import by sending last RawRecordsDto containing empty records list, field last=true and total amount sent field in field "counter".

### Create JobExecution

Parsing records starts from creating Job Execution. 
Send POST request with InitJobExecutionsRqDto.
```
curl -w '\n' -X POST -D -   \
   -H "Content-type: application/json"   \
   -H "x-okapi-tenant: diku"  \
   -H "x-okapi-token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWt1X2FkbWluIiwidXNlcl9pZCI6IjQwZDFiZDcxLWVhN2QtNTk4Ny1iZTEwLTEyOGUzODJiZDMwNyIsImNhY2hlX2tleSI6IjMyYTJhNDQ3LWE4MzQtNDE1Ni1iYmZjLTk4YTEyZWVhNzliMyIsImlhdCI6MTU1NzkyMzI2NSwidGVuYW50IjoiZGlrdSJ9.AgPDmXIOsudFB_ugWYvJCdyqq-1AQpsRWLNt9EvzCy0" \
   -d @initJobExecutionsRqDto.json \
   https://folio-testing-okapi.aws.indexdata.com:443/change-manager/jobExecutions
```

##### initJobExecutionsRqDto.json

```
{
    "files": [],
    "sourceType": "ONLINE",
    "jobProfileInfo": {
      "id": "c8f98545-898c-4f48-a494-3ab6736a3243",
      "name": "Default job profile",
      "dataType": "MARC"
    },
    "userId": "952d9764-c0a9-5d81-9c2e-cd93c2600990"
}
```

##### Response with JobExecution entity

```
{
  "parentJobExecutionId" : "9ded4e45-9ed0-4a4f-95bd-5407854c4d18",
  "jobExecutions" : [ {
    "id" : "9ded4e45-9ed0-4a4f-95bd-5407854c4d18",
    "hrId" : "86030",
    "parentJobId" : "9ded4e45-9ed0-4a4f-95bd-5407854c4d18",
    "subordinationType" : "PARENT_SINGLE",
    "jobProfileInfo" : {
      "id" : "c8f98545-898c-4f48-a494-3ab6736a3243",
      "name" : "Default job profile",
      "dataType" : "MARC"
    },
    "status" : "NEW",
    "uiStatus" : "INITIALIZATION",
    "userId" : "952d9764-c0a9-5d81-9c2e-cd93c2600990"
  } ]
}
```

### Post records to parsing

To initiate records parsing should send POST request containing RawRecordsDto, wich contains raw records list ("records" field)
to follow path: /change-manager/jobExecutions/{jobExecutionId}/records. 
The list of records can contain records in different formats for example:  "MARC_RAW", "MARC_JSON", "MARC_XML". \
{jobExecutionId} - JobExecution id, which can be retrieved from response of previous request.
```
curl -w '\n' -X POST -D -   \
   -H "Content-type: application/json"   \
   -H "Accept: text/plain, application/json"   \
   -H "x-okapi-tenant: diku"  \
   -H "x-okapi-token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWt1X2FkbWluIiwidXNlcl9pZCI6IjQwZDFiZDcxLWVhN2QtNTk4Ny1iZTEwLTEyOGUzODJiZDMwNyIsImNhY2hlX2tleSI6IjMyYTJhNDQ3LWE4MzQtNDE1Ni1iYmZjLTk4YTEyZWVhNzliMyIsImlhdCI6MTU1NzkyMzI2NSwidGVuYW50IjoiZGlrdSJ9.AgPDmXIOsudFB_ugWYvJCdyqq-1AQpsRWLNt9EvzCy0" \
   -d @rawRecordsDto.json \
   https://folio-testing-okapi.aws.indexdata.com:443/change-manager/jobExecutions/9ded4e45-9ed0-4a4f-95bd-5407854c4d18/records
```

##### example of rawRecordsDto.json to parse marc records in raw format

```
{
  "recordsMetadata": {
    "last": false,
    "counter": 3,
    "contentType":"MARC_RAW",
    "total": 3
  },
  "records": [
    "01240cas a2200397   4500001000700000005001700007008004100024010001700065022001400082035002600096035002200122035001100144035001900155040004400174050001500218082001100233222004200244245004300286260004700329265003800376300001500414310002200429321002500451362002300476570002900499650003300528650004500561655004200606700004500648853001800693863002300711902001600734905002100750948003700771950003400808\u001E366832\u001E20141106221425.0\u001E750907c19509999enkqr p       0   a0eng d\u001E  \u001Fa   58020553 \u001E  \u001Fa0022-0469\u001E  \u001Fa(CStRLIN)NYCX1604275S\u001E  \u001Fa(NIC)notisABP6388\u001E  \u001Fa366832\u001E  \u001Fa(OCoLC)1604275\u001E  \u001FdCtY\u001FdMBTI\u001FdCtY\u001FdMBTI\u001FdNIC\u001FdCStRLIN\u001FdNIC\u001E0 \u001FaBR140\u001Fb.J6\u001E  \u001Fa270.05\u001E04\u001FaThe Journal of ecclesiastical history\u001E04\u001FaThe Journal of ecclesiastical history.\u001E  \u001FaLondon,\u001FbCambridge University Press [etc.]\u001E  \u001Fa32 East 57th St., New York, 10022\u001E  \u001Fav.\u001Fb25 cm.\u001E  \u001FaQuarterly,\u001Fb1970-\u001E  \u001FaSemiannual,\u001Fb1950-69\u001E0 \u001Fav. 1-   Apr. 1950-\u001E  \u001FaEditor:   C. W. Dugmore.\u001E 0\u001FaChurch history\u001FxPeriodicals.\u001E 7\u001FaChurch history\u001F2fast\u001F0(OCoLC)fst00860740\u001E 7\u001FaPeriodicals\u001F2fast\u001F0(OCoLC)fst01411641\u001E1 \u001FaDugmore, C. W.\u001Fq(Clifford William),\u001Feed.\u001E03\u001F81\u001Fav.\u001Fi(year)\u001E40\u001F81\u001Fa1-49\u001Fi1950-1998\u001E  \u001Fapfnd\u001FbLintz\u001E  \u001Fa19890510120000.0\u001E2 \u001Fa20141106\u001Fbm\u001Fdbatch\u001Felts\u001Fxaddfast\u001E  \u001FlOLIN\u001FaBR140\u001Fb.J86\u001Fh01/01/01 N\u001E\u001D01542ccm a2200361   ",     
    "01240cas a2200397   4500001000700000005001700007008004100024010001700065022001400082035002600096035002200122035001100144035001900155040004400174050001500218082001100233222004200244245004300286260004700329265003800376300001500414310002200429321002500451362002300476570002900499650003300528650004500561655004200606700004500648853001800693863002300711902001600734905002100750948003700771950003400808\u001E366832\u001E20141106221425.0\u001E750907c19509999enkqr p       0   a0eng d\u001E  \u001Fa   58020553 \u001E  \u001Fa0022-0469\u001E  \u001Fa(CStRLIN)NYCX1604275S\u001E  \u001Fa(NIC)notisABP6388\u001E  \u001Fa366832\u001E  \u001Fa(OCoLC)1604275\u001E  \u001FdCtY\u001FdMBTI\u001FdCtY\u001FdMBTI\u001FdNIC\u001FdCStRLIN\u001FdNIC\u001E0 \u001FaBR140\u001Fb.J6\u001E  \u001Fa270.05\u001E04\u001FaThe Journal of ecclesiastical history\u001E04\u001FaThe Journal of ecclesiastical history.\u001E  \u001FaLondon,\u001FbCambridge University Press [etc.]\u001E  \u001Fa32 East 57th St., New York, 10022\u001E  \u001Fav.\u001Fb25 cm.\u001E  \u001FaQuarterly,\u001Fb1970-\u001E  \u001FaSemiannual,\u001Fb1950-69\u001E0 \u001Fav. 1-   Apr. 1950-\u001E  \u001FaEditor:   C. W. Dugmore.\u001E 0\u001FaChurch history\u001FxPeriodicals.\u001E 7\u001FaChurch history\u001F2fast\u001F0(OCoLC)fst00860740\u001E 7\u001FaPeriodicals\u001F2fast\u001F0(OCoLC)fst01411641\u001E1 \u001FaDugmore, C. W.\u001Fq(Clifford William),\u001Feed.\u001E03\u001F81\u001Fav.\u001Fi(year)\u001E40\u001F81\u001Fa1-49\u001Fi1950-1998\u001E  \u001Fapfnd\u001FbLintz\u001E  \u001Fa19890510120000.0\u001E2 \u001Fa20141106\u001Fbm\u001Fdbatch\u001Felts\u001Fxaddfast\u001E  \u001FlOLIN\u001FaBR140\u001Fb.J86\u001Fh01/01/01 N\u001E\u001D01542ccm a2200361   ",     
    "01240cas a2200397   4500001000700000005001700007008004100024010001700065022001400082035002600096035002200122035001100144035001900155040004400174050001500218082001100233222004200244245004300286260004700329265003800376300001500414310002200429321002500451362002300476570002900499650003300528650004500561655004200606700004500648853001800693863002300711902001600734905002100750948003700771950003400808\u001E366832\u001E20141106221425.0\u001E750907c19509999enkqr p       0   a0eng d\u001E  \u001Fa   58020553 \u001E  \u001Fa0022-0469\u001E  \u001Fa(CStRLIN)NYCX1604275S\u001E  \u001Fa(NIC)notisABP6388\u001E  \u001Fa366832\u001E  \u001Fa(OCoLC)1604275\u001E  \u001FdCtY\u001FdMBTI\u001FdCtY\u001FdMBTI\u001FdNIC\u001FdCStRLIN\u001FdNIC\u001E0 \u001FaBR140\u001Fb.J6\u001E  \u001Fa270.05\u001E04\u001FaThe Journal of ecclesiastical history\u001E04\u001FaThe Journal of ecclesiastical history.\u001E  \u001FaLondon,\u001FbCambridge University Press [etc.]\u001E  \u001Fa32 East 57th St., New York, 10022\u001E  \u001Fav.\u001Fb25 cm.\u001E  \u001FaQuarterly,\u001Fb1970-\u001E  \u001FaSemiannual,\u001Fb1950-69\u001E0 \u001Fav. 1-   Apr. 1950-\u001E  \u001FaEditor:   C. W. Dugmore.\u001E 0\u001FaChurch history\u001FxPeriodicals.\u001E 7\u001FaChurch history\u001F2fast\u001F0(OCoLC)fst00860740\u001E 7\u001FaPeriodicals\u001F2fast\u001F0(OCoLC)fst01411641\u001E1 \u001FaDugmore, C. W.\u001Fq(Clifford William),\u001Feed.\u001E03\u001F81\u001Fav.\u001Fi(year)\u001E40\u001F81\u001Fa1-49\u001Fi1950-1998\u001E  \u001Fapfnd\u001FbLintz\u001E  \u001Fa19890510120000.0\u001E2 \u001Fa20141106\u001Fbm\u001Fdbatch\u001Felts\u001Fxaddfast\u001E  \u001FlOLIN\u001FaBR140\u001Fb.J86\u001Fh01/01/01 N\u001E\u001D01542ccm a2200361   "     
  ]
}
```

##### example of rawRecordsDto.json to parse marc records in json format

```
{
  "recordsMetadata": {
    "last": false,
    "counter": 1,
    "total": 1
    "contentType":"MARC_JSON",
  },
  "records": [
    "{\"leader\": \"00648cam a2200193 a 4500\",\r\n    \"fields\": [\r\n      {\r\n        \"001\": \"FOLIOstorage\"\r\n      },\r\n      {\r\n        \"008\": \"960521s1972\\\\\\\\\\\\\\\\se\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\0\\\\\\\\\\\\swe\\\\\\\\\"\r\n      },\r\n      {\r\n        \"041\": {\r\n          \"ind1\": \"1\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"a\": \"swe\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"096\": {\r\n          \"ind1\": \"\\\\\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"y\": \"Z\"\r\n            },\r\n            {\r\n              \"b\": \"TAp Chalmers tekniska h\u00F6gskola. Inst. f\u00F6r byggnadsstatik. Skrift. 1972:4\"\r\n            },\r\n            {\r\n              \"s\": \"g\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"100\": {\r\n          \"ind1\": \"1\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"a\": \"Sahlin, Sven\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"245\": {\r\n          \"ind1\": \"0\",\r\n          \"ind2\": \"0\",\r\n          \"subfields\": [\r\n            {\r\n              \"a\": \"P\u00E5lslagning\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"260\": {\r\n          \"ind1\": \"\\\\\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"c\": \"1972\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"300\": {\r\n          \"ind1\": \"\\\\\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"a\": \"19 bl.\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"440\": {\r\n          \"ind1\": \"\\\\\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"a\": \"Skrift, Chalmers tekniska h\u00F6gskola, Institutionen f\u00F6r byggnadsstatik\"\r\n            },\r\n            {\r\n              \"x\": \"9903909802 ;\"\r\n            },\r\n            {\r\n              \"v\": \"72:4\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"907\": {\r\n          \"ind1\": \"\\\\\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"a\": \".b11154585\"\r\n            },\r\n            {\r\n              \"b\": \"hbib \"\r\n            },\r\n            {\r\n              \"c\": \"s\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"902\": {\r\n          \"ind1\": \"\\\\\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"a\": \"190206\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"998\": {\r\n          \"ind1\": \"\\\\\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"b\": \"0\"\r\n            },\r\n            {\r\n              \"c\": \"990511\"\r\n            },\r\n            {\r\n              \"d\": \"m\"\r\n            },\r\n            {\r\n              \"e\": \"b  \"\r\n            },\r\n            {\r\n              \"f\": \"s\"\r\n            },\r\n            {\r\n              \"g\": \"0\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"909\": {\r\n          \"ind1\": \"0\",\r\n          \"ind2\": \"0\",\r\n          \"subfields\": [\r\n            {\r\n              \"a\": \"m\"\r\n            },\r\n            {\r\n              \"c\": \"a\"\r\n            },\r\n            {\r\n              \"d\": \"b\"\r\n            }\r\n          ]\r\n        }\r\n      },\r\n      {\r\n        \"945\": {\r\n          \"ind1\": \"\\\\\",\r\n          \"ind2\": \"\\\\\",\r\n          \"subfields\": [\r\n            {\r\n              \"l\": \"hbib3\"\r\n            },\r\n            {\r\n              \"a\": \"TAp Chalmers tekniska h\u00F6gskola.Inst. f\u00F6r byggnadsstatik. Skrift 72:4\"\r\n            }\r\n          ]\r\n        }\r\n      }\r\n    ]\r\n  }"
  ]
}
```

##### example of rawRecordsDto.json to parse marc records in xml format

```
{
  "recordsMetadata": {
    "last": false,
    "counter": 2,
    "total": 2
    "contentType":"MARC_XML",
  },
  "records": [     
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?> <record xmlns=\"http:\/\/www.loc.gov\/MARC21\/slim\">\r\n    <leader>01731cas a2200469 a 4500<\/leader>\r\n    <controlfield tag=\"001\">2672432<\/controlfield>\r\n    <controlfield tag=\"005\">20151103060938.0<\/controlfield>\r\n    <controlfield tag=\"006\">m        d        <\/controlfield>\r\n    <controlfield tag=\"007\">cf mn---------<\/controlfield>\r\n    <controlfield tag=\"008\">060411c19919999cautr pss     0    0eng d<\/controlfield>\r\n    <datafield tag=\"010\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">  2006263262<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"022\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"y\">1071-0892<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"035\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">(OCoLC)ocm72550951<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"037\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"b\">Conservation, Getty Conservation Institute, 4503 Glencoe Ave., Marina del Rey, CA 90292<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"040\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">TXA<\/subfield>\r\n      <subfield code=\"c\">TXA<\/subfield>\r\n      <subfield code=\"d\">UtOrBLW<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"042\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">lcd<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"043\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">n-us-ca<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"049\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">TXAM<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"050\" ind1=\"1\" ind2=\"4\">\r\n      <subfield code=\"a\">CC135<\/subfield>\r\n      <subfield code=\"b\">.C577 Electronic<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"130\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">Conservation (Marina del Rey, Calif. : Online)<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"245\" ind1=\"1\" ind2=\"0\">\r\n      <subfield code=\"a\">Conservation :<\/subfield>\r\n      <subfield code=\"b\">the GCI newsletter.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"246\" ind1=\"3\" ind2=\"0\">\r\n      <subfield code=\"a\">GCI newsletter<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"264\" ind1=\" \" ind2=\"1\">\r\n      <subfield code=\"a\">Marina del Rey, Calif. :<\/subfield>\r\n      <subfield code=\"b\">Getty Conservation Institute,<\/subfield>\r\n      <subfield code=\"c\">[1991]-<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"310\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Three no. a year<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"336\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">text<\/subfield>\r\n      <subfield code=\"b\">txt<\/subfield>\r\n      <subfield code=\"2\">rdacontent<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"337\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">computer<\/subfield>\r\n      <subfield code=\"b\">c<\/subfield>\r\n      <subfield code=\"2\">rdamedia<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"338\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">computer tape cassette<\/subfield>\r\n      <subfield code=\"b\">cf<\/subfield>\r\n      <subfield code=\"2\">rdacarrier<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"362\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">Vol. 6, no. 1 (fall 1991)-<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"500\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Title from issue cover image (publisher's site, viewed Oct. 11, 2006).<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"500\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Latest issue consulted: Volume 21, number 2 (2006) (viewed Oct. 11, 2006).<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"500\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Electronic resource.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"515\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Vol. 6 complete in one issue.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"530\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Also issued in print.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"538\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Mode of access: World Wide Web.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"610\" ind1=\"2\" ind2=\"0\">\r\n      <subfield code=\"a\">Getty Conservation Institute<\/subfield>\r\n      <subfield code=\"v\">Periodicals.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"650\" ind1=\" \" ind2=\"0\">\r\n      <subfield code=\"a\">Historic preservation<\/subfield>\r\n      <subfield code=\"v\">Periodicals.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"650\" ind1=\" \" ind2=\"0\">\r\n      <subfield code=\"a\">Art<\/subfield>\r\n      <subfield code=\"x\">Conservation and restoration<\/subfield>\r\n      <subfield code=\"v\">Periodicals.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"710\" ind1=\"2\" ind2=\" \">\r\n      <subfield code=\"a\">Getty Conservation Institute.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"776\" ind1=\"1\" ind2=\" \">\r\n      <subfield code=\"t\">Conservation (Marina del Rey, Calif.)<\/subfield>\r\n      <subfield code=\"x\">1071-0892<\/subfield>\r\n      <subfield code=\"w\">(OCoLC)25038844<\/subfield>\r\n      <subfield code=\"w\">(DLC)   93660871<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"948\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">cataloged<\/subfield>\r\n      <subfield code=\"b\">h<\/subfield>\r\n      <subfield code=\"c\">2006\/12\/21<\/subfield>\r\n      <subfield code=\"d\">o<\/subfield>\r\n      <subfield code=\"e\">kyu<\/subfield>\r\n      <subfield code=\"f\">9:58:29 am<\/subfield>\r\n      <subfield code=\"g\">CO<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"994\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">C0<\/subfield>\r\n      <subfield code=\"b\">TXA<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"999\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">MARS<\/subfield>\r\n    <\/datafield>\r\n  <\/record>",
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?> <record xmlns=\"http:\/\/www.loc.gov\/MARC21\/slim\">\r\n    <leader>03551cjm a2200577 a 4500<\/leader>\r\n    <controlfield tag=\"001\">1520848<\/controlfield>\r\n    <controlfield tag=\"005\">20151004060535.0<\/controlfield>\r\n    <controlfield tag=\"007\">sd fsngnn|||e|<\/controlfield>\r\n    <controlfield tag=\"008\">941026s1991    xxumun   ei         N\/A d<\/controlfield>\r\n    <datafield tag=\"028\" ind1=\"0\" ind2=\"0\">\r\n      <subfield code=\"a\">15 679<\/subfield>\r\n      <subfield code=\"b\">LaserLight<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"035\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">(OCoLC)31357838<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"035\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"9\">AGX0747AM<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"040\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">FBP<\/subfield>\r\n      <subfield code=\"c\">FBP<\/subfield>\r\n      <subfield code=\"d\">TXA<\/subfield>\r\n      <subfield code=\"d\">UtOrBLW<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"041\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"g\">ger<\/subfield>\r\n      <subfield code=\"g\">eng<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"049\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">TXAV<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"090\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">M5<\/subfield>\r\n      <subfield code=\"b\">.H67 1991 v. 4<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"245\" ind1=\"0\" ind2=\"0\">\r\n      <subfield code=\"a\">Hot 100.<\/subfield>\r\n      <subfield code=\"n\">Vol. 4,<\/subfield>\r\n      <subfield code=\"p\">1788-1810.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"264\" ind1=\" \" ind2=\"1\">\r\n      <subfield code=\"a\">[United States] :<\/subfield>\r\n      <subfield code=\"b\">LaserLight,<\/subfield>\r\n      <subfield code=\"c\">[1991]<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"264\" ind1=\" \" ind2=\"4\">\r\n      <subfield code=\"c\">\u21171991<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"300\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">1 sound disc (59:06) :<\/subfield>\r\n      <subfield code=\"b\">digital, stereo. ;<\/subfield>\r\n      <subfield code=\"c\">4 3\/4 in. +<\/subfield>\r\n      <subfield code=\"e\">1 pamphlet.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"336\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">performed music<\/subfield>\r\n      <subfield code=\"b\">prm<\/subfield>\r\n      <subfield code=\"2\">rdacontent<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"337\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">audio<\/subfield>\r\n      <subfield code=\"b\">s<\/subfield>\r\n      <subfield code=\"2\">rdamedia<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"338\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">audio disc<\/subfield>\r\n      <subfield code=\"b\">sd<\/subfield>\r\n      <subfield code=\"2\">rdacarrier<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"500\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Compact disc.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"500\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Title on container insert: 100 masterpieces, the top 10 of classical music, 1788-1810.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"500\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Program notes by Uwe Kraemer in German and English on container insert.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"500\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">Sound recording.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"505\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">Symphony no. 40, 1st movement \/ Mozart -- Moonlight sonata, 1st movement \/ Beethoven -- Symphony no. 94, \"Surprise\", 2nd movement -- The magic flute - Overture \/ Mozart -- Fu\u0308r elise \/ Beethoven -- Emperor's hymn, from String Quartet in C \/ Haydn -- Symphony no. 5, 1st movement \/ Beethoven -- Clarinet concerto in A, 2nd movement \/ Mozart -- Minuet in G \/ Beethoven -- Trumpet concerto in E flat, 3rd movement \/ Haydn.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"511\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">Salzburg Mozarteum Orchestra, Hans Graf, conductor (1st work) ; Evelyne Dubourg, piano (2nd, 5th works) ; Hungarian State Orchestra, Janos Ferencsik, conductor (3rd work) ; Staatskapelle Dresden, Hans Vonk, conductor (4th work) ; Kodaly Quartet (6th work) ; Dresden Philharmonic, Herbert Kegel, conductor (7th work); Bela Kovacs, clarinet, Franz Liszt Chamber Orchestra, Janos Rolla, conductor (8th work) ; Budapest Strings (9th work) ; Ludwig Guttler, trumpet, New Leipzig Bach Collegium Musicum.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"650\" ind1=\" \" ind2=\"0\">\r\n      <subfield code=\"a\">Symphonies<\/subfield>\r\n      <subfield code=\"v\">Excerpts.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"650\" ind1=\" \" ind2=\"0\">\r\n      <subfield code=\"a\">Concertos<\/subfield>\r\n      <subfield code=\"v\">Excerpts.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"650\" ind1=\" \" ind2=\"0\">\r\n      <subfield code=\"a\">Instrumental music.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"650\" ind1=\" \" ind2=\"0\">\r\n      <subfield code=\"a\">Piano music.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"650\" ind1=\" \" ind2=\"0\">\r\n      <subfield code=\"a\">String quartets<\/subfield>\r\n      <subfield code=\"v\">Excerpts.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Mozart, Wolfgang Amadeus,<\/subfield>\r\n      <subfield code=\"d\">1756-1791.<\/subfield>\r\n      <subfield code=\"t\">Symphonies,<\/subfield>\r\n      <subfield code=\"n\">K. 550,<\/subfield>\r\n      <subfield code=\"r\">G minor.<\/subfield>\r\n      <subfield code=\"p\">Molto allegro.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Beethoven, Ludwig van,<\/subfield>\r\n      <subfield code=\"d\">1770-1827.<\/subfield>\r\n      <subfield code=\"t\">Sonatas,<\/subfield>\r\n      <subfield code=\"m\">piano,<\/subfield>\r\n      <subfield code=\"n\">no. 14, op. 27, no. 2,<\/subfield>\r\n      <subfield code=\"r\">C\u266F minor.<\/subfield>\r\n      <subfield code=\"p\">Adagio sostenuto.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Haydn, Joseph,<\/subfield>\r\n      <subfield code=\"d\">1732-1809.<\/subfield>\r\n      <subfield code=\"t\">Symphonies,<\/subfield>\r\n      <subfield code=\"n\">H. I, 94,<\/subfield>\r\n      <subfield code=\"r\">G major.<\/subfield>\r\n      <subfield code=\"p\">Andante.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Mozart, Wolfgang Amadeus,<\/subfield>\r\n      <subfield code=\"d\">1756-1791.<\/subfield>\r\n      <subfield code=\"t\">Zauberflo\u0308te.<\/subfield>\r\n      <subfield code=\"p\">Ouverture.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Beethoven, Ludwig van,<\/subfield>\r\n      <subfield code=\"d\">1770-1827.<\/subfield>\r\n      <subfield code=\"t\">Bagatelles,<\/subfield>\r\n      <subfield code=\"m\">piano,<\/subfield>\r\n      <subfield code=\"n\">WoO 59,<\/subfield>\r\n      <subfield code=\"r\">A minor.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Haydn, Joseph,<\/subfield>\r\n      <subfield code=\"d\">1732-1809.<\/subfield>\r\n      <subfield code=\"t\">Quartets,<\/subfield>\r\n      <subfield code=\"m\">violins (2), viola, cello,<\/subfield>\r\n      <subfield code=\"n\">H. III, 77,<\/subfield>\r\n      <subfield code=\"r\">C major.<\/subfield>\r\n      <subfield code=\"p\">Poco adagio cantabile.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Beethoven, Ludwig van,<\/subfield>\r\n      <subfield code=\"d\">1770-1827.<\/subfield>\r\n      <subfield code=\"t\">Symphonies,<\/subfield>\r\n      <subfield code=\"n\">no. 5, op. 67,<\/subfield>\r\n      <subfield code=\"r\">C minor.<\/subfield>\r\n      <subfield code=\"p\">Allegro con brio.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Mozart, Wolfgang Amadeus,<\/subfield>\r\n      <subfield code=\"d\">1756-1791.<\/subfield>\r\n      <subfield code=\"t\">Concertos,<\/subfield>\r\n      <subfield code=\"m\">clarinet, orchestra,<\/subfield>\r\n      <subfield code=\"n\">K. 622,<\/subfield>\r\n      <subfield code=\"r\">A major.<\/subfield>\r\n      <subfield code=\"p\">Adagio.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Beethoven, Ludwig van,<\/subfield>\r\n      <subfield code=\"d\">1770-1827.<\/subfield>\r\n      <subfield code=\"t\">Minuets,<\/subfield>\r\n      <subfield code=\"m\">orchestra,<\/subfield>\r\n      <subfield code=\"n\">WoO 10, no. 2,<\/subfield>\r\n      <subfield code=\"r\">G major.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\"2\">\r\n      <subfield code=\"a\">Haydn, Joseph,<\/subfield>\r\n      <subfield code=\"d\">1732-1809.<\/subfield>\r\n      <subfield code=\"t\">Concertos,<\/subfield>\r\n      <subfield code=\"m\">trumpet, orchestra,<\/subfield>\r\n      <subfield code=\"n\">H. VIIe, 1,<\/subfield>\r\n      <subfield code=\"r\">E\u266D major.<\/subfield>\r\n      <subfield code=\"p\">Allegro.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"700\" ind1=\"1\" ind2=\" \">\r\n      <subfield code=\"a\">Kraemer, Uwe.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"740\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">Hot one hundred.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"740\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">100 masterpieces, the top 10 of classical music, 1788-1810.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"740\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">100 masterpieces, the top ten of classical music, 1788-1810.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"740\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">One hundred masterpieces, the top 10 of classical music, 1788-1810.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"740\" ind1=\"0\" ind2=\" \">\r\n      <subfield code=\"a\">One hundred masterpieces, the top ten of classical music, 1788-1810.<\/subfield>\r\n    <\/datafield>\r\n    <datafield tag=\"999\" ind1=\" \" ind2=\" \">\r\n      <subfield code=\"a\">MARS<\/subfield>\r\n    <\/datafield>\r\n  <\/record>"	
  ]
}
```

##### Response
If the records parsing is successfully initiated, there won't be any content in the response (HTTP status 204).

JobExecution entity will be have following changed state
```
{
  "id" : "9ded4e45-9ed0-4a4f-95bd-5407854c4d18",
  "hrId" : "86030",
  "parentJobId" : "9ded4e45-9ed0-4a4f-95bd-5407854c4d18",
  "subordinationType" : "PARENT_SINGLE",
  "jobProfileInfo" : {
    "id" : "c8f98545-898c-4f48-a494-3ab6736a3243",
    "name" : "Default job profile",
    "dataType" : "MARC"
  },
  "runBy" : {
    "firstName" : "DIKU",
    "lastName" : "ADMINISTRATOR"
  },
  "progress" : {
    "current" : 1000,
    "total" : 1000
  },
  "startedDate" : "2019-05-15T14:36:00.776+0000",
  "status" : "PARSING_IN_PROGRESS",
  "uiStatus" : "PREPARING_FOR_PREVIEW",
  "userId" : "952d9764-c0a9-5d81-9c2e-cd93c2600990"
}
```

### Finishing raw records parsing
To indicate end of raw records transferring for parsing should send POST request containing last RawRecordsDto
to follow path: /change-manager/jobExecutions/{jobExecutionId}/records. \
{jobExecutionId} - JobExecution id, which can be retrieved from response of JobExecution creation request.
The last RawRecordsDto should contains empty records list ("records" field), appropriate record format value 
in "contentType" field (for example MARC_RAW) and field "last" = true.

```
curl -w '\n' -X POST -D -   \
   -H "Content-type: application/json"   \
   -H "Accept: text/plain, application/json"   \
   -H "x-okapi-tenant: diku"  \
   -H "x-okapi-token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWt1X2FkbWluIiwidXNlcl9pZCI6IjQwZDFiZDcxLWVhN2QtNTk4Ny1iZTEwLTEyOGUzODJiZDMwNyIsImNhY2hlX2tleSI6IjMyYTJhNDQ3LWE4MzQtNDE1Ni1iYmZjLTk4YTEyZWVhNzliMyIsImlhdCI6MTU1NzkyMzI2NSwidGVuYW50IjoiZGlrdSJ9.AgPDmXIOsudFB_ugWYvJCdyqq-1AQpsRWLNt9EvzCy0" \
   -d @lastRawRecordsDto.json \
   https://folio-testing-okapi.aws.indexdata.com:443/change-manager/jobExecutions/9ded4e45-9ed0-4a4f-95bd-5407854c4d18/records
```

##### lastRawRecordsDto.json

```
{
  "recordsMetadata": {
    "last": true,
    "counter": 3,
    "total": 3
    "contentType":"MARC_RAW",
  },
  "records": []
}
```

##### Response
Successful response won't have any content (HTTP status 204).

JobExecution entity will be have following changed state
```
{
  "id" : "9ded4e45-9ed0-4a4f-95bd-5407854c4d18",
  "hrId" : "86030",
  "parentJobId" : "9ded4e45-9ed0-4a4f-95bd-5407854c4d18",
  "subordinationType" : "PARENT_SINGLE",
  "jobProfileInfo" : {
    "id" : "c8f98545-898c-4f48-a494-3ab6736a3243",
    "name" : "Default job profile",
    "dataType" : "MARC"
  },
  "runBy" : {
    "firstName" : "DIKU",
    "lastName" : "ADMINISTRATOR"
  },
  "progress" : {
    "current" : 1000,
    "total" : 1000
  },
  "startedDate" : "2019-05-15T14:36:00.776+0000",
  "completedDate" : "2019-05-15T14:56:23.387+0000",
  "status" : "COMMITTED",
  "uiStatus" : "RUNNING_COMPLETE",
  "userId" : "952d9764-c0a9-5d81-9c2e-cd93c2600990"
}
```

### Delete records associated with JobExecution

To delete JobExecution and all associated records send a delete request on /change-manager/jobExecutions/{jobExecutionId}/records.

```
curl -w '\n' -X DELETE -D -   \
   -H "Content-type: application/json"   \
   -H "Accept: */*"   \
   -H "x-okapi-tenant: diku"  \
   -H "x-okapi-token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWt1X2FkbWluIiwidXNlcl9pZCI6IjQwZDFiZDcxLWVhN2QtNTk4Ny1iZTEwLTEyOGUzODJiZDMwNyIsImNhY2hlX2tleSI6IjMyYTJhNDQ3LWE4MzQtNDE1Ni1iYmZjLTk4YTEyZWVhNzliMyIsImlhdCI6MTU1NzkyMzI2NSwidGVuYW50IjoiZGlrdSJ9.AgPDmXIOsudFB_ugWYvJCdyqq-1AQpsRWLNt9EvzCy0" \
   https://folio-testing-okapi.aws.indexdata.com:443/change-manager/jobExecutions/9ded4e45-9ed0-4a4f-95bd-5407854c4d18/records
```

Successful response contains no content (HTTP status 204).
