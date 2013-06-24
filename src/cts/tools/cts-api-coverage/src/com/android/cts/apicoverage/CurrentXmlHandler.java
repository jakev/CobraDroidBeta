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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DefaultHandler} that builds an empty {@link ApiCoverage} object from scanning current.xml.
 */
class CurrentXmlHandler extends DefaultHandler {

    private String mCurrentPackageName;

    private String mCurrentClassName;

    private String mCurrentMethodName;

    private String mCurrentMethodReturnType;

    private List<String> mCurrentParameterTypes = new ArrayList<String>();

    private ApiCoverage mApiCoverage = new ApiCoverage();

    public ApiCoverage getApi() {
        return mApiCoverage;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if ("package".equalsIgnoreCase(localName)) {
            mCurrentPackageName = CurrentXmlHandler.getValue(attributes, "name");

            ApiPackage apiPackage = new ApiPackage(mCurrentPackageName);
            mApiCoverage.addPackage(apiPackage);

        } else if ("class".equalsIgnoreCase(localName)
                || "interface".equalsIgnoreCase(localName)) {
            mCurrentClassName = CurrentXmlHandler.getValue(attributes, "name");

            ApiClass apiClass = new ApiClass(mCurrentClassName);
            ApiPackage apiPackage = mApiCoverage.getPackage(mCurrentPackageName);
            apiPackage.addClass(apiClass);

        } else if ("constructor".equalsIgnoreCase(localName)) {
            mCurrentParameterTypes.clear();
        }  else if ("method".equalsIgnoreCase(localName)) {
            mCurrentMethodName = CurrentXmlHandler.getValue(attributes, "name");
            mCurrentMethodReturnType = CurrentXmlHandler.getValue(attributes, "return");
            mCurrentParameterTypes.clear();
        } else if ("parameter".equalsIgnoreCase(localName)) {
            mCurrentParameterTypes.add(CurrentXmlHandler.getValue(attributes, "type"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        super.endElement(uri, localName, name);
        if ("constructor".equalsIgnoreCase(localName)) {
            ApiConstructor apiConstructor = new ApiConstructor(mCurrentClassName,
                    mCurrentParameterTypes);
            ApiPackage apiPackage = mApiCoverage.getPackage(mCurrentPackageName);
            ApiClass apiClass = apiPackage.getClass(mCurrentClassName);
            apiClass.addConstructor(apiConstructor);
        }  else if ("method".equalsIgnoreCase(localName)) {
            ApiMethod apiMethod = new ApiMethod(mCurrentMethodName, mCurrentParameterTypes,
                    mCurrentMethodReturnType);
            ApiPackage apiPackage = mApiCoverage.getPackage(mCurrentPackageName);
            ApiClass apiClass = apiPackage.getClass(mCurrentClassName);
            apiClass.addMethod(apiMethod);
        }
    }

    static String getValue(Attributes attributes, String key) {
        // Strip away generics <...> and make inner classes always use a "." rather than "$".
        return attributes.getValue(key)
                .replaceAll("<.+>", "")
                .replace("$", ".");
    }
}