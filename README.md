<img align="right" class="img-fluid" padding="10px" src="https://raw.githubusercontent.com/ExtrieveTechnologies/QuickCapture/main/img/android.png" alt="img-verification">

#  SplicerAi v2.0
SplicerAi is an intelligent document analysis SDK offering the following functionalities:

1.  **Document Identification / Classification** : _Identification and categorization of documents_.
2.  **Document Data Extraction** : _Extraction of textual data from documents_.
3.  **Document Masking** : _Specific to Aadhaar documents, ensuring data privacy_.

> **Developer-friendly** & **Easy to integrate** SDK.

> **Works entirely offline***, locally on the device, with **no data transferred to any server or third party**.  

*For reduced build size if needed, an initial internet connection may optionally be required to fetch ML data or resource files, depending on the specific integration and features used by the consumer application*


Download / Addition
--------
You can use this SDK in any Android project simply by using Gradle :

```java
//Add expack central repo in settings.gradle
repositories {
  google()
  mavenCentral()
  maven {url 'https://expack.extrieve.in/maven/'}
}

//Then add implementation for SDK in dependencies in build.gradle (module:<yourmodulename>)
dependencies {
  implementation 'com.extrieve.splicer.aisdk:SplicerAIv2:<SDK-VERSION>'
  // Latest version: 2.0.29
}
SDK-VERSION - Need to replace with the correct v2 series.
```

Or Maven:

```xml
<dependency>
  <groupId>com.extrieve.splicer.aisdk</groupId>
  <artifactId>SplicerAIv2</artifactId>
  <version>SDK-VERSION</version>
</dependency>
SDK-VERSION - Need to replace with the correct v2 series.
```

Or can even integrate with the **.aar** library file and manually add the file dependency to the project/app.


Compatibility
-------------
 * **JAVA 17 Support**: SplicerAI v2 requires JAVA version 17 support for the application.
 * **Minimum Android SDK**: SplicerAI v2 requires a minimum API level of 21.
 * **Target Android SDK**: SplicerAI v2 features supports **API 36**.
  * **Compiled SDK Version**: SplicerAI v2 compiled against **API 34**.Host application using this SDK should compiled against 34 or later
 ----

 Run-time requirement
-------------
 * On Android, Google Play Services is mandatory.
 * Supported CPU architectures: arm64-v8a and armeabi-v7a.
 * Simulator and emulator environments are not supported.For testing on simulators, please contact the development support team to request a dedicated test version compatible with those environments.
 * Depending on your specific needs, you can import and use one or all of the classes provided by the SDK.
----

API & integration Details
-------------
Available properties and method
SDK has one core class and one supporting class :
```java
import com.extrieve.splicer.aisdk.*;
//OR : can import only required classes as per use cases.
import com.extrieve.splicer.aisdk.AiDocument;  
import com.extrieve.splicer.aisdk.Config;
```
---

In SplicerAi, prior to any document analysis, an independent document object needs to be created using the `AiDocument` class provided by the SplicerAi SDK. Also the **SDK needs to be activated using proper license** with Config.License.Acivate();

```java
//Creating a new document object.
AiDocument DocumentObject = new AiDocument(ImagePath);
//ImagePath - string type - path of document image 
```
# KYC Document
**DocumentObject** can be generated with the following properties :

1.  **ID** : _Identifier for the unique document object.Can be used for further reference_.
2.  **TYPE** : _Property to specify the KYC document type (AADHAAR,PAN etc.)_.
3.  **PATH** : _Physical path of the document image file_.
4.  **DATA** : _Text/character data will be identified and extracted from image_.

**DocumentObject** will be available with the following **Methods** :

## 1.  **KYCDetect**  
KYCDetect Method from **DocumentObject** will provide the **type of KYC document** with  document analysis intelligence by SplicerAi.
    
  ```java
  //KYCDetect will use callback function to return the result.
  DocumentObject.KYCDetect(this::handleKYCDetectCallBack);
  private void handleKYCDetectCallBack(resultJson) {
      //Use detect respose resultJson here
  }
  //Or use lambda 
  DocumentObject.KYCDetect(resultJson -> {
      //Use detect respose resultJson here
  });
  
  ```
    
Once the document type is identified, same will also be available in the **TYPE** property of **DocumentObject** .Following is the response structure :
    
```json
//Respose JSON data
{
  "STATUS": true,
  "DESCRIPTION":"<Description + subtype of document>",
   "TYPE" : "Name/type of document>",
   "SUBTYPE":"<subtype of document>", // AADHAAR_BACK
   "Confidence" : "<Level of accuracy: High/Medium/Low>",
   predictedDocs: {
	 //If any other documents are detected,
	 //Same will be listed out with the confidence level
      Aadhaar: "HIGH"
   },
}

```
    
## 2.  **KYCExtract**  
KYCExtract Method from **DocumentObject** will provide extracted data from the provided document in a JSON string format.
    
