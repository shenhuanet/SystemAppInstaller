package com.shenhua.systemappinstaller;

/**
 * Created by shenhua on 1/19/2017.
 * Email shenhuanet@126.com
 */
public class PackageInfo {

    private String appName;
    private String filePath;

    public PackageInfo(String appName, String filePath) {
        this.appName = appName;
        this.filePath = filePath;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
