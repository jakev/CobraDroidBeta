/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.cts.apicoverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Representation of a class in the API with constructors and methods. */
class ApiClass implements Comparable<ApiClass>, HasCoverage {

    private final String mName;

    private final List<ApiConstructor> mApiConstructors = new ArrayList<ApiConstructor>();

    private final List<ApiMethod> mApiMethods = new ArrayList<ApiMethod>();

    ApiClass(String name) {
        this.mName = name;
    }

    public int compareTo(ApiClass another) {
        return mName.compareTo(another.mName);
    }

    public String getName() {
        return mName;
    }

    public void addConstructor(ApiConstructor constructor) {
        mApiConstructors.add(constructor);
    }

    public ApiConstructor getConstructor(List<String> parameterTypes) {
        for (ApiConstructor constructor : mApiConstructors) {
            if (parameterTypes.equals(constructor.getParameterTypes())) {
                return constructor;
            }
        }
        return null;
    }

    public Collection<ApiConstructor> getConstructors() {
        return Collections.unmodifiableList(mApiConstructors);
    }

    public void addMethod(ApiMethod method) {
        mApiMethods.add(method);
    }

    public ApiMethod getMethod(String name, List<String> parameterTypes, String returnType) {
        for (ApiMethod method : mApiMethods) {
            if (name.equals(method.getName())
                    && parameterTypes.equals(method.getParameterTypes())
                    && returnType.equals(method.getReturnType())) {
                return method;
            }
        }
        return null;
    }

    public Collection<ApiMethod> getMethods() {
        return Collections.unmodifiableList(mApiMethods);
    }

    public int getNumCoveredMethods() {
        int numCovered = 0;
        for (ApiConstructor constructor : mApiConstructors) {
            if (constructor.isCovered()) {
                numCovered++;
            }
        }
        for (ApiMethod method : mApiMethods) {
            if (method.isCovered()) {
                numCovered++;
            }
        }
        return numCovered;
    }

    public int getTotalMethods() {
        return mApiConstructors.size() + mApiMethods.size();
    }

    public float getCoveragePercentage() {
        return (float) getNumCoveredMethods() / getTotalMethods() * 100;
    }
}