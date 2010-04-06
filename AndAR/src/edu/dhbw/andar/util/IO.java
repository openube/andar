package edu.dhbw.andar.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.dhbw.andopenglcam.R;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.text.GetChars;

public class IO {
	
	/**
	 * transfers required files to the the private file system part
	 * in order to be access them from C Code.
	 * required, as you can not access the files of the apk package directly
	 */
	public static void transferFilesToPrivateFS(File base, Resources res) throws IOException {
		AssetManager am = res.getAssets();
		if (!base.exists()) {
			base.mkdir();
		}
		if (base.exists()) {
			File cameraFile = new File(base, "camera_para.dat");
			if (!cameraFile.exists()) {
				copy(am.open("camera_para.dat"), new FileOutputStream(cameraFile));
			}
		}
	}
	/**
	 * 
	 * @param base
	 * @param assetFileName filename of the file in the assets folder
	 * @param res
	 * @throws IOException
	 */
	public static void transferFileToPrivateFS(File base, String assetFileName,Resources res) throws IOException {
		AssetManager am = res.getAssets();
		if (!base.exists()) {
			base.mkdir();
		}
		if (base.exists()) {
			File file = new File(base, assetFileName);
			if (!file.exists()) {
				copy(am.open(assetFileName), new FileOutputStream(file));
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
