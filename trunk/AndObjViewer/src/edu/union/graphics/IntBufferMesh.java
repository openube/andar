package edu.union.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * A Mesh which stores things in nio IntBuffers of FixedPoint values
 * @author bburns
 *
 */
public class IntBufferMesh extends FixedPointMesh {

	IntBuffer vertices;
	IntBuffer normals;
	IntBuffer texCoords;
	IntBuffer face_normals;

	int vertexCount;
	
	private static MeshFactory mf = null;
	
	public static MeshFactory factory() {
			if (mf == null) {
				mf = new MeshFactory() {
					public Mesh create() {
						System.err.println("Error, unsupported create method, must use parameters...");
						return null;
					}
					public Mesh create(int v, int t, int f) {
						return new IntBufferMesh(v, t, f);
					}
				};
			}
			return mf;
	}

	public IntBuffer makeIntBuffer(int size) {
		ByteBuffer bb;
		bb = ByteBuffer.allocateDirect(size*4);
		bb.order(ByteOrder.nativeOrder());
		return bb.asIntBuffer();
	}

	public IntBufferMesh(int vertexCount, int texCoordCount, int faceCount) {
		vertices = makeIntBuffer(vertexCount*3);
		normals = makeIntBuffer(vertexCount*3);
		texCoords = makeIntBuffer(texCoordCount*2);
		face_normals = makeIntBuffer(faceCount*3);

		this.vertexCount = vertexCount;
	}

	@Override
	public void addNormal(int[] normal) {
		// This is a workaround a bug in android...
		normals.put(normal[0]);
		normals.put(normal[1]);
		normals.put(normal[2]);
	}

	@Override
	public void addTextureCoordinate(int[] coord) {
		// This is a workaround a bug in android...
		texCoords.put(coord[0]);
		texCoords.put(coord[1]);
	}

	@Override
	public void addVertex(int[] vertex) {
		// This is a workaround a bug in android...
		vertices.put(vertex[0]);
		vertices.put(vertex[1]);
		vertices.put(vertex[2]);
	}

	@Override
	public int[] getFaceNormalx(int ix) {
		int[] norm = new int[3];
		face_normals.get(norm, ix*3, 3);
		return norm;
	}

	@Override
	public int[] getNormalx(int ix) {
		if (ix*3 < normals.capacity()) {
			int[] norm = new int[3];
			normals.position(ix*3);
			normals.get(norm);
			return norm;
		}
		else {
			return null;
		}
	}

	@Override
	public int[] getTextureCoordinatex(int ix) {
		int[] coord = new int[2];
		texCoords.position(ix*2);
		texCoords.get(coord);
		return coord;
	}

	@Override
	public int getVertexCount() {
		return vertices.capacity()/3;
	}

	@Override
	public int[] getVertexx(int ix) {
		int[] vert = new int[3];
		vertices.position(ix*3);
		vertices.get(vert);
		return vert;
	}

	@Override
	public void scale(int scale) {
		for (int i=0;i<vertices.capacity();i++) {
			int x = vertices.get(i);
			vertices.put(i, (int)((((long)x)*((long)scale))>>16));
		}
	}

	public IntBuffer getVertexBuffer() {
		return vertices;
	}

	public IntBuffer getNormalBuffer() {
		return normals;
	}

	public IntBuffer getTexCoordBuffer() {
		return texCoords;
	}

	@Override
	protected void addFaceNormal(int[] norm) {
		face_normals.put(norm[0]);
		face_normals.put(norm[1]);
		face_normals.put(norm[2]);
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

	@Override
	public void reorder() {
		// Each face has three vertices by three coordinates
		int newsize = getFaceCount()*3*3;
		if (vertices.capacity() < newsize) {
			ByteBuffer bb = ByteBuffer.allocateDirect(newsize*4);
			IntBuffer newVert = bb.asIntBuffer();
			vertices.position(0);
			newVert.put(vertices);
			// Workaround for android bug...
			if (newVert.position() < vertices.position())
				newVert.position(vertices.position());
			vertices.clear();
			vertices = newVert;
		}
		if (normals.capacity() < newsize) {
			ByteBuffer bb = ByteBuffer.allocateDirect(newsize*4);
			IntBuffer newNorm = bb.asIntBuffer();
			normals.position(0);
			newNorm.put(normals);
			if (newNorm.position() < normals.position())
				newNorm.position(normals.position());
			normals.clear();
			normals = newNorm;
		}
		if (texCoords.capacity() < newsize) {
			ByteBuffer bb = ByteBuffer.allocateDirect(newsize*4);
			IntBuffer newTex = bb.asIntBuffer();
			texCoords.position(0);
			newTex.put(texCoords);
			if (newTex.position() < texCoords.position())
				newTex.position(texCoords.position());
			texCoords.clear();
			texCoords = newTex;
		}
		super.reorder();
	}
}
