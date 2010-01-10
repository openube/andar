package saito.objtools;

import processing.core.*;
import saito.objloader.*;

public class OBJBoundingBox  implements PConstants{
	
	PApplet parent;
	private float x1 = MAX_FLOAT,y1 = MAX_FLOAT,z1 = MAX_FLOAT,x2 = MIN_FLOAT,y2 = MIN_FLOAT,z2 = MIN_FLOAT;
	private float centerX, centerY,centerZ;
	public float width, height, depth;

	
	public OBJBoundingBox(PApplet parent, OBJModel model){
		
		this.parent = parent;
		
		model.debug.println("OBJBoundingBox - \tGetting the Bounding Box");
		
		int numberOfVerts = model.getVertexsize();
		
		if(numberOfVerts == 0){
			
			model.debug.println("OBJBoundingBox - \tThe model has no verts. Have you loaded it yet?");
			
		}
		else{
		
			PVector v;
			
			for(int i = 0; i < numberOfVerts; i++){
				
				v = model.getVertex(i);
				
				x1 = Math.min(x1,v.x);
				y1 = Math.min(y1,v.y);
				z1 = Math.min(z1,v.z);
				
				x2 = Math.max(x2,v.x);
				y2 = Math.max(y2,v.y);
				z2 = Math.max(z2,v.z);
				
			}
			
			width  = (float)Math.sqrt((x2 - x1) * (x2 - x1)); 
			height = (float)Math.sqrt((y2 - y1) * (y2 - y1));
			depth  = (float)Math.sqrt((z2 - z1) * (z2 - z1));
			
			
//			width =  Math.abs(x1) + Math.abs(x2);
//			height = Math.abs(y1) + Math.abs(y2);
//			depth =  Math.abs(z1) + Math.abs(z2);
			
			centerX = x1 + (width / 2);
			centerY = y1 + (height/ 2);
			centerZ = z1 + (depth / 2);
		}
	}
	
	
	
	public void draw(){
		
	
		parent.rectMode(CORNERS);
			
		parent.pushMatrix();
		parent.translate(centerX, centerY, centerZ);
	  
		parent.box(width, height, depth);
		parent.popMatrix();

		
	}
	
	public float getMinX(){
		return x1;
	}
	public float getMinY(){
		return y1;
	}
	public float getMinZ(){
		return z1;
	}
	public float getMaxX(){
		return x2;
	}
	public float getMaxY(){
		return y2;
	}
	public float getMaxZ(){
		return z2;
	}
	public float[] getMinXYZ(){
		return new float[]{x1,y1,z1};
	}
	public float[] getMaxXYZ(){
		return new float[]{x2,y2,z2};
	}
	public float getCenterX(){
		return centerX;
	}
	public float getCenterY(){
		return centerY;
	}
	public float getCenterZ(){
		return centerZ;
	}
	public float[] getCenterXYZ(){
		return new float[]{centerX,centerY,centerZ};
	}

}
