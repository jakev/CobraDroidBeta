/*
 * Copyright (C) 2011 The Android Open Source Project
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

package android.security.cts;

import com.android.cts.stub.R;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources.NotFoundException;
import android.test.AndroidTestCase;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageSignatureTest extends AndroidTestCase {

    private static final String TAG = PackageSignatureTest.class.getSimpleName();

    public void testPackageSignatures() throws Exception {
        Set<String> badPackages = new HashSet<String>();
        Set<Signature> wellKnownSignatures = getWellKnownSignatures();

        PackageManager packageManager = mContext.getPackageManager();
        List<PackageInfo> allPackageInfos = packageManager.getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES |
                PackageManager.GET_SIGNATURES);
        for (PackageInfo packageInfo : allPackageInfos) {
            String packageName = packageInfo.packageName;
            if (packageName != null && !isWhitelistedPackage(packageName)) {
                for (Signature signature : packageInfo.signatures) {
                    if (wellKnownSignatures.contains(signature)) {
                        badPackages.add(packageInfo.packageName);
                    }
                }
            }
        }

        assertTrue("These packages should not be signed with a well known key: " + badPackages,
                badPackages.isEmpty());
    }

    private Set<Signature> getWellKnownSignatures() throws NotFoundException, IOException {
        Set<Signature> wellKnownSignatures = new HashSet<Signature>();
        wellKnownSignatures.add(getSignature(R.raw.sig_media));
        wellKnownSignatures.add(getSignature(R.raw.sig_platform));
        wellKnownSignatures.add(getSignature(R.raw.sig_shared));
        wellKnownSignatures.add(getSignature(R.raw.sig_testkey));
        return wellKnownSignatures;
    }

    private boolean isWhitelistedPackage(String packageName) {
        // Don't check the signatures of CTS test packages on the device.
        // devicesetup is the APK CTS loads to collect information needed in the final report
        return packageName.startsWith("com.android.cts")
                || packageName.equalsIgnoreCase("android.tests.devicesetup");
    }

    private static final int DEFAULT_BUFFER_BYTES = 1024 * 4;

    private Signature getSignature(int resId) throws NotFoundException, IOException {
        InputStream input = mContext.getResources().openRawResource(resId);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            byte[] buffer = new byte[DEFAULT_BUFFER_BYTES];
            int numBytes = 0;
            while ((numBytes = input.read(buffer)) != -1) {
                output.write(buffer, 0, numBytes);
            }
            return new Signature(output.toByteArray());
        } finally {
            input.close();
            output.close();
        }
    }

    /**
     * Writes a package's signature to a file on the device's external storage.
     * This method was used to generate the well known signatures used by this test.
     */
    @SuppressWarnings("unused")
    private void writeSignature(String packageName, String fileName)
            throws NameNotFoundException, IOException {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                PackageManager.GET_SIGNATURES);
        File directory = mContext.getExternalFilesDir(null);
        int numSignatures = packageInfo.signatures.length;
        Log.i(TAG, "Will dump " + numSignatures + " signatures to " + directory);
        for (int i = 0; i < numSignatures; i++) {
            Signature signature = packageInfo.signatures[i];
            byte[] signatureBytes = signature.toByteArray();
            File signatureFile = new File(directory, fileName + "." + i);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(signatureFile);
                output.write(signatureBytes);
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        }
    }
}
