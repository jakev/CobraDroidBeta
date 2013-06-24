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

package com.android.wallpaper.fall;

import android.os.Bundle;
import android.renderscript.Element;
import android.renderscript.ScriptC;
import android.renderscript.ProgramFragment;
import android.renderscript.ProgramStore;
import android.renderscript.ProgramVertex;
import android.renderscript.Allocation;
import android.renderscript.Sampler;
import android.renderscript.Type;
import android.renderscript.SimpleMesh;
import android.renderscript.Script;
import static android.renderscript.Sampler.Value.LINEAR;
import static android.renderscript.Sampler.Value.CLAMP;
import static android.renderscript.ProgramStore.DepthFunc.*;
import static android.renderscript.ProgramStore.BlendDstFunc;
import static android.renderscript.ProgramStore.BlendSrcFunc;
import static android.renderscript.Element.*;

import android.app.WallpaperManager;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import static android.util.MathUtils.*;

import java.util.TimeZone;

import com.android.wallpaper.R;
import com.android.wallpaper.RenderScriptScene;

class FallRS extends RenderScriptScene {
    private static final int MESH_RESOLUTION = 48;

    private static final int RSID_STATE = 0;
    private static final int RSID_CONSTANTS = 1;
    private static final int RSID_DROP = 2;

    private static final int TEXTURES_COUNT = 2;
    private static final int RSID_TEXTURE_RIVERBED = 0;
    private static final int RSID_TEXTURE_LEAVES = 1;

    private final BitmapFactory.Options mOptionsARGB = new BitmapFactory.Options();

    @SuppressWarnings({"FieldCanBeLocal"})
    private ProgramFragment mPfBackground;
    @SuppressWarnings({"FieldCanBeLocal"})
    private ProgramFragment mPfSky;
    @SuppressWarnings({"FieldCanBeLocal"})
    private ProgramStore mPfsBackground;
    @SuppressWarnings({"FieldCanBeLocal"})
    private ProgramStore mPfsLeaf;
    @SuppressWarnings({"FieldCanBeLocal"})
    private ProgramVertex mPvSky;
    @SuppressWarnings({"FieldCanBeLocal"})
    private ProgramVertex mPvWater;
    private ProgramVertex.MatrixAllocation mPvOrthoAlloc;
    @SuppressWarnings({"FieldCanBeLocal"})
    private Sampler mSampler;

    private Allocation mState;
    private Allocation mDropState;
    private DropState mDrop;
    private Type mStateType;
    private Type mDropType;
    private int mMeshWidth;
    private Allocation mUniformAlloc;

    private int mMeshHeight;
    @SuppressWarnings({"FieldCanBeLocal"})
    private SimpleMesh mMesh;
    private WorldState mWorldState;

    private float mGlHeight;

