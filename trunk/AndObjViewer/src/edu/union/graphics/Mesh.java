package edu.union.graphics;

import java.util.Hashtable;
import java.util.Vector;

/**
 * The Mesh class represents a collection of triangles which form a polygon.
 * The Mesh maintains two sets of normals:
 *    - per face normals (accessible through getFaceNormal)
 *    - per vertex normals (accessible through getFaceNormals and getNormal)
 **/
public abstract class Mesh {
	Mesh indexDelegate = null;
	protected boolean sharedVertexNormals;
	protected boolean sharedTextureCoords;

	Hashtable<Edge, int[]> neighbors;

	Vector<int[]> faces;
	Vector<int[]> face_normal_ix;
	Vector<int[]> face_tx_ix;

	String texture_file;
	String normal_file;
	String specular_file;

	/**
	 * Constructor
	 */
	public Mesh() {
		sharedVertexNormals = false;
		sharedTextureCoords = false;
		faces = new Vector<int[]>();
		face_normal_ix = new Vector<int[]>();
		face_tx_ix = new Vector<int[]>();

		neighbors = new Hashtable<Edge, int[]>();
	}

	/**
	 * Set a mesh to use as a delegate for Vertex/Texture/Normal indices, saves memory across animations.
	 * @param m
	 */
	public void setIndexDelegate(Mesh m) {
		indexDelegate = m;
	}

	/**
	 * Uses vertex indices and normal indices also.  Saves memory.
	 * @param b If true, share vertex and normal indices, otherwise don't
	 */
	public void setSharedVertexNormals(boolean b) {
		this.sharedVertexNormals = b;
	}

	/**
	 * Calculate the neighbors of each vertex
	 */
	public void calculateNeighbors() {
		neighbors.clear();
		for (int i=0;i<faces.size();i++) {
			int[] fce = faces.get(i);
			for (int j=0;j<3;j++) {
				Edge e = new Edge(fce[j],fce[(j+1)%3]);
				int[] vec = neighbors.get(e);
				if (vec == null) {
					vec = new int[2];
					vec[0] = i;
					vec[1] = -1;
					neighbors.put(e, vec);
				}
				else {
					if (vec[1] != -1)
						System.err.println
						("Warning: edge shared by three triangles!");
					vec[1] = i;
				}
			}
		}
	}

	/**
	 * Get the neighbor of a vertex
	 * @param e The edge
	 * @param v The index of the vertex whose neighbor's are wanted.
	 * @return The index of the neighboring vertex.
	 */
	public int getNeighbor(Edge e, int v) {
		if (indexDelegate != null)
			return indexDelegate.getNeighbor(e, v);

		int[] ns = neighbors.get(e);
		if (ns == null)
			return -1;
		if (ns[0] == v)
			return ns[1];
		else
			return ns[0];
	}

	/**
	 * Get both neighboring vertices for an Edge.
	 * @param e The edge to get neighbors for.
	 * @return An array of the two vertex indices.
	 */
	public int[] getNeighbors(Edge e) {
		if (indexDelegate != null)
			return indexDelegate.getNeighbors(e);
		return neighbors.get(e);
	}


	/**
	 * Calculate the normals for a face from the triangle.
	 * \param h The winding rule to use for the triangle
	 **/
	public abstract void calculateFaceNormals(ObjLoader.hand h);

	/**
	 * Calculate the average normal for a vertex from its constituent
	 * faces.
	 **/
	public abstract void calculateVertexNormals();

	/**
	 * Scale this mesh by the given factor.
	 * @paramparam scale The scaling factor.
	 **/
	public abstract void scale(float scale);

	/**
	 * Scale this mesh by the given factor specified in fixed-point.
	 * @param scale The scaling factor.
	 **/
	public abstract void scale(int scale);

	/**
	 * Get the number of faces in the mesh.
	 * @return The number of faces.
	 **/
	public int getFaceCount() {
		if (indexDelegate != null)
			return indexDelegate.getFaceCount();
		return faces.size();
	}

