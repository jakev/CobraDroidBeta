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

package com.android.musicvis;

import static android.renderscript.Element.RGB_565;
import static android.renderscript.Sampler.Value.LINEAR;
import static android.renderscript.Sampler.Value.WRAP;

import android.os.Handler;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Primitive;
import android.renderscript.ProgramFragment;
import android.renderscript.ProgramVertex;
import android.renderscript.Sampler;
import android.renderscript.ScriptC;
import android.renderscript.SimpleMesh;
import android.renderscript.Type;
import android.renderscript.Element.Builder;
import android.util.Log;

import java.util.TimeZone;

public class GenericWaveRS extends RenderScriptScene {

    private final Handler mHandler = new Handler();
    private final Runnable mDrawCube = new Runnable() {
        public void run() {
            updateWave();
        }
    };
    private boolean mVisible;
    private int mTexId;

    protected static class WorldState {
        public float yRotation;
        public int idle;
        public int waveCounter;
        public int width;
    }
    protected WorldState mWorldState = new WorldState();
    private Type mStateType;
    protected Allocation mState;

    private SimpleMesh mCubeMesh;

    protected Allocation mPointAlloc;
    // 1024 lines, with 4 points per line (2 space, 2 texture) each consisting of x and y,
    // so 8 floats per line.
    protected float [] mPointData = new float[1024*8];

    private Allocation mLineIdxAlloc;
    // 2 indices per line
    private short [] mIndexData = new short[1024*2];

    private ProgramVertex mPVBackground;
    private ProgramVertex.MatrixAllocation mPVAlloc;

    protected AudioCapture mAudioCapture = null;
    protected int [] mVizData = new int[1024];

    private ProgramFragment mPfBackground;
    private Sampler mSampler;
    private Allocation mTexture;

    private static final int RSID_STATE = 0;
    private static final int RSID_POINTS = 1;
    private static final int RSID_LINES = 2;
    private static final int RSID_PROGRAMVERTEX = 3;

