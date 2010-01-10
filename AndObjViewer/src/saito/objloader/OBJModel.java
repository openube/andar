package saito.objloader;
/*
 * Alias .obj loader for processing
 * programmed by Tatsuya SAITO / UCLA Design | Media Arts 
 * Created on 2005/04/17
 */


import processing.core.*;
import processing.opengl.*;

import java.io.BufferedReader;  
import java.util.Hashtable;
import java.util.Vector;
//import java.nio.*;

import javax.media.opengl.*;

/**
 * 
 * @author tatsuyas
 * @author mditton
 * 
 * @TODO: Add documentation and examples to the google code repository
 * @TODO: Add java doc commenting
 * @TODO: Add vertex normals and face normals from Collada Loader
 * 
 * google code address (because I always forget)
 * http://code.google.com/p/saitoobjloader/
 * 
 */


public class OBJModel{

	// global variables
	Vector vertexes; // vertexes

	Vector texturev; // texture coordinates

	Vector normv;    // normals

	Vector modelSegments; 

	Hashtable materials;
	
	Hashtable groups;

	String objName = "default";

	String defaultMaterialName = "default";

	Group defaultGroup = new Group("default");

	ModelSegment defaultModelSegment = new ModelSegment();

	// processing variables
	PApplet parent;

	PImage texture; // texture image applied from the code.

	// runtime rendering variables
	int mode = PConstants.TRIANGLES; // render mode (ex. POLYGON, POINTS ..)

	private boolean flagTexture = true;

	private boolean flagMaterial = true;

	private boolean flagLocalTexture = false;

	public Debug debug;

	String originalTexture;
	
	GL gl;

	// -------------------------------------------------------------------------
	// ------------------------------------------------------------ Constructors
	// -------------------------------------------------------------------------
	/**
	 * Class Constructor to setup an empty obj model
	 */
	public OBJModel(PApplet parent) {
		
		setup(parent);

	}
	
	//extra constructor because I got sick of having two lines to create and load the model. - MD
	/**
	 * Class Constructor, loads the string as an obj from the data directory
	 */
	public OBJModel(PApplet parent, String filename) {
		
		setup(parent);
		
		load(filename);
		
	}
	
	/**
	 * Class Constructor, loads the string as an obj from the data directory.<br></br>
	 * The boolean decides if local paths should be used when loading the mtl and textures in the mtl.<br></br>
	 */
	public OBJModel(PApplet parent, String filename, boolean localpaths) {
		
		setup(parent);
		
		flagLocalTexture = localpaths;
		
		load(filename);
		
	}
	
	/**
	 * Class Constructor, loads the string as an obj from the data directory. <br></br>
	 * The boolean decides if local paths should be used when loading the mtl and textures in the mtl.<br></br>
	 * The int sets the draw mode, to the processing draw mode, eg. TRIANGLES, POINTS, POLYGON, LINES, TRIANGLE_STRIP, QUAD_STRIP, QUADS.<br></br>
	 */
	public OBJModel(PApplet parent, String filename, boolean localpaths, int _mode) {
		
		setup(parent);
		
		flagLocalTexture = localpaths;
		
		drawMode(_mode);
		
		load(filename);
		
	}
	
	// -------------------------------------------------------------------------
	// ------------------------------------------------------------------- Setup
	// -------------------------------------------------------------------------
	private void setup(PApplet parent){
		
		this.parent = parent;

		parent.registerDispose(this);

		vertexes = new Vector();

		texturev = new Vector();

		normv = new Vector();

		groups = new Hashtable();

		modelSegments = new Vector();

		materials = new Hashtable();

		debug = new Debug(parent);

		debug.enabled = false;
		
		mode = PConstants.TRIANGLES;
		
	}
	
	/**
	 * Called after loading the obj model. <br></br>
	 * This will setup the Vertex buffer objects ready for the drawOPENGL method.<br></br>
	 * The obj file must be loaded for this method to work. <br></br>
	 */

