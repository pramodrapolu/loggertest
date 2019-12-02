package com.example.loggertest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loggertest.custom.LogProcessor;

import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import ch.qos.logback.classic.LoggerContext;

public class MainActivity extends AppCompatActivity {
    private static String  TAG = "BLAH";

    private static String STORAGE_NAME = "com.example.loggertest.KEY_STORAGE";

    private static String  FILENAME = "my-log";
    private static String  FILENAME_EXT = ".log";

    private Z1Logger logger = new Z1Logger(Z1Logger.loggerFor(this.getClass()));
    private long idx = 0;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = this.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);

        setupCreateActualEnc();

        customEncryptAndDecrypt();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createLoggerEntrys();
                Snackbar.make(view, "Created Log Entries", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    private void createLoggerEntrys() {
        for (int i = 1; i < 1000; i++){
            logger.debug("i have entry #[{}]",i);
        }
    }

    private void setupCreateActualEnc() {
        Button createActual = findViewById(R.id.decrypt);
        createActual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDecFile();
            }
        });
    }

    private void createDecFile() {
        LogProcessor logProcessor = new LogProcessor(this);
        File file = this.getExternalFilesDir("logs");
        String filePath = file.getAbsolutePath() + "/" + FILENAME + "-1" + FILENAME_EXT;

        try {
            FileInputStream inputStream = new FileInputStream(filePath);

            CipherInputStream decryptedStream = (CipherInputStream) logProcessor.decrypt(inputStream);
            String filePathDec = file.getAbsolutePath() + "/" + "intermediate-file" + FILENAME_EXT;

            FileOutputStream fileOutputStream = new FileOutputStream(filePathDec);

            byte[] b = new byte[8*1024];
            int numberOfBytedRead;
            while ((numberOfBytedRead = decryptedStream.read(b)) >= 0) {
                fileOutputStream.write(b, 0, numberOfBytedRead);

            }

            inputStream.close();
            decryptedStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "createActualEnc: exception in FIS");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setupZipAndUnZipFiles() {
        TextView zipFilesText = findViewById(R.id.zip_files);
        zipFilesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "zip", Toast.LENGTH_LONG).show();

                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                Log.d("BlahP", "path ->" + loggerContext.getProperty("path"));

                File dir = getApplicationContext().getExternalFilesDir("logs");
                if (dir != null) {
                    File zipdir = getApplicationContext().getExternalFilesDir("zipDir");
                    String zipFilePath = zipdir + "/MyZippedLogs.zip";
                    try {
                        zip(getListOfFilePaths(dir), zipFilePath);
                    } catch (IOException e) {
                        Log.d("Blah", "zip: failed\n" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        TextView unZipFiles = findViewById(R.id.unzip_files);
        unZipFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File zipdir = getApplicationContext().getExternalFilesDir("zipDir");
                String zipFilePath = zipdir + "/MyZippedLogs.zip";

                try {
                    unzip(zipFilePath, zipdir.getAbsolutePath());
                } catch (IOException e) {
                    Log.d("Blah", "onClick: Failed to Unzip");
                    e.printStackTrace();
                }
            }
        });
    }

    private List<String> getListOfFilePaths(File dir) {
        List<String> filePaths = new ArrayList<>();

        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    Log.d("Blah", "getListOfFilePaths: directory");
                } else {
                    // do something here with the file
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }

        return filePaths;
    }

    public static void zip(List<String> files, String zipFile) throws IOException {
        int BUFFER_SIZE = 10 * 1024;
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte[] data = new byte[BUFFER_SIZE];

            for (String file : files) {
                FileInputStream fileInputStream = new FileInputStream(file);
                origin = new BufferedInputStream(fileInputStream, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        } finally {
            out.close();
        }

        Log.d("Blah", "zip: process done");
    }

    public static void unzip(String zipFile, String location) throws IOException {
        try {
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry zipEntry = null;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String path = location + File.separator + zipEntry.getName();

                    if (zipEntry.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        FileOutputStream fout = new FileOutputStream(path, false);

                        try {
                            for (int c = zipInputStream.read(); c != -1; c = zipInputStream.read()) {
                                fout.write(c);
                            }
                            zipInputStream.closeEntry();
                        } finally {
                            fout.close();
                        }
                    }
                }
            } finally {
                zipInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Blah", "Unzip exception", e);
        }
    }


    // Writing to the stream manually without logback.
    private void customEncryptAndDecrypt() {
        try {

            KeyGenParameterSpec keySpec;
            Key key = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenerator keygen = KeyGenerator.getInstance("AES", "AndroidKeyStore");
                keySpec = new KeyGenParameterSpec.Builder("test_enc_key", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(false)
                        .build();
                keygen.init(keySpec);
                key =  keygen.generateKey();
            }


            String fileName = "Encrypted";
            File dir = this.getExternalFilesDir("WithoutLogback");
            String oldfilePath = dir.getAbsolutePath() + "/" + fileName + FILENAME_EXT;
            String newfilePath = dir.getAbsolutePath() + "/" + "Decrypted" + FILENAME_EXT;

            Cipher cipher;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            } else {

                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            }
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();

            if (iv != null) {
                String storeIV = Base64.encodeToString(iv, Base64.DEFAULT);
                sharedPrefs.edit().putString("iv_key", storeIV).apply();
            }

            OutputStream fs = new FileOutputStream(oldfilePath);
            CipherOutputStream out = new CipherOutputStream(fs, cipher);
            for (int i = 0; i < 1000; i++){
                out.write(("i have entry #" + i + "\n").getBytes());
            }
            out.flush();
            out.close();

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Key k1 = keyStore.getKey("test_enc_key", null);

            String storeIV1 = sharedPrefs.getString("iv_key", null);
            byte[] iv1 = Base64.decode(storeIV1, Base64.DEFAULT);

            Cipher decryptCipher;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            } else {

                decryptCipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            }

            if (iv1 != null) {
                Log.d(TAG, "customEncryptAndDecrypt: iv not null");
                decryptCipher.init(Cipher.DECRYPT_MODE, k1, new IvParameterSpec(iv1));
            } else {
                Log.d(TAG, "customEncryptAndDecrypt: iv null");
                decryptCipher.init(Cipher.DECRYPT_MODE, k1);
            }

            FileInputStream fis = new FileInputStream(oldfilePath);
            CipherInputStream in = new CipherInputStream(fis, decryptCipher);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(newfilePath);

            byte[] b = new byte[1024];
            int numberOfBytedRead;
            while ((numberOfBytedRead = in.read(b)) >= 0) {
                baos.write(b, 0, numberOfBytedRead);
                fileOutputStream.write(b, 0, numberOfBytedRead);
            }
            Log.d("BlahM", "ContentM ->" + new String(baos.toByteArray()));
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException | InvalidAlgorithmParameterException | CertificateException | UnrecoverableKeyException | KeyStoreException | NoSuchProviderException ex) {

            ex.printStackTrace();
        }
    }

}