    public FallRS(int width, int height) {
        super(width, height);

        mOptionsARGB.inScaled = false;
        mOptionsARGB.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    @Override
    public void setOffset(float xOffset, float yOffset, int xPixels, int yPixels) {
        mWorldState.xOffset = xOffset;
        mState.data(mWorldState);
    }

    @Override
    public Bundle onCommand(String action, int x, int y, int z, Bundle extras,
            boolean resultRequested) {
        if (WallpaperManager.COMMAND_TAP.equals(action)) {
            addDrop(x + (mWorldState.rotate == 0 ? (mWorldState.width * mWorldState.xOffset) : 0), y);
        } else if (WallpaperManager.COMMAND_DROP.equals(action)) {
            addDrop(x + (mWorldState.rotate == 0 ? (mWorldState.width * mWorldState.xOffset) : 0), y);
        }
        return null;
    }

    @Override
    public void start() {
        super.start();
        final WorldState worldState = mWorldState;
        final int width = worldState.width;
        final int x = width / 4 + (int)(Math.random() * (width / 2));
        final int y = worldState.height / 4 + (int)(Math.random() * (worldState.height / 2));
        addDrop(x + (mWorldState.rotate == 0 ? (width * worldState.xOffset) : 0), y);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        mWorldState.width = width;
        mWorldState.height = height;
        mWorldState.rotate = width > height ? 1 : 0;
        mState.data(mWorldState);

        mPvOrthoAlloc.setupProjectionNormalized(mWidth, mHeight);
    }

    @Override
    protected ScriptC createScript() {
        createMesh();
        createState();
        createProgramVertex();
        createProgramFragmentStore();
        createProgramFragment();
        loadTextures();

        ScriptC.Builder sb = new ScriptC.Builder(mRS);
        sb.setType(mStateType, "State", RSID_STATE);
        sb.setType(mDropType, "Drop", RSID_DROP);
        sb.setType(mUniformAlloc.getType(), "Constants", RSID_CONSTANTS);
        sb.setScript(mResources, R.raw.fall);
        Script.Invokable invokable = sb.addInvokable("initLeaves");
        sb.setRoot(true);

        ScriptC script = sb.create();
        script.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        script.setTimeZone(TimeZone.getDefault().getID());

        script.bindAllocation(mState, RSID_STATE);
        script.bindAllocation(mUniformAlloc, RSID_CONSTANTS);
        script.bindAllocation(mDropState, RSID_DROP);

        invokable.execute();

        return script;
    }

    private void createMesh() {
        SimpleMesh.TriangleMeshBuilder tmb = new SimpleMesh.TriangleMeshBuilder(mRS, 2, 0);

        final int width = mWidth > mHeight ? mHeight : mWidth;
        final int height = mWidth > mHeight ? mWidth : mHeight;

        int wResolution = MESH_RESOLUTION;
        int hResolution = (int) (MESH_RESOLUTION * height / (float) width);

        mGlHeight = 2.0f * height / (float) width;

        wResolution += 2;
        hResolution += 2;

        for (int y = 0; y <= hResolution; y++) {
            final float yOffset = (((float)y / hResolution) * 2.f - 1.f) * height / width;
            for (int x = 0; x <= wResolution; x++) {
                tmb.addVertex(((float)x / wResolution) * 2.f - 1.f, yOffset);
            }
        }

        for (int y = 0; y < hResolution; y++) {
            final boolean shift = (y & 0x1) == 0;
            final int yOffset = y * (wResolution + 1);
            for (int x = 0; x < wResolution; x++) {
                final int index = yOffset + x;
                final int iWR1 = index + wResolution + 1;
                if (shift) {
                    tmb.addTriangle(index, index + 1, iWR1);
                    tmb.addTriangle(index + 1, iWR1 + 1, iWR1);
                } else {
                    tmb.addTriangle(index, iWR1 + 1, iWR1);
                    tmb.addTriangle(index, index + 1, iWR1 + 1);
                }
            }
        }

        mMesh = tmb.create();
        mMesh.setName("WaterMesh");

        mMeshWidth = wResolution + 1;
        mMeshHeight = hResolution + 1;
    }

    static class WorldState {
        public int frameCount;
        public int width;
        public int height;
        public int meshWidth;
        public int meshHeight;
        public int rippleIndex;
        public float glWidth;
        public float glHeight;
        public float skySpeedX;
        public float skySpeedY;
        public int rotate;
        public int isPreview;
        public float xOffset;
    }

    static class DropState {
        public int dropX;
        public int dropY;
    }

    private void createState() {
        mWorldState = new WorldState();
        mWorldState.width = mWidth;
        mWorldState.height = mHeight;
        mWorldState.meshWidth = mMeshWidth;
        mWorldState.meshHeight = mMeshHeight;
        mWorldState.rippleIndex = 0;
        mWorldState.glWidth = 2.0f;
        mWorldState.glHeight = mGlHeight;
        mWorldState.skySpeedX = random(-0.001f, 0.001f);
        mWorldState.skySpeedY = random(0.00008f, 0.0002f);
        mWorldState.rotate = mWidth > mHeight ? 1 : 0;
        mWorldState.isPreview = isPreview() ? 1 : 0;

        mStateType = Type.createFromClass(mRS, WorldState.class, 1, "WorldState");
        mState = Allocation.createTyped(mRS, mStateType);
        mState.data(mWorldState);

        mDrop = new DropState();
        mDrop.dropX = -1;
        mDrop.dropY = -1;

        mDropType = Type.createFromClass(mRS, DropState.class, 1, "DropState");
        mDropState = Allocation.createTyped(mRS, mDropType);
        mDropState.data(mDrop);
    }

    private void loadTextures() {
        final Allocation[] textures = new Allocation[TEXTURES_COUNT];
        textures[RSID_TEXTURE_RIVERBED] = loadTexture(R.drawable.pond, "TRiverbed");
        textures[RSID_TEXTURE_LEAVES] = loadTextureARGB(R.drawable.leaves, "TLeaves");

        final int count = textures.length;
        for (int i = 0; i < count; i++) {
            textures[i].uploadToTexture(0);
        }
    }

    private Allocation loadTexture(int id, String name) {
        final Allocation allocation = Allocation.createFromBitmapResource(mRS, mResources,
                id, RGB_565(mRS), false);
        allocation.setName(name);
        return allocation;
    }

    private Allocation loadTextureARGB(int id, String name) {
        Bitmap b = BitmapFactory.decodeResource(mResources, id, mOptionsARGB);
        final Allocation allocation = Allocation.createFromBitmap(mRS, b, RGBA_8888(mRS), false);
        allocation.setName(name);
        return allocation;
    }

    private void createProgramFragment() {
        Sampler.Builder sampleBuilder = new Sampler.Builder(mRS);
        sampleBuilder.setMin(LINEAR);
        sampleBuilder.setMag(LINEAR);
        sampleBuilder.setWrapS(CLAMP);
        sampleBuilder.setWrapT(CLAMP);
        mSampler = sampleBuilder.create();

        ProgramFragment.Builder builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.REPLACE,
                           ProgramFragment.Builder.Format.RGBA, 0);
        mPfBackground = builder.create();
        mPfBackground.setName("PFBackground");
        mPfBackground.bindSampler(mSampler, 0);

        builder = new ProgramFragment.Builder(mRS);
        builder.setTexture(ProgramFragment.Builder.EnvMode.MODULATE,
                           ProgramFragment.Builder.Format.RGBA, 0);
        mPfSky = builder.create();
        mPfSky.setName("PFSky");
        mPfSky.bindSampler(mSampler, 0);
    }

