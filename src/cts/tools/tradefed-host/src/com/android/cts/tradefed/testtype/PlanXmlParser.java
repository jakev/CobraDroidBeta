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

import com.android.tradefed.util.xml.AbstractXmlParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Parses a test plan xml file.
 */
class PlanXmlParser extends AbstractXmlParser implements IPlanXmlParser {

    private Set<String> mUris;

    /**
     * SAX callback object. Handles parsing data from the xml tags.
     */
    private class EntryHandler extends DefaultHandler {

        private static final String ENTRY_TAG = "Entry";

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {
            if (ENTRY_TAG.equals(localName)) {
                final String entryUriValue = attributes.getValue("uri");
                mUris.add(entryUriValue);
            }
        }
    }

    PlanXmlParser() {
        // Uses a LinkedHashSet to have predictable iteration order
        mUris = new LinkedHashSet<String>();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> getTestUris() {
        return mUris;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DefaultHandler createXmlHandler() {
        return new EntryHandler();
    }
}
