package org.graphstream.geography;

import java.io.IOException;
import java.util.ArrayList;

import org.graphstream.stream.SourceBase;

/**
 * Abstract source for geographical data files.
 * 
 * It contains a list of descriptors which goal is to filter and classify the
 * features coming from data sources. This process has dual advantages. First,
 * it obviously gives better control to the user over the data as he can bind
 * matching features to custom categories (e.g. "ROAD", "LAKE", "SHOP") for a
 * later use. Secondly, geographical data files are often huge in size and
 * reducing the memory usage at the first step of the import surely is a good
 * idea.
 * 
 * @author Merwan Achibet
 */
public abstract class GeoSource extends SourceBase {

	/**
	 * The ID of this source.
	 */
	protected String sourceId;

	/**
	 * Descriptors for the features that we want to consider.
	 */
	protected ArrayList<Descriptor> descriptors;

	/**
	 * Spatial index storing geometric elements representing features.
	 */
	protected SpatialIndex index;

	protected GeoSource() {

		this.sourceId = String.format("<DGS stream %x>", System.nanoTime());

		this.descriptors = new ArrayList<Descriptor>();
	}

	/**
	 * Add a descriptor to the description list.
	 * 
	 * @param descriptor
	 */
	public void addDescriptor(Descriptor descriptor) {

		this.descriptors.add(descriptor);
	}

	// Abstract

	/**
	 * Prepare the import, generally by opening the source file or reading it in
	 * one batch and caching the data.
	 * 
	 * @param fileName
	 *            The name of the file to import data from.
	 * @throws IOException
	 */
	public abstract void begin(String fileName) throws IOException;

	/**
	 * Finish the file import, generally by closing input files.
	 * 
	 * @throws IOException
	 */
	public abstract void end() throws IOException;

	/**
	 * Process every feature from the input data in one batch.
	 * 
	 * @throws IOException
	 */
	public abstract void all() throws IOException;

	/**
	 * Process the next feature from the input data.
	 * 
	 * @return True if there is still features left.
	 * @throws IOException
	 */
	protected abstract void next() throws IOException;

	/**
	 * Add a given object to the GraphStream geometric representation of the
	 * studied space.
	 * 
	 * @param o
	 *            The object to add.
	 * @param descriptor
	 *            The descriptor that classified the object.
	 */
	protected abstract void keep(Object o, Descriptor descriptor);

	/**
	 * 
	 */
	public abstract void transform();
}
