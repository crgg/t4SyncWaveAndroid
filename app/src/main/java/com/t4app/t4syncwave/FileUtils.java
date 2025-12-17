package com.t4app.t4syncwave;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUtils {

    public static MultipartBody.Part createAudioPart(
            Context context,
            Uri audioUri,
            String formFieldName) throws IOException {

        File audioFile = audioFileFromUri(context, audioUri);

        String mimeType = context.getContentResolver().getType(audioUri);
        if (mimeType == null) {
            mimeType = "audio/*";
        }

        RequestBody requestBody =
                RequestBody.create(audioFile, MediaType.parse(mimeType));

        return MultipartBody.Part.createFormData(
                formFieldName,
                audioFile.getName(),
                requestBody
        );
    }


    public static File audioFileFromUri(Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        String fileName = getFileName(resolver, uri);
        if (fileName == null) {
            fileName = "audio_" + System.currentTimeMillis();
        }

        File outFile = new File(context.getCacheDir(), fileName);

        try (InputStream in = resolver.openInputStream(uri);
             OutputStream out = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[8 * 1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }

        return outFile;
    }

    private static String getFileName(ContentResolver resolver, Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

}
