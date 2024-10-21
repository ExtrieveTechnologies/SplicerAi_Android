package com.extrieve.splicerai;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.extrieve.splicer.aisdk.AiDocument;
import com.extrieve.splicer.aisdk.Config;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * <br>Note for unit & load testing:<br>
 * 1. Need to create and add separate folder for images & license files within app assets folder.<br>
 * 2. same folder name need to update in AiDocumentTest.java(referring to this file) to auto-read files.<br>
 * 3. For license: update folder name in testLicFolderName string and for images: update in testImageFolderName below<br>
 * 4. For defaultDocToVerify to validate doc type for each image input.
 */
@RunWith(AndroidJUnit4.class)
public class AiDocumentTest {

    private static Context context = ApplicationProvider.getApplicationContext();
    private static AssetManager assetManager = context.getAssets();
    private AiDocument aiDocument;
    private String imagePath = null;
    private final boolean onlyExtract = true, onlyVerify = true;
    private File[] savedFiles;
    String TAG = "SPLICERAI";
    String defaultLicPath = "TestLicenses/valid_demo_plus.lic";
    String testLicFolderName = "TestLicenses";
    String testImageFolderName = "TestImages/PAN";
    String defaultDocToVerify = "PAN CARD"; //Aadhaar,PAN CARD,Driving License,VOTER ID,PASSPORT,CHEQUE BOOK,Account opening Form
    String saveDir = null;
    ContextWrapper cw = new ContextWrapper(context);

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        assetManager = context.getAssets();
        cw = new ContextWrapper(context);
        saveDir = cw.getFilesDir().getAbsolutePath();
        assertEquals("com.extrieve.splicerai", context.getPackageName());
        try {
            runOnUiThread(this::initializeAiDocument);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        clearCacheDirectory();
    }

    @Test
    public void testGetKYCDocList() {
        ArrayList<String> docList = aiDocument.GetKYCDocList();
        assertNotNull(docList);
        assertFalse(docList.isEmpty());
    }

    @Test
    public void testKYCDetect() {
        testAiDocumentInitialization();
        CountDownLatch latch = new CountDownLatch(1);
        aiDocument.KYCDetect(response -> {
            assertKYCResponse(response);
            latch.countDown();
        });
        awaitLatch(latch);
    }

    @Test
    public void testKYCExtract() {
        testAiDocumentInitialization();
        CountDownLatch latch = new CountDownLatch(1);
        aiDocument.KYCExtract(response -> {
            assertKYCResponse(response);
            assertKeyValues(response);
            latch.countDown();
        });
        awaitLatch(latch);
    }

    @Test
    public void testKYCVerify() {
        testAiDocumentInitialization();
        ArrayList<String> docList = aiDocument.GetKYCDocList();
        assertNotNull(docList);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean status = new AtomicBoolean(false);

        verifyKYCForDocumentTypes(docList, latch, status, 1, null, null);
    }

    @Test
    public void testKYCForImageBatch() {
        int count = 1;
        assertNotNull(savedFiles);
        for (File file : savedFiles) {
            testKYCForImage(file, count);
            count++;
        }
    }

    @Test
    public void testLicenseForBatch() {
        setPathFromFiles(1);
    }

