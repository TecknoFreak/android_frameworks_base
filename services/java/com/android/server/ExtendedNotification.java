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

import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

public class ExtendedNotification {
        public int color;
        public int onMS;
        public int offMS;
		public String sound;
		public String vibrate;
		
    private static final String TAG = "ExtendedNotification";

    ExtendedNotification(String config) {
        if (!(TextUtils.isEmpty(config))) {
            String[] values = config.split(";");
            if (values.length < 3) {
                Log.e(TAG, "Error parsing ExtendedNotification '"
                        + config + "'");               
            } else {
                try {
                    this.color = Integer.parseInt(values[0]);
                    this.onMS = Integer.parseInt(values[1]);
                    this.offMS = Integer.parseInt(values[2]);
                    if (values.length == 5) {
                        this.sound = values[3];
                        this.vibrate = values[4];
                    } else {
                        this.sound = "";
                        this.vibrate = "";
				    }			
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing ExtendedNotification '"
                            + config + "'");
			    }
            }
        }		
	}
}
