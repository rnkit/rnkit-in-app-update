package io.rnkit.inappupdate;

/**
 * Created by Administrator on 2017/7/5.
 * 下载需要实现的接口
 */

public interface DownloadInterface {
    /**
     * 下载完成
     *
     * @param path 下载完成的文件路径
     */
    void downloadComplete(String path);

    /**
     * 开始下载的回调
     */
    void startDownloadCallback();

    /**
     * 下载进度的回调
     *
     * @param current 已经下载的字节大小
     * @param total   总的字节大小
     */
    void downProgressCallback(long current, long total);

    /**
     * 下载错误
     */
    void downloadErrorCallback(String errorMsg);

    /**
     * 下载被暂停的回调
     */
    void downloadPauseCallback(String type);
    /**
     * 恢复下载
     */
    void downloadResume();
}
