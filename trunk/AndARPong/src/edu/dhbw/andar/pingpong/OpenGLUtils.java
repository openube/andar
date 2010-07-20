package edu.dhbw.andar.pingpong;

/*
 * Copyright (C) 2008 aki@akjava.com
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class OpenGLUtils {

	public static FloatBuffer allocateFloatBuffer(int capacity){
		ByteBuffer vbb = ByteBuffer.allocateDirect(capacity);
        vbb.order(ByteOrder.nativeOrder());
        return vbb.asFloatBuffer();
	}
	
	public static IntBuffer allocateInttBuffer(int capacity){
		ByteBuffer vbb = ByteBuffer.allocateDirect(capacity);
        vbb.order(ByteOrder.nativeOrder());
        return vbb.asIntBuffer();
	}
	
	public static ShortBuffer allocateShortBuffer(int capacity){
		ByteBuffer vbb = ByteBuffer.allocateDirect(capacity);
        vbb.order(ByteOrder.nativeOrder());
        return vbb.asShortBuffer();
	}
	
	public static void addVertex3f(FloatBuffer buffer,float x,float y,float z){
		buffer.put(x);
		buffer.put(y);
		buffer.put(z);
	}
	
	public static void addIndex(ShortBuffer buffer,int index1,int index2,int index3){
		buffer.put((short) index1);
		buffer.put((short) index2);
		buffer.put((short) index3);
	}
	
	public static void addCoord2f(FloatBuffer buffer,float x,float y){
		buffer.put(x);
		buffer.put(y);
	}
	
	public static void addColorf(FloatBuffer buffer,float r,float g,float b,float a){
		buffer.put(r);
		buffer.put(g);
		buffer.put(b);
		buffer.put(a);
	}

	public static FloatBuffer toFloatBufferPositionZero(float[] values) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(values.length*4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer=vbb.asFloatBuffer();
        buffer.put(values);
        buffer.position(0);
		return buffer;
	}
	
	public static ShortBuffer toShortBuffer(short[] values) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(values.length*2);
        vbb.order(ByteOrder.nativeOrder());
        ShortBuffer buffer=vbb.asShortBuffer();
        buffer.put(values);
        buffer.position(0);
		return buffer;
	}
	
	
	public static Bitmap loadBitmap(Context mContext,int id){
		 InputStream is = mContext.getResources()
      .openRawResource(id);
Bitmap bitmap=null;
try {
  bitmap = BitmapFactory.decodeStream(is);
} finally {
  try {
      is.close();
  } catch(IOException e) {
      // Ignore.
  }
}
return bitmap;
	}
	
}