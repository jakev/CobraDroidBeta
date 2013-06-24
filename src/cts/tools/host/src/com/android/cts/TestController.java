/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.cts;

/**
 * Store the information of a test controller.
 *
 */
public class TestController {
    private String mJarPath;
    private String mPackageName;
    private String mClassName;
    private String mMethodName;

    public TestController(final String jarPath, final String packageName,
            final String className, final String methodName) {
        mJarPath = jarPath;
        mPackageName = packageName;
        mClassName = className;
        mMethodName = methodName;
    }

    /**
     * Get the jar file path of the controller.
     *
     * @return The jar file path of the controller.
     */
    public String getJarPath() {
        return mJarPath;
    }

    /**
     * Get the package name.
     *
     * @return The package name.
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * Get the class name.
     *
     * @return The class name.
     */
    public String getClassName() {
        return mClassName;
    }

    /**
     * Get the method name.
     *
     * @return The method name.
     */
    public String getMethodName() {
        return mMethodName;
    }

    /**
     * Get the full name of this test controller.
     *
     * @return The case name of this test.
     */
    public String getFullName() {
        return mPackageName + "." + mClassName + Test.METHOD_SEPARATOR + mMethodName;
    }

    /**
     * Check if it's a valid test controller.
     *
     * @return true if it's valid, else return false.
     */
    public boolean isValid() {
        if ((mJarPath == null) || (mJarPath.length() == 0)
                || (mPackageName == null) || (mPackageName.length() == 0)
                || (mClassName == null) || (mClassName.length() == 0)
                || (mMethodName == null) || (mMethodName.length() == 0)) {
            return false;
        } else {
            return true;
        }
    }
}
