package com.wangdaye.mysplash.common.utils.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wangdaye.mysplash.Mysplash;
import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.data.entity.table.DownloadMissionEntity;
import com.wangdaye.mysplash.common.basic.activity.MysplashActivity;
import com.wangdaye.mysplash.common.utils.DisplayUtils;
import com.wangdaye.mysplash.common.utils.manager.ThemeManager;

/**
 * NotificationFeed helper.
 *
 * A helper class that makes the operations of {@link NotificationManager} and
 * {@link Snackbar} easier.
 *
 * */

public class NotificationHelper {

    private static final String NOTIFICATION_GROUP_KEY = "mysplash_download_result_notification";
    private static final String PREFERENCE_NOTIFICATION = "notification";
    private static final String KEY_NOTIFICATION_ID = "notification_id";

    private static final int NOTIFICATION_GROUP_SUMMARY_ID = 1001;

    // feedback.

    public static void sendDownloadPhotoSuccessNotification(Context c, DownloadMissionEntity entity) {
        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(
                        getNotificationId(c),
                        buildSingleNotification(c, "Photo", entity.getNotificationTitle(), true, true));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_GROUP_SUMMARY_ID, buildGroupSummaryNotification(c, true, true));
        }
    }

    public static void sendDownloadCollectionSuccessNotification(Context c, DownloadMissionEntity entity) {
        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(
                        getNotificationId(c),
                        buildSingleNotification(
                                c, "Collection", entity.getNotificationTitle(), false, true));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_GROUP_SUMMARY_ID, buildGroupSummaryNotification(c, false, true));
        }
    }

    public static void sendDownloadPhotoFailedNotification(Context c, DownloadMissionEntity entity) {
        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(
                        getNotificationId(c),
                        buildSingleNotification(c, "Photo", entity.getNotificationTitle(), true, false));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_GROUP_SUMMARY_ID, buildGroupSummaryNotification(c, true, false));
        }
    }

    public static void sendDownloadCollectionFailedNotification(Context c, DownloadMissionEntity entity) {
        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(
                        getNotificationId(c),
                        buildSingleNotification(
                                c, "Collection", entity.getNotificationTitle(), false, false));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_GROUP_SUMMARY_ID, buildGroupSummaryNotification(c, false, false));
        }
    }

    private static Notification buildSingleNotification(Context c, String subText, String contentText,
                                                        boolean photo, boolean succeed) {
        String title;
        if (photo && succeed) {
            title = c.getString(R.string.feedback_download_photo_success);
        } else if (photo) {
            title = c.getString(R.string.feedback_download_photo_failed);
        } else if (succeed) {
            title = c.getString(R.string.feedback_download_collection_success);
        } else {
            title = c.getString(R.string.feedback_delete_collection_failed);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setSubText(subText)
                .setContentText(contentText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setGroup(NOTIFICATION_GROUP_KEY);
        }
        return builder.build();
    }

    private static Notification buildGroupSummaryNotification(Context c, boolean photo, boolean succeed) {
        String title;
        if (photo && succeed) {
            title = c.getString(R.string.feedback_download_photo_success);
        } else if (photo) {
            title = c.getString(R.string.feedback_download_photo_failed);
        } else if (succeed) {
            title = c.getString(R.string.feedback_download_collection_success);
        } else {
            title = c.getString(R.string.feedback_delete_collection_failed);
        }
        return new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setGroupSummary(true)
                .build();
    }

    private static int getNotificationId(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(
                PREFERENCE_NOTIFICATION,
                Context.MODE_PRIVATE);
        int id = sharedPreferences.getInt(KEY_NOTIFICATION_ID, 1) + 1;
        if (id > NOTIFICATION_GROUP_SUMMARY_ID - 1) {
            id = 1;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_NOTIFICATION_ID, id);
        editor.apply();

        return id;
    }

    // snack bar.

    public static void showSnackbar(String content) {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            View container = a.provideSnackbarContainer();
            if (container != null) {
                Snackbar snackbar = Snackbar
                        .make(container, content, Snackbar.LENGTH_SHORT);

                Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
                snackbarLayout.setBackgroundColor(ThemeManager.getRootColor(a));

                TextView contentTxt = snackbarLayout.findViewById(R.id.snackbar_text);
                DisplayUtils.setTypeface(a, contentTxt);
                contentTxt.setTextColor(ThemeManager.getContentColor(a));

                snackbar.show();
            }
        }
    }

    public static void showActionSnackbar(String content, String action,
                                           View.OnClickListener l) {
        MysplashActivity a = Mysplash.getInstance().getTopActivity();
        if (a != null) {
            View container = a.provideSnackbarContainer();
            if (container != null) {
                Snackbar snackbar = Snackbar
                        .make(container, content, Snackbar.LENGTH_LONG)
                        .setAction(action, l);

                Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
                snackbarLayout.setBackgroundColor(ThemeManager.getRootColor(a));

                TextView contentTxt = snackbarLayout.findViewById(R.id.snackbar_text);
                DisplayUtils.setTypeface(a, contentTxt);
                contentTxt.setTextColor(ThemeManager.getContentColor(a));

                Button actionBtn = snackbarLayout.findViewById(R.id.snackbar_action);
                actionBtn.setTextColor(ThemeManager.getTitleColor(a));

                snackbar.show();
            }
        }
    }
}
