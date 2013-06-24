#!/bin/bash
#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# sed -e '/.svn/d' |
# ---------------------------------------------------------

echo ">>> statistics about kind of tests (normal, border edge, exception, verify)"
cd ../../src
for mypack in `find dxc -maxdepth 4 -mindepth 4 -name "Test_*.java" | sort`
do
    resN=`egrep -c 'public void testN[0-9][0-9]?' $mypack`
    resB=`egrep -c 'public void testB[0-9][0-9]?' $mypack`
    resE=`egrep -c 'public void testE[0-9][0-9]?' $mypack`
    resVFE=`egrep -c 'public void testVFE[0-9][0-9]?' $mypack`
    echo "file:$mypack, N:$resN, B:$resB, E:$resE, VFE:$resVFE"
done
