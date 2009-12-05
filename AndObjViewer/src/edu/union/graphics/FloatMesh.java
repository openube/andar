package edu.union.graphics;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * A Mesh which stores things in Vectors of floating-point values
 * @author bburns
 */
public class FloatMesh extends Mesh {
	Vector<float[]> vertices;
	Vector<float[]> face_normals;
	Vector<float[]> vertex_normals;
	Vector<float[]> normals;
	Vector<float[]> texture_coords;
	Vector<float[]> face_planes;
	Hashtable<Edge, int[]> neighbors;

	public FloatMesh() {
		this.vertices = new Vector<float[]>();
		this.face_normals = new Vector<float[]>();
		this.vertex_normals = new Vector<float[]>();
		this.normals = new Vector<float[]>();
		this.texture_coords = new Vector<float[]>();
		this.face_planes = new Vector<float[]>();
	}

	public void calculateFacePlanes(ObjLoader.hand h) {
		face_planes.clear();
		Iterator<int[]> it = faces.iterator();
		float[] temp1 = new float[3];
		float[] temp2 = new float[3];

		while (it.hasNext()) {
			int[] face = it.next();
			float[] normal = new float[3];
			float[] plane = new float[4];

			float[] p0, p1, p2;
			if (h == ObjLoader.hand.RIGHT) {
				p0 = vertices.get(face[0]);
				p1 = vertices.get(face[1]);
				p2 = vertices.get(face[2]);
			}
			else {
				p0 = vertices.get(face[2]);
				p1 = vertices.get(face[1]);
				p2 = vertices.get(face[0]);
			}
			MatrixUtils.minus(p0,p1,temp1);
			MatrixUtils.minus(p2,p1,temp2);
			MatrixUtils.cross(temp1,temp2, normal);
			MatrixUtils.normalize(normal);

			plane[0] = normal[0];
			plane[1] = normal[1];
			plane[2] = normal[2];
			plane[3] = -(plane[0]*p0[0]+plane[1]*p0[1]+plane[2]*p0[2]);
			face_planes.add(plane);
		}
	}


	/**
	 * Calculate the normals for a face from the triangle.
	 * \param h The winding rule to use for the triangle
	 **/
	public void calculateFaceNormals(ObjLoader.hand h) {
		face_normals.clear();
		Iterator<int[]> it = faces.iterator();
		float[] temp1 = new float[3];
		float[] temp2 = new float[3];

		while (it.hasNext()) {
			int[] face = it.next();
			float[] normal = new float[3];

			float[] p0, p1, p2;
			if (h == ObjLoader.hand.RIGHT) {
				p0 = vertices.get(face[0]);
				p1 = vertices.get(face[1]);
				p2 = vertices.get(face[2]);
			}
			else {
				p0 = vertices.get(face[2]);
				p1 = vertices.get(face[1]);
				p2 = vertices.get(face[0]);
			}
			MatrixUtils.minus(p0,p1,temp1);
			MatrixUtils.minus(p2,p1,temp2);
			MatrixUtils.cross(temp1,temp2, normal);
			MatrixUtils.normalize(normal);
			face_normals.add(normal);
		}
	}

	/**
	 * Calculate the average normal for a vertex from its constituent
	 * faces.
	 **/
	@SuppressWarnings("unchecked")
	public void calculateVertexNormals() {
		vertex_normals.clear();
		Vector<float[]>[] norms = new Vector[vertices.size()];

		for (int i=0;i<vertices.size();i++)
			norms[i] = new Vector<float[]>();
		for (int i=0;i<faces.size();i++) {
			int[] face = faces.get(i);
			float[] norm = face_normals.get(i);
			norms[face[0]].add(norm);
			norms[face[1]].add(norm);
			norms[face[2]].add(norm);
		}
		for (int i=0;i<norms.length;i++) {
			float[] norm = new float[3];
			for (int k=0;k<norms[i].size();k++) {
				MatrixUtils.plus(norm, (norms[i].get(k)), null);
			}
			MatrixUtils.normalize(norm);
			vertex_normals.add(norm);
		}
	}


	// Other mesh methods...
	/**
	 * Scale this mesh by the given factor.
	 * \param scale The scaling factor.
	 **/
	public void scale(float scale) {
		for (int i=0;i<vertices.size();i++) {
			vertices.get(i)[0] *= scale;
			vertices.get(i)[1] *= scale;
			vertices.get(i)[2] *= scale;
		}
	}

	/**
	 * Get the number of faces in the mesh.
	 * \return The number of faces.
	 **/
	public int getFaceCount() {
		return faces.size();
	}

	/**
	 * Get a specific face in the mesh
	 * \param ix The index of the face.
	 * \return An array of three indices that refer to the face's vertices.
	 **/
	public int[] getFace(int ix) {
		return faces.get(ix);
	}

	/**
	 * Get a normal for a face.
	 * \param ix The index of the face.
	 * \return The normal for that face.
	 **/
	public float[] getFaceNormalf(int ix) {
		if (ix < 0 || ix >= face_normals.size())
			return null;
		return face_normals.get(ix);
	}


	/**
	 * Get a specific vertex.
	 * \param ix The index of the vertex
	 * \return The coordinates of the vertex.
	 **/
	public float[] getVertexf(int ix) {
		return vertices.get(ix);
	}

	/**
	 * Get the number of vertices in the mesh.
	 * @return The number of vertices in the mesh.
	 **/
	public int getVertexCount() {
		return vertices.size();
	}