	/**
	 * Get a specific face in the mesh
	 * @param ix The index of the face.
	 * @return An array of three indices that refer to the face's vertices.
	 **/
	public int[] getFace(int ix) {
		if (indexDelegate != null)
			return indexDelegate.getFace(ix);
		return faces.get(ix);
	}

	/**
	 * Get a normal for a face.
	 * @param ix The index of the face.
	 * @return The normal for that face.
	 **/
	public abstract float[] getFaceNormalf(int ix);

	/**
	 * Get a normal for a face.
	 * @param ix The index of the face.
	 * @return The normal for that face.
	 **/
	public abstract int[] getFaceNormalx(int ix);

	/**
	 * Get a specific vertex.
	 * @param ix The index of the vertex
	 * @return The coordinates of the vertex.
	 **/
	public abstract float[] getVertexf(int ix);

	/**
	 * Get a specific vertex.
	 * @param ix The index of the vertex
	 * @return The coordinates of the vertex.
	 **/
	public abstract int[] getVertexx(int ix);


	/**
	 * Get the number of vertices in the mesh.
	 * @return The number of vertices in the mesh.
	 **/
	public abstract int getVertexCount();

	/**
	 * Get a normal from the normal list
	 * @param ix The index of in the list
	 * @return The normal at that index.
	 **/
	public abstract float[] getNormalf(int ix);

	/**
	 * Get a normal from the normal list
	 * @param ix The index of in the list
	 * @return The normal at that index.
	 **/
	public abstract int[] getNormalx(int ix);

	/**
	 * Get the normals for a face.
	 * @param ix The index of the face.
	 * @return The indices for the normals at each vertex in the face.
	 **/
	public int[] getFaceNormals(int ix) {
		if (indexDelegate != null)
			return indexDelegate.getFaceNormals(ix);
		else if (sharedVertexNormals) {
			return getFace(ix);
		}
		else {
			if (ix < 0 || ix >= face_normal_ix.size()) {
				return null;
			}
			return face_normal_ix.get(ix);
		}
	}


	// Utility methods.  You shouldn't need anything below this line.

	/**
	 * Add a normal to the normal list.
	 * @param normal The normal to add.
	 **/
	public abstract void addNormal(float[] normal);


	/**
	 * Add a normal to the normal list.
	 * @param normal The normal to add.
	 **/
	public abstract void addNormal(int[] normal);

	/**
	 * Copy the normals from the vertex normals into the face
	 * normals.
	 **/
	public void copyNormals() {
		for (int i=0;i<faces.size();i++) {
			face_normal_ix.add(faces.get(i));
		}
		for (int i=0;i<getVertexCount();i++) {
			addNormal(getNormalx(i));
		}
	}

	/**
	 * Add a vertex to the mesh.
	 * @param vertex The coordinates of the vertex.
	 **/
	public abstract void addVertex(float[] vertex);

	/**
	 * Add a vertex to the mesh.
	 * @param vertex The coordinates of the vertex.
	 **/
	public abstract void addVertex(int[] vertex);


	/**
	 * Get the texture coordinate indices for a face.
	 * @param ix The index of the face.
	 * @return The specified texture indices, or null if they don't exist.
	 **/
	public int[] getFaceTextures(int ix) {
		if (indexDelegate != null)
			return indexDelegate.getFaceTextures(ix);
		else if (sharedTextureCoords) {
			return getFace(ix);
		}
		else if (ix < 0 || ix >= face_tx_ix.size())
			return null;
		else 
			return face_tx_ix.get(ix);
	}

	/**
	 * Add a texture index for a face.
	 * @param ixs The indices to add.
	 **/
	public void addTextureIndices(int[] ixs) {
		int[] ixs2 = new int[ixs.length];
		for (int i=0;i<ixs.length;i++) {
			ixs2[i] = ixs[i];
		}
		face_tx_ix.add(ixs2);
	}

	/**
	 * Add a texture coordinage
	 * @param coord The coordinate to add.
	 **/
	public abstract void addTextureCoordinate(float[] coord);

	/**
	 * Add a texture coordinage
	 * @param coord The coordinate to add.
	 **/
	public abstract void addTextureCoordinate(int[] coord);


