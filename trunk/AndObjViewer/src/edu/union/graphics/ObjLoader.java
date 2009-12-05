package edu.union.graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

/**
 * A loader for Wavefront OBJ files.
 * @author bburns
 */
public class ObjLoader extends AbstractModelLoader {
	public static enum hand { LEFT, RIGHT};
	protected hand coordinate_hand;

	protected static final String[] suffs = new String[] {".jpg", ".png", ".gif"};
	
	// Constructor
	public ObjLoader() {
		this(hand.RIGHT);
	}

	/**
	 * Constructor
	 * @param h The order in which triangle vertices are specifed.
	 */
	public ObjLoader(hand h) {
		this.coordinate_hand = h;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canLoad(File f) {
		return f.getName().endsWith(".obj");
	}

	/**
	 * {@inheritDoc}
	 */
	public Model load(String file) 
	throws IOException
	{
		Model m = load(new FileInputStream(file));
		int ix = file.indexOf(".obj");
		if (ix != -1) {
			String texture = file.substring(0, ix);
			for (int i=0;i<suffs.length;i++) {
				File f = new File(texture+suffs[i]);
				if (f.exists()) {
					m.getFrame(0).getMesh().setTextureFile(texture+suffs[i]);
					break;
				}
			}
		}
		return m;
	}

	/**
	 * {@inheritDoc}
	 */
	public Model load(InputStream in) 
	throws IOException
	{
		boolean file_normal = false;
		Mesh m = factory.create();
		int nCount = 0;
		float[] coord = new float[2];

		LineNumberReader input = new LineNumberReader(new InputStreamReader(in));	    
		String line = null;
		try {
			for (line = input.readLine(); 
			line != null; 
			line = input.readLine())
			{
				if (line.length() > 0) {
					if (line.startsWith("v ")) {
						float[] vertex = new float[3];
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						vertex[0] = Float.parseFloat(tok.nextToken());
						vertex[1] = Float.parseFloat(tok.nextToken());
						vertex[2] = Float.parseFloat(tok.nextToken());
						m.addVertex(vertex);
					}
					else if (line.startsWith("vt ")) {
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						coord[0] = Float.parseFloat(tok.nextToken());
						coord[1] = Float.parseFloat(tok.nextToken());
						m.addTextureCoordinate(coord);
					}
					else if (line.startsWith("f ")) {
						int[] face = new int[3];
						int[] face_n_ix = new int[3];
						int[] face_tx_ix = new int[3];
						int[] val;

						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						val = parseIntTriple(tok.nextToken());
						face[0] = val[0];
						if (val.length > 1 && val[1] > -1)
							face_tx_ix[0] = val[1];
						if (val.length > 2 && val[2] > -1)
							face_n_ix[0] = val[2];

						val = parseIntTriple(tok.nextToken());
						face[1] = val[0];
						if (val.length > 1 && val[1] > -1)
							face_tx_ix[1] = val[1];
						if (val.length > 2 && val[2] > -1)
							face_n_ix[1] = val[2];

						val = parseIntTriple(tok.nextToken());
						face[2] = val[0];
						if (val.length > 1 && val[1] > -1) {
							face_tx_ix[2] = val[1];
							m.addTextureIndices(face_tx_ix);
						}
						if (val.length > 2 && val[2] > -1) {
							face_n_ix[2] = val[2];
							m.addFaceNormals(face_n_ix);
						}
						m.addFace(face);
						if (tok.hasMoreTokens()) {
							val = parseIntTriple(tok.nextToken());
							face[1] = face[2];
							face[2] = val[0];
							if (val.length > 1 && val[1] > -1) {
								face_tx_ix[1] = face_tx_ix[2];
								face_tx_ix[2] = val[1];
								m.addTextureIndices(face_tx_ix);
							}
							if (val.length > 2 && val[2] > -1) {
								face_n_ix[1] = face_n_ix[2];
								face_n_ix[2] = val[2];
								m.addFaceNormals(face_n_ix);
							}
							m.addFace(face);
						}

					}
					else if (line.startsWith("vn ")) {
						nCount++;
						float[] norm = new float[3];
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						norm[0] = Float.parseFloat(tok.nextToken());
						norm[1] = Float.parseFloat(tok.nextToken());
						norm[2] = Float.parseFloat(tok.nextToken());
						m.addNormal(norm);
						file_normal = true;
					}
				}
			}
		}
		catch (Exception ex) {
			System.err.println("Error parsing file:");
			System.err.println(input.getLineNumber()+" : "+line);
		}
		if (!file_normal) {
			m.calculateFaceNormals(coordinate_hand);
			m.calculateVertexNormals();
			m.copyNormals();
		}
		//for (int i=0;i<vertex_normals.size();i++) {
		//m.setVertexNormal(i, vertex_normals.get(i));
		//}
		return new Model(m);
	}
	
	protected static int parseInt(String val) {
		if (val.length() == 0) {
			return -1;
		}
		return Integer.parseInt(val);
	}

	protected static int[] parseIntTriple(String face) {
		int ix = face.indexOf("/");
		if (ix == -1)
			return new int[] {Integer.parseInt(face)-1};
		else {
			int ix2 = face.indexOf("/", ix+1);
			if (ix2 == -1) {
				return new int[] 
				               {Integer.parseInt(face.substring(0,ix))-1,
						Integer.parseInt(face.substring(ix+1))-1};
			}
			else {
				return new int[] 
				               {parseInt(face.substring(0,ix))-1,
						parseInt(face.substring(ix+1,ix2))-1,
						parseInt(face.substring(ix2+1))-1
				               };
			}
		}
	}

}