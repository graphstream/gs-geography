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

package org.graphstream.geography.osm;

import java.util.HashMap;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Element;
import org.graphstream.geography.Polygon;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This geographical source implementation produces a neighborhood graph
 * according to euclidean distance.
 * 
 * The user needs to specify the distance threshold under which two buildings
 * are considered neighbors.
 * 
 * @author Merwan Achibet
 */
public class GeoSourceOSM_Neighborhood extends GeoSourceOSM {

	/**
	 * The neighborhood radius. If two buildings are separated by a distance
	 * less than this threshold, then they are considered neighbors.
	 */
	private double radius;

	/**
	 * Instantiate a new OpenStreetMap source producing a neighborhood graph.
	 * 
	 * @param fileName
	 *            The path to the input file.
	 * @param radius
	 *            The radius threshold under which two buildings are considered
	 *            neighbors.
	 */
	public GeoSourceOSM_Neighborhood(String fileName, double radius) {
		super(fileName);

		this.radius = radius;

		// By default, there is no attribute worth keeping.

		AttributeFilter filterBuilding = new AttributeFilter();

		// We are only interested in buildings.

		DescriptorOSM descriptorBuilding = new DescriptorOSM(this, "BUILDINGS", filterBuilding);

		descriptorBuilding.mustBe(Element.Type.POLYGON);
		descriptorBuilding.mustHave("building", "yes");

		addDescriptor(descriptorBuilding);

		// Go.

		read();
	}

	@Override
	public void transform() {

		// Keep a record of the buildings that have already been inserted into
		// the graph.

		HashMap<String, Coordinate> placedBuildings = new HashMap<String, Coordinate>();

		for(Element element : this.elements) {

			// Compute the center of the current building and add a new node at
			// this position.

			Coordinate centroid = ((Polygon)element).getCentroid();

			sendNodeAdded(this.sourceId, element.getId());

			sendNodeAttributeAdded(this.sourceId, element.getId(), "x", centroid.x);
			sendNodeAttributeAdded(this.sourceId, element.getId(), "y", centroid.y);

			// Bind the attributes.

			for(String key : element.getAttributes().keySet())
				sendNodeAttributeAdded(this.sourceId, element.getId(), key, element.getAttribute(key));

			// Draw an edge between the new node and already placed ones if
			// their distance is below the neighborhood radius.
			// TODO there surely is a faster way to do that using the quadtree.

			for(String id : placedBuildings.keySet())
				if(centroid.distance(placedBuildings.get(id)) < this.radius)
					sendEdgeAdded(this.sourceId, element.getId() + id, element.getId(), id, false);

			placedBuildings.put(element.getId(), centroid);
		}
	}

}