	/**
	 * Get a specific texture coordinate
	 * @param ix The index of the texture coordinate
	 * @return The specified texture coordinate, or null if it doesn't exist.
	 **/
	public abstract float[] getTextureCoordinatef(int ix);

	/**
	 * Get a specific texture coordinate
	 * @param ix The index of the texture coordinate
	 * @return The specified texture coordinate, or null if it doesn't exist.
	 **/
	public abstract int[] getTextureCoordinatex(int ix);

	/**
	 * Add a face to the mesh.
	 * @param face The three indices of the face's vertices
	 **/
	public void addFace(int[] face) {
		int[] fce = new int[3];
		for (int i=0;i<3;i++)
			fce[i] = face[i];
		faces.add(fce);
	}

	/**
	 * Add face normals
	 * @param The three indices of the face's normals in the normal list
	 **/
	public void addFaceNormals(int[] normals) {
		int[] fce = new int[3];
		for (int i=0;i<3;i++)
			fce[i] = normals[i];
		face_normal_ix.add(fce);
	}

	/**
	 * Get the name of the file that textures this mesh.
	 * @return A file name.
	 **/
	public String getTextureFile() {
		return texture_file;
	}

	/**
	 * Set the name of the file that textures this mesh.
	 * @param texture The new file name.
	 **/
	public void setTextureFile(String texture) {
		this.texture_file = texture;
	}

	/**
	 * Get the name of the file that textures this mesh.
	 * @return A file name.
	 **/
	public String getNormalFile() {
		return normal_file;
	}

	/**
	 * Set the name of the file that textures this mesh.
	 * @param texture The new file name.
	 **/
	public void setNormalFile(String texture) {
		this.normal_file = texture;
	}

	/**
	 * Get the name of the file that textures this mesh.
	 * @return A file name.
	 **/
	public String getSpecularFile() {
		return specular_file;
	}

	/**
	 * Set the name of the file that textures this mesh.
	 * @param texture The new file name.
	 **/
	public void setSpecularFile(String texture) {
		this.specular_file = texture;
	}

	/**
	 * Reorder the vertices and textures so that the are in face major order.
	 * Makes the mesh take more memory, but aligned better for VertexBuffer operations.
	 */
	public void reorder() {
		short ct = 0;
		Vector<int[]> verticesL = new Vector<int[]>();
		Vector<int[]> normalsL = new Vector<int[]>();
		Vector<int[]> texCoordsL = new Vector<int[]>();
		Vector<int[]> indices = new Vector<int[]>();

		for (int i=0;i<getFaceCount();i++) {
			int[] face = getFace(i);
			int[] face_n = getFaceNormals(i);
			int[] face_tx = getFaceTextures(i);
			int[] index = new int[3];
			for (int j=0;j<3;j++) {
				int[] n = getNormalx(face_n[j]);
				int[] v = getVertexx(face[j]);
				int[] tx = getTextureCoordinatex(face_tx[j]);
				verticesL.add(v);
				normalsL.add(n);
				texCoordsL.add(tx);
				index[j] = ct++;
			}
			indices.add(index);
		}

		clearVertices();
		clearNormals();
		clearTexCoords();

		for (int i=0;i<verticesL.size();i++) {
			addVertex(verticesL.get(i));
			addNormal(normalsL.get(i));
			addTextureCoordinate(texCoordsL.get(i));
		}

		clearFaces();
		for (int i=0;i<indices.size();i++) {
			addFace(indices.get(i));
		}
		this.face_tx_ix.clear();
		sharedVertexNormals = true;
		sharedTextureCoords = true;
	}

	/**
	 * Clear the vertices storage
	 */
	protected abstract void clearVertices();
	
	/**
	 * Clear the normals storage
	 */
	protected abstract void clearNormals();
	
	/**
	 * Clear the texture coordinate storage
	 */
	protected abstract void clearTexCoords();

	/**
	 * Clear the face index storage
	 */
	protected void clearFaces() {
		faces.clear();
	}
}