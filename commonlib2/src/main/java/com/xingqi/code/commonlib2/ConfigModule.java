package com.xingqi.code.commonlib2;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;


import com.xingqi.code.commonlib2.delegate.AppLifecycles;

import java.util.List;

public interface ConfigModule {

    void injectAppLifecycle(@NonNull Context context, @NonNull List<AppLifecycles> lifecycles);

    void injectActivityLifecycle(@NonNull Context context, @NonNull List<Application.ActivityLifecycleCallbacks> lifecycles);

    void injectFragmentLifecycle(@NonNull Context context, @NonNull List<FragmentManager.FragmentLifecycleCallbacks> lifecycles);
}