```java
//KYCDetect will use callback function to return the result.
DocumentObject.KYCExtract(this::handleKYCExtractCallBack);
private void handleKYCExtractCallBack(resultJson) {
  // Code to process the resultJson
}
//Or use lambda 
DocumentObject.KYCExtract(resultJson -> {
  //Detect respose
});

```
    
Once document data is extracted, the same will be available in **DATA** property of DocumentObject. Following is the response structure :
    
```json
{
  "STATUS": true,
  "DESCRIPTION": "AADHAAR_FRONT",// Description of operation - includes "subtype"
  "TYPE": "AADHAAR",
  "SUBTYPE": "AADHAAR_FRONT",//Dedicated Subtype (specify document front/back)
  "CONFIDENCE": "HIGH",
  "OCR_QUALITY": "DEFAULT",
  "PREDICTED_DOCS": {
    "AADHAAR": "HIGH"
  },
  "KEYVALUE": { 
	  "NAME": "NAME", 
	  "GENDER": "MALE", 
	  "DOB": "16/09/1981", 
	  "AADHAARNO": "2513 5077 5668", 
	  "FILENAME": ""  
  },
  "DATA": { 
    "AADHAAR NO": {
      "VALUE": "2513 5077 5668",
      "CONFIDENCE": "HIGH"
    },
    "ADDRESS": {
      "VALUE": "ADDRESS",
      "CONFIDENCE": "LOW"
    },
    "DOB": {
      "VALUE": "16/09/1981",
      "CONFIDENCE": "MEDIUM"
    },
    "GENDER": {
      "VALUE": "MALE",
      "CONFIDENCE": "HIGH"
    },
    "NAME": {
      "VALUE": "",
      "CONFIDENCE": "" // For empty value, confidence also will be empty
    }
  }
}
```
    
## 3.  **GetKYCDocList**  
GetKYCDocList Method from **DocumentObject** will provide the list of available KYC documents.
    
```java
//GetKYCDocList will return all the possible KYC document types.
String KYCDOcList = DocumentObject.GetKYCDocList();
```

Following is a sample response structure :

```json
["AADHAAR","PAN","PASSPORT"]
```
    
## 4.  **KYCVerify**  
KYCVerify Method from **DocumentObject** will verify KYC document with the **TYPE** provided.
    
```java
//KYCVerify will use callback function to return the result.
DocumentObject.KYCVerify("PAN",this::handleKYCVerifytCallBack);
*@param "Type of document to verify can get from GetKYCDocList Method".
*@param "A callback method to capture the KYCVerification response".
   
private void handleKYCVerifyCallBack(resultJson) 	{
	//Process the resultJson
}

//Or use lambda function
DocumentObject.KYCVerify("PAN",resultJson -> {
	//Detected response JSON
});
```

Following is a sample response structure :
```json
{
   STATUS: true/false,
   DESCRIPTION: "<Verified Successfully/failed>",
   //success/failure
   CONFIDENCE : "LOW/MEDIUM/HIGH",
   //if STATUS is success 
   OCRQUALITY: "<ocrquality>"
 }
```

## 5. **GetOCRText**
Retrieves OCR text data from the currently loaded image.

By default, `GetOCRText()` returns OCR text with bounding coordinates.

Depending on the value of `plainOCRText`:

- `true` → returns only OCR text content
- `false` → returns OCR text along with bounding box

```java
// Default behavior: returns OCR text with bounding coordinates
DocumentObject.GetOCRText(this::handleOCRTextCallback);

private void handleOCRTextCallback(String response) {
    // Process the resultJson
}

// Or use lambda function
DocumentObject.GetOCRText(response -> {
    // Process the resultJson
});
```
Following is a sample JSON response structure for default behaviour and when `plainOCRText = false`
```json
{
  "status": true,
  "description": "OCR Extraction Successful",
  "data": {
    "Document": {
      "fileName": "da1802f0c08d4c50b578d24a11166d55",
      "pages": [
        {
          "width": 1164,
          "height": 772,
          "items": [
            {
              "text": "Government of India",
              "left": 266,
              "top": 136,
              "right": 563,
              "bottom": 1752,
              "confidence": 96.03078
            },
            {
              "text": "SUPARNA HAZRA",
              "left": 278,
              "top": 238,
              "right": 556,
              "bottom": 274,
              "confidence": 97.0783
            }
          ]
        }
      ]
    }
  }
}
```
For getting plain OCR text only:
```java
 DocumentObject.GetOCRText(true, this::handleOCRTextCallback);

/**
 * @param plainOCRText
 *        true  ->  include only OCR text
 *        false ->  include OCR text with bounding coordinates
 *
 * @param callback
 *        A callback method to capture the GetOCRText response.
 */
private void handleOCRTextCallback(String response) {
    // Process the resultJson
}

// Or use lambda function
DocumentObject.GetOCRText(true, response -> {
    // Process the resultJson
});
```
Following is a sample JSON response structure when `plainOCRText = true`
```json
{
  "status": true,
  "description": "OCR Extraction Successful",
  "data": {
    "Document": {
      "fileName": "da1802f0c08d4c50b578d24a11166d55",
      "pages": [
        {
          "width": 738,
          "height": 1600,
          "items": [
            "Government of India",
            "SUPARNA HAZRA",
            "Female"
          ]
        }
      ]
    }
  }
}

```

