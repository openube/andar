package edu.dhbw.andar.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;

import android.hardware.Camera.Size;

public class GraphicsUtil {
	//private final static double epsilon = 0.001;
	//this epsilon being so large is intended, as often there will not be an adequate resolution with
	//the correct aspect ratio available
	//so we trade the correct aspect ratio for faster rendering
	private final static double epsilon = 0.17;
	
	/**
	 * Make a direct NIO FloatBuffer from an array of floats
	 * @param arr The array
	 * @return The newly created FloatBuffer
	 */
	public static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}
	/**
	 * Make a direct NIO ByteBuffer from an array of floats
	 * @param arr The array
	 * @return The newly created FloatBuffer
	 */
	public static ByteBuffer makeByteBuffer(byte[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length);
		bb.order(ByteOrder.nativeOrder());
		bb.put(arr);
		bb.position(0);
		return bb;
	}
	public static ByteBuffer makeByteBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(size);
		bb.position(0);
		return bb;
	}
	
	/**
	 * Get the optimal preview size for the given screen size.
	 * @param sizes
	 * @param screenWidth
	 * @param screenHeight
	 * @return
	 */
	public static Size getOptimalPreviewSize(List<Size> sizes, int screenWidth, int screenHeight) {
		double aspectRatio = ((double)screenWidth)/screenHeight;
		Size optimalSize = null;
		for (Iterator<Size> iterator = sizes.iterator(); iterator.hasNext();) {
			Size currSize =  iterator.next();
			double curAspectRatio = ((double)currSize.width)/currSize.height;
			//do the aspect ratios equal?
			if ( Math.abs( aspectRatio - curAspectRatio ) < epsilon ) {
				//they do
				if(optimalSize!=null) {
					//is the current size smaller than the one before
					if(optimalSize.height>currSize.height && optimalSize.width>currSize.width) {
						optimalSize = currSize;
					}
				} else {
					optimalSize = currSize;
				}
			}
		}
		if(optimalSize == null) {
			//did not find a size with the correct aspect ratio.. let's choose the smallest instead
			for (Iterator<Size> iterator = sizes.iterator(); iterator.hasNext();) {
				Size currSize =  iterator.next();
				if(optimalSize!=null) {
					//is the current size smaller than the one before
					if(optimalSize.height>currSize.height && optimalSize.width>currSize.width) {
						optimalSize = currSize;
					} else {
						optimalSize = currSize;
					}
				}else {
					optimalSize = currSize;
				}
				
			}
		}
		return optimalSize;
	}
	
	public static boolean containsSize(List<Size> sizes, Size size) {
		for (Iterator<Size> iterator = sizes.iterator(); iterator.hasNext();) {
			Size currSize =  iterator.next();
			if(currSize.width == size.width && currSize.height == size.height) {
				return true;
			}			
		}
		return false;
	}
	
	public static Size getSmallestSize(List<Size> sizes) {
		Size optimalSize = null;
		for (Iterator<Size> iterator = sizes.iterator(); iterator.hasNext();) {
			Size currSize =  iterator.next();		
			if(optimalSize == null) {
				optimalSize = currSize;
			} else if(optimalSize.height>currSize.height && optimalSize.width>currSize.width) {
				optimalSize = currSize;
			}
		}
		return optimalSize;
	}
	
}
