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
#pragma stateVertex(PVBackground)
#pragma stateRaster(parent)
#pragma stateFragment(PFBackground)
#pragma stateStore(PFSBackground)

#define RSID_POINTS 1

void dumpState() {

//    debugF("@@@@@ yrot: ", State->yRotation);

}


int main(int launchID) {

    int i;

    float mat1[16];
    float scale = 0.0041;
    matrixLoadRotate(mat1, 0.f, 0.f, 0.f, 1.f);
    matrixScale(mat1, scale, scale, scale);
    vpLoadModelMatrix(mat1);

    // draw the background image (416x233)
    bindTexture(NAMED_PFBackground, 0, NAMED_Tvumeter_background);
    drawQuadTexCoords(
            -208.0f, -33.0f, 0.0f,        // space
                0.09375f, 0.9551f,        // texture
            208, -33.0f, 0.0f,            // space
                0.90625, 0.9551f,         // texture
            208, 200.0f, 0.0f,            // space
                0.90625, 0.0449f,         // texture
            -208.0f, 200.0f, 0.0f,        // space
                0.09375f, 0.0449f);       // texture

    // draw the peak indicator light (56x58)
    if (State->mPeak > 0) {
        bindTexture(NAMED_PFBackground, 0, NAMED_Tvumeter_peak_on);
    } else {
        bindTexture(NAMED_PFBackground, 0, NAMED_Tvumeter_peak_off);
    }
    drawQuadTexCoords(
            140.0f, 70.0f, -1.0f,         // space
                0.0625f, 0.953125,        // texture
            196, 70.0f, -1.0f,            // space
                0.9375f, 0.953125,        // texture
            196, 128.0f, -1.0f,           // space
                0.9375f, 0.046875,        // texture
            140.0f, 128.0f, -1.0f,        // space
                0.0625f, 0.046875);       // texture



    // Draw the needle (88x262, center of rotation at 44,217 from top left)

    // set matrix so point of rotation becomes origin
    matrixLoadTranslate(mat1, 0.f, -57.0f * scale, 0.f);
    matrixRotate(mat1, State->mAngle - 90.f, 0.f, 0.f, 1.f);
    matrixScale(mat1, scale, scale, scale);
    vpLoadModelMatrix(mat1);
    bindTexture(NAMED_PFBackground, 0, NAMED_Tvumeter_needle);
    drawQuadTexCoords(
            -44.0f, -102.0f+57.f, 0.0f,         // space
                .15625f, 0.755859375f,  // texture
            44.0f, -102.0f+57.f, 0.0f,             // space
                0.84375f, 0.755859375f,  // texture
            44.0f, 160.0f+57.f, 0.0f,             // space
                0.84375f, 0.244140625f,  // texture
            -44.0f, 160.0f+57.f, 0.0f,         // space
                0.15625f, 0.244140625f); // texture


    // restore matrix
    matrixLoadRotate(mat1, 0.f, 0.f, 0.f, 1.f);
    matrixScale(mat1, scale, scale, scale);
    vpLoadModelMatrix(mat1);

    // erase the part of the needle we don't want to show
    bindTexture(NAMED_PFBackground, 0, NAMED_Tvumeter_black);
    drawQuad(-100.f, -55.f, 0.f,
             -100.f, -105.f, 0.f,
              100.f, -105.f, 0.f,
              100.f, -55.f, 0.f);


    // draw the frame (472x290)
    bindTexture(NAMED_PFBackground, 0, NAMED_Tvumeter_frame);
    drawQuadTexCoords(
            -236.0f, -60.0f, 0.0f,           // space
                0.0390625f, 0.783203125f,    // texture
            236, -60.0f, 0.0f,               // space
                0.9609375f, 0.783203125f,    // texture
            236, 230.0f, 0.0f,               // space
                0.9609375f, 0.216796875f,    // texture
            -236.0f, 230.0f, 0.0f,           // space
                0.0390625f, 0.216796875f);   // texture



    return 1;
}
