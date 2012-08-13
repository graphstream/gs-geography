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
 * matching features to custom categories (e.g. "ROAD", "LAKE", "LAND LOT") for
 * a later use. Secondly, geographical data files are often huge in size and
 * reducing the memory usage at the first step of the import surely is a good
 * idea.
 * 
 * All the geographical elements that pass the descriptors matching tests are
 * kept in memory whereas the others are simply ignored.
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

		this.sourceId = String.format("<GeoSource %x>", System.nanoTime());

		this.descriptors = new ArrayList<Descriptor>();
	}

	/**
	 * Add a descriptor to filter geographical data.
	 * 
	 * @param descriptor
	 */
	public void addDescriptor(Descriptor descriptor) {

		this.descriptors.add(descriptor);
	}

	/**
	 * Add a feature from the data source to the internal geometric
	 * representation of the studied space.
	 * 
	 * @param element
	 *            The element to add.
	 * @param descriptor
	 *            The descriptor that classified the element.
	 */
	public void keep(Element element, Descriptor descriptor) {

		this.index.add(element);
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
	 * Finalize the data import, generally by closing input data sources.
	 * 
	 * @throws IOException
	 */
	public abstract void end() throws IOException;

	/**
	 * Process every feature from the input data.
	 * 
	 * @throws IOException
	 */
	public abstract void read();

	/**
	 * Convert the geographical elements accumulated during the reading step to
	 * graph elements.
	 */
	public abstract void transform();
}
