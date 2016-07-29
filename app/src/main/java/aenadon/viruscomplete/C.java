package aenadon.viruscomplete;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class C {

    public static final int unsafe = 0;
    public static final int safe = 1;
    public static final int unrated = 2;

    public static final int REQUEST_FILE_ACCESS = 2;
    public static final int ACTIVITY_CHOOSE_FILE = 3;

    public static void errorCheck(int responseCode, Context ctx) {
        switch (responseCode) {
            case 204:
                new AlertDialog.Builder(ctx)
                        .setTitle(ctx.getString(R.string.api_limit_exceeded_title))
                        .setMessage(ctx.getString(R.string.api_limit_exceeded_message))
                        .setPositiveButton(ctx.getString(R.string.come_back_later), null)
                        .show();
                break;
            case 403: // This shouldn't ever happen, but, just in case...
                new AlertDialog.Builder(ctx)
                        .setTitle(ctx.getString(R.string.not_permitted_title))
                        .setMessage(ctx.getString(R.string.not_permitted_message))
                        .setPositiveButton(ctx.getString(R.string.leave), null)
                        .show();
                break;
        }
    }

    // ### http://stackoverflow.com/questions/13152736/how-to-generate-an-md5-checksum-for-a-file-in-android/16938703#16938703 ###
    public static String getMD5(String filePath) {

        // Get MD5 for the selected file
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            String returnVal = "";
            for (byte md5Byte : md5Bytes) { returnVal += Integer.toString((md5Byte & 0xff) + 0x100, 16).substring(1); }
            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not calculate MD5 value";
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