# **AADHAAR MASKING**

SplicerAi also provides an activity based class for AADHAAR MASKING.

```java
//Create a new intent to call aadhaar masking activity.
Intent maskIntent = new Intent(this, Class.forName("com.extrieve.splicer.AadhaarMask"));
//Pass the ID of the document to be masked
maskIntent.putExtra("DOCUMENT_ID",DocumentObject.ID);
//Set the config for Masking options
//Keep the extracted aadhaar number in respose.
Config.AadhaarMasking.ENABLE_AADHAAR_NO = true;
//Set the masking colour
Config.AadhaarMasking.MaskColor = Color.GRAY;
//Launch the activity.
activityResultLauncher.launch(maskIntent);

//Activity launcher registration
ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
	//Will get the masked image as a result of the intent
	Intent data = result.getData();
	//data Will contain repose JSON string
});

```

AadhaarMask intent call, requires a document id of previously created AiDocument Object.  
The response data will be in following structure :

```json
{
   STATUS: true/false,
   //Masking activity status
   DESCRIPTION : "SUCCESS",
   //Success or failure description
   MASKING_STATUS : true,
   //Extracted data from KYC document
   MASKING_STATUS: {
      COUNT : "10",
      //Number of masking done
      AADHAAR_NO: "2513 5077 5668",
      //Can enable & disable in config.
      MASKED_METHOD: "MANUAL/SYSTEM"
      //Specify how the masking happened
   }
   FILE : "<File path of masked image>"
   //eg : "/storage/files/Splicer/SP_20240122_183209.jpg"
}
```
## Config

The SDK includes a supporting class called for static configuration. This class holds all configurations related to the SDK. It also contains sub-configuration collection for further organization. This includes:

**Common** - Contains various configurations as follows:

- **SDKInfo**  - Contains all version related information on SDK.
	```java
	//JAVA
	Config.Common.SDKInfo;
	```
	```kotlin
	//Kotlin
	Config!!.Common!!.SDKInfo;
	```
- **DeviceInfo** - Will share all general information about the device.
	```java
	//JAVA
	Config.Common.DeviceInfo;
	```
	```kotlin
	//Kotlin
	Config!!.Common!!.DeviceInfo;
	```
**License** - Controls all activities related to licensing.
- ***Activate*** - *Method to activate the SDK license.*
	```java
 	//JAVA
	Config.License.Activate(hostApplicationContext,licenseString);
	```
 	```kotlin
  	//Kotlin
	Config!!.License!!.Activate(hostApplicationContext,licenseString)
	```
	 > **hostApplicationContext** : Application context of host/client application which is using the SDK.
	 	 > **licenseString** : Licence data in string format.
	 


# Notes

### Document supported
**Indian KYC Documents** : List of KYC documents, their respective subtypes, and the key-value pairs "expected" from the current trained set of the SplicerAi :

1. **PAN_CARD** : NAME, FATHER'S NAME, DOB, PAN NO, DATE OF INCORPORATION, BUSINESS NAME
2. **AADHAAR** : NAME, DOB, GENDER, AADHAAR NO, ADDRESS
3. **DRIVING_LICENSE** : NAME, DOB, S/D/W, ADDRESS, DATE OF ISSUE, DATE OF EXPIRY, LICENSE NO.
4. **VOTER_ID** : NAME, DOB, GUARDIAN'S NAME, ADDRESS, UID, GENDER
5. **PASSPORT** : SURNAME, GIVEN NAME, DOB, DATE OF ISSUE, DATE OF EXPIRY, PASSPORT NO, PLACE OF BIRTH, PLACE OF ISSUE, GENDER, COUNTRY, COUNTRY CODE

Following are the extra subtype supported : (subtypes will provided as part of **SUBTYPE** poreperty in resposnse & also will available in **DESCRIPTION**)

1.  **PAN_CARD** – PAN_CORPORATE
2.  **PASSPORT** – PASSPORT_BACK
3.  **VOTER_ID** – VOTER_ID_BACK
4.  **DRIVING_LICENSE** – DL_BACK
5.  **AADHAAR** – AADHAAR_BACK

Notes
-------------

### Regarding accuracy :
*The accuracy of Detection & Extaction technologies depends significantly on the quality of input images, including factors such as image wrapping, stretching, angle of rotation, lighting conditions, and colour consistency. While offline solutions are effective for reducing manual efforts in scenarios having additional verification measures, cannot guarantee 100% accuracy.Even though we are aiming and providing 85-95% accuracy on various operation*


**Extrieve** - *Your Expert in Document Management & AI Solutions.*

[© 1996 - 2026 Extrieve Technologies](https://www.extrieve.com/)
