package edu.union.graphics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The ModelLoader interface is implemented by all classes which can load Model objects
 * from binary streams.
 * @author bburns
 * 
 */
public interface ModelLoader {
	/**
	 * Set the factory to use for creating the Meshes in the Model.
	 * @param f The mesh factory.
	 */
	public void setFactory(MeshFactory f);

	/**
	 * Load a model from a named file.
	 * @param file The name of the file to open
	 * @return The newly loaded Model
	 * @throws IOException If an error occurs reading the file.
	 */
	public Model load(String file) throws IOException;
	
	/**
	 * Load a model from a file specified by a File object.
	 * @param f The File object specifying the file to read from.
	 * @return The newly loaded Model
	 * @throws IOException If an error occurs reading the file.
	 */
	public Model load(File f) throws IOException;
	
	/**
	 * Load a model from a stream of bytes.
	 * @param is The InputStream to load the model from.
	 * @return The newly loaded Model
	 * @throws IOException If an error occurs reading the file.
	 */
	public Model load(InputStream is) throws IOException;
	
	/**
	 * Determine if this loader can handle a particular file
	 * @param f The file to check.
	 * @return true if this loader can load an object from the file, false otherwise.
	 */
	public boolean canLoad(File f);

	/**
	 * Determine if this loader can handle a particular named file
	 * @param f The name of file to check.
	 * @return true if this loader can load an object from the file, false otherwise.
	 */
	public boolean canLoad(String f);
}