	public void setupOPENGL(){
		
		if(!(parent.g instanceof PGraphicsOpenGL))
		{
			
			throw new RuntimeException("This feature requires OpenGL");
			// I mean seriously, the function is called setupOPENGL!!!
		}
		
		gl = ((PGraphicsOpenGL) parent.g).gl;
		
		debug.println("Setting up OPENGL buffers");
		
		debug.println("Generating Buffers");
			    
		debug.println("number of model segments = " + modelSegments.size());
		
		ModelSegment tmpModelSegment;
		
		Material mtl;
		
		for (int i = 0; i < modelSegments.size(); i ++){
			
			tmpModelSegment = (ModelSegment) modelSegments.elementAt(i);
			
			if(tmpModelSegment.getSize() != 0){ // Why are there empty model segments? KAHN!!!  - MD
				
				debug.println("number of model elements = "    + tmpModelSegment.getSize());
				debug.println("model element uses this mtl = " + tmpModelSegment.getMtlname());
				
				tmpModelSegment.setupOPENGL(gl, debug , vertexes, texturev, normv);
				
				mtl = (Material) materials.get(tmpModelSegment.mtlName);
				
				mtl.setupOPENGL(gl, debug);
				
			}
		}
	    
	    debug.println("leaving setup function");
	}
	
	
	/**
	 * Draws the obj model using the Vertex Buffers that were made in the setupOPENGL method<br></br>
	 */
	public void drawOPENGL()
	{

		boolean fill = parent.g.fill;
		boolean stroke = parent.g.stroke;
		
		parent.fill(255);
		parent.stroke(255);
		parent.noFill();
		parent.noStroke();
		
		gl=((PGraphicsOpenGL)parent.g).beginGL();
		
		saveGLState();
		
	    ModelSegment tmpModelSegment;
	    Material mtl;
	    
	    for (int i = 0; i < modelSegments.size(); i ++){
			
			tmpModelSegment = (ModelSegment) modelSegments.elementAt(i);
			
			if(tmpModelSegment.getSize() != 0){ //again with the empty model segments WTF?

				mtl = (Material) materials.get(tmpModelSegment.mtlName);
				
				mtl.useOPENGLStart(gl, flagMaterial, flagTexture);
	
			    switch(mode){
		    
				    case(PConstants.POINTS):
			    		tmpModelSegment.drawOPENGL(gl, GL.GL_POINTS);
				    break;
				    
				    case(PConstants.LINES):
				    	tmpModelSegment.drawOPENGL(gl, GL.GL_LINES);
				    break;
				    
				    case(PConstants.TRIANGLES):
				    	tmpModelSegment.drawOPENGL(gl, GL.GL_TRIANGLES);
				    break;
				    
				    case(PConstants.TRIANGLE_STRIP):
				    	tmpModelSegment.drawOPENGL(gl, GL.GL_TRIANGLE_STRIP);
				    break;
				    
				    case(PConstants.QUADS):
				    	tmpModelSegment.drawOPENGL(gl, GL.GL_QUADS);
				    break;
				    
				    case(PConstants.QUAD_STRIP):
				    	tmpModelSegment.drawOPENGL(gl, GL.GL_QUAD_STRIP);
				    break;
				    
				    case(PConstants.POLYGON):
				    	tmpModelSegment.drawOPENGL(gl, GL.GL_POLYGON);
				    break;
			    	
			    }

		    	mtl.useOPENGLFinish(gl, flagMaterial, flagTexture);	    

			}
		}    
	    
	    restoreGLState();
	    
		((PGraphicsOpenGL)parent.g).endGL();
		
		parent.g.fill = fill;
		parent.g.stroke = stroke;
	}
	
	/**
	 * Called at the start of drawOPENGL.<br></br>
	 * This saves the current state ready so it doesn't get hammered from the objModel.<br></br> 
	 */
	
	private void saveGLState()
    {
		
        gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);

		gl.glMatrixMode(GL.GL_MODELVIEW);

        gl.glPushMatrix();

        gl.glMatrixMode(GL.GL_PROJECTION);