    private void testKYCForImage(File file, int count) {
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        try {
            runOnUiThread(() -> initializeAiDocumentWithFile(file));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        testAiDocumentInitialization();
        ArrayList<String> docList = aiDocument.GetKYCDocList();
        assertNotNull(docList);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean status = new AtomicBoolean(false);
        if (onlyExtract) {
            extractKYCForDocumentTypes(latch, status, file, count);
        }
        if (onlyVerify && !onlyExtract) {
            verifyKYCForDocumentTypes(docList, latch, status, count, file, null);
        }
    }

    private void verifyKYCForDocumentTypes(ArrayList<String> docList, CountDownLatch latch, AtomicBoolean status, int count, File file, String defaultDocType) {
        if (docList != null) {
            for (String documentType : docList) {
                if (status.get()) {
                    break;
                }
                assertNotNull(documentType);
                aiDocument.KYCVerify(documentType, response -> {
                    if (responseIsValid(response)) {
                        status.set(true);
                        if (file != null && file.exists()) {
                            deleteFile(file);
                        }
                        Log.i("VERIFIED:" + documentType.toUpperCase(), count + "-" + aiDocument.FILE_PATH);
                    }
                    latch.countDown();
                });
            }
        } else {
            assertNotNull(defaultDocType);
            aiDocument.KYCVerify(defaultDocType, response -> {
                if (responseIsValid(response)) {
                    status.set(true);
                    if (file != null && file.exists()) {
                        deleteFile(file);
                    }
                    Log.i("VERIFIED:" + defaultDocType.toUpperCase(), count + "-" + aiDocument.FILE_PATH);
                }
                latch.countDown();
            });
        }
        awaitLatch(latch);
    }

    private void extractKYCForDocumentTypes(CountDownLatch latch, AtomicBoolean status, File file, int count) {
        aiDocument.KYCExtract(response -> {
            if (responseIsValid(response)) {
                if (file != null && file.exists()) {
                    deleteFile(file);
                }
                Log.i("IMAGE_COUNT", count + "-" + aiDocument.FILE_PATH);
            }
            assertKYCResponse(response);
            assertKeyValues(response);
            if (onlyExtract && onlyVerify)
                verifyKYCForDocumentTypes(null, latch, status, count, file, defaultDocToVerify);
            latch.countDown();
        });
        awaitLatch(latch);
    }

    private void initializeAiDocument() {
        try {
            // Activate SDK, before initiating AiDocument class
            boolean ret = readLicenseFileAndActivate(defaultLicPath);
            if (!ret) Log.d("LICENSE", "Invalid license");

            setPathFromFiles(0);
            assertNotNull(imagePath);
            aiDocument = new AiDocument(context, imagePath);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeAiDocumentWithFile(File file) {
        try {
            // Activate SDK, before initiating AiDocument class
            boolean ret = readLicenseFileAndActivate(defaultLicPath);
            if (!ret) Log.d("LICENSE", "Invalid license");

            aiDocument = new AiDocument(context, file.toString());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void testAiDocumentInitialization() {
        assertNotNull(aiDocument);
        assertNotNull(aiDocument.FILE_PATH);
        assertNotNull(aiDocument.ID);
        assertNotNull(aiDocument.GetKYCDocList());
    }

    private void setPathFromFiles(int type) {
        imagePath = null;
        savedFiles = null;
        int count = 1;

        List<String> imageFiles = getFilesFromAssets(type);
        if (type == 0) {
            boolean ret = readLicenseFileAndActivate(defaultLicPath);
            assertTrue(ret);
            Log.i("LIC_VERIFIED", defaultLicPath);

            saveImageFiles(imageFiles);
            File cacheDir = cw.getFilesDir();
            savedFiles = cacheDir.listFiles();
            assertNotNull(savedFiles);
            for (File file : savedFiles) {
                assertTrue(file.exists());
                assertTrue(file.length() > 0);
                if (imagePath == null) {
                    imagePath = file.toString();
                }
                Log.i("IMAGE_VERIFIED", String.valueOf(count));
                count++;
            }
        } else {
            for (String file : imageFiles) {
                boolean ret = readLicenseFileAndActivate(file);
                assertTrue(ret);
                Log.i("LIC_VERIFIED", String.valueOf(count));
                count++;
            }
        }
    }

    private List<String> getFilesFromAssets(int type) {
        List<String> imageFiles = new ArrayList<>();
        String[] files;

        try {
            if (type == 1) {
                files = assetManager.list(testLicFolderName);
                if (files != null) {
                    for (String file : files) {
                        if (file.endsWith(".lic")) {
                            imageFiles.add(testLicFolderName + "/" + file);
                        }
                    }
                }
            } else {
                files = assetManager.list(testImageFolderName);
                if (files != null) {
                    for (String file : files) {
                        if (file.toLowerCase().endsWith(".jpg") || file.toLowerCase().endsWith(".png") || file.toLowerCase().endsWith(".jpeg")) {
                            imageFiles.add(file);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "No files found to read");
        }

        return imageFiles;
    }

    private void saveImageFiles(List<String> imageFiles) {
        for (String fileName : imageFiles) {
            try {
                InputStream inputStream = context.getAssets().open(testImageFolderName + "/" + fileName);
                File outputFile = new File(saveDir, fileName);

                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
                inputStream.close();
            } catch (IOException e) {
                Log.d(TAG, "Unable to save file to cache directory");
            }
        }
    }

    private void clearCacheDirectory() {
        File cacheDir = cw.getFilesDir();
        if (cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFile(file);
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (!deleteFile(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    private void assertKYCResponse(String response) {
        assertNotNull(response);
        try {
            JSONObject responseJson = new JSONObject(response);
            assertNotNull(responseJson.getBoolean("STATUS"));
            assertNotNull(responseJson.getString("DESCRIPTION"));
            assertNotNull(responseJson.getString("TYPE"));
            assertNotNull(responseJson.getString("CONFIDENCE"));
            assertNotNull(responseJson.getString("OCR_QUALITY"));
            JSONObject predictedDocs = responseJson.getJSONObject("PREDICTED_DOCS");
            //assertNotEquals(0, predictedDocs.length());
            Log.d(TAG + "_EXTRACTED", response);
        } catch (JSONException e) {
            Log.d(TAG + "_ERROR", response);
        }
    }

    private void assertKeyValues(String response) {
        assertNotNull(response);
        try {
            JSONObject responseJson = new JSONObject(response);
            JSONObject keyValues = responseJson.getJSONObject("KEY_VALUES");
            //assertNotEquals(0, keyValues.length());
            Log.d(TAG + "_KEY_VALUES", keyValues.toString());
        } catch (JSONException e) {
            Log.d(TAG + "_ERROR", response);
        }
    }

    private boolean responseIsValid(String response) {
        assertNotNull(response);
        try {
            JSONObject responseJson = new JSONObject(response);
            return responseJson.getBoolean("STATUS");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("LatchAwaitError", "Error waiting for latch", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * <h2>readLicenseFileAndActivate</h2>
     * Read the license file placed in assets folder, then activate.
     * <p>
     * This method attempts to read license file and validate the license string using an instance of {@code ExLicenseValidator}.
     * It checks if the license corresponds to a demo version by calling the {@code IsDemo} method.
     * <p>
     *
     * @param assetFileName The license file name to activate.
     * @return {@code false} Always returns {@code false}, indicating failure to activate the license.
     */
    public static boolean readLicenseFileAndActivate(String assetFileName) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(assetFileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (builder.length() > 0) {
            return Config.License.Activate(builder.toString());
        } else {
            return false;
        }
    }
}