/**
	Copyright (C) 2009  Tobias Domhan

    This file is part of AndOpenGLCam.

    AndObjViewer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AndObjViewer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AndObjViewer.  If not, see <http://www.gnu.org/licenses/>.
 
 */
package edu.dhbw.andopenglcam;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.dhbw.andopenglcam.interfaces.PreviewFrameSink;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;

/**
 * Handles callbacks of the camera preview
 * camera preview demo:
 * http://developer.android.com/guide/samples/ApiDemos/src/com/example/android/apis/graphics/CameraPreview.html
 * YCbCr 420 colorspace infos:
	 * http://wiki.multimedia.cx/index.php?title=YCbCr_4:2:0
	 * http://de.wikipedia.org/wiki/YCbCr-Farbmodell
	 * http://www.elektroniknet.de/home/bauelemente/embedded-video/grundlagen-der-videotechnik-ii-farbraum-gammakorrektur-digitale-video-signale/4/
 * @see android.hardware.Camera.PreviewCallback
 * @author Tobias Domhan
 *
 */
public class CameraPreviewHandler implements PreviewCallback {
	private GLSurfaceView glSurfaceView;
	private PreviewFrameSink frameSink;
	private Resources res;
	private int textureSize=256;
	private int previewFrameWidth=240;
	private int previewFrameHeight=160;
	
	/**
	 * 
	 */
	public CameraPreviewHandler(GLSurfaceView glSurfaceView, PreviewFrameSink sink, Resources res) {
		this.glSurfaceView = glSurfaceView;
		this.frameSink = sink;
		this.res = res;
	}
	
	/**
	 * the size of the camera preview frame is dynamic
	 * we will calculate the next power of two texture size
	 * in which the preview frame will fit
	 * and set the corresponding size in the renderer
	 * how to decode camera YUV to RGB for opengl:
	 * http://groups.google.de/group/android-developers/browse_thread/thread/c85e829ab209ceea/d3b29d3ddc8abf9b?lnk=gst&q=YUV+420#d3b29d3ddc8abf9b
	 * @param camera
	 */
	public void init(Camera camera) throws Exception {
		Parameters camParams = camera.getParameters();
		//check if the pixel format is supported
		if (camParams.getPreviewFormat() != PixelFormat.YCbCr_420_SP) {
			//Das Format ist semi planar, Erklärung:
			//semi-planar YCbCr 4:2:2 : two arrays, one with all Ys, one with Cb and Cr. 
			//Quelle: http://www.celinuxforum.org/CelfPubWiki/AudioVideoGraphicsSpec_R2
			throw new Exception(res.getString(R.string.error_unkown_pixel_format));
		}			
		//get width/height of the camera
		Size previewSize = camParams.getPreviewSize();
		previewFrameWidth = previewSize.width;
		previewFrameHeight = previewSize.height;
		textureSize = GenericFunctions.nextPowerOfTwo(Math.max(previewFrameWidth, previewFrameHeight));
		frame = new byte[textureSize*textureSize*3];
		for (int i = 0; i < frame.length; i++) {
			frame[i]=(byte) 128;
		}
		frameSink.setPreviewFrameSize(textureSize, previewFrameWidth, previewFrameHeight);
	}

	//size of a texture must be a power of 2
	private byte[] frame;//=new byte[256*256];
	
	/* 
	 * new frame from the camera arrived. convert and hand over
	 * to the renderer
	 * how to convert between YUV and RGB:http://en.wikipedia.org/wiki/YUV#Y.27UV444
	 * Conversion in C-Code(Android Project):
	 * http://www.netmite.com/android/mydroid/donut/development/tools/yuv420sp2rgb/yuv420sp2rgb.c
	 * http://code.google.com/p/android/issues/detail?id=823
	 * @see android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[], android.hardware.Camera)
	 */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		//prevent null pointer exceptions
		if (data == null) return;
		
		frameSink.getFrameLock().lock();
   		/*int bwCounter=0;
   		int yuvsCounter=0;
   		for (int y=0;y<previewFrameHeight;y++) {
   			System.arraycopy(data, yuvsCounter, frame, bwCounter, previewFrameWidth);
   			yuvsCounter=yuvsCounter+previewFrameWidth;
   			bwCounter=bwCounter+textureSize;
   		}*/
		
		convertYUV2RGB(data);
		//convertYUV2BWRGB(data);
		//convertAndroidProject(data);
		//toRGB565(data, previewFrameWidth, previewFrameHeight, frame);
		//decodeYUV(frame, data, previewFrameWidth, previewFrameHeight);
   		
