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

BASEDIR=`pwd`
CLASSFOLDERS='out/classes_cfh out/classes_jasmin out/classes_javac'

function findClassFile()
{
    i=0;
    for path in $1; 
    do
        if [ -f ${path}/$2 ]; then
            CLASSFOLDER=$path
            return
        fi
    done
}

function mapOpcode()
{
    MAPPED_OPCODE=""
    if [ -z $1 ]; then
        MAPPED_OPCODE=""
        return;
    fi
    
    line=`grep -e "^$1    " ${BASEDIR}/data/opcode_mapping`
    if [ $? -ne 0 ]; then
        MAPPED_OPCODE=""
    else
        echo $line
        MAPPED_OPCODE=`echo -n $line | cut -d" " -f2`
    fi
}

while read -u 3 myline;
do
    jpackageclass=`echo $myline | cut -d";" -f1` # e.g dxc.junit.verify.t482_9.Main_testVFE2
    jpackageclassfile=`echo $jpackageclass | sed -e 's/\./\//g;s/$/.java/'`
    echo $jpackageclassfile
    jtestclass=`echo $myline | cut -d";" -f2` # e.g dxc.junit.verity.t482_9.jm.T_t482_9_1
    jtestclassfile=`echo $jtestclass | sed -e 's/\./\//g;s/$/.class/'`
    jtestsourcefile=`echo $jtestclass | sed -e 's/\./\//g;s/$/.java/'`
#    echo $jtestclassfile
    
rm -rf out/tmp
mkdir -p out/tmp

findClassFile "${CLASSFOLDERS}" $jtestclassfile
cd ${CLASSFOLDER}
cp -P --parent $jtestclassfile ${BASEDIR}/out/tmp/
cd ${BASEDIR}

#findClassFile "${CLASSFOLDERS}" $jpackageclassfile
#cd ${CLASSFOLDER}
#cp -P --parents $jpackageclassfile ${BASEDIR}/out/tmp/
#cd ${BASEDIR}

# echo ${CLASSFOLDER}/${jtestclassfile}

OPCODE=`echo $jtestclass | sed -e 's/.*T_\(.*\)_[0-9]\+[_]*[a-z]*/\1/'`
mapOpcode ${OPCODE}
# echo ${OPCODE} " - " ${MAPPED_OPCODE}
if [ -z ${MAPPED_OPCODE} ]; then
    continue
fi

for subcode in `echo $MAPPED_OPCODE | cut -d";" -f1,2 --output-delimiter=" "`; do

    SCRIPT="s#out/classes_[^/]*/dxc/\(.*\)/${OPCODE}/jm/T_${OPCODE}\(.*\)\.class#src/dot/\1/${subcode}/d/T_${subcode}\2.d#"
    FILEDEST=`echo ${CLASSFOLDER}/${jtestclassfile} | sed -e $SCRIPT`
    echo ${FILEDEST}
#    SCRIPT="s#out/classes_[^/]*/dxc/\(.*\)${OPCODE}/Main_\(.*\)\.class#src/dot/\1/${subcode}/Main_\2.d#"
#    FILEDEST=`echo ${CLASSFOLDER}/${jpackageclassfile} | sed -e $SCRIPT`
#    echo ${FILEDEST}
    
    cd out/tmp
    OUT=`dx --dex --no-optimize --positions=lines --output="/tmp/dxclasses.jar" ${jtestclassfile} 2>&1`
    
    if [ $? -eq 0 ]; then
        cd ${BASEDIR}
        mkdir -p `dirname ${FILEDEST}`
        dexdump -g /tmp/dxclasses.jar > ${FILEDEST}
        sed -i -e 's/dxc\([\.|\/]\)junit/dot\1junit/' ${FILEDEST}
        SCRIPT="s/${OPCODE}\([\.|\/]\)jm\([\.|\/]\)/${subcode}\1d\2/g"
        sed -i -e ${SCRIPT} ${FILEDEST}
        SCRIPT="s/T_${OPCODE}/T_${subcode}/g"
        sed -i -e ${SCRIPT} ${FILEDEST}
        
        SCRIPT="s#dxc/\(.*\)/${OPCODE}/\(.*\)#src/dot/\1/${subcode}/\2#"
        jpackagedestfile=`echo ${jpackageclassfile} | sed -e $SCRIPT`
        cp src/${jpackageclassfile} ${jpackagedestfile}
        sed -i -e 's/dxc\([\.|\/]\)junit/dot\1junit/' ${jpackagedestfile}
        SCRIPT="s/${OPCODE}\([\.|\/]\)jm\([\.|\/]\)/${subcode}\1d\2/g"
        sed -i -e ${SCRIPT} ${jpackagedestfile}
        SCRIPT="s/T_${OPCODE}/T_${subcode}/g"
        sed -i -e ${SCRIPT} ${jpackagedestfile}
        sed -i -e "s/\(package .*\)${OPCODE}/\1${subcode}/" ${jpackagedestfile}


        SCRIPT="s#dxc/\(.*\)/${OPCODE}/jm/\(.*\)${OPCODE}\(.*\).class#src/dot/\1/${subcode}/d/\2${subcode}\3.java#"
        jpackagedestfile=`echo ${jtestclassfile} | sed -e $SCRIPT`
        cp src/${jtestsourcefile} ${jpackagedestfile}
        sed -i -e 's/dxc\([\.|\/]\)junit/dot\1junit/' ${jpackagedestfile}
        SCRIPT="s/${OPCODE}\([\.|\/]\)jm\([\.|\/|;]\)/${subcode}\1d\2/g"
        sed -i -e ${SCRIPT} ${jpackagedestfile}
        SCRIPT="s/T_${OPCODE}/T_${subcode}/g"
        sed -i -e ${SCRIPT} ${jpackagedestfile}
        sed -i -e "s/\(package .*\)${OPCODE}/\1${subcode}/" ${jpackagedestfile}
        
        srcdir=`dirname ${jtestsourcefile}`
        for srcfile in `find src/${srcdir} -maxdepth 1 -type f ! -name "T_*.java" -a -name "*.java"`; do
            echo $srcfile
            SCRIPT="s#dxc/\(.*\)/${OPCODE}/jm/\(.*\).java#dot/\1/${subcode}/d/\2.java#"
            jpackagedestfile=`echo ${srcfile} | sed -e $SCRIPT`
            cp ${srcfile} ${jpackagedestfile}
            sed -i -e 's/dxc\([\.|\/]\)junit/dot\1junit/' ${jpackagedestfile}
            SCRIPT="s/${OPCODE}\([\.|\/]\)jm\([\.|\/|;]\)/${subcode}\1d\2/g"
            sed -i -e ${SCRIPT} ${jpackagedestfile}
            SCRIPT="s/T_${OPCODE}/T_${subcode}/g"
            sed -i -e ${SCRIPT} ${jpackagedestfile}
            sed -i -e "s/\(package .*\)${OPCODE}/\1${subcode}/" ${jpackagedestfile}
        done
        
        srcdir=`dirname ${jpackageclassfile}`
        for srcfile in `find src/${srcdir} -maxdepth 1 -type f ! -name "Main_*.java" -a ! -name "Test_*.java" -a -name "*.java"`; do
            echo $srcfile
            SCRIPT="s#dxc/\(.*\)/${OPCODE}/\(.*\)#dot/\1/${subcode}/\2#"
            jpackagedestfile=`echo ${srcfile} | sed -e $SCRIPT`
            cp -v ${srcfile} ${jpackagedestfile}
        done
        
    else
        echo "--- not dexable"
    fi
    cd ${BASEDIR}
done


done 3<$BASEDIR/data/scriptdata

