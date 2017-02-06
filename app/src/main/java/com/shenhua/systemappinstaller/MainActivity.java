package com.shenhua.systemappinstaller;

import android.Manifest;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private String[] filePaths;
    private Button installBtn1;
    private Button installBtn2;
    private ItemAdapter adapter;
    private List<PackageInfo> packageInfos = new ArrayList<>();
    private static final String ASSETS_NAME = "apks";
    private static final String SD_DIR_NAME = "apks";
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.list_view);
        installBtn1 = (Button) findViewById(R.id.btn_install1);
        installBtn2 = (Button) findViewById(R.id.btn_install2);
        adapter = new ItemAdapter(this, packageInfos);
        listView.setAdapter(adapter);

        new RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            copyFiles();
                        } else {
                            Toast.makeText(MainActivity.this, "权限被用户拒绝，该功能无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void copyFiles() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在解压文件");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                CommonFileUtils.getInstance().recursionDeleteFile(new File(Environment.getExternalStorageDirectory() + File.separator + SD_DIR_NAME));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        CommonFileUtils.getInstance().copyAssetsToSD(MainActivity.this, ASSETS_NAME, SD_DIR_NAME)
                                .setFileOperateCallback(new CommonFileUtils.FileOperateCallback() {
                                    @Override
                                    public void onSuccess() {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "文件复制成功", Toast.LENGTH_SHORT).show();
                                        displayList();
                                    }

                                    @Override
                                    public void onFailed(String error) {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "文件复制失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        }).start();
    }

    private void displayList() {
        packageInfos.clear();
        File[] files = CommonFileUtils.getInstance().getSDDirFiles(SD_DIR_NAME, ".apk");
        if (files.length <= 0) return;
        filePaths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            filePaths[i] = files[i].getAbsolutePath();
            PackageInfo packageInfo = new PackageInfo(files[i].getName(), files[i].getAbsolutePath());
            packageInfos.add(packageInfo);
        }
        adapter.notifyDataSetChanged();
        if (packageInfos.size() > 0) {
            installBtn1.setEnabled(true);
            installBtn2.setEnabled(true);
        }
    }

    /**
     * 安装成系统应用
     *
     * @param view
     */
    public void oneKeyInstall(View view) {
        RootCmd rootCmd = new RootCmd(this);
        rootCmd.executeRemount(filePaths);
    }

    /**
     * 安装成普通应用
     *
     * @param view
     */
    public void serialInstall(View view) {
        for (int i = 0; i < packageInfos.size(); i++) {
            ApkOperateManager manager = new ApkOperateManager();
            manager.openApk(this, new File(packageInfos.get(i).getFilePath()));
        }
    }

    /**
     * 静默安装，需要将应用设置为系统应用 （root/system/app）
     *
     * @param file
     * @param packageName
     */
    private void installDefault(File file, String packageName) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在安装");
        ApkOperateManager manager = new ApkOperateManager();
        manager.installApkDefault(this, Uri.fromFile(file), packageName, new ApkOperateManager.OperateCallback() {
            @Override
            public void onPreDoing() {
                dialog.show();
            }

            @Override
            public void onSuccess(int returnCode) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String msg) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