        gl.glPushMatrix();

    }      

   /**
    * Returns back to original opengl state that Processing was in.<br></br>
    */
	private void restoreGLState()
    {

        gl.glMatrixMode(GL.GL_PROJECTION);

        gl.glPopMatrix();

        gl.glMatrixMode(GL.GL_MODELVIEW);

        gl.glPopMatrix();

        gl.glPopAttrib();

    }
	
	
	// -------------------------------------------------------------------------
	// ------------------------------------------------------------------- Utils
	// -------------------------------------------------------------------------
	
	/**
	 * A Debug method that prints information about the loaded model<br></br>
	 * This method only prints information if the debugMode is true.<br></br>
	 * V Size  = The number of vertex positions<br></br>
	 * Vt Size = The number of UV positions<br></br>
	 * Vn Size = The number of normals <br></br>
	 * G Size = the number of Groups in the model <br></br>
	 * S Size = the number of segments in the model, this should directly equate to the number of unique materials in the mtl file<br></br>
	 */
	public void showModelInfo() {

		debug.println("Obj Name: \t\t" + objName);
		debug.println("");
		debug.println("V  Size: \t\t" + vertexes.size());
		debug.println("Vt Size: \t\t" + texturev.size());
		debug.println("Vn Size: \t\t" + normv.size());
		debug.println("G  Size: \t\t" + groups.size());
		debug.println("S  Size: \t\t" + getSegmentSize());
		debug.println("");
	}
	
	/**
	 * Enables the debug mode.<br></br>
	 * Prints version and contact information to the console.<br></br>
	 */
	public void debugMode() {
		debug.enabled = true;
		debug.println("");
		debug.println("objloader version 018");
		debug.println("23 July 2009");
		debug.println("http://code.google.com/p/saitoobjloader/");		
		debug.println("");
		
	}
	
	/**
	 * Clears all Vectors ready for loading in a new model.<br></br>
	 * Doing something like this<br></br> 
	 * <br></br>
	 *  OBJModel model = new OBJModel(this, "myobj.obj");<br></br>
	 * // do something with model<br></br>
	 * model.clear();<br></br>
	 * model.load("myobj.obj");<br></br>
	 * <br></br>
	 * is vastly better for memory use than doing something like this<br></br>
	 * <br></br>
	 * OBJModel model = new OBJModel(this, "myobj.obj");<br></br>
	 * // do something with model<br></br>
	 * model = new OBJModel(this, "myOtherObj.obj");<br></br>
	 * <br></br>
	 * The second example is really bad because the original model is still in memory but nothing is pointing to it.<br></br> 
	 * We would have to wait for the Garbage Collector to do the clean up before freeing the memory. <br></br>
	 * If loading in lots of stuff is important then using the model.clear() could help a lot.<br></br>
	 */
	public void clear() {
		vertexes.clear();
		texturev.clear();
		normv.clear();
		groups.clear();
		modelSegments.clear();
		materials.clear();
		debug.println("OBJModel is empty");
		
	}
	
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------	
	// ------------------------------------------------------------ Load Toggles
	// ---------------------------------------------- called before load(String)
	// ---------------- Can be used in the case of the mtl having absolute paths 
	// ------------------------------------------------------ -----Nice work XSI
	// ------ If this is true the texture will be loaded from the data directory
	
	/**
	 * This is an option if your obj exporter includes hard coded paths to images listed in the mtl file.<br></br>
	 * This option must be set before loading mtl files.<br></br>
	 */
	
	public void enableLocalTexture() {
		flagLocalTexture = true;
		debug.println("local tex:\t\ton");
	}
	
	/**
	 * Turns off the local texture loading. <br></br>
	 * This will use the full path in the mtl file for loading the tga.<br></br>
	 */
	public void disableLocalTexture() {
		flagLocalTexture = false;
		debug.println("local tex:\t\toff");
	}

	
	// -------------------------------------------------------------------------
	// ----------------------------------------------------------- Draw Booleans
	// -------------------------------------------------------------------------

	/**
	 * Turns off the use of the textures in mtl file. <br></br>
	 * Only the ambient, diffuse and specular values will be used. <br></br> 
	 */
	public void disableTexture() {
		flagTexture = false;
		debug.println("texture:\t\toff");
	}
	
	/**
	 * Turns on the use of textures that are listed in the mtl file<br></br>
	 */
	public void enableTexture() {
		flagTexture = true;
		debug.println("texture:\t\ton");
	}

	/**
	 * Disables the material completely.<br></br>
	 * With this on you can set the appearance of the model in processing before calling model.draw();<br></br>
	 * <br></br>
	 * background(32);<br></br>
	 * stroke(255);<br></br>
	 * noFill();<br></br>
	 * model.draw();<br></br>
	 * 
	 */
	public void disableMaterial() {
		flagMaterial = false;
		debug.println("material:\t\toff");
	}
	
	/**
	 * Turns back on the use of the material that came from the mtl file<br></br>
	 */
	public void enableMaterial() {
		flagMaterial = true;
		debug.println("material:\t\ton");
	}

	/**
	 * Sets an override texture for the drawing of the model.<br></br>
	 * Any PImage supplied will be used over all model segments<br></br>
	 * @param PImage<br></br>
	 * <br></br>
	 * NOTE: I think this method will be changing in the future to allow for more direct control over each modelSegment<br></br>
	 */
	public void texture(PImage tex) {

		texture = tex;
		debug.println("Using new texture");
	}
	
	/**
	 * Set's the beginShape mode for drawing the model. <br></br>
	 * This will vary depending on the model and the export settings.<br></br> 
	 * A safe bet is to triangulate the model before exporting and set the drawmode to TRANGLES.<br></br>
	 * Also due to inconsistencies in OPENGL points, the POINTS mode may draw nothing in OPENGL.<br></br>
	 * A common misconception is that LINES will result in a wireframe. For this effect you should leave the drawmode as the correct mode and disable the material and use sroke() to get a wireframe<br></br>
	 * @param TRIANGLES, POINTS, POLYGON, LINES, TRIANGLE_STRIP, QUAD_STRIP, QUADS<br></br>
	 */
	public void drawMode(int mode) {
		this.mode = mode;
		
		switch(mode){
			case(PConstants.POINTS):
				debug.println("draw mode:\t\tPOINTS");
				break;
			
			case(PConstants.LINES):
				debug.println("draw mode:\t\tLINES");
				break;
				
			case(PConstants.POLYGON):
				debug.println("draw mode:\t\tPOLYGON");
				break;
				
			case(PConstants.TRIANGLES):
				debug.println("draw mode:\t\tTRIANGLES");
				break;
				
			case(PConstants.TRIANGLE_STRIP):
				debug.println("draw mode:\t\tTRIANGLE_STRIP");
				break;
				
			case(PConstants.QUADS):
				debug.println("draw mode:\t\tQUADS");
				break;
				
			case(PConstants.QUAD_STRIP):
				debug.println("draw mode:\t\t");
				break;
		}
	}
	
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------- Draw
	// -------------------------------------------------------------------------
	/**
	 * The draw method of the OBJModel.<br></br> 
	 * This method used the standard Processing system of beginShape, endShape to draw the model.<br></br>
	 */
	
	public void draw() 
	{
		
		drawModel();
		
	}

	private void drawModel() {
		
		try {
			PVector v = null, vt = null, vn = null;

//			int vtidx = 0, vnidx = 0, vidx = 0;

			Material tmpMaterial = null;

			boolean useTexture;
			
			ModelSegment tmpModelSegment;
			
			ModelElement tmpModelElement;
			
			// render all triangles
			for (int s = 0; s < getSegmentSize(); s++) {

				useTexture = true;

				tmpModelSegment = (ModelSegment) modelSegments.elementAt(s);

				tmpMaterial = (Material) materials.get(tmpModelSegment.mtlName);
				
				// if the material is not assigned for some
				// reason, it uses the default material setting
				if (tmpMaterial == null) 
				{
					tmpMaterial = (Material) materials.get(defaultMaterialName);

					debug.println("Material '" + tmpModelSegment.mtlName + "' not defined");
				}

				if (flagMaterial) 
				{	
					
					parent.ambient(  255.0f * tmpMaterial.Ka[0], 255.0f * tmpMaterial.Ka[1], 255.0f * tmpMaterial.Ka[2]);
					parent.specular( 255.0f * tmpMaterial.Ks[0], 255.0f * tmpMaterial.Ks[1], 255.0f * tmpMaterial.Ks[2]);
					parent.fill(     255.0f * tmpMaterial.Kd[0], 255.0f * tmpMaterial.Kd[1], 255.0f * tmpMaterial.Kd[2], 255.0f * tmpMaterial.d);
					
				}

				for (int f = 0; f < tmpModelSegment.getSize(); f++) 
				{
					tmpModelElement = (ModelElement) (tmpModelSegment.getElement(f));

					if (tmpModelElement.getSize() > 0) 
					{
						
						parent.textureMode(PApplet.NORMAL);
	
						parent.beginShape(mode); // specify render mode
	
						if (flagTexture == false || tmpMaterial.map_Kd == null)
						{	
							useTexture = false;
						}
	
						if (useTexture)
						{
							if (texture != null)
							{
								parent.texture(texture); // setting applied texture
							}
							else
							{
								parent.texture(tmpMaterial.map_Kd); // setting texture from mtl info
							}							
						}
					

						for (int fp = 0; fp < tmpModelElement.getSize(); fp++) 
						{
//							vidx = tmpModelElement.getVertexIndex(fp);
//
//							v = (PVector) vertexes.elementAt(vidx);

							v = (PVector) vertexes.elementAt(tmpModelElement.getVertexIndex(fp));
							
							if (v != null) 
							{
								try 
								{

									if (tmpModelElement.nindexes.size() > 0) {

//										vnidx = tmpModelElement.getNormalIndex(fp);
//
//										vn = (PVector) normv.elementAt(vnidx);

										vn = (PVector) normv.elementAt( tmpModelElement.getNormalIndex(fp) );										
										
										parent.normal(vn.x, vn.y, vn.z);
									}

									if (useTexture) 
									{
//										vtidx = tmpModelElement.getTextureIndex(fp);
//										
//										vt = (PVector) texturev.elementAt(vtidx);

										vt = (PVector) texturev.elementAt( tmpModelElement.getTextureIndex(fp) );										
										
										parent.vertex(v.x, v.y, v.z, vt.x, 1.0f- (vt.y));

									} 
									else
									{
										parent.vertex(v.x, v.y, v.z);
									}

								} 
								catch (Exception e) 
								{
									e.printStackTrace();
								}
							}

							else 
							{	
								parent.vertex(v.x, v.y, v.z);
							}

						}
					
						parent.endShape();
						
						parent.textureMode(PApplet.IMAGE);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}


	// -------------------------------------------------------------------------
	// ------------------------------------------------------------- Obj Loading 
	// -------------------------------------------------------------------------
	
	/**
	 * The manual load method for obj files. This method is automaticly called when using Constructors that include the file name<br></br>
	 * The method uses the Processing createReader() function to get a BufferedReader object in order to read the file one line at a time.<br></br>
	 * This is slightly better method than loadStrings() as it's less memory intensive for large obj files.  <br></br>
	 */
	public void load(String filename) 
	{	
		parseOBJ(getBufferedReader(filename));

		if (debug.enabled) 
		{
			this.showModelInfo();
		}
	}
	
	/**
	 * Used in the loading of obj files and mtl files that come from mtl files.<br></br>
	 * @param The filename. A String containing the location of the obj file. The createReader function should take care of finding the file in all the usual Processing places.<br></br>
	 * @return a BufferedReader<br></br>
	 */
	private BufferedReader getBufferedReader(String filename) 
	{	
		debug.println("Loading this file = " + filename);
		
		BufferedReader retval = parent.createReader(filename);
		
		if(retval != null)
		{
			return retval;
		}
		else
		{
			PApplet.println("Could not find this file " + filename);
			return null;
		}

	}
	
	// -------------------------------------------------------------------------
	// ------------------------------------------------------------- Obj Parsing 
	// -------------------------------------------------------------------------
	
	/**
	 * The method that does all the grunt work in reading and processing the obj file.<br></br>
	 */
	private void parseOBJ(BufferedReader bread) 
	{
		try 
		{
			// adding default variables to the global data table
			// creating the default group

			groups.put("default", defaultGroup);

			Group currentGroup = defaultGroup;

			// creating the default material

			Material defaultMaterial = new Material();

			defaultMaterial.mtlName = defaultMaterialName;

			materials.put(defaultMaterialName, defaultMaterial);

			String currentMaterial = defaultMaterialName;

			// creating the default model segment

			modelSegments.add(defaultModelSegment);

			defaultModelSegment.mtlName = currentMaterial;

			currentGroup.segments.add(defaultModelSegment);

			ModelSegment currentModelSegment = defaultModelSegment;

			String line;

			while ((line = bread.readLine()) != null) 
			{
				// debug.println(line);
				// parse the line
				
				// The below patch/hack comes from Carles Tom‡s Mart’ and is a fix for single backslashes in Rhino obj files
				
				// BEGINNING OF RHINO OBJ FILES HACK
	            // Statements can be broken in multiple lines using '\' at the end of a line.
	            // In regular expressions, the backslash is also an escape character. 
	            // The regular expression \\ matches a single backslash. This regular expression as a Java string, becomes "\\\\". 
	            // That's right: 4 backslashes to match a single one.
	            while(line.contains("\\")) 
	            {
	                line = line.split("\\\\")[0];
	                final String s = bread.readLine();
	                if(s != null) line += s;
	            }
	            // END OF RHINO OBJ FILES HACK

				String[] elements = line.split("\\s+");

				// if not a blank line, process the line.
				if (elements.length > 0) 
				{

					// analyze the format
					if (elements[0].equals("v")) // point 
					{ 
						PVector tmpv = new PVector();
						tmpv.x = Float.valueOf(elements[1]).floatValue();
						tmpv.y = Float.valueOf(elements[2]).floatValue();
						tmpv.z = Float.valueOf(elements[3]).floatValue();
						tmpv.y = -tmpv.y; 
						vertexes.add(tmpv);
					} 
					else if (elements[0].equals("vn")) // normal
					{ 
						PVector tmpv = new PVector();
						tmpv.x = Float.valueOf(elements[1]).floatValue();
						tmpv.y = Float.valueOf(elements[2]).floatValue();
						tmpv.z = Float.valueOf(elements[3]).floatValue();
						normv.add(tmpv);
					} 
					else if (elements[0].equals("vt")) // uv 
					{
						PVector tmpv = new PVector();
						tmpv.x = Float.valueOf(elements[1]).floatValue();
						tmpv.y = Float.valueOf(elements[2]).floatValue();
						texturev.add(tmpv);
					} 
					else if (elements[0].equals("o")) 
					{
						if (elements[1] != null)
							objName = elements[1];
					} 
					else if (elements[0].equals("mtllib")) 
					{
						if (elements[1] != null)
							this.parseMTL(this.getBufferedReader(elements[1]));
					}

					// elements that needs to consider the current context

					else if (elements[0].equals("g")) { // grouping
						// setting
						ModelSegment newModelSegment = new ModelSegment();

						modelSegments.add(newModelSegment);
						
						currentModelSegment = newModelSegment;

						currentModelSegment.mtlName = currentMaterial;

						for (int e = 1; e < elements.length; e++) {
							
							if (groups.get(elements[e]) == null) {
								
								// debug.println("group '" + elements[e] +"'");
								Group newGroup = new Group(elements[e]);
								
								groups.put(elements[e], newGroup);
								
							}
						}
					} 
					
					else if (elements[0].equals("usemtl")) 
					{ 
						// material setting
						
						ModelSegment newModelSegment = new ModelSegment();
						
						modelSegments.add(newModelSegment);
						
						currentModelSegment = newModelSegment;
						
						currentModelSegment.mtlName = elements[1];

					} 
					
					else if (elements[0].equals("f")) // Element 
					{ 
						
						ModelElement tmpf = new ModelElement();

						if (elements.length < 3) {
							
							debug.println("Warning: potential model data error");
							
						}

						for (int i = 1; i < elements.length; i++) {
							
							String seg = elements[i];
							
							if (seg.indexOf("/") > 0) {
								
								String[] forder = seg.split("/");

								if (forder.length > 2) 
								{
									
									if (forder[0].length() > 0)
									{
										tmpf.indexes.add(Integer.valueOf(forder[0]));
									}
									
									if (forder[1].length() > 0)
									{
										tmpf.tindexes.add(Integer.valueOf(forder[1]));
									}
									
									if (forder[2].length() > 0)
									{	
										tmpf.nindexes.add(Integer.valueOf(forder[2]));
									}
									
								} 
								else if (forder.length > 1) 
								{
									
									if (forder[0].length() > 0)
									{	
										tmpf.indexes.add(Integer.valueOf(forder[0]));
									}
									
									if (forder[1].length() > 0)
									{
										tmpf.tindexes.add(Integer.valueOf(forder[1]));
									}
									
								} 
								else if (forder.length > 0) 
								{
									
									if (forder[0].length() > 0)
									{
										tmpf.indexes.add(Integer.valueOf(forder[0]));
									}
									
								}
							} 
							else 
							{
								
								if (seg.length() > 0)
								{
									tmpf.indexes.add(Integer.valueOf(seg));
								}
							}
						}
						
						currentModelSegment.elements.add(tmpf);
						
					} 
					else if (elements[0].equals("ll")) { // line
						
						ModelElement tmpf = new ModelElement();
						
						tmpf.iType = PConstants.POLYGON;

						if (elements.length < 2) 
						{	
							debug.println("Warning: potential model data error");	
						}

						for (int i = 1; i < elements.length; i++) 
						{
							tmpf.indexes.add(Integer.valueOf(elements[i]));	
						}

						currentModelSegment.elements.add(tmpf);

					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();	
		}

		// Depending on the layout of the obj file, extra empty modeSegments can be created.
		// Here I check each segment to ensure it contains indexes. 
		// If there aren't any then the Segment will draw no faces so it's fine to kill it off.
		for(int i = getSegmentSize()-1; i >= 0; i--)
		{
			
			if(getIndexCountInSegment(i) == 0 )
			{ //again with the empty model segments WTF?
				
				modelSegments.remove(i);
			}
		}
		
	}

	// -------------------------------------------------------------------------
	// ------------------------------------------------------------- MTL Parsing 
	// -------------------------------------------------------------------------
	
	/**
	 * The method that does the work of parsing the mtl file. <br></br>
	 * This method is called automaticly when an mtl reference is found in obj. <br></br>
	 * This can cause issues when the mtl file location is a hard coded path.<br></br> 
	 * I'll try to fix this up in the future by turning that into an option.<br></br>
	 */
	
	private void parseMTL(BufferedReader bread) {
		try {
			
			String line;
			
			Material currentMtl = null;

			while ((line = bread.readLine()) != null) {
				
				// parse the line
				
				line = line.trim();
				
				String elements[] = line.split("\\s+");
				
				if (elements.length > 0) {
					// analyze the format

					if (elements[0].equals("newmtl")) {
						
						debug.println("material: \t\t'" + elements[1] + "'");
						
						String mtlName = elements[1];
						
						Material tmpMtl = new Material();
						
						currentMtl = tmpMtl;
						
						materials.put(mtlName, tmpMtl);
						
					}
					// I'd like to do something with this but at the moment it'd only be supported in the OPENGL mode
//					else if (elements[0].equals("map_Ka") && elements.length > 1) {
//
//						debug.println("texture ambient \t\t'" + elements[1] + "'");
//
//						// String texname = elements[1];
//						// currentMtl.map_Ka = parent.loadImage(texname);
//						
//
//					}
					else if (elements[0].equals("map_Kd") && elements.length > 1) {

						if (!flagLocalTexture) {
							debug.println("texture diffuse \t\t'" + elements[1] + "'");
						}

						String texname = elements[1];

						if (flagLocalTexture) {

							int p1 = 0;
							
							// TODO get the system folder slash. (where is that pocket java guide when you need it) 
							String slash = "\\";
							
							while (p1 != -1) 
							{	
								p1 = texname.indexOf(slash);
								texname = texname.substring(p1 + 1);
							}
							
							debug.println("diffuse: \t\t'" + texname + "'");

						}

						currentMtl.map_Kd = parent.loadImage(texname);
						
						originalTexture = texname;

					} 
					else if (elements[0].equals("Ka") && elements.length > 1) 
					{	
						currentMtl.Ka[0] = Float.valueOf(elements[1]).floatValue();
						currentMtl.Ka[1] = Float.valueOf(elements[2]).floatValue();
						currentMtl.Ka[2] = Float.valueOf(elements[3]).floatValue();
					}
					else if (elements[0].equals("Kd") && elements.length > 1) 
					{
						currentMtl.Kd[0] = Float.valueOf(elements[1]).floatValue();
						currentMtl.Kd[1] = Float.valueOf(elements[2]).floatValue();
						currentMtl.Kd[2] = Float.valueOf(elements[3]).floatValue();
					}
					else if (elements[0].equals("Ks") && elements.length > 1) 
					{
						currentMtl.Ks[0] = Float.valueOf(elements[1]).floatValue();
						currentMtl.Ks[1] = Float.valueOf(elements[2]).floatValue();
						currentMtl.Ks[2] = Float.valueOf(elements[3]).floatValue();
					}
					else if (elements[0].equals("d") && elements.length > 1) 
					{	
						currentMtl.d = Float.valueOf(elements[1]).floatValue();
						
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// -------------------------------------------------------------------------
	// --------------------------------------------------- Get and Set Functions 
	// -------------------------------------------------------------------------
	
	/**
	 * Gets the size of the Groups from the obj Model. <br></br>
	 * At the moment groups are an unexplored feature.<br></br>
	 * So you can get the size, but it's not going to do much for you.<br></br>
	 * @return int count of the group<br></br>
	 */
	public int getGroupsize() 
	{
		return this.groups.size();
	}
	
	/**
	 * Returns the group via name <br></br>
	 * Until I find a practical use for Groups this feature isn't going anywhere. <br></br>
	 * @param A String of the group name that was in the obj file <br></br> 
	 * @return a Group <br></br>
	 */
	public Group getGroup(String groupName) 
	{
		return (Group) this.groups.get(groupName);
	}
	
	/**
	 * Gets the number of segments in the model.<br></br>
	 * A segment is a unique material and an array of indexes into the vert, norm and uv Vectors<br></br>
	 * @return int
	 */
	public int getSegmentSize() 
	{
		return this.modelSegments.size();
	}
	
	/**
	 * Gets the total number of faces in the model.<br></br>
	 * This is the total of the index count across all segments<br></br>
	 * This is mostly used when you need raw verts for physics simulation<br></br>
	 * @return int
	 */
	public int getTotalFaceCount() 
	{
		
		int tmp = 0;
		
		for(int i = 0; i < getSegmentSize(); i ++)
		{
			tmp += getIndexCountInSegment(i);
		}
		
		return tmp;
	}
	
	/**
	 * Gets an array of PVectors that make up the position co-ordinates of the face.<br></br>
	 * This method needs one int that must be between 0 and the getTotalFaceCount()<br></br>
	 * This is mostly used when you need raw verts for physics simulation<br></br>
	 * @return PVector[]
	 */
	public PVector[] getFaceVertArray(int faceNumber) 
	{
		
		int segmentNumber = 0;
		
		int indexNumber = faceNumber;
		
		//debug.println("segmentNumber, indexNumber = " + segmentNumber + " " + indexNumber);
		
		while (indexNumber >= getIndexCountInSegment(segmentNumber))
		{
			indexNumber -= getIndexCountInSegment(segmentNumber);
			segmentNumber++;
		}
		
		//debug.println("segmentNumber, indexNumber = " + segmentNumber + " " + indexNumber);
		
		int[] vertindexes = getVertIndexArrayInSegment(segmentNumber, indexNumber);
		
		//parent.println(vertindexes);
		
		PVector[] tmp = new PVector[vertindexes.length];
		
		for(int i = 0; i < tmp.length; i ++)
		{
			tmp[i] = new PVector();
			tmp[i].set(getVertex(vertindexes[i]));
		}
		
		return tmp;
	}
	
	/**
	 * Gets the number of Index count in the Segment. <br></br>
	 * In effect this is the number of faces in the Segment. <br></br>
	 * As each Index is an Array of ints to the vert, normal, or uv Array <br></br>
	 * @param a number between 0 and the number of segments <br></br>
	 * @return int <br></br>
	 */
	public int getIndexCountInSegment(int i)
	{
		return  ((ModelSegment)modelSegments.elementAt(i)).getSize();
	}
	
	// there are just to many casts here. It feels very muddy.
	/**
	 * Returns an array of ints. Use these ints to get the verts of a single face.<br></br>
	 * @param the segment number<br></br>
	 * @param the face number<br></br>
	 * @return int[] of indexes<br></br>
	 */
	public int[] getVertIndexArrayInSegment(int i, int num)
	{
		return ((ModelElement)((ModelSegment)modelSegments.elementAt(i)).getElement(num)).getVertexIndexArray();
	}
	
	/**
	 * Returns an array of ints. Use these ints to get the normals of a single face.<br></br>
	 * @param the segment number<br></br>
	 * @param the face number<br></br>
	 * @return int[] of indexes<br></br>
	 */
	public int[] getNormalIndexArrayInSegment(int i, int num)
	{
		return ((ModelElement)((ModelSegment)modelSegments.elementAt(i)).getElement(num)).getNormalIndexArray();
	}
	
	/**
	 * Returns an array of ints. Use these ints to get the UV's of a single face.<br></br>
	 * @param the segment number<br></br>
	 * @param the face number<br></br>
	 * @return int[] of indexes<br></br>
	 */
	public int[] getTextureIndexArrayInSegment(int i, int num)
	{
		return ((ModelElement)((ModelSegment)modelSegments.elementAt(i)).getElement(num)).getTextureIndexArray();
	}

	/**
	 * Get's the total number of Verts in the model.<br></br> 
	 * @return an int of the number of verts<br></br>
	 */
	public int getVertexsize() 
	{
		return this.vertexes.size();
	}

	/**
	 * Get's the total number of Verts in the model.<br></br> 
	 * @return an int of the number of verts<br></br>
	 */
	public int getVertexSize() 
	{
		return this.vertexes.size();
	}

	/**
	 * Get's the total number of Normals in the model. <br></br>
	 * It can happen that the Normal count is identical to the Vert count. This will depend on the effecency of the exporter that has been used.<br></br>
	 * In a situation where the count is identical often there is a relationship between a certain numbered Normal and the same numbered Vert.<br></br>
	 * However this can also be total luck. The correct method of getting the normal for the correct vert is to go through the ModelSegment to ModelElement to VertIndex and NormalIndex.<br></br>  
	 * @return an int of the number of normals<br></br>
	 */
	public int getNormalSize() 
	{
		return this.normv.size();
	}
	
	/**
	 * Get's the total number of UVs in the model.<br></br> 
	 * @return an int of the number of UV's<br></br>
	 */
	public int getUVSize() 
	{
		return this.texturev.size();
	}	

	/**
	 * Returns a reference to a numbered Vertex. As this is a reference to the original vertex you can directly manipulate the PVector without having to set it back.<br></br>
	 * PVector tmp = model.getVertex(0);<br></br>
	 * tmp.x += 10;<br></br>
	 * @param an index to the vert<br></br>
	 * @return a PVector<br></br>
	 */
	public PVector getVertex(int i) 
	{
		return (PVector) vertexes.elementAt(i);
	}
	
	/**
	 * Returns a reference to a numbered Normal. As this is a reference to the original normal you can directly manipulate the PVector without having to set it back.<br></br>
	 * PVector tmp = model.getNormal(0);<br></br>
	 * tmp.mult(-1);<br></br>
	 * @param an index to the normal<br></br>
	 * @return a PVector<br></br>
	 */	
	public PVector getNormal(int i) 
	{
		return (PVector) normv.elementAt(i);
	}	
	
	/**
	 * Returns a reference to a numbered Textured Coordinate. As this is a reference to the original UV you can directly manipulate the PVector without having to set it back.<br></br>
	 * It is important to note that the UV's of an obj model are in a NORMALIZED space (between 0-1).<br></br>
	 * Another important issue is that the native processing renderer does not tile textures that are outside 0-1<br></br>
	 * This can have the effect of streaking pixel lines at the edges of the texture.<br></br> 
	 * PVector tmp = model.getUV(0);<br></br>
	 * tmp.x += 0.01;<br></br>
	 * @param an index to the normal<br></br>
	 * @return a PVector<br></br>
	 */	
	public PVector getUV(int i) 
	{
		return (PVector) texturev.elementAt(i);
	}		

	/**
	 * Sets the vert at index i to the PVector supplied
	 * @param index into the vert array
	 * @param A supplied PVector
	 */
	public void setVertex(int i, PVector vertex) 
	{
		((PVector) vertexes.elementAt(i)).set(vertex);
	}
	
	/**
	 * Sets the vert at index i to the x,y,z values supplied
	 * @param i
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setVertex(int i, float x, float y, float z ) 
	{
		((PVector) vertexes.elementAt(i)).set(x,y,z);
	}
	
	/**
	 * Sets the Normal at index i to the PVector supplied
	 * @param index into the normal array
	 * @param A supplied PVector
	 */
	public void setNormal(int i, PVector normal) 
	{
		((PVector) normv.elementAt(i)).set(normal);
	}
	
	/**
	 * Sets the UV at index i to the PVector supplied
	 * @param index into the uv array
	 * @param A supplied PVector
	 */
	public void setUV(int i, PVector uv) 
	{
		((PVector) texturev.elementAt(i)).set(uv);
	}	
	
	/**
	 * Sets an override texture for the drawing of the model.<br></br>
	 * Any PImage supplied will be used over all model segments<br></br>
	 * @param PImage<br></br>
	 * <br></br>
	 * NOTE: This method is identical to texture(), It has the better syntax.
	 */
	public void setTexture(PImage textureName) {
		texture = textureName;
	}

	/**
	 * Sets the override texture back to null. This sends the model back to original method of drawing the segment with the texture contained in the segments material
	 */
	public void originalTexture() {
		texture = null;
	}
	
	// -------------------------------------------------------------------------
	// -------------------------------------------- Processing Library functions
	// -------------- I'm not using them, but I didn't want to forget about them
	
//	public void pre() {
//	}
//	
//	public void post() {
//	}
//	
//	public void size(int w, int h) {
//	}
	
//	if the mouse and keyboard functions ever get used, these imports will be needed at the top of the package
//	import java.awt.event.KeyEvent;
	
//	import java.awt.event.MouseEvent;
//	public void mouse(MouseEvent event) {
//	}
//
//	public void key(KeyEvent e) {
//	}

	public void dispose() {
		// System.gc();
	}
	
}