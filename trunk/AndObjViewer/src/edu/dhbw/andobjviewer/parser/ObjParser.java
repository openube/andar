/**
	Copyright (C) 2010  Tobias Domhan

    This file is part of AndObjViewer.

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
package edu.dhbw.andobjviewer.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import edu.dhbw.andobjviewer.models.Group;
import edu.dhbw.andobjviewer.models.Model;
import edu.dhbw.andobjviewer.util.FileUtil;

/**
 * Simple obj parser.
 * Does not support the full obj specification.
 * It supports:
 * 	- vertices
 *  - vertice normals
 *  - texture coordinates
 *  - basic materials
 *  - faces(faces may not omit the face normal)
 *  - limited texture support, through the map_Kd statement (no options allowed, only image files allowed)
 * @author tobi
 *
 */
public class ObjParser {
	private final int VERTEX_DIMENSIONS = 3;
	private final int TEXTURE_COORD_DIMENSIONS = 2;
	
	private FileUtil fileUtil;
	
	public ObjParser(FileUtil fileUtil) {
		this.fileUtil = fileUtil;
	}
	
	/**
	 * parses an wavefront obj model file
	 * @param modelName name of the model
	 * @param is stream of the file to parse
	 * @return the parsed model
	 * @throws IOException
	 * @throws ParseException
	 */
	public Model parse(String modelName, BufferedReader is) throws IOException, ParseException {
		//global vertices/normals
		Vector<float[]> vertices = new Vector<float[]>();
		Vector<float[]> normals = new Vector<float[]>();
		Vector<float[]> texcoords = new Vector<float[]>();
		
		
		Model model = new Model();
		Group curGroup = new Group();
		MtlParser mtlParser = new MtlParser(model,fileUtil);
		
		String line;
		int lineNum = 1;
		for (line = is.readLine(); 
		line != null; 
		line = is.readLine(), lineNum++)
		{
			line = Util.getCanonicalLine(line);
			if (line.length() > 0) {
				if (line.startsWith("# ")) {
					//ignore comments
				} else if (line.startsWith("v ")) {
					//add new vertex to vector
					String endOfLine = line.substring(2);
					String[] coords = Util.splitBySpace(endOfLine);
					vertices.add(new float[]{
							Float.parseFloat(coords[0]),
							Float.parseFloat(coords[1]),
							Float.parseFloat(coords[2])});
				}
				else if (line.startsWith("vt ")) {
					//add new texture vertex to vector
					String endOfLine = line.substring(3);
					String[] coords = Util.splitBySpace(endOfLine);
					texcoords.add(new float[]{
							Float.parseFloat(coords[0]),
							Float.parseFloat(coords[1])});
				}
				else if (line.startsWith("f ")) {
					//add face to group
					String endOfLine = line.substring(2);
					String[] coords = Util.splitBySpace(endOfLine);
					if(coords.length != 3) {
						throw new ParseException(modelName,
								lineNum, "only triangle faces are supported");
					}
					for (int i = 0; i < 3; i++) {//only triangles supported
						String[] verts = coords[i].split("/");
						int vertexID=0;
						int textureID=-1;
						int normalID=0;
						if(verts.length == 2) {
							//vertex reference
							vertexID = Integer.parseInt(verts[0])-1;
							//normal reference
							normalID = Integer.parseInt(verts[1])-1;
							throw new ParseException(modelName,
									lineNum,
									"vertex normal needed.");
						} else if(verts.length == 3) {
							//vertex reference
							vertexID = Integer.parseInt(verts[0])-1;
							if(!verts[1].equals("")) {
								//might be omitted
								//texture coord reference
								textureID = Integer.parseInt(verts[1])-1;
							}
							//normal reference
							normalID = Integer.parseInt(verts[2])-1;
						} else {
							throw new ParseException(modelName,
									lineNum,
									"a faces needs reference a vertex, a normal vertex and optionally a texture coordinate per vertex.");
						}
						float[] vec;
						try {
							vec = vertices.get(vertexID);
						} catch (ArrayIndexOutOfBoundsException ex) {
							throw new ParseException(modelName,
									lineNum,
									"non existing vertex referenced.");
						}
						if(vec==null)
							throw new ParseException(modelName,
									lineNum,
									"non existing vertex referenced.");
						for (int j = 0; j < VERTEX_DIMENSIONS; j++)
							curGroup.groupVertices.add(vec[j]);
						if(textureID != -1) {
							//in case there is a texture on the face
							try {
								vec = vertices.get(textureID);
							} catch (ArrayIndexOutOfBoundsException ex) {
								throw new ParseException(modelName,
										lineNum,
										"non existing texture coord referenced.");
							}
							if(vec==null)
								throw new ParseException(modelName,
										lineNum,
										"non existing texture coordinate referenced.");
							for (int j = 0; j < TEXTURE_COORD_DIMENSIONS; j++)
								curGroup.groupTexcoords.add(vec[j]);
						}
						try {
							vec = normals.get(normalID);
						} catch (ArrayIndexOutOfBoundsException ex) {
							throw new ParseException(modelName,
									lineNum,
									"non existing normal vertex referenced.");
						}
						if(vec==null)
							throw new ParseException(modelName,
									lineNum,
									"non existing normal vertex referenced.");
						for (int j = 0; j < VERTEX_DIMENSIONS; j++)
							curGroup.groupNormals.add(vec[j]);
					}
				}
				else if (line.startsWith("vn ")) {
					//add new vertex normal to vector
					String endOfLine = line.substring(3);
					String[] coords = Util.splitBySpace(endOfLine);
					normals.add(new float[]{
							Float.parseFloat(coords[0]),
							Float.parseFloat(coords[1]),
							Float.parseFloat(coords[2])});
				} else if (line.startsWith("mtllib ")) {
					//parse material file
					//get  ID of the mtl file
					String filename = line.substring(7);
					String[] files = Util.splitBySpace(filename);
					for (int i = 0; i < files.length; i++) {
						BufferedReader mtlFile = fileUtil.getReaderFromName(files[i]);
						if(mtlFile != null)
							mtlParser.parse(model, mtlFile);
					}					
				} else if(line.startsWith("usemtl ")) {
					//material changed -> new group
					if(curGroup.groupVertices.size()>0) {
						model.addGroup(curGroup);
						curGroup = new Group();
					}
					//the rest of the line contains the name of the new material
					curGroup.setMaterialName(line.substring(7));
				} else if(line.startsWith("g ")) {
					//new group definition
					if(curGroup.groupVertices.size()>0) {
						model.addGroup(curGroup);
						curGroup = new Group();
						//group name will be ignored so far...is there any use?
					}
				}
			}
		}
		if(curGroup.groupVertices.size()>0) {
			model.addGroup(curGroup);
		}
		Iterator<Group> groupIt = model.getGroups().iterator();
		while (groupIt.hasNext()) {
			Group group = (Group) groupIt.next();
			group.setMaterial(model.getMaterial(group.getMaterialName()));
		}
		return model;
	}
}