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

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Class that outputs an XML report of the {@link ApiCoverage} collected. It can be viewed in
 * a browser when used with the api-coverage.css and api-coverage.xsl files.
 */
class XmlReport {

    public static void printXmlReport(List<File> testApks, ApiCoverage apiCoverage,
            OutputStream outputStream) {
        PrintStream out = new PrintStream(outputStream);
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<?xml-stylesheet type=\"text/xsl\"  href=\"api-coverage.xsl\"?>");

        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy h:mm a z");
        String date = format.format(new Date(System.currentTimeMillis()));
        out.println("<api-coverage generatedTime=\"" + date + "\">");

        out.println("<debug>");
        out.println("<sources>");
        for (File testApk : testApks) {
            out.println("<apk path=\"" + testApk.getPath() + "\" />");
        }
        out.println("</sources>");
        out.println("</debug>");

        out.println("<api>");

        CoverageComparator comparator = new CoverageComparator();
        List<ApiPackage> packages = new ArrayList<ApiPackage>(apiCoverage.getPackages());
        Collections.sort(packages, comparator);
        for (ApiPackage pkg : packages) {
            if (pkg.getName().startsWith("android")
                    && pkg.getTotalMethods() > 0) {
                out.println("<package name=\"" + pkg.getName()
                        + "\" numCovered=\"" + pkg.getNumCoveredMethods()
                        + "\" numTotal=\"" + pkg.getTotalMethods()
                        + "\" coveragePercentage=\""
                            + Math.round(pkg.getCoveragePercentage())
                        + "\">");

                List<ApiClass> classes = new ArrayList<ApiClass>(pkg.getClasses());
                Collections.sort(classes, comparator);

                for (ApiClass apiClass : classes) {
                    if (apiClass.getTotalMethods() > 0) {
                        out.println("<class name=\"" + apiClass.getName()
                                + "\" numCovered=\"" + apiClass.getNumCoveredMethods()
                                + "\" numTotal=\"" + apiClass.getTotalMethods()
                                + "\" coveragePercentage=\""
                                    + Math.round(apiClass.getCoveragePercentage())
                                + "\">");

                        for (ApiConstructor constructor : apiClass.getConstructors()) {
                            out.println("<constructor name=\"" + constructor.getName()
                                    + "\" covered=\"" + constructor.isCovered() + "\">");

                            for (String parameterType : constructor.getParameterTypes()) {
                                out.println("<parameter type=\"" + parameterType + "\" />");
                            }

                            out.println("</constructor>");
                        }

                        for (ApiMethod method : apiClass.getMethods()) {
                            out.println("<method name=\"" + method.getName()
                                    + "\" returnType=\"" + method.getReturnType()
                                    + "\" covered=\"" + method.isCovered() + "\">");

                            for (String parameterType : method.getParameterTypes()) {
                                out.println("<parameter type=\"" + parameterType + "\" />");
                            }

                            out.println("</method>");
                        }
                        out.println("</class>");
                    }
                }
                out.println("</package>");
            }
        }

        out.println("</api>");
        out.println("</api-coverage>");
    }
}
