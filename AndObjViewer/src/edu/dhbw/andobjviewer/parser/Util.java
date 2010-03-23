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

import java.util.regex.Pattern;

public class Util {
	
	private static final Pattern trimWhiteSpaces = Pattern.compile("[\\s]+");
	private static final Pattern removeInlineComments = Pattern.compile("#");
	private static final Pattern splitBySpace = Pattern.compile(" ");

	
	/**
	 * returns a canonical line of a obj or mtl file.
	 * e.g. it removes multiple whitespaces or comments from the given string.
	 * @param line
	 * @return
	 */
	public static final String getCanonicalLine(String line) {
		line = trimWhiteSpaces.matcher(line).replaceAll(" ");
		if(line.contains("#")) {
			String[] parts = removeInlineComments.split(line);
			if(parts.length > 0)
				line = parts[0];//remove inline comments
		}
		return line;
	}
	public static String[] splitBySpace(String str) {
		return splitBySpace.split(str);
	}
	
}
