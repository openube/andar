package saito.objtools;

import processing.core.*;
import saito.objloader.*;

public class OBJTransform 
{

	PApplet parent;


	public OBJTransform(PApplet parent){
		this.parent = parent;
	}

	public void scaleOBJ(OBJModel model, float scale)
	{

		scaleOBJ(model, scale, scale, scale);

	}

	public void scaleOBJ(OBJModel model, float scaleX, float scaleY, float scaleZ){

		int numberOfVerts = model.getVertexsize();

		if(numberOfVerts == 0)
		{

			model.debug.println("OBJTransform - \tThe model has no verts. Have you loaded it yet?");

		}
		else
		{

			PVector v;

			for(int i = 0; i < numberOfVerts; i++)
			{

				v = model.getVertex(i);

				v.x *= scaleX;
				v.y *= scaleY;
				v.z *= scaleZ;

			}
		}
	}

	public void moveOBJ(OBJModel model, float moveX, float moveY, float moveZ)
	{

		int numberOfVerts = model.getVertexsize();

		if(numberOfVerts == 0)
		{

			model.debug.println("OBJTransform - \tThe model has no verts. Have you loaded it yet?");

		}
		else
		{

			PVector v;

			PVector m = new PVector(moveX, moveY, moveZ);

			for(int i = 0; i < numberOfVerts; i++){

				v = model.getVertex(i);

				v.add(m);

			}
		}
	}

	public void centerOBJ(OBJModel model){

		OBJBoundingBox obox = new OBJBoundingBox(parent, model);

		moveOBJ(model, -obox.getCenterX(), -obox.getCenterY(), -obox.getCenterZ());

	}


	public void hackUVMapToZeroOne(OBJModel m)
	{
		int count = m.getUVSize();

		PVector minimum = new PVector(PApplet.MAX_INT, PApplet.MAX_INT, 0);

		PVector maximum = new PVector(PApplet.MIN_INT, PApplet.MIN_INT, 0); 

		PVector temp;
		
		for(int i = 0; i < count; i ++)
		{
			temp = m.getUV(i);

			minimum.x = PApplet.min( minimum.x, temp.x);
			minimum.y = PApplet.min( minimum.y, temp.y);

			maximum.x = PApplet.max( maximum.x, temp.x);
			maximum.y = PApplet.max( maximum.y, temp.y);  

		} 

		for(int i = 0; i < count; i ++)
		{
			temp = m.getUV(i);

			temp.x = PApplet.map(temp.x, minimum.x, maximum.x, 0.0f, 1.0f);
			temp.y = PApplet.map(temp.y, minimum.y, maximum.y, 0.0f, 1.0f);

		}   
	}
	
	public void hackUVClampToZeroOne(OBJModel m)
	{
		int count = m.getUVSize();

		PVector temp;
		
		for(int i = 0; i < count; i ++)
		{
			temp = m.getUV(i);

			temp.x = PApplet.constrain(temp.x, 0.0f, 1.0f);
			temp.y = PApplet.constrain(temp.y, 0.0f, 1.0f);
			 
		} 
	}
}