    protected GenericWaveRS(int width, int height, int texid) {
        super(width, height);
        mTexId = texid;
        mWidth = width;
        mHeight = height;
        // the x, s and t coordinates don't change, so set those now
        int outlen = mPointData.length / 8;
        int half = outlen / 2;
        for(int i = 0; i < outlen; i++) {
            mPointData[i*8]   = i - half;          // start point X (Y set later)
            mPointData[i*8+2] = 0;                 // start point S
            mPointData[i*8+3] = 0;                 // start point T
            mPointData[i*8+4]   = i - half;        // end point X (Y set later)
            mPointData[i*8+6] = 1.0f;                 // end point S
            mPointData[i*8+7] = 0f;              // end point T
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        mWorldState.width = width;
        if (mPVAlloc != null) {
            mPVAlloc.setupProjectionNormalized(mWidth, mHeight);
        }
    }

    @Override
    protected ScriptC createScript() {

        // Create a renderscript type from a java class. The specified name doesn't
        // really matter; the name by which we refer to the object in RenderScript
        // will be specified later.
        mStateType = Type.createFromClass(mRS, WorldState.class, 1, "WorldState");
        // Create an allocation from the type we just created.
        mState = Allocation.createTyped(mRS, mStateType);
        // set our java object as the data for the renderscript allocation
        mWorldState.yRotation = 0.0f;
        mWorldState.width = mWidth;
        mState.data(mWorldState);

        /*
         *  Now put our model in to a form that renderscript can work with:
         *  - create a buffer of floats that are the coordinates for the points that define the cube
         *  - create a buffer of integers that are the indices of the points that form lines
         *  - combine the two in to a mesh
         */

        // First set up the coordinate system and such
        ProgramVertex.Builder pvb = new ProgramVertex.Builder(mRS, null, null);
        mPVBackground = pvb.create();
        mPVBackground.setName("PVBackground");
        mPVAlloc = new ProgramVertex.MatrixAllocation(mRS);
        mPVBackground.bindAllocation(mPVAlloc);
        mPVAlloc.setupProjectionNormalized(mWidth, mHeight);

        // Start creating the mesh
        final SimpleMesh.Builder meshBuilder = new SimpleMesh.Builder(mRS);

        // Create the Element for the points
        Builder elementBuilder = new Builder(mRS);
        elementBuilder.add(Element.ATTRIB_POSITION_2(mRS), "position");
        elementBuilder.add(Element.ATTRIB_TEXTURE_2(mRS), "texture");
        final Element vertexElement = elementBuilder.create();
        final int vertexSlot = meshBuilder.addVertexType(vertexElement, mPointData.length / 4);
        // Specify the type and number of indices we need. We'll allocate them later.
        meshBuilder.setIndexType(Element.INDEX_16(mRS), mIndexData.length);
        // This will be a line mesh
        meshBuilder.setPrimitive(Primitive.LINE);

        // Create the Allocation for the vertices
        mCubeMesh = meshBuilder.create();
        mCubeMesh.setName("CubeMesh");
        mPointAlloc = mCubeMesh.createVertexAllocation(vertexSlot);
        mPointAlloc.setName("PointBuffer");

        // Create the Allocation for the indices
        mLineIdxAlloc = mCubeMesh.createIndexAllocation();

        // Bind the allocations to the mesh
        mCubeMesh.bindVertexAllocation(mPointAlloc, 0);
        mCubeMesh.bindIndexAllocation(mLineIdxAlloc);

        /*
         *  put the vertex and index data in their respective buffers
         */
        updateWave();
        for(int i = 0; i < mIndexData.length; i ++) {
            mIndexData[i] = (short) i;
        }

        /*
         *  upload the vertex and index data
         */
        mPointAlloc.data(mPointData);
        mPointAlloc.uploadToBufferObject();
        mLineIdxAlloc.data(mIndexData);
        mLineIdxAlloc.uploadToBufferObject();

        /*
         * load the texture
         */
        mTexture = Allocation.createFromBitmapResourceBoxed(mRS, mResources, mTexId, RGB_565(mRS), false);
        mTexture.setName("Tlinetexture");
        mTexture.uploadToTexture(0);

        /*
         * create a program fragment to use the texture
         */
        Sampler.Builder samplerBuilder = new Sampler.Builder(mRS);
        samplerBuilder.setMin(LINEAR);
        samplerBuilder.setMag(LINEAR);
        samplerBuilder.setWrapS(WRAP);
        samplerBuilder.setWrapT(WRAP);
        mSampler = samplerBuilder.create();

        ProgramFragment.Builder builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                           ProgramFragment.Builder.Format.RGBA, 0);
        mPfBackground = builder.create();
        mPfBackground.setName("PFBackground");
        mPfBackground.bindSampler(mSampler, 0);

        // Time to create the script
        ScriptC.Builder sb = new ScriptC.Builder(mRS);
        // Specify the name by which to refer to the WorldState object in the
        // renderscript.
        sb.setType(mStateType, "State", RSID_STATE);
        sb.setType(mCubeMesh.getVertexType(0), "Points", RSID_POINTS);
        // this crashes when uncommented
        //sb.setType(mCubeMesh.getIndexType(), "Lines", RSID_LINES);
        sb.setScript(mResources, R.raw.waveform);
        sb.setRoot(true);

        ScriptC script = sb.create();
        script.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        script.setTimeZone(TimeZone.getDefault().getID());

        script.bindAllocation(mState, RSID_STATE);
        script.bindAllocation(mPointAlloc, RSID_POINTS);
        script.bindAllocation(mLineIdxAlloc, RSID_LINES);
        script.bindAllocation(mPVAlloc.mAlloc, RSID_PROGRAMVERTEX);

        return script;
    }

    @Override
    public void setOffset(float xOffset, float yOffset,
            float xStep, float yStep, int xPixels, int yPixels) {
        // update our state, then push it to the renderscript

        if (xStep <= 0.0f) {
            xStep = xOffset / 2; // originator didn't set step size, assume we're halfway
        }
        // rotate 180 degrees per screen
        mWorldState.yRotation = xStep == 0.f ? 0.f : (xOffset / xStep) * 180;
        mState.data(mWorldState);
    }

    @Override
    public void start() {
        super.start();
        mVisible = true;
        if (mAudioCapture != null) {
            mAudioCapture.start();
        }
        SystemClock.sleep(200);
        updateWave();
    }

    @Override
    public void stop() {
        super.stop();
        mVisible = false;
        if (mAudioCapture != null) {
            mAudioCapture.stop();
        }
        updateWave();
    }

    public void update() {
    }

    void updateWave() {
        mHandler.removeCallbacks(mDrawCube);
        if (!mVisible) {
            return;
        }
        mHandler.postDelayed(mDrawCube, 20);
        update();
        mWorldState.waveCounter++;
        mState.data(mWorldState);
    }
}
