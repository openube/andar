package edu.dhbw.andobjviewer.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.res.Resources;

public class MemUtil {
	public static FloatBuffer makeFloatBufferFromArray(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}
	
	/**
	 * creates a floatbuffer of the given size.
	 * @param size
	 * @return
	 */
	public static FloatBuffer makeFloatBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(size*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.position(0);
		return fb;
	}

	public static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}
	
}
