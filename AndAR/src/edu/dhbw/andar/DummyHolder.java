package edu.dhbw.andar;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

public class DummyHolder implements SurfaceHolder {
	Canvas canvas;
	public DummyHolder() {
		canvas = new Canvas();
		
	}

	@Override
	public void addCallback(Callback callback) {		
	}

	@Override
	public Surface getSurface() {
		return null;
	}

	@Override
	public Rect getSurfaceFrame() {
		return null;
	}

	@Override
	public boolean isCreating() {
		return false;
	}

	@Override
	public Canvas lockCanvas() {
		return canvas;
	}

	@Override
	public Canvas lockCanvas(Rect dirty) {
		return null;
	}

	@Override
	public void removeCallback(Callback callback) {
		
	}

	@Override
	public void setFixedSize(int width, int height) {
		
	}

	@Override
	public void setFormat(int format) {
		
	}

	@Override
	public void setKeepScreenOn(boolean screenOn) {
		
	}

	@Override
	public void setSizeFromLayout() {
		
	}

	@Override
	public void setType(int type) {
		
	}

	@Override
	public void unlockCanvasAndPost(Canvas canvas) {
		
	}
	

}
