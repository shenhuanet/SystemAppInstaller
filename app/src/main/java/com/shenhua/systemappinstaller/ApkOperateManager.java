package com.shenhua.systemappinstaller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by shenhua on 1/21/2017.
 * Email shenhuanet@126.com
 */
public class ApkOperateManager {

    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private OperateCallback mCcallback;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                mCcallback.onSuccess((Integer) msg.obj);
            } else if (msg.what == FAILED) {
                mCcallback.onFailed((String) msg.obj);
            }
        }
    };

    /***
     * 安装apk
     */
    public void installApk(Context context, String fileName) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + fileName),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 卸载apk
     */
    public void deleteApk(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
    }

    /**
     * 打开apk
     *
     * @param context context
     * @param file    new File("/system/app/app.apk")
     */
    public void openApk(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 静默安装
     */
    public void installApkDefault(Context context, Uri uri, String packageName, final OperateCallback callback) {
        this.mCcallback = callback;
        mCcallback.onPreDoing();
        int installFlag = 0;
        installFlag |= PackageManager.INSTALL_REPLACE_EXISTING;
        try {
            PackageManager pm = context.getPackageManager();
            pm.installPackage(uri, new IPackageInstallObserver.Stub() {
                @Override
                public void packageInstalled(String packageName, int returnCode) throws RemoteException {
                    System.out.println("shenhua sout:packageInstalled:" + packageName + "    code:>" + returnCode);
                    handler.obtainMessage(SUCCESS, returnCode).sendToTarget();
                }
            }, installFlag, packageName);
        } catch (Exception e) {
            e.printStackTrace();
            handler.obtainMessage(FAILED, e.getMessage()).sendToTarget();
        }
    }

    /**
     * 静默卸载
     */
    public void deleteApkDefault(Context context, String packageName, final OperateCallback callback) {
        this.mCcallback = callback;
        mCcallback.onPreDoing();
        int deleteFlag = PackageManager.DELETE_ALL_USERS;
        try {
            PackageManager pm = context.getPackageManager();
            pm.deletePackage(packageName, new IPackageDeleteObserver.Stub() {
                @Override
                public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                    System.out.println("shenhua sout:packageDeleted:" + packageName + "    code:>" + returnCode);
                    handler.obtainMessage(SUCCESS, returnCode).sendToTarget();
                }
            }, deleteFlag);
        } catch (Exception e) {
            e.printStackTrace();
            handler.obtainMessage(FAILED, e.getMessage()).sendToTarget();
        }
    }

    public interface OperateCallback {

        void onPreDoing();

        void onSuccess(int returnCode);

        void onFailed(String msg);
    }

}
