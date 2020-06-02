package com.xingqi.code.commonlib2.delegate;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;


import com.xingqi.code.commonlib2.ConfigModule;
import com.xingqi.code.commonlib2.integration.ManifestParser;

import java.util.ArrayList;
import java.util.List;

public class AppDelegate implements AppLifecycles{

    private Application mApplication;

    private List<ConfigModule> mModules;

    private List<AppLifecycles> mAppLifecycles = new ArrayList<>();
    private List<Application.ActivityLifecycleCallbacks> mActivityLifecycles = new ArrayList<>();

    protected Application.ActivityLifecycleCallbacks mActivityLifecycle;

    public AppDelegate(@NonNull Context context) {
        this.mModules = new ManifestParser(context).parse();
        for (ConfigModule module : mModules) {
            module.injectAppLifecycle(context, mAppLifecycles);
            module.injectActivityLifecycle(context, mActivityLifecycles);
        }
    }

    @Override
    public void attachBaseContext(@NonNull Context base) {
        for (AppLifecycles lifecycle : mAppLifecycles) {
            lifecycle.attachBaseContext(base);
        }
    }

    @Override
    public void onCreate(@NonNull Application application) {
        this.mApplication = application;
        this.mModules = null;
        mApplication.registerActivityLifecycleCallbacks(mActivityLifecycle);
        //框架外ActivityLifecycleCallback
        for (Application.ActivityLifecycleCallbacks lifecycle : mActivityLifecycles) {
            mApplication.registerActivityLifecycleCallbacks(lifecycle);
        }
        //执行框架外部, 开发者扩展的 App onCreate 逻辑
        for (AppLifecycles lifecycle : mAppLifecycles) {
            lifecycle.onCreate(mApplication);
        }
    }

    @Override
    public void onTerminate(@NonNull Application application) {
        if (mActivityLifecycle != null) {
            mApplication.unregisterActivityLifecycleCallbacks(mActivityLifecycle);
        }
        if (mActivityLifecycles != null && mActivityLifecycles.size() > 0) {
            for (Application.ActivityLifecycleCallbacks lifecycle : mActivityLifecycles) {
                mApplication.unregisterActivityLifecycleCallbacks(lifecycle);
            }
        }
        if (mAppLifecycles != null && mAppLifecycles.size() > 0) {
            for (AppLifecycles lifecycle : mAppLifecycles) {
                lifecycle.onTerminate(mApplication);
            }
        }
        this.mActivityLifecycle = null;
        this.mActivityLifecycles = null;
        this.mAppLifecycles = null;
        this.mApplication = null;
    }

}
