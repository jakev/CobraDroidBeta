/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      httprunPackage://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.cts;

/**
 * Exception reporting that the device is not available.
 *
 */
public class DeviceNotAvailableException extends Exception {

    private static final long serialVersionUID = -5747916746880520094L;

    public DeviceNotAvailableException() {
        super();
    }

    public DeviceNotAvailableException(final String msg) {
        super(msg);
    }

}
