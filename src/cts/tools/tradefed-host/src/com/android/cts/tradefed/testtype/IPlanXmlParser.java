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

package com.android.cts.tradefed.testtype;

import com.android.tradefed.util.xml.AbstractXmlParser.ParseException;

import java.io.InputStream;
import java.util.Collection;

/**
 * Interface for accessing test plan data.
 */
interface IPlanXmlParser {

    /**
     * Parse the test plan data from given stream.
     *
     * @param xmlStream the {@link InputStream} that contains the test plan xml.
     */
    public void parse(InputStream xmlStream) throws ParseException;

    /**
     * Gets the list of test uris parsed from the plan.
     * <p/>
     * Must be called after {@link IPlanXmlParser#parse(InputStream)}.
     */
    public Collection<String> getTestUris();
}
