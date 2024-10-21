/*
 * Copyright (c) 2024. Extrieve Technologies Pvt. Ltd. All rights reserved.
 *
 * No part of this [software/program/application/documentation] may be reproduced, distributed, or transmitted in any form or by any means, including photocopying, recording, or other electronic or mechanical methods, without the prior written permission of the publisher, except in the case of brief quotations embodied in critical reviews and certain other noncommercial uses permitted by copyright law. For permission requests, write to the publisher, addressed “Attention: Permissions Coordinator,” at the address below.
 *
 * Extrieve Technologies
 * Enterprise DMS, Workflow, OCR, PDF solutions & SDKs with AI
 * www.extrieve.com
 * Info@extrieve.com | devsupport@extrieve.com
 *
 */
package com.extrieve.splicerai;

import static android.content.ContentValues.TAG;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.extrieve.splicer.aisdk.AiDocument;
import com.extrieve.splicer.aisdk.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class KYCDocAnalysis extends AppCompatActivity {
    ImageView inputImage, copy, share, saveJson;
    Button kycDetect, kycExtract, kycVerify;
    Spinner selectDocument;
    TextView showResult, plainText, jsonText;
    ScrollView showResultInTableView;
    LinearLayout showResultInTable;
    String filePath, resData, extractedJson;
    Boolean resStatus;
    File imgFile;
    List<String> dynamicData;
    /*DEV_HELP : Declare variables for the classes from SDK*/
    AiDocument aiDocument = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kyc_doc_analysis);

        inputImage = findViewById(R.id.inputImage);
        copy = findViewById(R.id.copy);
        share = findViewById(R.id.share);
        saveJson = findViewById(R.id.saveJson);
        kycDetect = findViewById(R.id.kycDetect);
        kycExtract = findViewById(R.id.kycExtract);
        kycVerify = findViewById(R.id.kycVerify);
        selectDocument = findViewById(R.id.selectDocument);
        showResult = findViewById(R.id.showResult);
        showResultInTable = findViewById(R.id.showResultInTable);
        showResultInTableView = findViewById(R.id.showResultInTableView);
        plainText = findViewById(R.id.plainText);
        jsonText = findViewById(R.id.jsonText);
        showResult.setMovementMethod(new ScrollingMovementMethod());

        //get intent
        Intent TextractIntent = getIntent();
        filePath = TextractIntent.getStringExtra("KYCDocImagePath");
        // Activate SDK, before initiating AiDocument class
        String LicString = "eJxazXCBkQENMEIxMtg7q+LaWcdnDrxgHgtYng+JzQ9lMwFJDjCbgyEzryS1KC8xhxMql5Kam88DZnMyBBfkZCanFjlmMoMFBECEi8aC+85gesl9ZxGweAlDcn6uXmpFSVFmalmqXjFEl15iZnFKNrqzyQdMaHxTzxAXZqCDQ4JCXQEAAAD//wMAClwgzw==";
        boolean ret = Config.License.Activate(this, LicString);
        if (!ret) Toast.makeText(this, "Expired license, Use valid one", Toast.LENGTH_SHORT).show();
        //to trigger all on create actions
        onInit();

        kycDetect.setOnClickListener(v -> {
            doKycDetection();
        });
        kycExtract.setOnClickListener(v -> {
            doKycExtraction();
        });
        kycVerify.setOnClickListener(v -> {
            doKycVerification();
        });
        plainText.setOnClickListener(view -> {
            convertResponse(1);
        });
        jsonText.setOnClickListener(view -> {
            convertResponse(2);
        });
        copy.setOnClickListener(v -> {
            copyDataFromTextview();
        });
        share.setOnClickListener(v -> {
            shareText();
        });
        saveJson.setOnClickListener(v -> {
            saveJsonToDisk();
        });
    }

    /*DEV_HELP : On create UI & Initialising of AiDocument class*/
    private void onInit() {
        resStatus = false;
        extractedJson = null;
        saveJson.setVisibility(View.GONE);
        kycVerify.setVisibility(View.GONE);
        if (filePath != null) {
            imgFile = new File(filePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                inputImage.setImageBitmap(myBitmap);
                try {
                    /* DEV_HELP : Initialise object of AiDocument class.
                    Pass the current activity context & image Path to process kyc extraction */
                    aiDocument = new AiDocument(this, filePath);
                    dynamicData = new ArrayList<>();
                    dynamicData = aiDocument.GetKYCDocList();
                    if (!dynamicData.isEmpty()) kycVerify.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dynamicData);
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    selectDocument.setAdapter(adapter);
                    selectDocument.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            String selectedOption = dynamicData.get(position);
                            Log.d(TAG, "Doc_selected: " + selectedOption);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // Do nothing here
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    /*DEV_HELP : To get document type, confidence, possible document with confidence scale*/
    private void doKycDetection() {
        resData = null;
        aiDocument.KYCDetect(response -> {
            try {
                if (response == null) {
                    resData = "Detection is not enabled";
                    Toast.makeText(this, "Detection is not enabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject data = new JSONObject(response);
                resData = data.toString(4);
                convertResponse(1);
            } catch (JSONException e) {
                resData = "Runtime Json error";
            }
        });
    }
    /*DEV_HELP : To extract details like name, gender, dob, id number, etc..*/
    private void doKycExtraction() {
        resData = null;
        aiDocument.KYCExtract(response -> {
            try {
                if (response == null) {
                    resData = "Enable extraction feature";
                    Toast.makeText(this, "Enable extraction feature", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject data = new JSONObject(response);
                resData = extractedJson = data.toString(4);
                if(extractedJson!=null) saveJson.setVisibility(View.VISIBLE);
                convertResponse(1);
            } catch (JSONException e) {
                resData = "Runtime Json error";
            }
        });
    }
    /*DEV_HELP : To verify document type*/
    private void doKycVerification() {
        String docType = dynamicData.get(selectDocument.getSelectedItemPosition());
        if (docType.isEmpty()) return;
        resData = null;
        /* DEV_HELP : docName can be Aadhaar, Pan, Passport, etc..
        refer GetKYCDocList method to know all available docs */
        aiDocument.KYCVerify(docType, response -> {
            try {
                if (response == null) {
                    resData = "Enable extraction feature";
                    Toast.makeText(this, "Enable extraction feature", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject data = new JSONObject(response);
                resData = data.toString(4);
                convertResponse(1);
            } catch (JSONException e) {
                resData = "Runtime Json error";
            }
        });
    }

    /*DEV_HELP : Non-sdk methods for copy, share, save to downloads */
    private void convertResponse(int i) {
        showResult.setText("");
        if (i == 1) {
            plainText.setBackgroundResource(R.drawable.button_bg);
            jsonText.setBackgroundResource(0);
            showResult.setVisibility(View.GONE);
            showResultInTableView.setVisibility(View.VISIBLE);

            if (resData != null) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(resData);
                    TableLayout tableLayout = new TableLayout(this);
                    tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    tableLayout.setStretchAllColumns(true);
                    createTableRows(jsonObject, tableLayout);
                    showResultInTable.removeAllViews();
                    showResultInTable.addView(tableLayout);
                    showResult.setText(resData);
                } catch (JSONException e) {
                    Log.d("Error", "Runtime error in convertResponse: "+ e);
                }
            } else {
                showResult.setVisibility(View.VISIBLE);
                showResultInTableView.setVisibility(View.GONE);
                showResult.setText(R.string.no_data_found);
            }
        } else {
            plainText.setBackgroundResource(0);
            jsonText.setBackgroundResource(R.drawable.button_bg);
            showResult.setVisibility(View.VISIBLE);
            showResultInTableView.setVisibility(View.GONE);

            if (resData != null) {
                showResult.setText(resData);
            } else {
                showResult.setText(R.string.no_data_found);
            }
        }
    }

    private void createTableRows(JSONObject jsonObject, TableLayout tableLayout) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            TableRow tableRow = new TableRow(this);

            TextView keyTextView = new TextView(this);
            keyTextView.setText(key);
            keyTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            keyTextView.setPadding(20, 10, 10, 10);
            keyTextView.setGravity(Gravity.CENTER);
            keyTextView.setTextColor(Color.BLACK);
            keyTextView.setSingleLine(false); // Allow text to wrap to the next line

            Object value = null;
            try {
                value = jsonObject.get(key);
            } catch (JSONException ignored) {
            }

            if (value instanceof JSONObject) {
                TextView nestedHeaderTextView = new TextView(this);
                nestedHeaderTextView.setText(key);
                nestedHeaderTextView.setTypeface(null, Typeface.BOLD);
                nestedHeaderTextView.setGravity(Gravity.CENTER);
                nestedHeaderTextView.setPadding(10, 10, 10, 10);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.setMargins(10, 10, 10, 10);
                nestedHeaderTextView.setLayoutParams(layoutParams);
                nestedHeaderTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                nestedHeaderTextView.setBackgroundColor(Color.WHITE);
                nestedHeaderTextView.setTextColor(Color.parseColor("#076a8e"));
                nestedHeaderTextView.setSingleLine(false); // Allow text to wrap to the next line
                tableLayout.addView(nestedHeaderTextView);
                createTableRows((JSONObject) value, tableLayout);
            } else {

                TextView valueTextView = new TextView(this);
                valueTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                valueTextView.setSingleLine(false); // Allow text to wrap to the next line
                valueTextView.setEllipsize(null); // Ensure text is not truncated
                valueTextView.setPadding(10, 10, 10, 10);
                // Set layout parameters for the TextView to ensure it wraps content within the TableRow
                TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                valueTextView.setLayoutParams(params);

                // Optional: Set padding to ensure text does not touch the edges
                valueTextView.setPadding(10, 10, 10, 10);

                if (value != null && value.equals(true)) {
                    resStatus = true;
                }
                if (Boolean.TRUE.equals(resStatus)) { // Use Boolean.TRUE for comparison
                    valueTextView.setTextColor(Color.parseColor("#029902"));
                } else {
                    valueTextView.setTextColor(Color.BLACK);
                }
                valueTextView.setText(Objects.requireNonNull(value).toString());
                valueTextView.setGravity(Gravity.CENTER);
                valueTextView.setTypeface(null, Typeface.BOLD);
                tableRow.addView(keyTextView);
                tableRow.addView(valueTextView);
                tableLayout.addView(tableRow);
            }
        }
        resStatus = false;
    }

    private void copyDataFromTextview() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("KYCDocAnalysis", showResult.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Response Json copied", Toast.LENGTH_SHORT).show();
    }

    private void shareText() {
        String s = String.valueOf(showResult.getText());
        Intent shareIn = new Intent(Intent.ACTION_SEND);
        shareIn.setType("text/plain");
        shareIn.putExtra(Intent.EXTRA_SUBJECT, "KYCDocAnalysis");
        shareIn.putExtra(Intent.EXTRA_TEXT, s);
        startActivity(Intent.createChooser(shareIn, "Share Response Json Via"));
    }

    private void saveJsonToDisk() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            saveFileUsingMediaStore();
        } else {
            saveFileLegacy();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveFileUsingMediaStore() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "extraction_" + timeStamp + ".json";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Splicer");

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

        try (OutputStream outputStream = resolver.openOutputStream(uri)) {
            outputStream.write(extractedJson.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Saved JSON to Downloads/Splicer", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.d("Error", "Runtime error in saveJsonToDisk: " + e);
            saveJson.setVisibility(View.GONE);
        }
    }

    private void saveFileLegacy() {
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(downloadsFolder + File.separator + "Splicer" + File.separator, "extraction_" + timeStamp + ".json");

        // Ensure the folder exists
        file.getParentFile().mkdirs();

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(extractedJson);
            Toast.makeText(this, "Saved JSON to Downloads/Splicer", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.d("Error", "Runtime error in saveJsonToDisk: " + e);
            saveJson.setVisibility(View.GONE);
        }
    }
}