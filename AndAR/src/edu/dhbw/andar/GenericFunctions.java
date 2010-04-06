/**
	Copyright (C) 2009,2010  Tobias Domhan

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
package edu.dhbw.andar;

/**
 * @author Tobias Domhan
 *
 */
public class GenericFunctions {
	/**
	 * tests if an integer is power of two
	 * source: http://www.devmaster.net/forums/showthread.php?t=1728
	 * @param value
	 * @return
	 */
	static final boolean isPowerOfTwo (int value)	{
		if(value != 0) {
			return (value & -value) == value;
		} else {
			return false;
		}
	}
	
	/**
	 * returns the smallest power of two that is greater than
	 * or equal to the absolute value of x
	 * @param x
	 * @return
	 */
	static final public int nextPowerOfTwo(int x) {
		double val = (double) x;
		return (int) Math.pow(2, Math.ceil(log2(val)));
	}
	
	/**
	 * return the log of x base 2
	 * @param x
	 * @return
	 */
	static final public double log2(double x) {
		return Math.log(x)/Math.log(2);
	}
}
