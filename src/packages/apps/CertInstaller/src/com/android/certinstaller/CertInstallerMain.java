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

package com.android.certinstaller;

import android.content.Intent;
import android.os.Bundle;
import android.security.Credentials;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * The main class for installing certificates to the system keystore. It reacts
 * to the public {@link Credentials#INSTALL_ACTION} intent.
 */
public class CertInstallerMain extends CertFile implements Runnable {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) return;

        new Thread(new Runnable() {
            public void run() {
                // don't want to call startActivityForResult() (invoked in
                // installFromFile()) here as it makes the new activity (thus
                // the whole display) get stuck for about 5 seconds
                runOnUiThread(CertInstallerMain.this);
            }
        }).start();
    }

    public void run() {
        Intent intent = getIntent();
        String action = (intent == null) ? null : intent.getAction();

        if (Credentials.INSTALL_ACTION.equals(action)) {
            Bundle bundle = intent.getExtras();

            if ((bundle == null) || bundle.isEmpty()) {
                if (!isSdCardPresent()) {
                    Toast.makeText(this, R.string.sdcard_not_present,
                            Toast.LENGTH_SHORT).show();
                } else {
                    List<File> allFiles = getAllCertFiles();
                    if (allFiles.isEmpty()) {
                        Toast.makeText(this, R.string.no_cert_file_found,
                                Toast.LENGTH_SHORT).show();
                    } else if (allFiles.size() == 1) {
                        installFromFile(allFiles.get(0));
                        return;
                    } else {
                        startActivity(new Intent(this, CertFileList.class));
                    }
                }
            } else {
                Intent newIntent = new Intent(this, CertInstaller.class);
                newIntent.putExtras(intent);
                startActivity(newIntent);
            }
        }
        finish();
    }

    @Override
    protected void onInstallationDone(boolean success) {
        finish();
    }

    @Override
    protected void onError(int errorId) {
        finish();
    }
}
