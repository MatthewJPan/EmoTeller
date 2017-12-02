package com.microsoft.projectoxford.emotionsample;

import com.microsoft.projectoxford.emotionsample.NetUtil;

/**
 * Created by mac on 17/4/25.
 * source: http://www.cnblogs.com/zyw-205520/p/3770705.html
 */


public class Application extends android.app.Application {
    private static Application mApplication;
    public static int mNetWorkState;

    public static synchronized Application getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        initData();
    }


    public void initData() {
        mNetWorkState = NetUtil.getNetworkState(this);
    }
}