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
 * Abstract source for geographic files.
 * 
 * This class and its implementations are the core of the import process. They
 * possess a list of descriptors to select, filter and categorize interesting
 * geographic objects from the input files.
 * 
 * All the geographical elements that pass descriptors matching tests are kept
 * in memory and optionally referenced in a spatial index whereas the others are
 * simply ignored.
 * 
 * @author Merwan Achibet
 */
public abstract class GeoSource extends SourceBase {

	/**
	 * The ID of this source.
	 */
	protected String sourceId;

	/**
	 * Descriptors for the geographic objects that the user want to consider.
	 */
	protected ArrayList<Descriptor> descriptors;

	/**
	 * The geometric elements that matched any of the descriptors definitions.
	 */
	protected ArrayList<Element> elements;

	/**
	 * Index spatially referencing the spatial points shaping the kept elements.
	 */
	protected SpatialIndex index;

	/**
	 * Instantiate a new geographic source.
	 */
	protected GeoSource() {

		this.sourceId = String.format("<GeoSource %x>", System.nanoTime());

		this.elements = new ArrayList<Element>();
	}

	/**
	 * Add a descriptor to select and categorize geographic objects.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 */
	public void addDescriptor(Descriptor descriptor) {

		if(this.descriptors == null)
			this.descriptors = new ArrayList<Descriptor>();

		this.descriptors.add(descriptor);
	}

	/**
	 * Process a single geographic object coming from the data source and check
	 * if it suits the user's needs. If it is the case, keep it for a later use,
	 * ignore it otherwise.
	 * 
	 * @param o
	 *            The geometric object to check.
	 */
	protected void process(Object o) {

		if(this.descriptors == null)
			return;

		for(Descriptor descriptor : this.descriptors)
			if(o != null && descriptor.matches(o))
				this.keep(descriptor.newElement(o), descriptor);
	}

	/**
	 * Add a geometric element to the list of kept elements and optionally
	 * reference it in a spatial index.
	 * 
	 * @param element
	 *            The element to add.
	 * @param descriptor
	 *            The descriptor that classified the elements.
	 */
	protected void keep(Element element, Descriptor descriptor) {

		this.elements.add(element);

		if(this.index != null && descriptor.areElementsSentToSpatialIndex())
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
	 * Go through all the data of the input file.
	 */
	protected abstract void traverse();

	/**
	 * Finalize the data import, generally by closing input data sources and
	 * freeing resources.
	 * 
	 * @throws IOException
	 */
	protected abstract void end() throws IOException;

	/**
	 * Populate the output graph from the geometric elements accumulated during
	 * the selection phase.
	 * 
	 * This is were the magic happens. A programmer that wants to build a
	 * specific implementation of GeoSource will do most of its work in this
	 * method. A geographer that simply wants to import geographic data into a
	 * graph will prefer to directly use an implemented use-case.
	 */
	public abstract void transform();

}
