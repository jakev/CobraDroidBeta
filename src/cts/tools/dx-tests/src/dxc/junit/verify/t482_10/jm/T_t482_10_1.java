/*
 * Copyright (C) 2008 The Android Open Source Project
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

package dxc.junit.verify.t482_10.jm;

public class T_t482_10_1 {
    
    public void run(){
        
        int arr[];
        arr = new int[2];
        int f = 1;
        
        try{
            arr = new int[3];
            if(f == 1)
                throw new Exception();
            
        }catch(Exception e){
            f = arr[0];
        }
        finally {
            f = 1;
        }
    }
}
