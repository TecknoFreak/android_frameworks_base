/*
 * Copyright (C) 2013 The Android Open Source Project, Team PSX
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

 package com.android.server;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.server.ExtendedNotification;
import com.android.server.NotificationManagerService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ExtendedNotificationCollection {

    private static final String TAG = "ExtendedNotificationCollection";
    private HashMap<String, ExtendedNotification> mNotificationPulseCustomexnInfo;
    private Map<String, String> mPackageNameMappings;
    private SettingsObserver mSettingsObserver;
    private Handler mHandler;
    final Context mContext;
	
	ExtendedNotificationCollection (Context context){
        mNotificationPulseCustomexnInfo = new HashMap<String, ExtendedNotification>();
        mHandler = new Handler();
        mContext = context;
		
        mPackageNameMappings = new HashMap<String, String>();
        Resources resources = mContext.getResources();
        for (String mapping : resources.getStringArray(
                 com.android.internal.R.array.notification_light_package_mapping)) {
            String[] map = mapping.split("\\|");
            mPackageNameMappings.put(map[0], map[1]);
        }
        mSettingsObserver = new SettingsObserver(mHandler);
        mSettingsObserver.observe();
        // register for various Intents
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_SWITCHED);
        mContext.registerReceiver(mIntentReceiver, filter);	
	}
	
    class SettingsObserver extends ContentObserver {

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NOTIFICATION_LIGHT_PULSE_CUSTOM_ENABLE), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NOTIFICATION_LIGHT_PULSE_CUSTOM_VALUES), false, this);
            update(null);
        }

        @Override public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = mContext.getContentResolver();

            // LED custom notification colors
            mNotificationPulseCustomexnInfo.clear();
            if (Settings.System.getIntForUser(resolver,
                    Settings.System.NOTIFICATION_LIGHT_PULSE_CUSTOM_ENABLE, 0,
                    UserHandle.USER_CURRENT) != 0) {
                parseNotificationPulseCustomValuesString(Settings.System.getStringForUser(resolver,
                        Settings.System.NOTIFICATION_LIGHT_PULSE_CUSTOM_VALUES, UserHandle.USER_CURRENT));
            }

        }
    }
	
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
          
            if (action.equals(Intent.ACTION_USER_SWITCHED)) {
                // reload per-user settings
                mSettingsObserver.update(null);
            }
        }
    };
	
    private void parseNotificationPulseCustomValuesString(String customexnInfoString) {
        if (TextUtils.isEmpty(customexnInfoString)) {
            return;
        }

        for (String packageValuesString : customexnInfoString.split("\\|")) {
            String[] packageValues = packageValuesString.split("=");
            if (packageValues.length != 2) {
                Log.e(TAG, "Error parsing extended Notification values for unknown package");
                continue;
            }
            String packageName = packageValues[0];
            ExtendedNotification exnInfo = new ExtendedNotification(packageValues[1]);
            mNotificationPulseCustomexnInfo.put(packageName, exnInfo);
        }
    }
	
    private String mapPackage(String pkg) {
        if(!mPackageNameMappings.containsKey(pkg)) {
            return pkg;
        }
        return mPackageNameMappings.get(pkg);
    }
	
    public ExtendedNotification getExtendedNotification(NotificationManagerService.NotificationRecord ledNotification) {
        final String packageName = ledNotification.sbn.getPackageName();
        return mNotificationPulseCustomexnInfo.get(mapPackage(packageName));
    }
	
}
