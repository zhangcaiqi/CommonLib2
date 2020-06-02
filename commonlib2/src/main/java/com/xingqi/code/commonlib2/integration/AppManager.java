/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xingqi.code.commonlib2.integration;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Message;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public final class AppManager {
    /**
     * true 为不需要加入到 Activity 容器进行统一管理,默认为 false
     */
    public static final String IS_NOT_ADD_ACTIVITY_LIST = "is_not_add_activity_list";
    private static volatile AppManager sAppManager;
    protected final String TAG = this.getClass().getSimpleName();
    private Application mApplication;
    /**
     * 管理所有存活的 Activity, 容器中的顺序仅仅是 Activity 的创建顺序, 并不能保证和 Activity 任务栈顺序一致
     */
    private List<Activity> mActivityList;
    /**
     * 当前在前台的 Activity
     */
    private Activity mCurrentActivity;
    /**
     * 此方法作废, 现在可通过 {@link AppManager#getAppManager()} 直接访问 {@link AppManager}
     * <p>
     * 提供给外部扩展 {@link AppManager} 的 {@link #onReceive(Message)} 方法
     */
    private HandleListener mHandleListener;

    private AppManager() {
    }

    public static AppManager getAppManager() {
        if (sAppManager == null) {
            synchronized (AppManager.class) {
                if (sAppManager == null) {
                    sAppManager = new AppManager();
                }
            }
        }
        return sAppManager;
    }

    /**
     * 此方法作废, 现在可通过 {@link AppManager#getAppManager()} 直接访问 {@link AppManager}
     * <p>
     * 通过此方法远程遥控 {@link AppManager}, 使 {@link #onReceive(Message)} 执行对应方法
     *
     * @param msg {@link Message}
     */
    @Deprecated
    public static void post(Message msg) {
        getAppManager().onReceive(msg);
    }

    public AppManager init(Application application) {
        this.mApplication = application;
        return sAppManager;
    }

    /**
     * 此方法作废, 现在可通过 {@link AppManager#getAppManager()} 直接访问 {@link AppManager}
     * <p>
     * 可通过 {@link #setHandleListener(HandleListener)}, 让外部可扩展新的事件
     *
     * @param message
     */
    @Deprecated
    public void onReceive(Message message) {
        if (mHandleListener != null) {
            mHandleListener.handleMessage(this, message);
        }
    }

    @Deprecated
    public HandleListener getHandleListener() {
        return mHandleListener;
    }

    @Deprecated
    public void setHandleListener(HandleListener handleListener) {
        this.mHandleListener = handleListener;
    }


    /**
     * 让在栈顶的 {@link Activity} ,打开指定的 {@link Activity}
     *
     * @param intent
     */
    public void startActivity(Intent intent) {
        if (getTopActivity() == null) {
            //如果没有前台的activity就使用new_task模式启动activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mApplication.startActivity(intent);
            return;
        }
        getTopActivity().startActivity(intent);
    }

    /**
     * 让在栈顶的 {@link Activity} ,打开指定的 {@link Activity}
     *
     * @param activityClass
     */
    public void startActivity(Class activityClass) {
        startActivity(new Intent(mApplication, activityClass));
    }

    /**
     * 释放资源
     */
    public void release() {
        mActivityList.clear();
        mHandleListener = null;
        mActivityList = null;
        mCurrentActivity = null;
        mApplication = null;
    }

    @Nullable
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.mCurrentActivity = currentActivity;
    }

    @Nullable
    public Activity getTopActivity() {
        if (mActivityList == null) {
            return null;
        }
        return mActivityList.size() > 0 ? mActivityList.get(mActivityList.size() - 1) : null;
    }

    public List<Activity> getActivityList() {
        if (mActivityList == null) {
            mActivityList = new LinkedList<>();
        }
        return mActivityList;
    }

    public void addActivity(Activity activity) {
        synchronized (AppManager.class) {
            List<Activity> activities = getActivityList();
            if (!activities.contains(activity)) {
                activities.add(activity);
            }
        }
    }

    /**
     * 删除集合里的指定的 {@link Activity} 实例
     *
     * @param {@link Activity}
     */
    public void removeActivity(Activity activity) {
        if (mActivityList == null) {
            return;
        }
        synchronized (AppManager.class) {
            mActivityList.remove(activity);
        }
    }

    /**
     * 删除集合里的指定位置的 {@link Activity}
     *
     * @param location
     */
    public Activity removeActivity(int location) {
        if (mActivityList == null) {
            return null;
        }
        synchronized (AppManager.class) {
            if (location > 0 && location < mActivityList.size()) {
                return mActivityList.remove(location);
            }
        }
        return null;
    }

    /**
     * 关闭指定的 {@link Activity} class 的所有的实例
     *
     * @param activityClass
     */
    public void killActivity(Class<?> activityClass) {
        if (mActivityList == null) {
            return;
        }
        synchronized (AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();

                if (next.getClass().equals(activityClass)) {
                    iterator.remove();
                    next.finish();
                }
            }
        }
    }

    /**
     * 指定的 {@link Activity} 实例是否存活
     *
     * @param {@link Activity}
     * @return
     */
    public boolean activityInstanceIsLive(Activity activity) {
        if (mActivityList == null) {
            return false;
        }
        return mActivityList.contains(activity);
    }

    /**
     * 指定的 {@link Activity} class 是否存活(同一个 {@link Activity} class 可能有多个实例)
     *
     * @param activityClass
     * @return
     */
    public boolean activityClassIsLive(Class<?> activityClass) {
        if (mActivityList == null) {
            return false;
        }
        for (Activity activity : mActivityList) {
            if (activity.getClass().equals(activityClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指定 {@link Activity} class 的实例,没有则返回 null(同一个 {@link Activity} class 有多个实例,则返回最早创建的实例)
     *
     * @param activityClass
     * @return
     */
    public Activity findActivity(Class<?> activityClass) {
        if (mActivityList == null) {
            return null;
        }
        for (Activity activity : mActivityList) {
            if (activity.getClass().equals(activityClass)) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 关闭所有 {@link Activity}
     */
    public void killAll() {
//        while (getActivityList().size() != 0) { //此方法只能兼容LinkedList
//            getActivityList().remove(0).finish();
//        }
        synchronized (AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();
                iterator.remove();
                next.finish();
            }
        }
    }

    public void killAll(Class<?>... excludeActivityClasses) {
        List<Class<?>> excludeList = Arrays.asList(excludeActivityClasses);
        synchronized (AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();

                if (excludeList.contains(next.getClass())) {
                    continue;
                }

                iterator.remove();
                next.finish();
            }
        }
    }

    public void killAll(String... excludeActivityName) {
        List<String> excludeList = Arrays.asList(excludeActivityName);
        synchronized (AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();

                if (excludeList.contains(next.getClass().getName())) {
                    continue;
                }

                iterator.remove();
                next.finish();
            }
        }
    }

    public void appExit() {
        try {
            killAll();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public interface HandleListener {
        void handleMessage(AppManager appManager, Message message);
    }
}
