/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.apps.tag.provider;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * Interface to write a stream of data to a pipe.
 */
interface TagProviderPipeDataWriter {
    /**
     * Called from a background thread to stream data out to a pipe.
     * Note that the pipe is blocking, so this thread can block on
     * writes for an arbitrary amount of time if the client is slow
     * at reading.
     *
     * @param output The pipe where data should be written.  This will be
     * closed for you upon returning from this function.
     * @param uri The URI whose data is to be written.
     */
    public void writeMimeDataToPipe(ParcelFileDescriptor output, Uri uri);
}