package edu.union.graphics;

import java.io.Serializable;
import java.util.Vector;

public class Animation implements Serializable {
	Model model;
	int frameStart;
	int frameEnd;
	int fps;
	String name;

	public Animation(Model m, int start, int end, int fps, String name) {
		this.model = m;
		this.frameStart = start;
		this.frameEnd = end;
		this.fps = fps;
		this.name = name;
	}

	public Model getModel() {
		return model;
	}

	public String getName() {
		return name;
	}

	public int getStartFrame() {
		return frameStart;
	}

	public int getEndFrame() {
		return frameEnd;
	}

	public int getFramesPerSecond() {
		return fps;
	}

	public static Vector<Animation> buildAnimationsHeuristic
	(Model m, Frame[] frames, int fps) 
	{
		Vector<Animation> anims = new Vector<Animation>();
		String prefix = null;
		int start = 0;
		for (int i=0;i<frames.length;i++) {
			String name = frames[i].getName();
			if (prefix != null) {
				if (!name.startsWith(prefix)) {
					anims.add(new Animation(m, start, i-1, fps, prefix));
					start = i;
					prefix = null;
				}
			}
			if (prefix == null) {
				int j=0;
				for (j=0;j<name.length();j++)
					if (Character.isDigit(name.charAt(j)))
						break;
				prefix = name.substring(0,j);
			}
		}
		anims.add(new Animation(m, start, frames.length-1, fps, prefix));
		return anims;
	}
}