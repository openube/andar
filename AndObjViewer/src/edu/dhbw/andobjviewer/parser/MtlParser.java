/**
	Copyright (C) 2010  Tobias Domhan

    This file is part of AndObjViewer.

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
package edu.dhbw.andobjviewer.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import edu.dhbw.andobjviewer.models.Material;
import edu.dhbw.andobjviewer.models.Model;


import android.R;


public class MtlParser {

	
	public MtlParser(Model model) {
	}

	/**
	 * parses the given material definition
	 * @param line
	 */
	public void parse(Model model, BufferedReader is) {
		Material curMat = null;
		int lineNum = 1;
		String line;
		try {
			for (line = is.readLine(); 
			line != null; 
			line = is.readLine(), lineNum++)
			{
				line = Util.getCanonicalLine(line);
				if (line.length() > 0) {
					if (line.startsWith("newmtl ")) {
						// specular color
						String mtlName = line.substring(7);
						curMat = new Material(mtlName);
						model.addMaterial(curMat);
					} else if(curMat == null) {
						//if the current material is not set, there is no need to parse anything
					} else if (line.startsWith("# ")) {
						//ignore comments
					} else if (line.startsWith(" Ka ")) {
						//ambient color
						String endOfLine = line.substring(4);
						curMat.setAmbient(parseTriple(endOfLine));
					} else if (line.startsWith(" Kd ")) {
						// diffuse color
						String endOfLine = line.substring(4);
						curMat.setDiffuse(parseTriple(endOfLine));
					} else if (line.startsWith(" Ks ")) {
						// specular color
						String endOfLine = line.substring(4);
						curMat.setSpecular(parseTriple(endOfLine));
					} else if (line.startsWith(" Ns ")) {
						// specular color
						String endOfLine = line.substring(4);
						curMat.setShininess(Float.parseFloat(endOfLine));
					} else if (line.startsWith(" Tr ")) {
						// specular color
						String endOfLine = line.substring(4);
						curMat.setAlpha(Float.parseFloat(endOfLine));
					} else if (line.startsWith(" d ")) {
						// specular color
						String endOfLine = line.substring(3);
						curMat.setAlpha(Float.parseFloat(endOfLine));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static float[] parseTriple(String str) {
		String[] colorVals = str.split(" ");
		float[] colorArr = new float[]{
				Float.parseFloat(colorVals[0]),
				Float.parseFloat(colorVals[1]),
				Float.parseFloat(colorVals[2])};
		return colorArr;
		
	}
	
}
