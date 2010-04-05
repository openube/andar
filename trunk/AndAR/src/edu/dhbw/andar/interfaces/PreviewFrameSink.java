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
package edu.dhbw.andar.interfaces;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Tobias Domhan
 *
 */
public interface PreviewFrameSink {
	void setNextFrame(ByteBuffer buf);
	ReentrantLock getFrameLock();
	/**
	 * Set the size of the texture(must be power of two)
	 */
	void setPreviewFrameSize(int textureSize, int realWidth, int realHeight);
	/**
	 * sets the mode(either GL10.GL_RGB or GL10.GL_LUMINANCE)
	 * @param pMode
	 */
	public void setMode(int pMode);
}
