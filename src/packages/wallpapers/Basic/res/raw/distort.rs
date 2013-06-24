// Copyright (C) 2009 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#pragma version(1)
#pragma stateVertex(PVSky)
#pragma stateFragment(PFBackground)
#pragma stateStore(PFSBackground)

#define RSID_RIPPLE_MAP 1
#define RSID_REFRACTION_MAP 2

#define LEAVES_TEXTURES_COUNT 4

#define LEAF_SIZE 0.55f

#define REFRACTION 1.333f
#define DAMP 3

#define DROP_RADIUS 2
// The higher, the smaller the ripple
#define RIPPLE_HEIGHT 10.0f

float skyOffsetX;
float skyOffsetY;

int lastDrop;

struct vert_s {
    float x;
    float y;
    float z;
    float s;
    float t;
    float nx;
    float ny;
    float nz;
};

struct drop_s {
    float amp;
    float phase;
    float x;
    float y;
};
struct drop_s gDrops[10];

int offset(int x, int y, int width) {
    return x + 1 + (y + 1) * (width + 2);
}

void init() {
    gDrops[0].amp = 0.2f;
    gDrops[0].phase = 0;
}

void initLeaves() {
    if (State->isPreview) lastDrop = uptimeMillis();

    struct Leaves_s *leaf = Leaves;
    int leavesCount = State->leavesCount;
    float width = State->glWidth;
    float height = State->glHeight;

    int i;
    for (i = 0; i < leavesCount; i ++) {
        int sprite = randf(LEAVES_TEXTURES_COUNT);
        leaf->x = randf2(-width * 0.5f, width * 0.5f);
        leaf->y = randf2(-height * 0.5f, height * 0.5f);
        leaf->scale = randf2(0.4f, 0.5f);
        leaf->angle = randf2(0.0f, 360.0f);
        leaf->spin = degf(randf2(-0.02f, 0.02f)) * 0.25f;
        leaf->u1 = sprite / (float) LEAVES_TEXTURES_COUNT;
        leaf->u2 = (sprite + 1) / (float) LEAVES_TEXTURES_COUNT;
        leaf->altitude = -1.0f;
        leaf->rippled = 1.0f;
        leaf->deltaX = randf2(-0.02f, 0.02f) / 60.0f;
        leaf->deltaY = -0.08f * randf2(0.9f, 1.1f) / 60.0f;
        leaf++;
    }
}

void dropWithStrength(int x, int y, int r, int s) {
    int width = State->meshWidth;
    int height = State->meshHeight;

    if (x < r) x = r;
    if (y < r) y = r;
    if (x >= width - r) x = width - r - 1;
    if (y >= height - r) y = height - r - 1;

    x = width - x;

    int rippleMapSize = State->rippleMapSize;
    int index = State->rippleIndex;
    int origin = offset(0, 0, width);

    int* current = loadArrayI32(RSID_RIPPLE_MAP, index * rippleMapSize + origin);
    int sqr = r * r;
    float invs = 1.0f / s;

    int h = 0;
    for ( ; h < r; h += 1) {
        int sqv = h * h;
        int yn = origin + (y - h) * (width + 2);
        int yp = origin + (y + h) * (width + 2);
        int w = 0;
        for ( ; w < r; w += 1) {
            int squ = w * w;
            if (squ + sqv < sqr) {
                int v = -sqrtf((sqr - (squ + sqv)) << 16) * invs;
                current[yn + x + w] = v;
                current[yp + x + w] = v;
                current[yn + x - w] = v;
                current[yp + x - w] = v;
            }
        }
    }
}

void drop(int x, int y, int r) {
    dropWithStrength(x, y, r, 1);
}

void updateRipples() {
    int rippleMapSize = State->rippleMapSize;
    int width = State->meshWidth;
    int height = State->meshHeight;
    int index = State->rippleIndex;
    int origin = offset(0, 0, width);

    int* current = loadArrayI32(RSID_RIPPLE_MAP, index * rippleMapSize + origin);
    int* next = loadArrayI32(RSID_RIPPLE_MAP, (1 - index) * rippleMapSize + origin);

    State->rippleIndex = 1 - index;

    int a = 1;
    int b = width + 2;
    int h = height;
    while (h) {
        int w = width;
        while (w) {
            int droplet = ((current[-b] + current[b] + current[-a] + current[a]) >> 1) - *next;
            *next = droplet - (droplet >> DAMP);
            current += 1;
            next += 1;
            w -= 1;
        }
        current += 2;
        next += 2;
        h -= 1;
    }
}

