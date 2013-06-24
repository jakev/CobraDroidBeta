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

package com.android.wallpaper.grass;

import android.renderscript.Sampler;
import static android.renderscript.ProgramStore.DepthFunc.*;
import static android.renderscript.ProgramStore.BlendSrcFunc;
import static android.renderscript.ProgramStore.BlendDstFunc;
import android.renderscript.ProgramFragment;
import android.renderscript.ProgramStore;
import android.renderscript.Allocation;
import android.renderscript.ProgramVertex;
import static android.renderscript.Element.*;
import static android.util.MathUtils.*;
import android.renderscript.ScriptC;
import android.renderscript.Type;
import android.renderscript.Dimension;
import android.renderscript.Element;
import android.renderscript.SimpleMesh;
import android.renderscript.Primitive;
import static android.renderscript.Sampler.Value.*;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.os.Bundle;
import android.text.format.Time;
import com.android.wallpaper.R;
import com.android.wallpaper.RenderScriptScene;

import java.util.TimeZone;
import java.util.Calendar;

class GrassRS extends RenderScriptScene {
    @SuppressWarnings({"UnusedDeclaration"})
    private static final String LOG_TAG = "Grass";
    private static final boolean DEBUG = false;

    private static final int LOCATION_UPDATE_MIN_TIME = DEBUG ? 5 * 60 * 1000 : 60 * 60 * 1000; // 1 hour
    private static final int LOCATION_UPDATE_MIN_DISTANCE = DEBUG ? 10 : 150 * 1000; // 150 km

    private static final float TESSELATION = 0.5f;
    private static final int TEXTURES_COUNT = 5;

    private static final int RSID_STATE = 0;
    private static final int RSID_BLADES = 1;
    private static final int BLADES_COUNT = 200;

    class BladesStruct {
        public float angle;
        public int size;
        public float xPos;
        public float yPos;
        public float offset;
        public float scale;
        public float lengthX;
        public float lengthY;
        public float hardness;
        public float h;
        public float s;
        public float b;
        public float turbulencex;
    };

    private static final int RSID_BLADES_BUFFER = 2;

    private ScriptC.Invokable mUpdateBladesInvokable;
    @SuppressWarnings({ "FieldCanBeLocal" })
    private ProgramFragment mPfBackground;
    @SuppressWarnings({ "FieldCanBeLocal" })
    private ProgramFragment mPfGrass;
    @SuppressWarnings({ "FieldCanBeLocal" })
    private ProgramStore mPfsBackground;
    @SuppressWarnings({ "FieldCanBeLocal" })
    private ProgramVertex mPvBackground;
    @SuppressWarnings({"FieldCanBeLocal"})
    private ProgramVertex.MatrixAllocation mPvOrthoAlloc;

    @SuppressWarnings({ "FieldCanBeLocal" })
    private Allocation[] mTextures;

    private Type mStateType;
    private Allocation mState;

    private Type mBladesType;
    private Allocation mBlades;
    private Allocation mBladesBuffer;
    private Allocation mBladesIndicies;
    @SuppressWarnings({"FieldCanBeLocal"})
    private SimpleMesh mBladesMesh;


    private int mVerticies;
    private int mIndicies;
    private int[] mBladeSizes;
    private final float[] mFloatData5 = new float[5];

    private WorldState mWorldState;

    private final Context mContext;
    private final LocationManager mLocationManager;

    private LocationUpdater mLocationUpdater;
    private GrassRS.TimezoneTracker mTimezoneTracker;

    GrassRS(Context context, int width, int height) {
        super(width, height);

        mContext = context;
        mLocationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void start() {
        super.start();

        if (mTimezoneTracker == null) {
            mTimezoneTracker = new TimezoneTracker();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_DATE_CHANGED);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            mContext.registerReceiver(mTimezoneTracker, filter);
        }

        if (mLocationUpdater == null) {
            mLocationUpdater = new LocationUpdater();
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationUpdater);
        }

