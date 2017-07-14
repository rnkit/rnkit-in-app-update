package io.rnkit.inappupdate;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;

import org.lzh.framework.updatepluginlib.callback.UpdateDownloadCB;
import org.lzh.framework.updatepluginlib.creator.DownloadCreator;
import org.lzh.framework.updatepluginlib.model.Update;

import java.io.File;
import java.util.UUID;

public class NotificationDownloadCreator implements DownloadCreator {
    @Override
    public UpdateDownloadCB create(Update update, Activity activity) {
        return new NotificationCB(activity);
    }

    private static class NotificationCB implements UpdateDownloadCB {

        NotificationManager manager;
        NotificationCompat.Builder builder;
        int id;

        NotificationCB (Activity activity) {
            this.manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            builder = new NotificationCompat.Builder(activity);
            builder.setProgress(100, 0, false)
                    .setSmallIcon(activity.getApplicationInfo().icon)
                    .setAutoCancel(false)
                    .setContentText("Download...")
                    .build();
            id = Math.abs(UUID.randomUUID().hashCode());
        }

        @Override
        public void onUpdateStart() {
            manager.notify(id,builder.build());
        }

        @Override
        public void onUpdateComplete(File file) {
            manager.cancel(id);
        }

        @Override
        public void onUpdateProgress(long current, long total) {
            int progress = (int) (current * 1f / total * 100);
            builder.setProgress(100,progress,false);
            manager.notify(id,builder.build());
        }

        @Override
        public void onUpdateError(Throwable t) {
            manager.cancel(id);
        }
    }
}
