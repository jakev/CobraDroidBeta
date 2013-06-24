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
package android.media.cts;

import com.android.cts.stub.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class MediaStubActivity extends Activity {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;
    private SurfaceHolder mHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mediaplayer);

        SurfaceView surfaceV = (SurfaceView)findViewById(R.id.surface);
        ViewGroup.LayoutParams lp = surfaceV.getLayoutParams();
        lp.width = WIDTH;
        lp.height = HEIGHT;
        surfaceV.setLayoutParams(lp);
        mHolder = surfaceV.getHolder();
        mHolder.setFixedSize(WIDTH, HEIGHT);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public SurfaceHolder getSurfaceHolder() {
        return mHolder;
    }
}
