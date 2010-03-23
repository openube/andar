package edu.dhbw.andobjviewer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import android.content.res.Resources;

public class FileUtil {	
	
	private File baseFolder;
	
	
	
	
	public File getBaseFolder() {
		return baseFolder;
	}




	public void setBaseFolder(File baseFolder) {
		this.baseFolder = baseFolder;
	}




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
}
