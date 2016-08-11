package aenadon.viruscomplete;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

class AlertDialogs {

    /*
     * This class contains all the AlertDialogs shown throughout the app
     * and can be called by simply calling AlertDialogs.desiredMessageBox(context[,failureMessage]);
     */

    public static void apiLimitExceeded(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.api_limit_exceeded_title),
                ctx.getString(R.string.api_limit_exceeded_message),
                ctx.getString(R.string.come_back_later));
    }

    public static void notPermitted403(Context ctx) { // This shouldn't ever happen, but, just in case...
        createOneButtonBox(ctx,
                ctx.getString(R.string.not_permitted_title),
                ctx.getString(R.string.not_permitted_message),
                ctx.getString(R.string.leave));
    }

    public static void invalidURL(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.invalid_url_title),
                ctx.getString(R.string.invalid_url_message),
                ctx.getString(R.string.try_again));
    }

    public static void urlScanStillQueued(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.scan_queued_title),
                ctx.getString(R.string.scan_still_queued_message),
                ctx.getString(R.string.come_back_later));
    }

    public static void urlIsQueued(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.scan_queued_title),
                ctx.getString(R.string.urlscan_queued_message),
                ctx.getString(R.string.come_back_later));
    }

    public static void resourceIsQueued(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.scan_queued_title),
                ctx.getString(R.string.file_hash_scan_queued_message),
                ctx.getString(R.string.come_back_later));
    }

    public static void notAHash(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.not_a_hash_title),
                ctx.getString(R.string.not_a_hash_message),
                ctx.getString(R.string.try_again));
    }

    public static void onFailureMessage(Context ctx, String failureMessage) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.data_error_title),
                ctx.getString(R.string.data_error_message) + failureMessage,
                ctx.getString(R.string.sorry));
    }

    public static void strangeError(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.strange_error_title),
                ctx.getString(R.string.strange_error_message),
                ctx.getString(R.string.try_again));
    }

    public static void serverError(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.server_error_title),
                ctx.getString(R.string.server_error_message),
                ctx.getString(R.string.try_again));
    }

    public static void resNotFound(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.res_not_found_title),
                ctx.getString(R.string.res_not_found_message),
                ctx.getString(R.string.try_again));
    }

    public static void permissionDenied(Context ctx) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.permission_denied_title),
                ctx.getString(R.string.permission_denied_message),
                ctx.getString(R.string.try_again));
    }


    public static void askPolitelyForPermission(final Context ctx) {
        new AlertDialog.Builder(ctx) // explain the user why we need permission
                .setTitle(ctx.getString(R.string.permission_request_title))
                .setMessage(ctx.getString(R.string.permission_request_message)) // explain why we need permission and then request it
                .setPositiveButton(ctx.getString(R.string.permission_request_button), new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show the official permission dialog when user presses "Proceed"
                        ActivityCompat.requestPermissions((Activity)ctx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, C.REQUEST_FILE_ACCESS);
                    }
                })
                .show();
    }

    // Constructs the message box
    private static void createOneButtonBox(Context ctx, String title, String message, String button) {
        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, null)
                .show();
    }


}