	/**
	 * Get a normal from the normal list
	 * \param ix The index of in the list
	 * \return The normal at that index.
	 **/
	public float[] getNormalf(int ix) {
		if (ix < 0 || ix >= normals.size())
			return null;
		return normals.get(ix);
	}


	/**
	 * Get the normals for a face.
	 * \param ix The index of the face.
	 * \return The indices for the normals at each vertex in the face.
	 **/
	public int[] getFaceNormals(int ix) {
		if (sharedVertexNormals) {
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
	 * \param normal The normal to add.
	 **/
	public void addNormal(float[] normal) {
		normals.add(normal);
	}

	/**
	 * Copy the normals from the vertex normals into the face
	 * normals.
	 **/
	public void copyNormals() {
		for (int i=0;i<faces.size();i++) {
			face_normal_ix.add(faces.get(i));
		}
		for (int i=0;i<vertex_normals.size();i++) {
			normals.add(vertex_normals.get(i));
		}
	}

	/**
	 * Add a vertex to the mesh.
	 * \param vertex The coordinates of the vertex.
	 **/
	public void addVertex(float[] vertex) {
		vertices.add(vertex);
	}


	/**
	 * Get the texture coordinate indices for a face.
	 * \param ix The index of the face.
	 * \return The specified texture indices, or null if they don't exist.
	 **/
	public int[] getFaceTextures(int ix) {
		if (ix < 0 || ix >= face_tx_ix.size())
			return null;
		return face_tx_ix.get(ix);
	}

	/**
	 * Add a texture index for a face.
	 * \param ixs The indices to add.
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
	 * \param coord The coordinate to add.
	 **/
	public void addTextureCoordinate(float[] coord) {
		float[] crd = new float[coord.length];
		for (int i=0;i<crd.length;i++) {
			crd[i] = coord[i];
		}
		texture_coords.add(crd);
	}

	/**
	 * Get a specific texture coordinate
	 * \param ix The index of the texture coordinate
	 * \return The specified texture coordinate, or null if it doesn't exist.
	 **/
	public float[] getTextureCoordinatef(int ix) {
		if (ix < 0 || ix >= texture_coords.size()) {
			System.out.println("No such coord!");
			return null;
		}
		return texture_coords.get(ix);
	}

	/**
	 * Add a face to the mesh.
	 * \param face The three indices of the face's vertices
	 **/
	public void addFace(int[] face) {
		int[] fce = new int[3];
		for (int i=0;i<3;i++)
			fce[i] = face[i];
		faces.add(fce);
	}

	/**
	 * Add face normals
	 * \param The three indices of the face's normals in the normal list
	 **/
	public void addFaceNormals(int[] normals) {
		int[] fce = new int[3];
		for (int i=0;i<3;i++)
			fce[i] = normals[i];
		face_normal_ix.add(fce);
	}

	/**
	 * Get the name of the file that textures this mesh.
	 * \return A file name.
	 **/
	public String getTextureFile() {
		return texture_file;
	}

	/**
	 * Set the name of the file that textures this mesh.
	 * \param texture The new file name.
	 **/
	public void setTextureFile(String texture) {
		this.texture_file = texture;
	}

	/**
	 * Get the name of the file that textures this mesh.
	 * \return A file name.
	 **/
	public String getNormalFile() {
		return normal_file;
	}

	/**
	 * Set the name of the file that textures this mesh.
	 * \param texture The new file name.
	 **/
	public void setNormalFile(String texture) {
		this.normal_file = texture;
	}

	/**
	 * Get the name of the file that textures this mesh.
	 * \return A file name.
	 **/
	public String getSpecularFile() {
		return specular_file;
	}

	/**
	 * Set the name of the file that textures this mesh.
	 * \param texture The new file name.
	 **/
	public void setSpecularFile(String texture) {
		this.specular_file = texture;
	}

	@Override
	public void addNormal(int[] normal) {
		addNormal(FixedPointUtils.toFloat(normal));
	}

	@Override
	public void addTextureCoordinate(int[] coord) {
		addTextureCoordinate(FixedPointUtils.toFloat(coord));
	}

	@Override
	public void addVertex(int[] vertex) {
		addVertex(FixedPointUtils.toFloat(vertex));
	}

	@Override
	public int[] getFaceNormalx(int ix) {
		return FixedPointUtils.toFixed(getFaceNormalf(ix));
	}

	@Override
	public int[] getNormalx(int ix) {
		return FixedPointUtils.toFixed(getNormalf(ix));
	}

	@Override
	public int[] getTextureCoordinatex(int ix) {
		return FixedPointUtils.toFixed(getTextureCoordinatef(ix));
	}

	@Override
	public int[] getVertexx(int ix) {
		return FixedPointUtils.toFixed(getVertexf(ix));
	}

	@Override
	public void scale(int scale) {
		scale(FixedPointUtils.toFixed(scale));
	}

	@Override
	protected void clearNormals() {
		normals.clear();
		
	}

	@Override
	protected void clearTexCoords() {
		texture_coords.clear();
	}

	@Override
	protected void clearVertices() {
		vertices.clear();
	}
	
	private static MeshFactory mf = null;
	
	public static MeshFactory factory() {
			if (mf == null) {
				mf = new MeshFactory() {
					public Mesh create() {
						return new FloatMesh();
					}
					public Mesh create(int v, int t, int f) {
						return new FloatMesh();
					}
				};
			}
			return mf;
	}
}