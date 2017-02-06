package com.shenhua.systemappinstaller;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;

/**
 * cmd 操作类
 * Created by shenhua on 1/23/2017.
 * Email shenhuanet@126.com
 */
public class RootCmd {

    private final static int kSystemRootStateUnKnow = -1;
    private final static int kSystemRootStateDisable = 0;
    private final static int kSystemRootStateEnable = 1;
    private static int systemRootState = kSystemRootStateUnKnow;
    private Context context;
    private ProgressDialog dialog;
    private int size;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == size - 1 && dialog != null) {
                dialog.setMessage("应用安装完毕，正在重启设备...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        RootCmd.reboot();
                    }
                }).start();
            }
        }
    };

    public RootCmd(Context context) {
        this.context = context;
    }

    public void executeRemount(final String[] f) {
        this.size = f.length;
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在安装中...");
        dialog.show();
        final Executor executor = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                command.run();
            }
        };
        SerialExecutor serialExecutor = new SerialExecutor(executor);
        for (int i = 0; i < f.length; i++) {
            final int finalI = i;
            serialExecutor.addRun(new Runnable() {
                @Override
                public void run() {
                    remountToSystem(f[finalI]);
                    handler.obtainMessage(finalI).sendToTarget();
                }
            });
        }
        serialExecutor.scheduleNext();
    }

    private static void remountToSystem(String fileInSd) {
        System.out.println("shenhua sout:" + "start " + fileInSd);
        try {
            Process process = Runtime.getRuntime().exec("su ");
            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system\n");
            dos.writeBytes("cp " + fileInSd + " /system/app\n");
            dos.writeBytes("chmod 666 /system/app/" + CommonFileUtils.getFileName(fileInSd) + ".apk\n");
            dos.writeBytes("exit\n");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            readBR(reader);
            readBR(errorReader);
            dos.flush();
            dos.close();
//            try {
//                System.out.println("shenhua sout:--->" + process.exitValue());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readBR(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buff = new char[1024];
        int ch;
        while ((ch = reader.read(buff)) != -1) {
            sb.append(buff, 0, ch);
        }
        reader.close();
//        System.out.println("shenhua sout:cmd输出:" + sb.toString());
    }

    /**
     * 判断系统是否root
     *
     * @return true为已root
     */
    public static boolean isRootSystem() {
        if (systemRootState == kSystemRootStateEnable) {
            return true;
        } else if (systemRootState == kSystemRootStateDisable) {
            return false;
        }
        File f;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (String kSuSearchPath : kSuSearchPaths) {
                f = new File(kSuSearchPath + "su");
                if (f.exists()) {
                    systemRootState = kSystemRootStateEnable;
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        systemRootState = kSystemRootStateDisable;
        return false;
    }

    public static void reboot() {
        try {
            Process process = Runtime.getRuntime().exec("su \n");
            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes("reboot \n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
