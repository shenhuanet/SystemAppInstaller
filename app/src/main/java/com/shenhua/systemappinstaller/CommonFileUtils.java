package com.shenhua.systemappinstaller;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenhua on 1/17/2017.
 * Email shenhuanet@126.com
 */
public class CommonFileUtils {

    public static CommonFileUtils getInstance() {
        return new CommonFileUtils();
    }

    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private FileOperateCallback callback;
    private volatile boolean isSuccess;
    private String errorStr;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (callback != null) {
                if (msg.what == SUCCESS) {
                    callback.onSuccess();
                }
                if (msg.what == FAILED) {
                    callback.onFailed(msg.obj.toString());
                }
            }
        }
    };

    public CommonFileUtils copyAssetsToSD(final Context context, final String srcPath, final String sdPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                copyAssetsToDst(context, srcPath, sdPath);
                if (isSuccess)
                    handler.obtainMessage(SUCCESS).sendToTarget();
                else
                    handler.obtainMessage(FAILED, errorStr).sendToTarget();
            }
        }).start();
        return this;
    }

    public void setFileOperateCallback(FileOperateCallback callback) {
        this.callback = callback;
    }

    public File[] getSDDirFiles(String sdDir, String type) {
        File filesPath = new File(Environment.getExternalStorageDirectory(), sdDir);
        if (!filesPath.isDirectory()) return null;
        FileNameFilter fileNameFilter = new FileNameFilter();
        fileNameFilter.addType(type);
        File[] files = filesPath.listFiles(fileNameFilter);
        if (files.length == 0) return null;
        return files;
    }

    public interface FileOperateCallback {
        void onSuccess();

        void onFailed(String error);
    }

    public boolean isSdcardReady() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public String getSdcardPath() {
        return Environment.getExternalStorageDirectory().toString() + File.separator;
    }

    public String getCachePath(Context context) {
        File cacheDir = context.getCacheDir();
        return cacheDir.getPath() + File.separator;
    }

    public void createDipPath(String file) {
        String parentFile = file.substring(0, file.lastIndexOf("/"));
        File file1 = new File(file);
        File parent = new File(parentFile);
        if (!file1.exists()) {
            parent.mkdirs();
            try {
                file1.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyAssetsToDst(Context context, String srcPath, String dstPath) {
        try {
            String fileNames[] = context.getAssets().list(srcPath);
            if (fileNames.length > 0) {
                File file = new File(Environment.getExternalStorageDirectory(), dstPath);
                if (!file.exists()) file.mkdirs();
                for (String fileName : fileNames) {
                    if (!srcPath.equals("")) { // assets 文件夹下的目录
                        copyAssetsToDst(context, srcPath + File.separator + fileName, dstPath + File.separator + fileName);
                    } else { // assets 文件夹
                        copyAssetsToDst(context, fileName, dstPath + File.separator + fileName);
                    }
                }
            } else {
                File outFile = new File(Environment.getExternalStorageDirectory(), dstPath);
                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            errorStr = e.getMessage();
            isSuccess = false;
        }
    }

    /**
     * 获取路径中的文件名
     *
     * @param pathAndName apks/app.apk
     * @return app
     */
    public static String getFileName(String pathAndName) {
        int start = pathAndName.lastIndexOf("/");
        int end = pathAndName.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathAndName.substring(start + 1, end);
        } else {
            return null;
        }

    }

    public class FileNameFilter implements FilenameFilter {

        List<String> types;

        /**
         * 构造一个FileNameFilter对象，其指定文件类型为空。
         */
        protected FileNameFilter() {
            types = new ArrayList<>();
        }

        /**
         * 构造一个FileNameFilter对象，具有指定的文件类型。
         *
         * @param types ".apk"
         */
        protected FileNameFilter(List<String> types) {
            super();
            this.types = types;
        }

        @Override
        public boolean accept(File dir, String filename) {
            for (String type : types) {
                if (filename.endsWith(type)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 添加指定类型的文件。
         *
         * @param type 将添加的文件类型，如".mp3"。
         */
        public void addType(String type) {
            types.add(type);
        }
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public void recursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                recursionDeleteFile(f);
            }
            file.delete();
        }
    }
}