		frameSink.setNextFrame(ByteBuffer.wrap(frame));		
		frameSink.getFrameLock().unlock();
		this.glSurfaceView.requestRender();
		//camera.setOneShotPreviewCallback(this);
	}
	
	private void convertYUV2BWRGB(byte[] data) {
		int pixelPtr=0;
		//iterate through all pixels
		for (int i = 0; i < previewFrameHeight; i++) {
            for (int j = 0; j < previewFrameWidth; j++) {            	
            	frame[pixelPtr++]=data[i * previewFrameWidth + j];//R
            	frame[pixelPtr++]=data[i * previewFrameWidth + j];//G
            	frame[pixelPtr++]=data[i * previewFrameWidth + j];//B
            }
            pixelPtr = (i+1) * textureSize * 3;
		}
	}
	
	private void convertYUV2RGB(byte[] data) {
		int pixelPtr=0;
		int pUV = previewFrameWidth * previewFrameHeight;
		//iterate through all pixels
		for (int i = 0; i < previewFrameHeight; i++) {
            for (int j = 0; j < previewFrameWidth; j++) {
            	int nY = data[i * previewFrameWidth + j];
            	int nV = data[pUV + (i/2) * previewFrameWidth + 2 * (j/2)];
                int nU = data[pUV + (i/2) * previewFrameWidth + 2 * (j/2) + 1];
                // Yuv Convert
                //nY -= 16;
                //nU -= 128;
                //nV -= 128;
                int nR = (int)(1.164 * nY + 2.018 * nU);
                int nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
                int nB = (int)(1.164 * nY + 1.596 * nV);
                
                //http://www.jpeg.org/public/jfif.pdf
                //int nR=(int)(nY  + 1.402 * (nV-128));
                //int nG = (int)(nY - 0.34414 * (nU-128) - 0.71414 * (nV-128));
                //int nB = (int)(nY + 1.772 * (nU-128));
                //int nR = (int)((298.082 * nY + 408.583 *nV)/256.0 -222.921);
                //int nG = (int)((298.082 * nY - 100.291 *nU - 208.120 * nV)/256.0 + 135.576);
                //int nB = (int)((298.082 * nY + 516.412 *nU)/256.0 - 276.836);
                
                frame[pixelPtr++]=(byte)nB;
                frame[pixelPtr++]=(byte)nG;
                frame[pixelPtr++]=(byte)nR;
            }
            pixelPtr = (i+1) * textureSize * 3;
		}
	}
	
	
	private void convertAndroidProject(byte[] data) {
		int pY = 0;
		int pUV = previewFrameWidth * previewFrameHeight;
		int pixelPtr=0;
		int width = previewFrameWidth;
		int height = previewFrameHeight;
		final int bytes_per_pixel = 2;
		int nR, nG, nB;
		int nY, nU, nV;

		//iterate through all pixels
		for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                nY = data[pY + i * width + j];
                nV = data[pUV + (i/2) * width + bytes_per_pixel * (j/2)];
                nU = data[pUV + (i/2) * width + bytes_per_pixel * (j/2) + 1];
            
                // Yuv Convert
                nY -= 16;
                nU -= 128;
                nV -= 128;
            
                if (nY < 0)
                    nY = 0;
            
                 //nR = (int)(1.164 * nY + 2.018 * nU);
                 //nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
                 //nB = (int)(1.164 * nY + 1.596 * nV);
            
                nB = (int)(1192 * nY + 2066 * nU);
                nG = (int)(1192 * nY - 833 * nV -  400 * nU);
                nR = (int)(1192 * nY + 1634 * nV);
            
                nR = Math.min(262143, Math.max(0, nR));
                nG = Math.min(262143, Math.max(0, nG));
                nB = Math.min(262143, Math.max(0, nB));
            
                nR >>= 10; nR &= 0xff;
                nG >>= 10; nG &= 0xff;
                nB >>= 10; nB &= 0xff;
                
                frame[pixelPtr++] = (byte) nR;
                frame[pixelPtr++] = (byte) nG;
                frame[pixelPtr++] = (byte) nB;
            }
            pixelPtr = (i+1) * textureSize * 3;
        }
	}
	
	/**
	 * Converts semi-planar YUV420 as generated for camera preview into RGB565
	 * format for use as an OpenGL ES texture. It assumes that both the input
	 * and output data are contiguous and start at zero.
	 * 
	 * @param yuvs the array of YUV420 semi-planar data
	 * @param rgbs an array into which the RGB565 data will be written
	 * @param width the number of pixels horizontally
	 * @param height the number of pixels vertically
	 */

	//we tackle the conversion two pixels at a time for greater speed
	private void toRGB565(byte[] yuvs, int width, int height, byte[] rgbs) {
	    //the end of the luminance data
	    final int lumEnd = width * height;
	    //points to the next luminance value pair
	    int lumPtr = 0;
	    //points to the next chromiance value pair
	    int chrPtr = lumEnd;
	    //points to the next byte output pair of RGB565 value
	    int outPtr = 0;
	    //the end of the current luminance scanline
	    int lineEnd = width;

	    int row = 0;
	    
	    while (true) {

	        //skip back to the start of the chromiance values when necessary
	        if (lumPtr == lineEnd) {
	            if (lumPtr == lumEnd) break; //we've reached the end
	            //division here is a bit expensive, but's only done once per scanline
	            chrPtr = lumEnd + ((lumPtr  >> 1) / width) * width;
	            lineEnd += width;
	            row++;
	            outPtr = row * textureSize * 3;
	        }

	        //read the luminance and chromiance values
	        final int Y1 = yuvs[lumPtr++] & 0xff; 
	        final int Y2 = yuvs[lumPtr++] & 0xff; 
	        final int Cr = (yuvs[chrPtr++] & 0xff) - 128; 
	        final int Cb = (yuvs[chrPtr++] & 0xff) - 128;
	        int R, G, B;

	        //generate first RGB components
	        B = Y1 + ((454 * Cb) >> 8);
	        if(B < 0) B = 0; else if(B > 255) B = 255; 
	        G = Y1 - ((88 * Cb + 183 * Cr) >> 8); 
	        if(G < 0) G = 0; else if(G > 255) G = 255; 
	        R = Y1 + ((359 * Cr) >> 8); 
	        if(R < 0) R = 0; else if(R > 255) R = 255; 
	        //NOTE: this assume little-endian encoding
	        rgbs[outPtr++]  = (byte) (((G & 0x3c) << 3) | (B >> 3));
	        rgbs[outPtr++]  = (byte) ((R & 0xf8) | (G >> 5));

	        //generate second RGB components
	        B = Y2 + ((454 * Cb) >> 8);
	        if(B < 0) B = 0; else if(B > 255) B = 255; 
	        G = Y2 - ((88 * Cb + 183 * Cr) >> 8); 
	        if(G < 0) G = 0; else if(G > 255) G = 255; 
	        R = Y2 + ((359 * Cr) >> 8); 
	        if(R < 0) R = 0; else if(R > 255) R = 255; 
	        //NOTE: this assume little-endian encoding
	        rgbs[outPtr++]  = (byte) (((G & 0x3c) << 3) | (B >> 3));
	        rgbs[outPtr++]  = (byte) ((R & 0xf8) | (G >> 5));
	    }
	}
	
	// decode Y, U, and V values on the YUV 420 buffer described as	YCbCr_422_SP by Android
	// David Manpearl 081201
	public void decodeYUV(byte[] out, byte[] fg, int width, int height) throws NullPointerException, IllegalArgumentException {
	        final int sz = width * height;
	        if(out == null) throw new NullPointerException("buffer 'out' is	null");
	        if(out.length < sz) throw new IllegalArgumentException("buffer 'out'	size " + out.length + " < minimum " + sz);
	        if(fg == null) throw new NullPointerException("buffer 'fg' is null");
	        if(fg.length < sz) throw new IllegalArgumentException("buffer 'fg'	size " + fg.length + " < minimum " + sz * 3/ 2);
	        int i, j;
	        int Y, Cr = 0, Cb = 0;
	        int pixPtr=0;
	        for(j = 0; j < height; j++) {
	                //int pixPtr = j * width;
	                final int jDiv2 = j >> 1;
	                for(i = 0; i < width; i++) {
	                        Y = fg[pixPtr]; if(Y < 0) Y += 255;
	                        if((i & 0x1) != 1) {
	                                final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
	                                Cb = fg[cOff];
	                                if(Cb < 0) Cb += 127; else Cb -= 128;
	                                Cr = fg[cOff + 1];
	                                if(Cr < 0) Cr += 127; else Cr -= 128;
	                        }
	                        int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
	                        if(R < 0) R = 0; else if(R > 255) R = 255;
	                        int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >>
	3) + (Cr >> 4) + (Cr >> 5);
	                        if(G < 0) G = 0; else if(G > 255) G = 255;
	                        int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
	                        if(B < 0) B = 0; else if(B > 255) B = 255;
	                        //out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
	                        out[pixPtr++]=(byte)R;
                            out[pixPtr++]=(byte)G;
                            out[pixPtr++]=(byte)B;
	                }
	                pixPtr = (j+1) * textureSize * 3;
	        }

	}





}