int refraction(int d, int wave, int *map) {
    int i = d;
    if (i < 0) i = -i;
    if (i > 512) i = 512;
    int w = (wave + 0x10000) >> 8;
    w &= ~(w >> 31);
    int r = (map[i] * w) >> 3;
    if (d < 0) {
        return -r;
    }
    return r;
}

void generateRipples() {
    int rippleMapSize = State->rippleMapSize;
    int width = State->meshWidth;
    int height = State->meshHeight;
    int index = State->rippleIndex;
    int origin = offset(0, 0, width);

    int b = width + 2;

    int* current = loadArrayI32(RSID_RIPPLE_MAP, index * rippleMapSize + origin);
    int *map = loadArrayI32(RSID_REFRACTION_MAP, 0);
    float *vertices = loadSimpleMeshVerticesF(NAMED_WaterMesh, 0);
    struct vert_s *vert = (struct vert_s *)vertices;

    float fw = 1.0f / width;
    float fh = 1.0f / height;
    float fy = (1.0f / 512.0f) * (1.0f / RIPPLE_HEIGHT);
/*
    int h = height - 1;
    while (h >= 0) {
        int w = width - 1;
        int wave = *current;
        int offset = h * width;
        struct vert_s *vtx = vert + offset + w;

        while (w >= 0) {
            int nextWave = current[1];
            int dx = nextWave - wave;
            int dy = current[b] - wave;

            int offsetx = refraction(dx, wave, map) >> 16;
            int u = (width - w) + offsetx;
            u &= ~(u >> 31);
            if (u >= width) u = width - 1;

            int offsety = refraction(dy, wave, map) >> 16;
            int v = (height - h) + offsety;
            v &= ~(v >> 31);
            if (v >= height) v = height - 1;

            vtx->s = u * fw;
            vtx->t = v * fh;
            vtx->z = dy * fy;
            debugF("es", vtx->s);
            vtx --;

            w -= 1;
            current += 1;
            wave = nextWave;
        }
        h -= 1;
        current += 2;
    }
*/
    {
        gDrops[0].x = width / 2;
        gDrops[0].y = height / 2;

        int x, y;
        struct vert_s *vtx = vert;
        for (y=0; y < height; y++) {
            for (x=0; x < width; x++) {
                struct drop_s * d = &gDrops[0];
                float z = 0;

                {
                    float dx = d->x - x;
                    float dy = d->y - y;
                    float dist = sqrtf(dx*dx + dy*dy);
                    z = sinf(dist + d->phase) * d->amp;

                    vtx->s = (float)x / width;
                    vtx->t = (float)y / height;
                    vtx->z = z;
                    vtx ++;
                }
            }
        }
        gDrops[0].phase += 0.02;
    }

    // Compute the normals for lighting
    int y = 0;
    for ( ; y < (height-1); y += 1) {
        int x = 0;
        int yOffset = y * width;
        struct vert_s *v = vert;
        v += y * width;

        for ( ; x < (width-1); x += 1) {
            struct vec3_s n1, n2, n3;
            vec3Sub(&n1, (struct vec3_s *)&(v+1)->x, (struct vec3_s *)&v->x);
            vec3Sub(&n2, (struct vec3_s *)&(v+width)->x, (struct vec3_s *)&v->x);
            vec3Cross(&n3, &n1, &n2);
            vec3Norm(&n3);

            // Average of previous normal and N1 x N2
            vec3Sub(&n1, (struct vec3_s *)&(v+width+1)->x, (struct vec3_s *)&v->x);
            vec3Cross(&n2, &n1, &n2);
            vec3Add(&n3, &n3, &n2);
            vec3Norm(&n3);

            v->nx = n3.x;
            v->ny = n3.y;
            v->nz = -n3.z;
            v->s += v->nx * 0.01;
            v->t += v->ny * 0.01;
            v += 1;

            // reset Z
            //vertices[(yOffset + x) << 3 + 7] = 0.0f;
        }
    }
}

