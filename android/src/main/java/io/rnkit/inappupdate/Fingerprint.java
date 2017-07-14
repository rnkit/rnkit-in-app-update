package io.rnkit.inappupdate;

/**
 * Created by SimMan on 2017/7/13.
 */

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Fingerprint {

    public static final int BLOCK_SIZE = 4 * 1024 * 1024;


    public static String getFileMd5(String filePath) {
        File file = new File(filePath);
        return getFileMd5(file);
    }

    /**
     * RandomAccessFile 获取文件的MD5值
     *
     * @param file 文件路径
     * @return md5
     */
    public static String getFileMd5(File file) {
        MessageDigest messageDigest;
        RandomAccessFile randomAccessFile = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            if (file == null) {
                return "";
            }
            if (!file.exists()) {
                return "";
            }
            randomAccessFile = new RandomAccessFile(file, "r");
            byte[] bytes = new byte[1024 * 1024 * 10];
            int len = 0;
            while ((len = randomAccessFile.read(bytes)) != -1) {
                messageDigest.update(bytes, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                    randomAccessFile = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 计算文件内容的etag
     *
     * @param file 文件对象
     * @return 文件内容的etag
     * @throws IOException 文件读取异常
     */
    public static String getEtagWithFile(File file) throws IOException {
        FileInputStream fi = new FileInputStream(file);
        return getEtagWithstream(fi, file.length());
    }

    /**
     * 计算文件内容的etag
     *
     * @param filePath 文件路径
     * @return 文件内容的etag
     * @throws IOException 文件读取异常
     */
    public static String getEtagWithFile(String filePath) throws IOException {
        File f = new File(filePath);
        return getEtagWithFile(f);
    }

    /**
     * 计算输入流的etag
     *
     * @param in  数据输入流
     * @param len 数据流长度
     * @return 数据流的etag值
     * @throws IOException 文件读取异常
     */
    public static String getEtagWithstream(InputStream in, long len) throws IOException {
        if (len == 0) {
            return "Fto5o-5ea0sNMlW_75VgGJCv2AcJ";
        }
        byte[] buffer = new byte[64 * 1024];
        byte[][] blocks = new byte[(int) ((len + BLOCK_SIZE - 1) / BLOCK_SIZE)][];
        for (int i = 0; i < blocks.length; i++) {
            long left = len - (long) BLOCK_SIZE * i;
            long read = left > BLOCK_SIZE ? BLOCK_SIZE : left;
            blocks[i] = oneBlock(buffer, in, (int) read);
        }
        return resultEncode(blocks);
    }

    /**
     * 单块计算hash
     *
     * @param buffer 数据缓冲区
     * @param in     输入数据
     * @param len    输入数据长度
     * @return 计算结果
     * @throws IOException 读取出错
     */
    private static byte[] oneBlock(byte[] buffer, InputStream in, int len) throws IOException {
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("sha-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //never reach
            return null;
        }
        int buffSize = buffer.length;
        while (len != 0) {
            int next = buffSize > len ? len : buffSize;
            //noinspection ResultOfMethodCallIgnored
            in.read(buffer, 0, next);
            sha1.update(buffer, 0, next);
            len -= next;
        }

        return sha1.digest();
    }

    /**
     * 合并结果
     *
     * @param sha1s 每块计算结果的列表
     * @return 最终的结果
     */

    private static String resultEncode(byte[][] sha1s) {
        byte head = 0x16;
        byte[] finalHash = sha1s[0];
        int len = finalHash.length;
        byte[] ret = new byte[len + 1];
        if (sha1s.length != 1) {
            head = (byte) 0x96;
            MessageDigest sha1 = null;
            try {
                sha1 = MessageDigest.getInstance("sha-1");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                // never reach
                return null;
            }
            for (byte[] s : sha1s) {
                sha1.update(s);
            }
            finalHash = sha1.digest();
        }
        ret[0] = head;
        System.arraycopy(finalHash, 0, ret, 1, len);
        return encodeToString(ret);
    }

    private static String encodeToString(byte[] data) {
        return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_WRAP);
    }
}