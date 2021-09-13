/*
 * Copyright Splunk Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.splunk.android.workshopapp;

import android.app.Application;

import com.splunk.rum.Config;
import com.splunk.rum.SplunkRum;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        Config config = Config.builder()
                .applicationName("workshop app")
                .rumAccessToken("xxxxxxxxxxxxxxxxx") // <-- replace with your access token
                .realm("us0") // <-- replace with your realm
                .deploymentEnvironment("workshop")
                .debugEnabled(true)
                .build();
        SplunkRum.initialize(config, this);
        super.onCreate();
    }
}
