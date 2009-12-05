 package edu.union.graphics;

import java.util.Vector;

/**
 * A Mesh which stores things in Vectors of fixed-point values.
 * @author bburns
 */
public class IntMesh extends FixedPointMesh {
	
	Vector<int[]> vertices;
	Vector<int[]> normals;
	Vector<int[]> texCoords;
	Vector<int[]> face_normals;
	
	public IntMesh() {
		vertices = new Vector<int[]>();
		normals = new Vector<int[]>();
		texCoords = new Vector<int[]>();
		face_normals = new Vector<int[]>();
	}
	
	@Override
	public void addNormal(int[] normal) {
		normals.add(normal);
	}

	@Override
	public void addTextureCoordinate(int[] coord) {
		texCoords.add(coord);
	}

	@Override
	public void addVertex(int[] vertex) {
		vertices.add(vertex);
	}

	@Override
	public int[] getFaceNormalx(int ix) {
		return face_normals.get(ix);
	}

	@Override
	public int[] getNormalx(int ix) {
		return normals.get(ix);
	}

	@Override
	public int[] getTextureCoordinatex(int ix) {
		return texCoords.get(ix);
	}

	@Override
	public int getVertexCount() {
		return vertices.size();
	}

	@Override
	public int[] getVertexx(int ix) {
		return vertices.get(ix);
	}

	@Override
	public void scale(int scale) {
		for (int i=0;i<vertices.size();i++) {
			int[] vx = vertices.get(i);
			vx[0] = (int)((((long)vx[0])*((long)scale))>>16);
			vx[1] = (int)((((long)vx[1])*((long)scale))>>16);
			vx[2] = (int)((((long)vx[2])*((long)scale))>>16);
		}
	}
	
	@Override
	protected void addFaceNormal(int[] norm) {
		face_normals.add(norm);
	}

	@Override
	protected void clearFaceNormals() {
		face_normals.clear();	
	}

	@Override
	protected void clearNormals() {
		normals.clear();
	}

	@Override
	protected void clearTexCoords() {
		texCoords.clear();
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
						return new IntMesh();
					}
					public Mesh create(int v, int t, int f) {
						return new IntMesh();
					}
				};
			}
			return mf;
	}
}
