package edu.dhbw.andobjviewer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SDCardFileUtil extends BaseFileUtil {	



	/* (non-Javadoc)
	 * @see edu.dhbw.andobjviewer.util.FileUtilInterface#getReaderFromName(java.lang.String)
	 */
	public BufferedReader getReaderFromName(String name) {
		if (baseFolder != null) {
			try {
				return new BufferedReader(new FileReader(new File(baseFolder, name)));
			} catch (FileNotFoundException e) {
				return null;
			}
		} else {
			try {
				return new BufferedReader(new FileReader(new File(name)));
			} catch (FileNotFoundException e) {
				return null;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.dhbw.andobjviewer.util.FileUtilInterface#getBitmapFromName(java.lang.String)
	 */
	public Bitmap getBitmapFromName(String name) {
		if (baseFolder != null) {
			String path = new File(baseFolder,name).getAbsolutePath();
			return BitmapFactory.decodeFile(path);
		} else {
			return BitmapFactory.decodeFile(name);
		}
	}
}
