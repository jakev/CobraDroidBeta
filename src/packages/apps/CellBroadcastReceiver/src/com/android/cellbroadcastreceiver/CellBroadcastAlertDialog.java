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

package com.android.cellbroadcastreceiver;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Custom alert dialog with optional flashing warning icon.
 * Alert audio and text-to-speech handled by {@link CellBroadcastAlertAudio}.
 */
public class CellBroadcastAlertDialog extends AlertDialog {

    /** Whether to show the flashing warning icon. */
    private final boolean mShowWarningIcon;

    /** The broadcast delivery time, for marking as read (or 0). */
    private final long mDeliveryTime;

    /** Length of time for the warning icon to be visible. */
    private static final int WARNING_ICON_ON_DURATION_MSEC = 800;

    /** Length of time for the warning icon to be off. */
    private static final int WARNING_ICON_OFF_DURATION_MSEC = 800;

    /** Warning icon state. false = visible, true = off */
    private boolean mIconAnimationState;

    /** Stop animating icon after {@link #onStop()} is called. */
    private boolean mStopAnimation;

    /** The warning icon Drawable. */
    private Drawable mWarningIcon;

    /** The View containing the warning icon. */
    private ImageView mWarningIconView;

    /** Keyguard lock to show emergency alerts while in the lock screen. */
    private KeyguardManager.KeyguardLock mKeyguardLock;

    /** Icon animation handler for flashing warning alerts. */
    private final Handler mAnimationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mIconAnimationState) {
                mWarningIconView.setAlpha(255);
                if (!mStopAnimation) {
                    mAnimationHandler.sendEmptyMessageDelayed(0, WARNING_ICON_ON_DURATION_MSEC);
                }
            } else {
                mWarningIconView.setAlpha(0);
                if (!mStopAnimation) {
                    mAnimationHandler.sendEmptyMessageDelayed(0, WARNING_ICON_OFF_DURATION_MSEC);
                }
            }
            mIconAnimationState = !mIconAnimationState;
            mWarningIconView.invalidateDrawable(mWarningIcon);
        }
    };

    /**
     * Create a new alert dialog for the broadcast notification.
     * @param context the local Context
     * @param titleId the resource ID of the dialog title
     * @param body the message body contents
     * @param showWarningIcon true if the flashing warning icon should be shown
     * @param deliveryTime the delivery time of the broadcast, for marking as read
     */
    public CellBroadcastAlertDialog(Context context, int titleId, CharSequence body,
            boolean showWarningIcon, long deliveryTime) {
        super(context);
        mShowWarningIcon = showWarningIcon;
        mDeliveryTime = deliveryTime;

        setTitle(titleId);
        setMessage(body);
        setCancelable(true);
        setOnCancelListener(new AlertDialog.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        setButton(DialogInterface.BUTTON_NEUTRAL, context.getText(R.string.button_dismiss),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

        // Set warning icon for emergency alert
        if (mShowWarningIcon) {
            mWarningIcon = getContext().getResources().getDrawable(R.drawable.ic_warning_large);
            setIcon(mWarningIcon);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mShowWarningIcon) {
            // Turn screen on and show above the keyguard for emergency alert
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
        super.onCreate(savedInstanceState);
        if (mShowWarningIcon) {
            KeyguardManager km = (KeyguardManager)
                    getContext().getSystemService(Context.KEYGUARD_SERVICE);
            mKeyguardLock = km.newKeyguardLock("CellBroadcastReceiver");
            mWarningIconView = (ImageView) findViewById(com.android.internal.R.id.icon);
        }
    }

    /**
     * Start animating warning icon.
     */
    @Override
    protected void onStart() {
        if (mShowWarningIcon) {
            // Disable keyguard
            mKeyguardLock.disableKeyguard();
            // start icon animation
            mAnimationHandler.sendEmptyMessageDelayed(0, WARNING_ICON_ON_DURATION_MSEC);
        }
    }

    /**
     * Stop animating warning icon and stop the {@link CellBroadcastAlertAudio}
     * service if necessary.
     */
    @Override
    protected void onStop() {
        // Stop playing alert sound/vibration/speech (if started)
        Context context = getContext();
        context.stopService(new Intent(context, CellBroadcastAlertAudio.class));
        // Start database service to mark broadcast as read
        Intent intent = new Intent(context, CellBroadcastDatabaseService.class);
        intent.setAction(CellBroadcastDatabaseService.ACTION_MARK_BROADCAST_READ);
        intent.putExtra(CellBroadcastDatabaseService.DATABASE_DELIVERY_TIME_EXTRA, mDeliveryTime);
        context.startService(intent);
        if (mShowWarningIcon) {
            // Reenable keyguard
            mKeyguardLock.reenableKeyguard();
            // stop animating icon
            mStopAnimation = true;
        }
    }

    /**
     * Ignore the back button for emergency alerts (user must dismiss with button).
     */
    @Override
    public void onBackPressed() {
        if (!mShowWarningIcon) {
            super.onBackPressed();
        }
    }
}