void drawLeaf(struct Leaves_s *leaf, int meshWidth, int meshHeight, float glWidth, float glHeight,
        int rotate) {

    float x = leaf->x;
    float x1 = x - LEAF_SIZE;
    float x2 = x + LEAF_SIZE;

    float y = leaf->y;
    float y1 = y - LEAF_SIZE;
    float y2 = y + LEAF_SIZE;

    float u1 = leaf->u1;
    float u2 = leaf->u2;

    float z1 = 0.0f;
    float z2 = 0.0f;
    float z3 = 0.0f;
    float z4 = 0.0f;

    float a = leaf->altitude;
    float s = leaf->scale;
    float r = leaf->angle;

    float tz = 0.0f;
    if (a > 0.0f) {
        tz = -a;
    }

    x1 -= x;
    x2 -= x;
    y1 -= y;
    y2 -= y;

    float matrix[16];

    if (a > 0.0f) {
        color(0.0f, 0.0f, 0.0f, 0.15f);

        if (rotate) {
            matrixLoadRotate(matrix, 90.0f, 0.0f, 0.0f, 1.0f);
        } else {
            matrixLoadIdentity(matrix);
        }
        matrixTranslate(matrix, x, y, 0.0f);
        matrixScale(matrix, s, s, 1.0f);
        matrixRotate(matrix, r, 0.0f, 0.0f, 1.0f);
        vpLoadModelMatrix(matrix);

        drawQuadTexCoords(x1, y1, z1, u1, 1.0f,
                          x2, y1, z2, u2, 1.0f,
                          x2, y2, z3, u2, 0.0f,
                          x1, y2, z4, u1, 0.0f);

        float alpha = 1.0f;
        if (a >= 0.4f) alpha = 1.0f - (a - 0.5f) / 0.1f;
        color(1.0f, 1.0f, 1.0f, alpha);
    } else {
        color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    if (rotate) {
        matrixLoadRotate(matrix, 90.0f, 0.0f, 0.0f, 1.0f);
    } else {
        matrixLoadIdentity(matrix);
    }
    matrixTranslate(matrix, x, y, tz);
    matrixScale(matrix, s, s, 1.0f);
    matrixRotate(matrix, r, 0.0f, 0.0f, 1.0f);
    vpLoadModelMatrix(matrix);

    drawQuadTexCoords(x1, y1, z1, u1, 1.0f,
                      x2, y1, z2, u2, 1.0f,
                      x2, y2, z3, u2, 0.0f,
                      x1, y2, z4, u1, 0.0f);

    float spin = leaf->spin;
    if (a <= 0.0f) {
        float rippled = leaf->rippled;
        if (rippled < 0.0f) {
            drop(((x + glWidth * 0.5f) / glWidth) * meshWidth,
                 meshHeight - ((y + glHeight * 0.5f) / glHeight) * meshHeight, 1);
            spin /= 4.0f;
            leaf->spin = spin;
            leaf->rippled = 1.0f;
        }
        leaf->x = x + leaf->deltaX;
        leaf->y = y + leaf->deltaY;
        r += spin;
        leaf->angle = r;
    } else {
        a -= 0.005f;
        leaf->altitude = a;
        r += spin * 2.0f;
        leaf->angle = r;
    }

    if (-LEAF_SIZE * s + x > glWidth / 2.0f || LEAF_SIZE * s + x < -glWidth / 2.0f ||
            LEAF_SIZE * s + y < -glHeight / 2.0f) {

        int sprite = randf(LEAVES_TEXTURES_COUNT);
        leaf->x = randf2(-glWidth * 0.5f, glWidth * 0.5f);
        leaf->y = randf2(-glHeight * 0.5f, glHeight * 0.5f);
        leaf->scale = randf2(0.4f, 0.5f);
        leaf->spin = degf(randf2(-0.02f, 0.02f)) * 0.25f;
        leaf->u1 = sprite / (float) LEAVES_TEXTURES_COUNT;
        leaf->u2 = (sprite + 1) / (float) LEAVES_TEXTURES_COUNT;
        leaf->altitude = 0.6f;
        leaf->rippled = -1.0f;
        leaf->deltaX = randf2(-0.02f, 0.02f) / 60.0f;
        leaf->deltaY = -0.08f * randf2(0.9f, 1.1f) / 60.0f;
    }
}

void drawLeaves() {
    bindProgramFragment(NAMED_PFSky);
    bindProgramFragmentStore(NAMED_PFSLeaf);
    bindProgramVertex(NAMED_PVSky);
    bindTexture(NAMED_PFSky, 0, NAMED_TLeaves);

    color(1.0f, 1.0f, 1.0f, 1.0f);

    int leavesCount = State->leavesCount;
    int width = State->meshWidth;
    int height = State->meshHeight;
    float glWidth = State->glWidth;
    float glHeight = State->glHeight;
    int rotate = State->rotate;

    struct Leaves_s *leaf = Leaves;

    int i = 0;
    for ( ; i < leavesCount; i += 1) {
        drawLeaf(leaf, width, height, glWidth, glHeight, rotate);
        leaf += 1;
    }

    float matrix[16];
    matrixLoadIdentity(matrix);
    vpLoadModelMatrix(matrix);
}

void drawRiverbed() {
    bindTexture(NAMED_PFBackground, 0, NAMED_TRiverbed);

    drawSimpleMesh(NAMED_WaterMesh);
}

void drawSky() {
    color(1.0f, 1.0f, 1.0f, 0.4f);

    bindProgramFragment(NAMED_PFSky);
    bindProgramFragmentStore(NAMED_PFSLeaf);
    bindTexture(NAMED_PFSky, 0, NAMED_TSky);

    float x = skyOffsetX + State->skySpeedX;
    float y = skyOffsetY + State->skySpeedY;

    if (x > 1.0f) x = 0.0f;
    if (x < -1.0f) x = 0.0f;
    if (y > 1.0f) y = 0.0f;

    skyOffsetX = x;
    skyOffsetY = y;

    float matrix[16];
    matrixLoadTranslate(matrix, x, y, 0.0f);
    vpLoadTextureMatrix(matrix);

    drawSimpleMesh(NAMED_WaterMesh);

    matrixLoadIdentity(matrix);
    vpLoadTextureMatrix(matrix);
}

void drawLighting() {
    ambient(0.0f, 0.0f, 0.0f, 1.0f);
    diffuse(0.0f, 0.0f, 0.0f, 1.0f);
    specular(0.44f, 0.44f, 0.44f, 1.0f);
    shininess(40.0f);

    bindProgramFragmentStore(NAMED_PFSBackground);
    bindProgramFragment(NAMED_PFLighting);
    bindProgramVertex(NAMED_PVLight);

    drawSimpleMesh(NAMED_WaterMesh);
}

void drawNormals() {
    int width = State->meshWidth;
    int height = State->meshHeight;

    float *vertices = loadSimpleMeshVerticesF(NAMED_WaterMesh, 0);

    bindProgramVertex(NAMED_PVSky);
    bindProgramFragment(NAMED_PFLighting);

    color(1.0f, 0.0f, 0.0f, 1.0f);

    float scale = 1.0f / 10.0f;
    int y = 0;
    for ( ; y < height; y += 1) {
        int yOffset = y * width;
        int x = 0;
        for ( ; x < width; x += 1) {
            int offset = (yOffset + x) << 3;
            float vx = vertices[offset + 5];
            float vy = vertices[offset + 6];
            float vz = vertices[offset + 7];
            float nx = vertices[offset + 0];
            float ny = vertices[offset + 1];
            float nz = vertices[offset + 2];
            drawLine(vx, vy, vz, vx + nx * scale, vy + ny * scale, vz + nz * scale);
        }
    }
}

int main(int index) {
    if (Drop->dropX != -1) {
        drop(Drop->dropX, Drop->dropY, DROP_RADIUS);
        Drop->dropX = -1;
        Drop->dropY = -1;
    }

    if (State->isPreview) {
        int now = uptimeMillis();
        if (now - lastDrop > 2000) {
            float x = randf(State->meshWidth);
            float y = randf(State->meshHeight);

            drop(x, y, DROP_RADIUS);

            lastDrop = now;
        }
    }

    updateRipples();
    generateRipples();
    updateSimpleMesh(NAMED_WaterMesh);

    if (State->rotate) {
        float matrix[16];
        matrixLoadRotate(matrix, 90.0f, 0.0f, 0.0f, 1.0f);
        vpLoadModelMatrix(matrix);
    }

    drawRiverbed();
    //drawSky();
    drawLighting();
    //drawLeaves();
    //drawNormals();

    return 1;
}
