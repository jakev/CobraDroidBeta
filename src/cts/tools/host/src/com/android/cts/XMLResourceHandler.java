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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An abstract class to deal with the XML information using DOM.
 */
public abstract class XMLResourceHandler {

    protected String getElementContent(Node elem) {
        return elem.getChildNodes().item(0).getNodeValue().trim();
    }

    /**
     * Get string from DOM node by attribute name.
     *
     * @param elem a node from DOM tree.
     * @param attrName the attribute name.
     * @return string value of the attribute name from the DOM node.
     */
    static public String getStringAttributeValue(Node elem, String attrName) {
        Node node = elem.getAttributes().getNamedItem(attrName);
        if (node == null) {
            return null;
        }
        return node.getNodeValue().trim();
    }

    /**
     * Get integer attribute value.
     *
     * @param elem The element node.
     * @param attrName The attribute name.
     * @return The attribute value in integer.
     */
    protected int getAttributeValue(Node elem, String attrName) {
        return Integer.parseInt(getStringAttributeValue(elem, attrName));
    }

    /**
     * Get child by attribute.
     *
     * @param parent The parent node.
     * @param attrName The attribute name.
     * @param attrValue The attribute value.
     * @return The child node.
     */
    protected Node getChildByAttribute(Node parent, String attrName, String attrValue) {
        if (parent == null || attrName == null || attrValue == null) {
            return null;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (attrValue.equals(getStringAttributeValue(child, attrName))) {
                    return child;
                }
            }
        }

        return null;
    }

    /**
     * Set the attribute value.
     *
     * @param doc The document.
     * @param elem The element node.
     * @param name The attribute name.
     * @param value The attribute value in integer.
     */
    protected void setAttribute(Document doc, Node elem, String name, int value) {
        setAttribute(doc, elem, name, Integer.toString(value));
    }

    /**
     * Set the attribute value.
     *
     * @param doc The document.
     * @param elem The element node.
     * @param name The attribute name.
     * @param value The attribute value in string.
     */
    protected void setAttribute(Document doc, Node elem, String name, String value) {
        Attr attrNode = doc.createAttribute(name);
        attrNode.setNodeValue(value);

        elem.getAttributes().setNamedItem(attrNode);
    }

    /**
     * Write a DOM Document object into a file.
     *
     * @param file XML file to be written
     * @param doc DOM Document
     */
    protected static void writeToFile(File file, Document doc) throws FileNotFoundException,
            IOException, TransformerFactoryConfigurationError, TransformerException {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        // enable indent in result file
        t.setOutputProperty("indent", "yes");
        FileOutputStream fos = new FileOutputStream(file);
        try {
            StreamResult sr = new StreamResult(fos);
            t.transform(new DOMSource(doc), sr);
        } finally {
            fos.close();
        }
    }
}
