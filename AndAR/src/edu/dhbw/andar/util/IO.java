package edu.dhbw.andar.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.dhbw.andopenglcam.R;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.text.GetChars;

public class IO {
	
	/**
	 * transfers required files to the sc card to be access from C Code
	 */
	public static void transferFilesToSDCard(Resources res) {
		File andarFolder = new File("/sdcard/andar");
		if (!andarFolder.exists()) {
			andarFolder.mkdir();
		}
		if (andarFolder.exists()) {
			File patternFile = new File("/sdcard/andar/patt.hiro");
			if (!patternFile.exists()) {
				try {
					copy(res.openRawResource(R.raw.patt), new FileOutputStream(patternFile));
				} catch (NotFoundException e) {
					//TODO notify user
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					//TODO notify user
					e.printStackTrace();
				} catch (IOException e) {
					//TODO notify user
					e.printStackTrace();
				}
			}
			File cameraFile = new File("/sdcard/andar/camera_para.dat");
			if (!cameraFile.exists()) {
				try {
					copy(res.openRawResource(R.raw.camera_para), new FileOutputStream(cameraFile));
				} catch (NotFoundException e) {
					//TODO notify user
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					//TODO notify user
					e.printStackTrace();
				} catch (IOException e) {
					//TODO notify user
					e.printStackTrace();
				}
			}
		}
	}
	static void copy( InputStream in, OutputStream out ) throws IOException 
	  { 
	    byte[] buffer = new byte[ 0xFFFF ]; 
	    for ( int len; (len = in.read(buffer)) != -1; ) 
	      out.write( buffer, 0, len ); 
	  }
}
