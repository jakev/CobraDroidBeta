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


package android.app.cts;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.WindowManager.LayoutParams;

@TestTargetClass(Activity.class)
public class LifecycleTest extends ActivityTestsBase {
    private Intent mTopIntent;
    private Intent mTabIntent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTopIntent = mIntent;
        mTabIntent = new Intent(mContext, LaunchpadTabActivity.class);
        mTabIntent.putExtra("tab", new ComponentName(mContext, LaunchpadActivity.class));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Activity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserInteraction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserLeaveHint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisible",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeContextMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInstanceCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getApplication",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isChild",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindowManager",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onNewIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateThumbnail",
            args = {Bitmap.class, Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDescription",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDestroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onConfigurationChanged",
            args = {Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getChangingConfigurations",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRetainNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLowMemory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String[].class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedCommitUpdates",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPersistent",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewById",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultKeyMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyMultiple",
            args = {int.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowAttributesChanged",
            args = {LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasWindowFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelMenu",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPreparePanel",
            args = {int.class, View.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuOpened",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuItemSelected",
            args = {int.class, MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPanelClosed",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {ContextMenu.class, View.class, ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareDialog",
            args = {int.class, Dialog.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "showDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dismissDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSearchRequested",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startSearch",
            args = {String.class, boolean.class, Bundle.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "takeKeyEvents",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestWindowFeature",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableResource",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableUri",
            args = {int.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawable",
            args = {int.class, Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableAlpha",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMenuInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onApplyThemeResource",
            args = {Theme.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityForResult",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityIfNeeded",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startNextMatchingActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityFromChild",
            args = {Activity.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingPackage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFinishing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finish",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishFromChild",
            args = {Activity.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivityFromChild",
            args = {Activity.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onActivityResult",
            args = {int.class, int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createPendingResult",
            args = {int.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRequestedOrientation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRequestedOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTaskId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isTaskRoot",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveTaskToBack",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalClassName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getComponentName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPreferences",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemService",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitleColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitleColor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTitleChanged",
            args = {CharSequence.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onChildTitleChanged",
            args = {Activity.class, CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminateVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminate",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSecondaryProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolumeControlStream",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVolumeControlStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "runOnUiThread",
            args = {Runnable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateView",
            args = {String.class, Context.class, AttributeSet.class}
        )
    })
    public void testTabDialog() {
        mIntent = mTabIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_DIALOG);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Activity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserInteraction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserLeaveHint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisible",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeContextMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInstanceCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getApplication",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isChild",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindowManager",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onNewIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateThumbnail",
            args = {Bitmap.class, Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDescription",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDestroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onConfigurationChanged",
            args = {Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getChangingConfigurations",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRetainNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLowMemory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String[].class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedCommitUpdates",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPersistent",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewById",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultKeyMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyMultiple",
            args = {int.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowAttributesChanged",
            args = {LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasWindowFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelMenu",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPreparePanel",
            args = {int.class, View.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuOpened",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuItemSelected",
            args = {int.class, MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPanelClosed",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {ContextMenu.class, View.class, ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareDialog",
            args = {int.class, Dialog.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "showDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dismissDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSearchRequested",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startSearch",
            args = {String.class, boolean.class, Bundle.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "takeKeyEvents",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestWindowFeature",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableResource",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableUri",
            args = {int.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawable",
            args = {int.class, Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableAlpha",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMenuInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onApplyThemeResource",
            args = {Theme.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityForResult",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityIfNeeded",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startNextMatchingActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityFromChild",
            args = {Activity.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingPackage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFinishing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finish",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishFromChild",
            args = {Activity.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivityFromChild",
            args = {Activity.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onActivityResult",
            args = {int.class, int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createPendingResult",
            args = {int.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRequestedOrientation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRequestedOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTaskId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isTaskRoot",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveTaskToBack",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalClassName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getComponentName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPreferences",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemService",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitleColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitleColor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTitleChanged",
            args = {CharSequence.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onChildTitleChanged",
            args = {Activity.class, CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminateVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminate",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSecondaryProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolumeControlStream",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVolumeControlStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "runOnUiThread",
            args = {Runnable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateView",
            args = {String.class, Context.class, AttributeSet.class}
        )
    })
    public void testDialog() {
        mIntent = mTopIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_DIALOG);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Activity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserInteraction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserLeaveHint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisible",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeContextMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInstanceCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getApplication",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isChild",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindowManager",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onNewIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateThumbnail",
            args = {Bitmap.class, Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDescription",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDestroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onConfigurationChanged",
            args = {Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getChangingConfigurations",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRetainNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLowMemory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String[].class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedCommitUpdates",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPersistent",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewById",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultKeyMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyMultiple",
            args = {int.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowAttributesChanged",
            args = {LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasWindowFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelMenu",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPreparePanel",
            args = {int.class, View.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuOpened",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuItemSelected",
            args = {int.class, MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPanelClosed",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {ContextMenu.class, View.class, ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareDialog",
            args = {int.class, Dialog.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "showDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dismissDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSearchRequested",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startSearch",
            args = {String.class, boolean.class, Bundle.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "takeKeyEvents",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestWindowFeature",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableResource",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableUri",
            args = {int.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawable",
            args = {int.class, Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableAlpha",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMenuInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onApplyThemeResource",
            args = {Theme.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityForResult",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityIfNeeded",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startNextMatchingActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityFromChild",
            args = {Activity.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingPackage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFinishing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finish",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishFromChild",
            args = {Activity.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivityFromChild",
            args = {Activity.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onActivityResult",
            args = {int.class, int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createPendingResult",
            args = {int.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRequestedOrientation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRequestedOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTaskId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isTaskRoot",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveTaskToBack",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalClassName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getComponentName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPreferences",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemService",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitleColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitleColor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTitleChanged",
            args = {CharSequence.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onChildTitleChanged",
            args = {Activity.class, CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminateVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminate",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSecondaryProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolumeControlStream",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVolumeControlStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "runOnUiThread",
            args = {Runnable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateView",
            args = {String.class, Context.class, AttributeSet.class}
        )
    })
    public void testTabScreen() {
        mIntent = mTabIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_SCREEN);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Activity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserInteraction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserLeaveHint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisible",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeContextMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInstanceCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getApplication",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isChild",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindowManager",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onNewIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateThumbnail",
            args = {Bitmap.class, Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDescription",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDestroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onConfigurationChanged",
            args = {Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getChangingConfigurations",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,

            method = "onRetainNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLowMemory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String[].class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedCommitUpdates",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPersistent",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewById",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultKeyMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyMultiple",
            args = {int.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowAttributesChanged",
            args = {LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasWindowFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelMenu",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPreparePanel",
            args = {int.class, View.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuOpened",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuItemSelected",
            args = {int.class, MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPanelClosed",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {ContextMenu.class, View.class, ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareDialog",
            args = {int.class, Dialog.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "showDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dismissDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSearchRequested",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startSearch",
            args = {String.class, boolean.class, Bundle.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "takeKeyEvents",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestWindowFeature",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableResource",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableUri",
            args = {int.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawable",
            args = {int.class, Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableAlpha",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMenuInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onApplyThemeResource",
            args = {Theme.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityForResult",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityIfNeeded",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startNextMatchingActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityFromChild",
            args = {Activity.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingPackage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFinishing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finish",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishFromChild",
            args = {Activity.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivityFromChild",
            args = {Activity.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onActivityResult",
            args = {int.class, int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createPendingResult",
            args = {int.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRequestedOrientation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRequestedOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTaskId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isTaskRoot",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveTaskToBack",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalClassName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getComponentName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPreferences",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemService",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitleColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitleColor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTitleChanged",
            args = {CharSequence.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onChildTitleChanged",
            args = {Activity.class, CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminateVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminate",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSecondaryProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolumeControlStream",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVolumeControlStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "runOnUiThread",
            args = {Runnable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateView",
            args = {String.class, Context.class, AttributeSet.class}
        )
    })
    public void testScreen() {
        mIntent = mTopIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_SCREEN);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Activity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserInteraction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserLeaveHint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisible",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeContextMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInstanceCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getApplication",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isChild",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindowManager",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onNewIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateThumbnail",
            args = {Bitmap.class, Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDescription",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDestroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onConfigurationChanged",
            args = {Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getChangingConfigurations",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRetainNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLowMemory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String[].class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedCommitUpdates",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPersistent",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewById",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultKeyMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyMultiple",
            args = {int.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowAttributesChanged",
            args = {LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasWindowFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelMenu",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPreparePanel",
            args = {int.class, View.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuOpened",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuItemSelected",
            args = {int.class, MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPanelClosed",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {ContextMenu.class, View.class, ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareDialog",
            args = {int.class, Dialog.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "showDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dismissDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSearchRequested",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startSearch",
            args = {String.class, boolean.class, Bundle.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "takeKeyEvents",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestWindowFeature",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableResource",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableUri",
            args = {int.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawable",
            args = {int.class, Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableAlpha",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMenuInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onApplyThemeResource",
            args = {Theme.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityForResult",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityIfNeeded",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startNextMatchingActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityFromChild",
            args = {Activity.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingPackage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFinishing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finish",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishFromChild",
            args = {Activity.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivityFromChild",
            args = {Activity.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onActivityResult",
            args = {int.class, int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createPendingResult",
            args = {int.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRequestedOrientation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRequestedOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTaskId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isTaskRoot",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveTaskToBack",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalClassName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getComponentName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPreferences",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemService",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitleColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitleColor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTitleChanged",
            args = {CharSequence.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onChildTitleChanged",
            args = {Activity.class, CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminateVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminate",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSecondaryProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolumeControlStream",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVolumeControlStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "runOnUiThread",
            args = {Runnable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateView",
            args = {String.class, Context.class, AttributeSet.class}
        )
    })
    public void testTabBasic() {
        mIntent = mTabIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_BASIC);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Activity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserInteraction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUserLeaveHint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisible",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeContextMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInstanceCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getApplication",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isChild",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParent",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindowManager",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWallpaperDesiredMinimumHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostCreate",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostResume",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onNewIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPause",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateThumbnail",
            args = {Bitmap.class, Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDescription",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDestroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onConfigurationChanged",
            args = {Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getChangingConfigurations",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRetainNonConfigurationInstance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLowMemory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedQuery",
            args = {Uri.class, String[].class, String.class, String[].class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "managedCommitUpdates",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stopManagingCursor",
            args = {Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPersistent",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewById",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addContentView",
            args = {View.class, ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultKeyMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
             method = "onKeyMultiple",
            args = {int.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowAttributesChanged",
            args = {LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContentChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasWindowFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelMenu",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPreparePanel",
            args = {int.class, View.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuOpened",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuItemSelected",
            args = {int.class, MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPanelClosed",
            args = {int.class, Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareOptionsMenu",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOptionsMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {ContextMenu.class, View.class, ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterForContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openContextMenu",
            args = {View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextItemSelected",
            args = {MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onContextMenuClosed",
            args = {Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareDialog",
            args = {int.class, Dialog.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "showDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dismissDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeDialog",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSearchRequested",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startSearch",
            args = {String.class, boolean.class, Bundle.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "takeKeyEvents",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestWindowFeature",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableResource",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableUri",
            args = {int.class, Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawable",
            args = {int.class, Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeatureDrawableAlpha",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMenuInflater",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onApplyThemeResource",
            args = {Theme.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityForResult",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityIfNeeded",
            args = {Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startNextMatchingActivity",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startActivityFromChild",
            args = {Activity.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setResult",
            args = {int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingPackage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallingActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFinishing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finish",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishFromChild",
            args = {Activity.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finishActivityFromChild",
            args = {Activity.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onActivityResult",
            args = {int.class, int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createPendingResult",
            args = {int.class, Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRequestedOrientation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRequestedOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTaskId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isTaskRoot",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "moveTaskToBack",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalClassName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getComponentName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPreferences",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemService",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitleColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitleColor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTitleChanged",
            args = {CharSequence.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onChildTitleChanged",
            args = {Activity.class, CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminateVisibility",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressBarIndeterminate",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSecondaryProgress",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolumeControlStream",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVolumeControlStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "runOnUiThread",
            args = {Runnable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateView",
            args = {String.class, Context.class, AttributeSet.class}
        )
    })
    public void testBasic() {
        mIntent = mTopIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_BASIC);
    }
}
