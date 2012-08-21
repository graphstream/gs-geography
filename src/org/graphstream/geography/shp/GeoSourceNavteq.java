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

package org.graphstream.geography.shp;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.Element;

// TODO
/**
 * This geographical source implementation produces a road network from Navteq
 * shapefiles and takes care of the Z-index conflicts.
 * 
 * @author Merwan Achibet
 */
public class GeoSourceNavteq extends GeoSourceSHP {

	/**
	 * The path to the file describing the road network.
	 */
	protected String roadsFileName;

	/**
	 * The path to the file containing the Z-indexes of all nodes.
	 */
	protected String zFileName;

	/**
	 * The descriptor matching geographic objects with Z-index points.
	 */
	protected Descriptor zDescriptor;

	/**
	 * The descriptor matching geographic objects with representations of roads.
	 */
	protected Descriptor roadDescriptor;

	/**
	 * Instantiate a new Navteq source producing a road network.
	 * 
	 * @param roadsFileName
	 *            The path to the file containing road data.
	 * @param zFileName
	 *            The path to the file containing Z data.
	 */
	public GeoSourceNavteq(String roadsFileName, String zFileName) {

		this.roadsFileName = roadsFileName;
		this.zFileName = zFileName;

		// First: select and filter the Z-index points.

		// By default there is no attribute worth keeping.

		AttributeFilter filterZ = new AttributeFilter(AttributeFilter.Mode.KEEP);

		// We are only interested in intersection points.

		this.zDescriptor = new DescriptorSHP(this, "Z", filterZ);

		this.zDescriptor.sendElementsToSpatialIndex();
		this.zDescriptor.mustHave("INTRSECT", "Y");

		// Second: select and filter the road points.

		// By default, there is no attribute worth keeping.

		AttributeFilter filterRoad = new AttributeFilter(AttributeFilter.Mode.KEEP);

		// We are only interested in line features..

		this.roadDescriptor = new DescriptorSHP(this, "Z", filterRoad);

		// descriptorRoad.onlyConsiderLineEndPoints();
		// descriptorRoad.sendElementsToSpatialIndex();
		this.roadDescriptor.mustBe(Element.Type.LINE);

		// Read the Z level file and store the data in the spatial index.

		addDescriptor(this.zDescriptor);

		read(this.zFileName);

		// Read the road file.

		this.descriptors.clear();
		addDescriptor(this.roadDescriptor);

		read(this.roadsFileName);
	}

	/**
	 * Give the descriptor matching geographic objects with Z-index points.
	 * 
	 * @return The Z-index point descriptor.
	 */
	public Descriptor getZDescriptor() {

		return this.zDescriptor;
	}
	
	/**
	 * Give the descriptor matching geographic objects with representations of
	 * roads
	 * 
	 * @return The road descriptor.
	 */
	public Descriptor getRoadDescriptor() {

		return this.roadDescriptor;
	}
	
	/**
	 * Read a shapefile.
	 * 
	 * @param fileName
	 *            The path to file.
	 */
	protected void read(String fileName) {

		try {
			begin(fileName);
			traverse();
			end();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void transform() {

		System.out.println("Tadaaaa!");
	}

}
