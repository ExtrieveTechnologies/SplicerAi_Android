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

import android.Manifest;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.extrieve.splicer.aisdk.AiDocument;
import com.extrieve.splicer.aisdk.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ImageView imageViewer;
    LinearLayout maskImageButton, kycAnalyseButton, buildOutputButton;
    private static final int PICK_IMAGE = 1;
    private static final int MASK_IMAGE = 2;
    private static final int KYC_DOC_IMAGE = 3;
    String type = null;
    Uri imgUri;
    Bitmap bitmap;
    String bmp = null;

    /*DEV_HELP : Declare variables for ActivityResultLauncher to accept result from masking activity
     * As AadhaarMask is an activity based class*/
    ActivityResultLauncher<Intent> maskingResultLauncher = null, kycDocResultLauncher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewer = findViewById(R.id.displayImageView);
        maskImageButton = findViewById(R.id.maskImageButton);
        kycAnalyseButton = findViewById(R.id.kycAnalyseButton);
        buildOutputButton = findViewById(R.id.buildOutputButton);
        maskImageButton.setOnClickListener(view -> ActionCall(1));
        kycAnalyseButton.setOnClickListener(view -> ActionCall(2));
        buildOutputButton.setOnClickListener(view -> ActionCall(3));

        /*DEV_HELP : Basic permission for App/SDK to work*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        /*DEV_HELP : Activate SDK with license string before initiating AiDocument class*/
        String LicString = "eJxazXCBkQENMEIxMiiSOW741fGZAy+YxwKW54OyQYAfymYCkhxgNg+Dh4uTW7BumKufi38QJ1hMgCEltUxBNzOvJDW9KLEkMz+PByzOyRBckJOZnFrkmMkMVQgEHhoL7jsDaRcDIC0CFi9hSM7P1UutKCnKTC1L1SuG6NJLzCxOyUb3BvmACY0Pdg2DGFa7EzNFkdQgA1PPEBdmYJCEBIW6AgAAAP//AwDs1C2N";
        boolean ret = Config.License.Activate(this, LicString);
        if (!ret)
            Toast.makeText(this, "Expired license, Use valid one", Toast.LENGTH_SHORT).show();

        /*DEV_HELP : set config settings for masking & document classifier*/
        Config.AadhaarMasking.MaskMode = "AUTOMATE";
        Config.AadhaarMasking.MaskType = "PARTIAL";
        Config.AadhaarMasking.MaskAllAadhaarNumber = true;
        Config.AadhaarMasking.MaskColor = String.valueOf(Color.YELLOW);
        Config.AadhaarMasking.MaskedFile = "OVERWRITE";
        Config.DocumentClassification.RemoveNullFields = false;
        Config.DocumentClassification.ResponseType = "JSON";

        /* DEV_HELP : assign ActivityResult for getting result from masking & kyc doc */
        maskingResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            handleMaskingResponse(result.getResultCode(), result.getData());
        });
        kycDocResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            handleKycDocResponse(result.getResultCode(), result.getData());
        });
    }

    /* DEV_HELP : To handle post masking & kyc doc extraction */
    private void handleMaskingResponse(int resultCode, @Nullable Intent data) {
        String maskResponse = Objects.requireNonNull(data.getExtras()).getString("DATA");
        if (resultCode == RESULT_OK && maskResponse != null){
            imageViewer.setVisibility(View.VISIBLE);
            try {
                if (maskResponse == null) {
                    finishActivity(MASK_IMAGE);
                    return;
                }
                JSONObject respDtObj = new JSONObject(maskResponse);
                boolean STATUS = respDtObj.getBoolean("STATUS");
                String DESCRIPTION = respDtObj.getString("DESCRIPTION");
                if (STATUS) {
                    String maskedImgPath = respDtObj.getString("MASK_PATH");
                    Bitmap bitmap = BitmapFactory.decodeFile(maskedImgPath);
                    imageViewer.setImageBitmap(bitmap);
                } else {
                    imageViewer.setVisibility(View.VISIBLE);
                    imageViewer.setImageResource(R.drawable.logofirst);
                    Toast.makeText(this, DESCRIPTION, Toast.LENGTH_LONG).show();
                }
                finishActivity(MASK_IMAGE);
            } catch (JSONException e) {
                Log.d("Error", "Runtime error in onActivityResult: " + e);
            }
            Log.d(TAG, "MaskingProcess: " + Objects.requireNonNull(data.getExtras()).get("DATA"));
            finishActivity(MASK_IMAGE);
        }
    }
    private void handleKycDocResponse(int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            imageViewer.setVisibility(View.VISIBLE);
            Boolean status = (Boolean) Objects.requireNonNull(data).getExtras().get("STATUS");
            String ErrReason = String.valueOf(data.getExtras().get("ERROR"));
            if (Boolean.TRUE.equals(status)) {
                String maskedImgPath = String.valueOf(Objects.requireNonNull(data).getExtras().get("MASKED_IMG_PATH"));
                Bitmap bitmap = BitmapFactory.decodeFile(maskedImgPath);
                imageViewer.setImageBitmap(bitmap);
            } else {
                imageViewer.setVisibility(View.VISIBLE);
                imageViewer.setImageResource(R.drawable.logofirst);
                Toast.makeText(this, ErrReason, Toast.LENGTH_LONG).show();
            }
            Log.d(TAG, "KycDocProcess: " + Objects.requireNonNull(data.getExtras()).get("MASKED_IMG_PATH"));
            finishActivity(KYC_DOC_IMAGE);
        }
    }

    /* DEV_HELP : To init the aadhaar masking process */
    private void ProcessAadhaarMasking(String imagePath) {
        Intent maskIntent = null;
        try {
            /* DEV_HELP : Initialise object of AiDocument class.
            Pass the current activity context & image Path to mask */
            AiDocument aadhaarDoc1 = new AiDocument(this, imagePath);
            Log.d(TAG, "Document initialised successfully");

            /* DEV_HELP : ID is unique id generated for each document */
            Log.d(TAG, "ID: " + aadhaarDoc1.ID);
            Log.d(TAG, "TYPE: " + aadhaarDoc1.TYPE);
            Log.d(TAG, "FILE_PATH: " + aadhaarDoc1.FILE_PATH);

            /* DEV_HELP :redirecting to aadhaar masking activity with ID*/
            try {
                maskIntent = new Intent(this, Class.forName("com.extrieve.splicer.aisdk.AadhaarMask"));
                maskIntent.putExtra("DOCUMENT_ID", aadhaarDoc1.ID);
                Config.AadhaarMasking.EXTRACT_AADHAAR_NUMBER = true;
                maskingResultLauncher.launch(maskIntent);
            } catch (Exception e) {
                Log.d("Error", "Runtime error in AadhaarMask: " + e);
            }
        } catch (Exception e) {
            Log.d("Error", "Runtime error in CheckAadhaarAndProcessMasking: " + e);
            Toast.makeText(this, "Failed to create document object :" + e, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        bitmap = null;
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && requestCode != RESULT_CANCELED) {
            imageViewer.setVisibility(View.VISIBLE);
            imgUri = Objects.requireNonNull(data).getData();
            File photoFile = null;
            String outPath = "";
            File imgFile = null;

            try {
                imgFile = new File(BuildStoragePath());
                photoFile = createImageFile(imgFile);
                outPath = photoFile.getAbsolutePath();
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
            } catch (IOException ex) {
                Toast.makeText(this, "photoFile  creation failed", Toast.LENGTH_LONG).show();
                return;
            }
            if (!outPath.isEmpty()) {
                if (bitmap == null) {
                    Toast.makeText(this, "Failed to open the photo", Toast.LENGTH_LONG).show();
                    imageViewer.setImageBitmap(bitmap);
                    return;
                }
                bmp = saveImage(bitmap);
                Intent KYCDocIntent = null;
                try {
                    if (type == null) {
                        Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    switch (type) {
                        case "Mask":
                            ProcessAadhaarMasking(bmp);
                            break;
                        case "AiDocument":
                            KYCDocIntent = new Intent(this, Class.forName("com.extrieve.splicerai.KYCDocAnalysis"));
                            if (bmp != null) KYCDocIntent.putExtra("KYCDocImagePath", bmp);
                            startActivityForResult(KYCDocIntent, KYC_DOC_IMAGE);
                            break;
                    }
                } catch (ClassNotFoundException e) {
                    Log.d("Error", "Runtime error in onActivityResult: " + e);
                }
                //end added by karthik
                Log.d("KARTHIK", "Image Received");
            }
            finishActivity(PICK_IMAGE);
        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_CANCELED) {
            type = null;
            Log.d("KARTHIK", "cancelled by user");
        } else {
            Log.d("KARTHIK", "No file selected");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*DEV_HELP : Non-sdk methods*/
    private void ActionCall(int UType) {
        if (UType == 1) {
            type = "Mask";
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(gallery, "Select Image"), PICK_IMAGE);
        }
        if (UType == 2) {
            type = "AiDocument";
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(gallery, "Select Image"), PICK_IMAGE);
        }
        if (UType == 3) {
            if (type == null) {
                Toast.makeText(this, "Please mask image to\nsave to Downloads", Toast.LENGTH_SHORT).show();
                return;
            }
            String fPath = saveFinalBuildImage();
            if (!fPath.isEmpty()) {
                Toast.makeText(this, "Saved to Downloads/Splicer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String BuildStoragePath() {
        ContextWrapper c = new ContextWrapper(this);
        return Objects.requireNonNull(c.getExternalFilesDir(".MaskableImages")).getAbsolutePath();
    }

    public static File createImageFile(File BaseFolder) throws IOException {
        Log.d(TAG, "START createImageFile");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Splicer_" + timeStamp + ".jpg";
        File image = new File(BaseFolder, imageFileName);
        Log.d(TAG, "END createImageFile");
        return image;
    }

    private String saveImage(Bitmap finalBitmap) {
        String root = BuildStoragePath();
        File myDir = new File(root);
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fName = "Splicer_" + timeStamp + ".jpg";
        File file = new File(myDir, fName);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.d("Error", "Runtime error in saveImage: " + e);
        }
        return String.valueOf(file);
    }

    private String saveFinalBuildImage() {
        BitmapDrawable drawable = (BitmapDrawable) imageViewer.getDrawable();
        Bitmap finalBitmap = drawable.getBitmap();
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File myDir = new File(root + File.separator + "Splicer");
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fName = "Splicer_" + timeStamp + ".jpg";
        File file = new File(myDir, fName);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + File.separator + "Splicer" + File.separator + fName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.d("Error", "Runtime error in saveFinalBuildImage: " + e);
        }
        return file.getAbsolutePath();
    }
}