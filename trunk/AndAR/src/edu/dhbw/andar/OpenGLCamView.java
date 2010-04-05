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
package edu.dhbw.andar;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * View that will display the camera preview on an opengl square
 * 
 * Continuous Rendering vs. Render When Dirty:
 * http://android-developers.blogspot.com/2009/04/introducing-glsurfaceview.html
 * @author Tobias Domhan
 *
 */
public class OpenGLCamView extends GLSurfaceView {
	
	

	/**
	 * @param context
	 */
	public OpenGLCamView(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public OpenGLCamView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	
}
