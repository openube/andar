package edu.union.graphics;

/**
 * The MeshFactory interface is implemented by factories which can create Mesh objects.
 * @author bburns
 */
public interface MeshFactory {
	/**
	 * Create a Mesh.
	 * @return The created mesh, or null if construction without parameters isn't supported.
	 */
	public Mesh create();
	
	/**
	 * Create a mesh. Preallocate storage.
	 * @param vertexCount The number of vertices in the mesh.
	 * @param texCoordCount The number of texture coordinates in the mesh.
	 * @param faceCount The number of faces in the mesh.
	 * @return The newly created Mesh.
	 */
	public Mesh create(int vertexCount, int texCoordCount, int faceCount);
}
