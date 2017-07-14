package io.rnkit.inappupdate;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import org.lzh.framework.updatepluginlib.UpdateBuilder;
import org.lzh.framework.updatepluginlib.Updater;
import org.lzh.framework.updatepluginlib.callback.UpdateDownloadCB;
import org.lzh.framework.updatepluginlib.creator.InstallCreator;
import org.lzh.framework.updatepluginlib.model.Update;
import org.lzh.framework.updatepluginlib.strategy.UpdateStrategy;
import org.lzh.framework.updatepluginlib.util.Utils;

import java.io.File;

/**
 * Created by Administrator on 2017/7/5.
 * 下载的工具类
 */

public class DownloadUtil {

    private static DownloadUtil downloadUtil;
    /**
     * 是否显示通知栏
     */
    private boolean isShowNotification;
    /**
     * 下载APK的rul
     */
    private String url;
    private String versionName;
    private UpdateBuilder builder;
    private Update update;

    /**
     * 下载回调方法
     */
    private DownloadInterface downloadInterface;

    private DownloadUtil() {
    }

    public static DownloadUtil getInstance() {
        if (downloadUtil == null) {
            synchronized (DownloadUtil.class) {
                downloadUtil = new DownloadUtil();
            }
        }
        return downloadUtil;
    }

    /**
     * @param context            当前的Activity
     * @param url                apk下载地址
     * @param isForce            是否是强制的，如果为true，则在屏幕中间显示一个下载进度条，并且为true的情况下，会忽略网络条件和下载策略
     * @param strategy           0默认，表示只判断当前环境，如果是wifi就下载，否则就不下载
     *                           1表示监听wifi，如果当前是wifi就下载，如果当前不是wifi，就监听网络变化，如果在程序运行期间，切换到了wifi，就提示用户下载
     * @param isShowNotification 是否在通知栏显示进度，只在isForce为false的条件下生效
     */
    public void update(Context context, String url, String versionName, boolean isForce, int strategy, boolean isShowNotification) {
        this.url = url;
        this.versionName = versionName;
        this.isShowNotification = isShowNotification;
        if (isForce) {
            //强制更新
            beginDownload(true, versionName, url, false);
        } else {
            //非强制更新
            switch (strategy) {
                case 1:
                    //监听wifi策略
                    if (Utils.isConnectedByWifi()) {
                        //开始下载
                        beginDownload(false, versionName, url, isShowNotification);
                    } else {
                        //注册监听网络状态的广播
                        IntentFilter mFilter = new IntentFilter();
                        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                        context.registerReceiver(broadcastReceiver, mFilter);
                    }
                    break;
                default:
                    //默认策略
                    if (Utils.isConnectedByWifi()) {
                        //开始下载
                        beginDownload(false, versionName, url, isShowNotification);
                    }
                    break;
            }
        }
    }

    /**
     * 解除广播注册
     */
    public void unRegisterReceiver(Context context) {
        context.unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utils.isConnectedByWifi()) {
                //已经连接Wifi了，提示用户是否下载
                beginDownload(false, versionName, url, isShowNotification);
            } else {
                //判断当前是否有下载任务，如果有的话，就暂停，同时通知用户
                if (builder != null) {
                    if (((CustomDownloadWorker) builder.getDownloadWorker()).isDownloading()) {
                        //当前正在下载，暂停下载
                        pauseDownload("network");
                    }
                }
            }
        }
    };


    private void beginDownload(final boolean isForce, String url, String versionName, final boolean isShowNotification) {
        builder = UpdateBuilder.create();
        update = new Update("");
        update.setForced(isForce);
        update.setIgnore(false);
        update.setVersionName(versionName);
        update.setUpdateUrl(url);
        builder.downloadWorker(new CustomDownloadWorker());
        builder.strategy(new UpdateStrategy() {
            @Override
            public boolean isShowUpdateDialog(Update update) {
                return false;
            }

            @Override
            public boolean isAutoInstall() {
                return false;
            }

            @Override
            public boolean isShowDownloadDialog() {
                return isForce || isShowNotification;
            }
        });
        builder.installDialogCreator(new InstallCreator() {
            @Override
            public Dialog create(Update update, String path, Activity activity) {
                if (downloadInterface != null)
                    downloadInterface.downloadComplete(path);
                return null;
            }
        });
        if (isShowNotification) {
            builder.downloadDialogCreator(new NotificationDownloadCreator());
        } else {
            builder.downloadDialogCreator(null);
        }
        builder.downloadCB(new UpdateDownloadCB() {
            @Override
            public void onUpdateStart() {
                if (downloadInterface != null)
                    downloadInterface.startDownloadCallback();
            }

            @Override
            public void onUpdateComplete(File file) {
            }

            @Override
            public void onUpdateProgress(long current, long total) {
                if (downloadInterface != null)
                    downloadInterface.downProgressCallback(current, total);
            }

            @Override
            public void onUpdateError(Throwable t) {
                if (downloadInterface != null)
                    downloadInterface.downloadErrorCallback(t.getMessage());
            }
        });
        Updater.getInstance().downUpdate(update, builder);
    }

    /**
     * 暂停下载
     */
    public void pauseDownload(String type) {
        if (update != null && builder != null) {
            ((CustomDownloadWorker) builder.getDownloadWorker()).pauseDownload();
            if (downloadInterface != null)
                downloadInterface.downloadPauseCallback(type);
        }
    }

    /**
     * 恢复下载
     */
    public void resumeDownload() {
        if (update != null && builder != null) {
            Updater.getInstance().downUpdate(update, builder);
        }
    }

    /**
     * 设置下载的回调接口
     */
    public void setDownloadInterface(DownloadInterface downloadInterface) {
        this.downloadInterface = downloadInterface;
    }

    /**
     * 安装apk包
     *
     * @param path apk包的路径
     */
    public void installAPK(Context context, String path) {
        Utils.installApk(context, path);
    }
}
