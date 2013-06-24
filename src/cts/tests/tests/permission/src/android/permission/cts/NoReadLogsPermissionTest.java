/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.permission.cts;

import android.permission.cts.FileUtils.FileStatus;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Verify the read system log require specific permissions.
 */
public class NoReadLogsPermissionTest extends AndroidTestCase {
    private static final String LOGTAG = "CTS";

    /**
     * Verify that we won't get the system log without a READ_LOGS permission.
     * <p>Requires Permission:
     *   {@link android.Manifest.permission#READ_LOGS }.
     * @throws IOException
     */
    @MediumTest
    public void testLogcat() throws IOException {
        Process logcatProc = null;
        BufferedReader reader = null;
        try {
            logcatProc = Runtime.getRuntime().exec(new String[]
                    {"logcat", "-d", "AndroidRuntime:E " + LOGTAG + ":V *:S" });
            Log.d(LOGTAG, "no read logs permission test");

            reader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()));

            String line;
            final StringBuilder log = new StringBuilder();
            String separator = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                log.append(line);
                log.append(separator);
            }

            // no permission get empty log
            assertEquals(0, log.length());

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public void testLogFilePermissions() {
        File logDir = new File("/dev/log");
        File[] logFiles = logDir.listFiles();
        assertTrue("Where are the log files? Please check that they are not world readable.",
                logFiles.length > 0);

        FileStatus status = new FileStatus();
        for (File log : logFiles) {
            if (FileUtils.getFileStatus(log.getAbsolutePath(), status, false)) {
                assertEquals("Log file " + log.getAbsolutePath() + " should have user root.",
                        0, status.uid);
                assertTrue("Log file " + log.getAbsolutePath() + " should have group log.",
                        "log".equals(FileUtils.getGroupName(status.gid)));
                assertFalse("Log file "  + log.getAbsolutePath() + " should not be world readable.",
                        status.hasModeFlag(FileUtils.S_IROTH));
            }
        }
    }
}
