/*
 * Copyright 2006 - 2012 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */

package org.graphstream.geography;

import java.io.IOException;
import java.util.ArrayList;

import org.graphstream.geography.index.SpatialIndex;
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
 * kept in memory in a spatial index whereas the others are simply ignored.
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
	 * 
	 */
	protected ArrayList<Element> elements;

	/**
	 * Spatial index storing geometric elements representing features.
	 */
	protected SpatialIndex index;

	protected GeoSource() {

		this.sourceId = String.format("<GeoSource %x>", System.nanoTime());

		this.descriptors = new ArrayList<Descriptor>();

		this.elements = new ArrayList<Element>();
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
	 * Process a single feature coming from the data source and check if it
	 * suits the user's needs. If it is the case, keep it for a later use,
	 * ignore it otherwise.
	 * 
	 * @param feature
	 *            The GeoTools feature to consider.
	 * @throws IOException
	 */
	protected void process(Object o) {

		for(Descriptor descriptor : this.descriptors)
			if(o != null && descriptor.matches(o))
				this.keep(descriptor.newElement(o), descriptor);
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

		this.elements.add(element);
		
		if(this.index != null)
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
	protected abstract void begin(String fileName) throws IOException;

	/**
	 * 
	 */
	protected abstract void traverse();

	/**
	 * Finalize the data import, generally by closing input data sources.
	 * 
	 * @throws IOException
	 */
	protected abstract void end() throws IOException;

	/**
	 * Convert the geographical elements accumulated during the reading step to
	 * graph elements.
	 */
	public abstract void transform();
}
