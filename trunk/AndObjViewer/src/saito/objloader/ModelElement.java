package saito.objloader;

/*
 * Alias .obj loader for processing
 * programmed by Tatsuya SAITO / UCLA Design | Media Arts 
 * Created on 2005/04/17
 *
 * 
 *  
 */

import java.util.Vector;

import processing.core.PConstants;

/**
 * @author tatsuyas
 * @author mattDitton
 * 
 * Each model element contains the indexes to the verts, normals and UV's needed to make a face
 */

public class ModelElement implements PConstants{

	public int iType = POLYGON;
	
	public Vector indexes;
	public Vector tindexes;
	public Vector nindexes;
	
	/**
	 * Constructor for the ModelElement. A model element is all the indexes needed to draw a face.
	 */
	public ModelElement() 
	{
		indexes = new Vector();
		tindexes = new Vector();
		nindexes = new Vector();
	}
	
	public int getSize(){
		return indexes.size();
	}
	
	public int[] getVertexIndexArray(){
		
		int[] v = new int[getSize()];
		
		for(int i = 0; i < v.length; i ++){
			v[i] = getVertexIndex(i);
		}
		                  
		return v;
	}
	
	public int[] getNormalIndexArray(){
		
		int[] v = new int[getSize()];
		
		for(int i = 0; i < v.length; i ++){
			v[i] = getNormalIndex(i);
		}
		                  
		return v;
	}
	
	public int[] getTextureIndexArray(){
		
		int[] v = new int[getSize()];
		
		for(int i = 0; i < v.length; i ++){
			v[i] = getTextureIndex(i);
		}
		                  
		return v;
	}
	
	
	//remember kids arrays start at 0 (hence the -1) But OBJ files start the indexes at 1.
	public int getVertexIndex(int i){
		return ((Integer)indexes.elementAt(i)).intValue() -1;
	}
	public int getTextureIndex(int i){
		return ((Integer)tindexes.elementAt(i)).intValue() -1;		
	}
	public int getNormalIndex(int i){
		return ((Integer)nindexes.elementAt(i)).intValue() -1;
	}

}
