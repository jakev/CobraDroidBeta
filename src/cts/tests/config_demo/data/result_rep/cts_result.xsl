<?xml version="1.0" encoding="utf-8"?>
<!-- Copyright (C) 2008 The Android Open Source Project

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
  
          http://www.apache.org/licenses/LICENSE-2.0
  
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
-->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template match="/">
       
        <html>
            <STYLE type="text/css">
                    .plan { background-color : #9acd32 }
                    .head { font-size : 30; background-color : #A8A6A6 }
                    .head1 { font-size : 20; background-color : #A8A6A6 }
                    .lgreen {background-color : #9acd32}
                    .pass {background-color : #00ff00}
                    .failed {background-color : #ff0000}
                    .timeout {background-color : #febf00}
                    .notrun {background-color : #C6C3C3}
                    .gray {background-color : #C6C3C3}
            </STYLE>
            <body>
                <p class="head">CTS Test result</p>

                <!-- plan information -->
                <TABLE >
                    <TR class="plan">
                        <TD>Plan name</TD>
                        <TD>Start time</TD>
                        <TD>End time</TD>
                        <TD>Version</TD>
                    </TR>
                    <TR>
                        <TD>
                            <xsl:value-of select="TestResult/@testPlan"/>
                        </TD>
                        <TD>
                            <xsl:value-of select="TestResult/@starttime"/>
                        </TD>
                        <TD>
                            <xsl:value-of select="TestResult/@endtime"/>
                        </TD>
                        <TD>
                            <xsl:value-of select="TestResult/@version"/>
                        </TD>
                    </TR>
                </TABLE>
                <!-- Device infor -->
                <p class="head1">Test Device information</p>
                <TABLE >
                    <TR>
                        <TD class="lgreen">Device Make</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@buildName"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Build model</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@deviceID"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Firmware Version</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@buildVersion"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Firmware Build Number</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@buildID"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Android Platform Version</TD>
                        <TD>
                            <xsl:value-of
                                select="TestResult/DeviceInfo/BuildInfo/@androidPlatformVersion"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Supported Locales</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@locales"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Screen size</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/Screen/@resolution"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Phone number</TD>
                        <TD>
                            <xsl:value-of
                                select="TestResult/DeviceInfo/PhoneSubInfo/@subscriberId"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">x dpi</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@Xdpi"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">y dpi</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@Ydpi"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Touch</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@touch"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Navigation</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@navigation"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Keypad</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@keypad"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">Network</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@network"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">IMEI</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@imei"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="lgreen">IMSI</TD>
                        <TD>
                            <xsl:value-of select="TestResult/DeviceInfo/BuildInfo/@imsi"/>
                        </TD>
                    </TR>
                </TABLE>
                <!-- Summary -->
                <p class="head1">Summary</p>
                <TABLE >
                    <TR>
                        <TD class="pass">pass</TD>
                        <TD>
                            <xsl:value-of select="TestResult/Summary/@pass"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="failed">failed</TD>
                        <TD>
                            <xsl:value-of select="TestResult/Summary/@failed"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="timeout">timeout</TD>
                        <TD>
                            <xsl:value-of select="TestResult/Summary/@timeout"/>
                        </TD>
                    </TR>
                    <TR>
                        <TD class="notrun">notrun</TD>
                        <TD>
                            <xsl:value-of select="TestResult/Summary/@notrun"/>
                        </TD>
                    </TR>
                </TABLE>
                <!-- test package -->
                <xsl:for-each select="TestResult/TestPackage">
                    <p class="head1">
                        TestPackage:
                        <xsl:value-of select="@name"/>
                    </p>

                    <TABLE border="0.1">
                        <!-- level 1 test suite -->
                        <xsl:for-each select="TestSuite">
                            <TR>
                                <TD class="gray">
                                    <xsl:value-of select="@name"/>
                                </TD>
                                <TD class="gray"></TD>
                                <TD class="gray"></TD>
                                <TD class="gray"></TD>
                                <TD class="gray"></TD>
                            </TR>
                            <xsl:for-each select="TestCase">
                                <TR>
                                    <TD></TD>
                                    <TD class="gray">
                                        <xsl:value-of select="@name"/>
                                    </TD>
                                    <TD class="gray"></TD>
                                    <TD class="gray"></TD>
                                    <TD class="gray"></TD>
                                </TR>
                                <xsl:for-each select="Test">
                                    <TR>
                                        <TD></TD>
                                        <TD></TD>

                                            <xsl:if test="@result='pass'">
                                                <TD class="pass">
                                                    <xsl:value-of select="@name"/>
                                                </TD>
                                                <TD class="pass">
                                                    <xsl:value-of select="@result"/>
                                                </TD>
                                            </xsl:if>
                                            
                                            <xsl:if test="@result='fail'">
                                                <TD class="failed">
                                                    <xsl:value-of select="@name"/>
                                                </TD>
                                                <TD class="failed">
                                                    <xsl:value-of select="@result"/>
                                                </TD>
                                            </xsl:if>
    
                                            <xsl:if test="@result='timeout'">
                                                <TD class="timeout">
                                                    <xsl:value-of select="@name"/>
                                                </TD>
                                                <TD class="timeout">
                                                    <xsl:value-of select="@result"/>
                                                </TD>
                                            </xsl:if>

                                            <xsl:if test="@result='notrun'">
                                                <TD class="notrun">
                                                    <xsl:value-of select="@name"/>
                                                </TD>
                                                <TD class="notrun">
                                                    <xsl:value-of select="@result"/>
                                                </TD>
                                            </xsl:if>

                                        <TD></TD>
                                    </TR>
                                </xsl:for-each>
                            </xsl:for-each>
                            <!-- level 2 test suite -->
                            <xsl:for-each select="TestSuite">
                                <TR>
                                    <TD></TD>
                                    <TD class="gray">
                                        <xsl:value-of select="@name"/>
                                    </TD>
                                    <TD class="gray"></TD>
                                    <TD class="gray"></TD>
                                    <TD class="gray"></TD>
                                </TR>   
                                    <xsl:for-each select="TestCase">
                                        <TR>
                                            <TD></TD>
                                            <TD></TD>
                                            <TD class="gray">
                                                <xsl:value-of select="@name"/>
                                            </TD>
                                            <TD class="gray"></TD>
                                            <TD class="gray"></TD>
                                        </TR>
                                        <xsl:for-each select="Test">
                                            <TR>
                                                <TD></TD>
                                                <TD></TD>
                                                <TD></TD>
                                                
                                                <xsl:if test="@result='pass'">
                                                    <TD class="pass">
                                                        <xsl:value-of select="@name"/>
                                                    </TD>
                                                    <TD class="pass">
                                                        <xsl:value-of select="@result"/>
                                                    </TD>
                                                </xsl:if>
                                            
                                                <xsl:if test="@result='fail'">
                                                    <TD class="failed">
                                                        <xsl:value-of select="@name"/>
                                                    </TD>
                                                    <TD class="failed">
                                                        <xsl:value-of select="@result"/>
                                                    </TD>
                                                </xsl:if>
        
                                                <xsl:if test="@result='timeout'">
                                                    <TD class="timeout">
                                                        <xsl:value-of select="@name"/>
                                                    </TD>
                                                    <TD class="timeout">
                                                        <xsl:value-of select="@result"/>
                                                    </TD>
                                                </xsl:if>

                                                <xsl:if test="@result='notrun'">
                                                    <TD class="notrun">
                                                        <xsl:value-of select="@name"/>
                                                    </TD>
                                                    <TD class="notrun">
                                                        <xsl:value-of select="@result"/>
                                                    </TD>
                                                </xsl:if>
                                                
                                            </TR>
                                        </xsl:for-each>
                                    </xsl:for-each>
                                
                            </xsl:for-each>
                        </xsl:for-each>
                    </TABLE>
                </xsl:for-each>
                <!-- end test package -->
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
