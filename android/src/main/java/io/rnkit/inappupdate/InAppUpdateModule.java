package io.rnkit.inappupdate;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * Created by carlos on 2017/7/6.
 * 给JS调用的下载模块
 */

public class InAppUpdateModule extends ReactContextBaseJavaModule implements DownloadInterface {

    private String apkLocalPath;
    private ReadableMap argsOptions;

    ReactApplicationContext reactContext;

    public InAppUpdateModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        DownloadUtil.getInstance().setDownloadInterface(this);
    }

    @Override
    public String getName() {
        return "RNKitInAppUpdateModule";
    }

    @ReactMethod
    public void downloadApk(final ReadableMap options) {
        argsOptions = options;
        String url = options.hasKey("url") ? options.getString("url") : null;
        Boolean isForce = options.hasKey("isForce") && options.getBoolean("isForce");
        Boolean isShowNotification = options.hasKey("isShowNotification") && options.getBoolean("isShowNotification");
        int strategy = options.hasKey("strategy") ? options.getInt("strategy") : 0;
        String versionName = options.hasKey("version") ? options.getString("version") : System.currentTimeMillis() + "";
        DownloadUtil.getInstance().update(getCurrentActivity(), versionName, url, isForce, strategy, isShowNotification);
    }

    @Override
    public void downloadComplete(final String path) {
        apkLocalPath = path;
        WritableMap map = Arguments.createMap();
        try {
            if (argsOptions.hasKey("algorithm")) {
                String algorithm = argsOptions.getString("algorithm");
                if (algorithm.equals("md5")) {
                    String md5 = Fingerprint.getFileMd5(apkLocalPath);
                    map.putString("md5", md5);
                } else if (algorithm.equals("etag")) {
                    String etag = Fingerprint.getEtagWithFile(apkLocalPath);
                    map.putString("etag", etag);
                }
            }
            map.putString("path", path);
            map.putBoolean("success", true);
        } catch (Exception e) {
            map.putBoolean("success", false);
        } finally {
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("RNKitApkDownloadComplete", map);
        }
    }

    @ReactMethod
    public void installApk(final String apkLocalPath) {
        DownloadUtil.getInstance().installAPK(getCurrentActivity(), apkLocalPath);
    }

    @Override
    public void startDownloadCallback() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("RNKitApkStartDownload", null);
    }

    @Override
    public void startDownloadCallback() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("RNKitApkStartDownload", null);
    }

    @Override
    public void downProgressCallback(long current, long total) {
        WritableMap map = Arguments.createMap();
        map.putInt("received", (int) current);
        map.putInt("total", (int) total);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("RNKitApkDownloadProgress", map);
    }

    @Override
    public void downloadErrorCallback(String errorMsg) {
        WritableMap map = Arguments.createMap();
        map.putString("errorMsg", errorMsg);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("RNKitApkDownloadError", map);
    }

    @Override
    public void downloadPauseCallback(String type) {
        WritableMap map = Arguments.createMap();
        map.putString("type", type);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("RNKitApkDownloadPause", map);
    }

    @ReactMethod
    @Override
    public void downloadResume() {
        DownloadUtil.getInstance().resumeDownload();
    }

    @ReactMethod
    public void pauseDownload() {
        DownloadUtil.getInstance().pauseDownload("manual");
    }

    @ReactMethod
    public void getAppMetaData(String key, Promise promise) {
        String value = AppMetaData.getAppMetaData(reactContext, key);
        promise.resolve(value);
    }
}
