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

cd ../../src

echo "normal tests (testN...): "
egrep -R 'public void testN[0-9][0-9]?' * | grep ".java" | sed -e '/.svn/d' | sort | wc -l

echo "border egde tests (testB...): "
egrep -R 'public void testB[0-9][0-9]?' * | grep ".java" | sed -e '/.svn/d' | sort | wc -l

echo "exception tests (testE...): "
egrep -R 'public void testE[0-9][0-9]?' * | grep ".java" | sed -e '/.svn/d' | sort | wc -l

echo "verify error tests (testVFE...): "
egrep -R 'public void testVFE[0-9][0-9]?' * | grep ".java" | sed -e '/.svn/d' | sort | wc -l
