package saito.objloader;

import java.util.Vector;
import java.nio.*;

import javax.media.opengl.GL;

import processing.core.*;
 

public class ModelSegment 
{
	public Vector elements; 
 
	public String mtlName;
	
	public IntBuffer indexIB;
	
	public FloatBuffer dataFB;
	
	int[] glbuf;
	
	/**
	 * Constructor for the ModelSegment, each Segment holds a Vector of Elements. each element is a collection of indexes to the vert, normal, and UV arrays that make up a single face.
	 */
	public ModelSegment() {
		elements = new Vector();
	}

	public String getMtlname() {
		return mtlName;
	}
	
	public Object getElement(int index){
		return elements.elementAt(index);
	}

	public int getSize() {
		return elements.size();
	}
	
	public int getTotalIndexCount() {
		
		int count = 0;
		
		for(int i = 0; i < getSize(); i++){
			count += ((ModelElement)getElement(i)).getSize();
		}
		
		return count;
	}
	
	
	/* The Plan
	 * each model segment  maintains it's own  data Float buffer. 
	 * Each vertex is organized like this
	 * 
	 * (p.vx, p.vy, p.vz, t.vx, t.vy, n.vx, n.vy, n.vz) * number of elements in the model segment
	 * 
	 * 8 elements to each vertex. (not the class just the 3D model concept)
	 * each vertex is added to the data float buffer in order so that moving from start to finish draws the model
	 * Maximum speed at the cost of memory. 
	 * But the whole point of the OPENGL mode is speed. 
	 * (and the normal process doesn't have the best memory usage anyway)
	 */
	
	public void setupOPENGL(GL gl, Debug debug, Vector p, Vector t, Vector n){
		
		//disclaimer: this is damn ugly. with a little more thought I'm sure I could make this a little prettier.
		int[] vertind = new int[0];
		int[] texind  = new int[0];
		int[] normind = new int[0];
		
		for (int j = 0; j <  getSize(); j ++){
		
			ModelElement tmpf = (ModelElement) (getElement(j));
			
			if(j == 0)
			{
				
				vertind = tmpf.getVertexIndexArray();
				
				texind =  tmpf.getTextureIndexArray();
				
				normind = tmpf.getNormalIndexArray();
				
				
			}
			else
			{	
				vertind = PApplet.concat(vertind, tmpf.getVertexIndexArray());
				
				texind =  PApplet.concat(texind,  tmpf.getTextureIndexArray());
				
				normind = PApplet.concat(normind, tmpf.getNormalIndexArray());
			}
		}
		
		int stride = 8;
		
		float[] f = new float[vertind.length*stride];
		
		debug.println("there are this many floats = " + f.length);
		
		for(int i = 0; i < f.length/stride; i++)
		{
			
			PVector points    = (PVector)p.elementAt(vertind[i]);
			PVector textureUV = (PVector)t.elementAt(texind[i]);
			PVector normals   = (PVector)n.elementAt(normind[i]);
			
			f[i*stride]   = points.x;
			f[i*stride+1] = points.y; // negative to account for processing top left 0,0,0
			f[i*stride+2] = points.z;
			
			f[i*stride+3] = textureUV.x;
			f[i*stride+4] = 1.0f - textureUV.y; // flipped to account for top left
			
			f[i*stride+5] = normals.x;
			f[i*stride+6] = normals.y;
			f[i*stride+7] = normals.z;
						
		}
		
		
		glbuf = new int[2];
		
		gl.glGenBuffers(1,glbuf,0);
		
		dataFB = setupFloatBuffer(f);
		
		bindThisBuffer(gl, glbuf[0], f, dataFB);
	 
		int[] index = new int[f.length / 8];
		
		for(int i = 0; i < index.length; i++)
		{
			index[i]  = i;	
		}
		
		indexIB = setupIntBuffer(index);
		
	}
	
	public void glstart(GL gl){
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, glbuf[0]); 
	
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);  // Enable Vertex Arrays
		gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);  
		gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
		
		// AAARRRGGGG Stride is in BYTES motherf-----
		gl.glVertexPointer(3, GL.GL_FLOAT, 32, 0); 
		
		
		gl.glTexCoordPointer(2, GL.GL_FLOAT, 32, 12);
		
		
		gl.glNormalPointer(GL.GL_FLOAT, 32, 20);   
		
		
		// turn on backface culling
		
		gl.glFrontFace(GL.GL_CCW);
		
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		
//		gl.glPolygonMode(GL.GL_BACK, GL.GL_POINTS);
		
//		gl.glEnable(GL.GL_CULL_FACE);
//		
//		gl.glCullFace(GL.GL_BACK);
		
	
	}
	
	public void drawOPENGL(GL gl, int GLTYPE){
		
		glstart(gl);
		
		//once I get the indexing better I'll use glDrawElements
		//gl.glDrawElements(GLTYPE , indexIB.capacity(), GL.GL_UNSIGNED_INT, indexIB);
		gl.glDrawArrays(GLTYPE , 0, indexIB.capacity());
		
		glend(gl);
	}
	
	public void glend(GL gl){

		gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);

	    gl.glDisableClientState(GL.GL_NORMAL_ARRAY);  

	    gl.glDisableClientState(GL.GL_VERTEX_ARRAY);   
	
	}
	
	private IntBuffer setupIntBuffer(int[] i){
		
		IntBuffer fb = ByteBuffer.allocateDirect(4 * i.length).order(ByteOrder.nativeOrder()).asIntBuffer();
		fb.put(i);
		fb.rewind();
		
		return fb;
		
	}
	
	private FloatBuffer setupFloatBuffer(float[] f){
		
		FloatBuffer fb = ByteBuffer.allocateDirect(4 * f.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
		fb.put(f);
		fb.rewind();
		
		return fb;
		
	}
	
	private void bindThisBuffer(GL gl, int num, float[] f, FloatBuffer FB){
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, num);
		
	    gl.glBufferData(GL.GL_ARRAY_BUFFER, 4 * f.length, FB, GL.GL_STATIC_DRAW); 
		
	}

}
 
