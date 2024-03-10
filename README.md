
<img class="img-fluid" align="center" src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/QuickCapture.png?raw=true" width="30%" alt="img-verification"><img align="right" class="img-fluid" padding="10px" src="https://github.com/ExtrieveTechnologies/QuickCapture/blob/main/android.png?raw=true?raw=true" alt="img-verification">

# SplicerAi Intelligent Document Analysis SDK
SplicerAi Intelligent Document Analysis SDK Specially designed for native ANDROID from [Extrieve](https://www.extrieve.com/).

SplicerAi is an intelligent document analysis SDK offering the following functionalities:

1. **Document Identification / Classification** : *Identification and categorization of documents*.
2. **Document Data Extraction** : *Extraction of textual data from documents*.
3. **Document Masking** : *Specific to Aadhaar documents, ensuring data privacy*.

In SplicerAi, prior to any document analysis, an independent document object needs to be created using the `AiDocument` class provided by the SplicerAi SDK.
```java
//Creating a new document object.
AiDocument DocumentObject = new AiDocument(ImagePath);
//ImagePath - string type - path of document image 
```
**DocumentObject** will generate with following properties :

1.  **ID** : *Identifier for the unique document object.Can use for further reference*.
2.  **TYPE** : *Property to specefy the KYC document type (AADHAAR,PAN etc.)*.
3.  **PATH** : *Physical path of the document image file*.
4.  **DATA** : *Text/character data will be identified and extract from image*.

**DocumentObject** will be available with following **Methods** :

1.  **KYCDetect**
	       KYCDetect Method from **DocumentObject** will provide the **type of the KYC document** with the document analysis intelligence by SplicerAi.
    ```java
    //KYCDetect will use callback function to return the result.
    DocumentObject.KYCDetect(handleKYCDetectCallBack);
    function handleKYCDetectCallBack(resultJson) {
	    //Use detect respose resultJson here
	}
	//Or use lambda 
	DocumentObject.KYCDetect(resultJson -> {
	    //Use detect respose resultJson here
	});
    ```
	Once the document type identified same also will be available in the **TYPE** property of **DocumentObject** .Following is the response structure :
	```JSON
	//Respose JSON data
	{
	   DOCNAME: "<name/type of document>",
	   Confidence: "<Level of accuracy /High>",
	   predictedDocs: {
		 //If any other documents detected,
		 //Same will list out with the confidence level
	      Aadhaar: "HIGH"
	   },
	   //classification succes or not
	   CLASSIFICATION: true/false,
	}
	```

2. **KYCExtract**
ExtractData Method from DocumentObject will provide the extracted data from the provided document in a JSON string format.
	  ```java
	//KYCDetect will use callback function to return the result.
	DocumentObject.KYCExtract(handleKYCExtractCallBack);
	  function handleKYCExtractCallBack(resultJson) {
		// Code to process the resultJson
	}
	//Or use lambda 
	DocumentObject.KYCExtract(resultJson -> {
		//Detect respose
	});
	```
	Once the document data extracted, same will be available in the DATA property of DocumentObject.Following is the response structure :
	```JSON
	{
	   DOCNAME: "AADHAAR",
	   Confidence: "High",
	   predictedDocs: {
	      Aadhaar: "HIGH"
	   },
	   CLASSIFICATION: true,
	   //Extracted data from KYC document
	   KEYVALUE: {
	      NAME: "NAME",
	      GENDER: "MALE",
	      DOB: "16/09/1981",
	      AADHAARNO: "2513 5077 5668",
	      FILENAME: ""
	   }
	}
	```
3. **GetKYCDocList**
	 GetKYCDocList Method from **DocumentObject** will provide the list of available KYC document.
    ```java
    //GetKYCDocList will return all the possible KYC document types.
    String KYCDOcList = DocumentObject.GetKYCDocList();
    ```
    Following is a sample response structure :
	```JSON
	["AADHAAR","PAN","PASSPORT"]
	```

4. **KYCVerify**
	KYCVerify Method from **DocumentObject** will verify the KYC document with the **TYPE** provided.
	```java
	//KYCVerify will use callback function to return the result.
	DocumentObject.KYCVerify("PAN",handleKYCVerifytCallBack);
	*@param "Type of document to verify can get from GetKYCDocList Method".
	*@param "A callback method to capture the KYCVerification response".
    
	function handleKYCVerifyCallBack(resultJson) 	{
		//Process the resultJson
	}
	
	//Or use lambda function
	DocumentObject.KYCVerify("PAN",resultJson -> {
		//Detected response JSON
	});
	```
	
    Following is a sample response structure :
	
	```JSON
	{
	   STATUS: true/false,
	   //success/failure
	   CONFIDENCE : "LOW/MEDIUM/HIGH"
	   //if STATUS is success 
	 }
	```
	 
**AADHAAR MASKING**

SplicerAi also provide an activity based class for AADHAAR MASKING.
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
On AadhaarMask intent call, requires document id of previously created AiDocument Object.
The respose data will be in following structure :
```JSON
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

---
[© 1996 - 2023 Extrieve Technologies](https://www.extrieve.com/)