        updateLocation();
    }

    @Override
    public void stop() {
        super.stop();

        if (mTimezoneTracker != null) {
            mContext.unregisterReceiver(mTimezoneTracker);
            mTimezoneTracker = null;
        }

        if (mLocationUpdater != null) {
            mLocationManager.removeUpdates(mLocationUpdater);
            mLocationUpdater = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        mWorldState.width = width;
        mWorldState.height = height;
        mState.data(mWorldState);

        mUpdateBladesInvokable.execute();
        mPvOrthoAlloc.setupOrthoWindow(width, height);
    }

    @Override
    protected ScriptC createScript() {
        createProgramVertex();
        createProgramFragmentStore();
        loadTextures();
        createProgramFragment();
        createScriptStructures();

        ScriptC.Builder sb = new ScriptC.Builder(mRS);
        sb.setType(mStateType, "State", RSID_STATE);
        sb.setType(mBladesType, "Blades", RSID_BLADES);
        sb.setScript(mResources, R.raw.grass);
        sb.setRoot(true);
        mUpdateBladesInvokable = sb.addInvokable("updateBlades");

        ScriptC script = sb.create();
        script.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        script.setTimeZone(TimeZone.getDefault().getID());

        script.bindAllocation(mState, RSID_STATE);
        script.bindAllocation(mBlades, RSID_BLADES);
        script.bindAllocation(mBladesBuffer, RSID_BLADES_BUFFER);

        return script;
    }

    private void createScriptStructures() {
        createBlades();
        createState();
    }

    @Override
    public void setOffset(float xOffset, float yOffset, int xPixels, int yPixels) {
        mWorldState.xOffset = xOffset;
        mState.data(mWorldState);
    }

    static class WorldState {
        public int bladesCount;
        public int indexCount;
        public int width;
        public int height;
        public float xOffset;
        public float dawn;
        public float morning;
        public float afternoon;
        public float dusk;
        public int isPreview;
    }

    private void createState() {
        final boolean isPreview = isPreview();

        mWorldState = new WorldState();
        mWorldState.width = mWidth;
        mWorldState.height = mHeight;
        mWorldState.bladesCount = BLADES_COUNT;
        mWorldState.indexCount = mIndicies;
        mWorldState.isPreview = isPreview ? 1 : 0;
        if (isPreview) {
            mWorldState.xOffset = 0.5f;
        }

        mStateType = Type.createFromClass(mRS, WorldState.class, 1, "WorldState");
        mState = Allocation.createTyped(mRS, mStateType);
        mState.data(mWorldState);
    }

    private void createBlades() {
        mVerticies = 0;
        mIndicies = 0;

        mBladesType = Type.createFromClass(mRS, BladesStruct.class, BLADES_COUNT, "Blade");
        mBlades = Allocation.createTyped(mRS, mBladesType);
        BladesStruct bs = new BladesStruct();

        mBladeSizes = new int[BLADES_COUNT];
        for (int i = 0; i < BLADES_COUNT; i++) {
            createBlade(bs);
            mIndicies += bs.size * 2 * 3;
            mVerticies += bs.size + 2;
            mBlades.subData(i, bs);
            mBladeSizes[i] = bs.size;
        }

        createMesh();
    }

    private void createMesh() {
        Builder elementBuilder = new Builder(mRS);
        elementBuilder.add(Element.ATTRIB_COLOR_U8_4(mRS), "color");
        elementBuilder.add(Element.ATTRIB_POSITION_2(mRS), "position");
        elementBuilder.add(Element.ATTRIB_TEXTURE_2(mRS), "texture");
        final Element vertexElement = elementBuilder.create();

        final SimpleMesh.Builder meshBuilder = new SimpleMesh.Builder(mRS);
        final int vertexSlot = meshBuilder.addVertexType(vertexElement, mVerticies  * 2);
        meshBuilder.setIndexType(Element.INDEX_16(mRS), mIndicies);
        meshBuilder.setPrimitive(Primitive.TRIANGLE);
        mBladesMesh = meshBuilder.create();
        mBladesMesh.setName("BladesMesh");

        mBladesBuffer = mBladesMesh.createVertexAllocation(vertexSlot);
        mBladesBuffer.setName("BladesBuffer");
        mBladesMesh.bindVertexAllocation(mBladesBuffer, 0);
        mBladesIndicies = mBladesMesh.createIndexAllocation();
        mBladesMesh.bindIndexAllocation(mBladesIndicies);

        // Assign the texture coordinates of each triangle
        final float[] floatData = mFloatData5;
        final Allocation buffer = mBladesBuffer;

        short[] idx = new short[mIndicies];

        int bufferIndex = 0;
        int i2 = 0;
        for (int i = 0; i < mVerticies; i +=2) {
            floatData[3] = 0.0f;
            floatData[4] = 0.0f;
            buffer.subData1D(bufferIndex++, 1, floatData);

            floatData[3] = 1.0f;
            floatData[4] = 0.0f;
            buffer.subData1D(bufferIndex++, 1, floatData);
        }

        int idxIdx = 0;
        int vtxIdx = 0;
        for (int i = 0; i < mBladeSizes.length; i++) {
            for (int ct = 0; ct < mBladeSizes[i]; ct ++) {
                idx[idxIdx + 0] = (short)(vtxIdx + 0);
                idx[idxIdx + 1] = (short)(vtxIdx + 1);
                idx[idxIdx + 2] = (short)(vtxIdx + 2);
                idx[idxIdx + 3] = (short)(vtxIdx + 1);
                idx[idxIdx + 4] = (short)(vtxIdx + 3);
                idx[idxIdx + 5] = (short)(vtxIdx + 2);
                idxIdx += 6;
                vtxIdx += 2;
            }
            vtxIdx += 2;
        }

        mBladesIndicies.data(idx);
        mBladesIndicies.uploadToBufferObject();
    }

    private void createBlade(BladesStruct blades) {
        final float size = random(4.0f) + 4.0f;
        final int xpos = random(-mWidth, mWidth);

        //noinspection PointlessArithmeticExpression
        blades.angle = 0.0f;
        blades.size = (int)(size / TESSELATION);
        blades.xPos = xpos;
        blades.yPos = mHeight;
        blades.offset = random(0.2f) - 0.1f;
        blades.scale = 4.0f / (size / TESSELATION) + (random(0.6f) + 0.2f) * TESSELATION;
        blades.lengthX = (random(4.5f) + 3.0f) * TESSELATION * size;
        blades.lengthY = (random(5.5f) + 2.0f) * TESSELATION * size;
        blades.hardness = (random(1.0f) + 0.2f) * TESSELATION;
        blades.h = random(0.02f) + 0.2f;
        blades.s = random(0.22f) + 0.78f;
        blades.b = random(0.65f) + 0.35f;
        blades.turbulencex = xpos * 0.006f;
    }

    private void loadTextures() {
        mTextures = new Allocation[TEXTURES_COUNT];

        final Allocation[] textures = mTextures;
        textures[0] = loadTexture(R.drawable.night, "TNight");
        textures[1] = loadTexture(R.drawable.sunrise, "TSunrise");
        textures[2] = loadTexture(R.drawable.sky, "TSky");
        textures[3] = loadTexture(R.drawable.sunset, "TSunset");
        textures[4] = generateTextureAlpha(4, 1, new int[] { 0x00FFFF00 }, "TAa");

        final int count = textures.length;
        for (int i = 0; i < count; i++) {
            textures[i].uploadToTexture(0);
        }
    }

    private Allocation generateTextureAlpha(int width, int height, int[] data, String name) {
        final Type.Builder builder = new Type.Builder(mRS, A_8(mRS));
        builder.add(Dimension.X, width);
        builder.add(Dimension.Y, height);
        builder.add(Dimension.LOD, 1);

        final Allocation allocation = Allocation.createTyped(mRS, builder.create());
        allocation.setName(name);

        int[] grey1 = new int[] {0x3f3f3f3f};
        int[] grey2 = new int[] {0x00000000};
        Allocation.Adapter2D a = allocation.createAdapter2D();
        a.setConstraint(Dimension.LOD, 0);
        a.subData(0, 0, 4, 1, data);
        a.setConstraint(Dimension.LOD, 1);
        a.subData(0, 0, 2, 1, grey1);
        a.setConstraint(Dimension.LOD, 2);
        a.subData(0, 0, 1, 1, grey2);

        return allocation;
    }

    private Allocation loadTexture(int id, String name) {
        final Allocation allocation = Allocation.createFromBitmapResource(mRS, mResources,
                id, RGB_565(mRS), false);
        allocation.setName(name);
        return allocation;
    }

    private void createProgramFragment() {
        Sampler.Builder samplerBuilder = new Sampler.Builder(mRS);
        samplerBuilder.setMin(LINEAR_MIP_LINEAR);
        samplerBuilder.setMag(LINEAR);
        samplerBuilder.setWrapS(WRAP);
        samplerBuilder.setWrapT(WRAP);
        Sampler sl = samplerBuilder.create();

        samplerBuilder.setMin(NEAREST);
        samplerBuilder.setMag(NEAREST);
        Sampler sn = samplerBuilder.create();

        ProgramFragment.Builder builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                           ProgramFragment.Builder.Format.ALPHA, 0);
        mPfGrass = builder.create();
        mPfGrass.setName("PFGrass");
        mPfGrass.bindSampler(sl, 0);

        builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                           ProgramFragment.Builder.Format.RGB, 0);
        mPfBackground = builder.create();
        mPfBackground.setName("PFBackground");
        mPfBackground.bindSampler(sn, 0);
    }

    private void createProgramFragmentStore() {
        ProgramStore.Builder builder = new ProgramStore.Builder(mRS, null, null);
        builder.setDepthFunc(ALWAYS);
        builder.setBlendFunc(BlendSrcFunc.SRC_ALPHA, BlendDstFunc.ONE_MINUS_SRC_ALPHA);
        builder.setDitherEnable(false);
        builder.setDepthMask(false);
        mPfsBackground = builder.create();
        mPfsBackground.setName("PFSBackground");
    }

    private void createProgramVertex() {
        mPvOrthoAlloc = new ProgramVertex.MatrixAllocation(mRS);
        mPvOrthoAlloc.setupOrthoWindow(mWidth, mHeight);

        ProgramVertex.Builder pvb = new ProgramVertex.Builder(mRS, null, null);
        mPvBackground = pvb.create();
        mPvBackground.bindAllocation(mPvOrthoAlloc);
        mPvBackground.setName("PVBackground");
    }

    private void updateLocation() {
        updateLocation(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
    }

    private void updateLocation(Location location) {
        if (location != null) {
            final String timeZone = Time.getCurrentTimezone();
            final SunCalculator calculator = new SunCalculator(location, timeZone);
            final Calendar now = Calendar.getInstance();

            final double sunrise = calculator.computeSunriseTime(SunCalculator.ZENITH_CIVIL, now);
            mWorldState.dawn = SunCalculator.timeToDayFraction(sunrise);

            final double sunset = calculator.computeSunsetTime(SunCalculator.ZENITH_CIVIL, now);
            mWorldState.dusk = SunCalculator.timeToDayFraction(sunset);
        } else {
            mWorldState.dawn = 0.3f;
            mWorldState.dusk = 0.75f;
        }

        mWorldState.morning = mWorldState.dawn + 1.0f / 12.0f; // 2 hours for sunrise
        mWorldState.afternoon = mWorldState.dusk - 1.0f / 12.0f; // 2 hours for sunset

        // Send the new data to RenderScript
        mState.data(mWorldState);
    }

    private class LocationUpdater implements LocationListener {
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    private class TimezoneTracker extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            getScript().setTimeZone(Time.getCurrentTimezone());
            updateLocation();
        }
    }
}
