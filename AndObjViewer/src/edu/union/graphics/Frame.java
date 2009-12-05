package edu.union.graphics;

/**
 * A single frame in an animation.
 * @author bburns
 */
public class Frame {
	String name;
	Mesh mesh;

	/**
	 * Constructor
	 * @param name The name of this frame.
	 * @param mesh The mesh for this frame.
	 */
	public Frame(String name, Mesh mesh) {
		this.name = name;
		this.mesh = mesh;
	}

	/**
	 * Get the name of this mesh.
	 * @return The Frame's name.
	 */
	public String getName() { return name; }

	/**
	 * Get the mesh for this frame.
	 * @return The Mesh for this Frame
	 */
	public Mesh getMesh() { return mesh; }
}