    private void createProgramFragmentStore() {
        ProgramStore.Builder builder = new ProgramStore.Builder(mRS, null, null);
        builder.setDepthFunc(ALWAYS);
        builder.setBlendFunc(BlendSrcFunc.ONE, BlendDstFunc.ONE);
        builder.setDitherEnable(false);
        builder.setDepthMask(true);
        mPfsBackground = builder.create();
        mPfsBackground.setName("PFSBackground");

        builder = new ProgramStore.Builder(mRS, null, null);
        builder.setDepthFunc(ALWAYS);
        builder.setBlendFunc(BlendSrcFunc.SRC_ALPHA, BlendDstFunc.ONE_MINUS_SRC_ALPHA);
        builder.setDitherEnable(false);
        builder.setDepthMask(true);
        mPfsLeaf = builder.create();
        mPfsLeaf.setName("PFSLeaf");
    }

    private void createProgramVertex() {
        mPvOrthoAlloc = new ProgramVertex.MatrixAllocation(mRS);
        mPvOrthoAlloc.setupProjectionNormalized(mWidth, mHeight);

        ProgramVertex.Builder builder = new ProgramVertex.Builder(mRS, null, null);
        mPvSky = builder.create();
        mPvSky.bindAllocation(mPvOrthoAlloc);
        mPvSky.setName("PVSky");

        Element.Builder eb = new Element.Builder(mRS);
        // Make this an array when we can.
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop01");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop02");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop03");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop04");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop05");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop06");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop07");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop08");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop09");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Drop10");
        eb.add(Element.createVector(mRS, Element.DataType.FLOAT_32, 4), "Offset");
        eb.add(Element.USER_F32(mRS), "Rotate");
        Element e = eb.create();

        mUniformAlloc = Allocation.createSized(mRS, e, 1);

        ProgramVertex.ShaderBuilder sb = new ProgramVertex.ShaderBuilder(mRS);

        String t = "\n" +
                "vec2 addDrop(vec4 d, vec2 pos, float dxMul) {\n" +
                "  vec2 ret = vec2(0.0, 0.0);\n" +
                "  vec2 delta = d.xy - pos;\n" +
                "  delta.x *= dxMul;\n" +
                "  float dist = length(delta);\n" +
                "  if (dist < d.w) { \n" +
                "    float amp = d.z * dist;\n" +
                "    amp /= d.w * d.w;\n" +
                "    amp *= sin(d.w - dist);\n" +
                "    ret = delta * amp;\n" +
                "  }\n" +
                "  return ret;\n" +
                "}\n" +

                "void main() {\n" +
                "  vec2 pos = ATTRIB_position.xy;\n" +
                "  gl_Position = vec4(pos.x, pos.y, 0.0, 1.0);\n" +
                "  float dxMul = 1.0;\n" +

                "  varTex0 = vec4((pos.x + 1.0), (pos.y + 1.6666), 0.0, 0.0);\n" +

                "  if (UNI_Rotate < 0.9) {\n" +
                "    varTex0.xy *= vec2(0.25, 0.33);\n" +
                "    varTex0.x += UNI_Offset.x * 0.5;\n" +
                "    pos.x += UNI_Offset.x * 2.0;\n" +
                "  } else {\n" +
                "    varTex0.xy *= vec2(0.5, 0.3125);\n" +
                "    dxMul = 2.5;\n" +
                "  }\n" +

                "  varColor = vec4(1.0, 1.0, 1.0, 1.0);\n" +
                "  pos.xy += vec2(1.0, 1.0);\n" +
                "  pos.xy *= vec2(25.0, 42.0);\n" +

                "  varTex0.xy += addDrop(UNI_Drop01, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop02, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop03, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop04, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop05, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop06, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop07, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop08, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop09, pos, dxMul);\n" +
                "  varTex0.xy += addDrop(UNI_Drop10, pos, dxMul);\n" +
                "}\n";
        sb.setShader(t);
        sb.addConstant(mUniformAlloc.getType());
        sb.addInput(mMesh.getVertexType(0).getElement());
        mPvWater = sb.create();
        mPvWater.bindAllocation(mPvOrthoAlloc);
        mPvWater.setName("PVWater");
        mPvWater.bindConstants(mUniformAlloc, 1);

    }

    void addDrop(float x, float y) {
        mDrop.dropX = (int) ((x / mWidth) * mMeshWidth);
        mDrop.dropY = (int) ((y / mHeight) * mMeshHeight);
        mDropState.data(mDrop);
    }
}