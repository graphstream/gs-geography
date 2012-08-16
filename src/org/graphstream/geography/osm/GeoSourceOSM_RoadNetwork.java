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

import java.util.ArrayList;
import java.util.List;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Element;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This geographical source implementation extracts the road network from an
 * OpenStreetMap file.
 * 
 * @author Merwan Achibet
 */
public class GeoSourceOSM_RoadNetwork extends GeoSourceOSM {

	/**
	 * A record of nodes already added to the output graph.
	 */
	protected List<String> addedNodeIds;

	public GeoSourceOSM_RoadNetwork(String fileName) {
		super(fileName);
		
		// The only attribute worth keeping is "highway". If present, it
		// indicates that the current element is some kind of road (e.g.
		// highway:primary or highway:residential).

		AttributeFilter filterRoad = new AttributeFilter();

		filterRoad.add("highway");

		// Roads are linear features that possess a "highway" key with whatever
		// value.

		DescriptorOSM descriptorRoad = new DescriptorOSM(this, "ROAD", filterRoad);

		descriptorRoad.mustBe(Element.Type.LINE);
		descriptorRoad.mustHave("highway");

		addDescriptor(descriptorRoad);
		
		//
		
		read();
	}

	@Override
	public void transform() {

		this.addedNodeIds = new ArrayList<String>();

		for(Element element : this.elements) {

			Line line = (Line)element;

			// Add each point shaping the road to the graph, as nodes.

			ArrayList<Point> points = line.getPoints();

			// Start with the first point of the line.

			Point from = points.get(0);
			String idFrom = from.getId();

			addNode(from);

			for(int i = 1, l = points.size(); i < l; ++i) {

				// Add the next point.

				Point to = points.get(i);
				String idTo = to.getId();

				addNode(to);

				// Link it to the previous point.

				sendEdgeAdded(this.sourceId, line.getId() + "_" + idFrom + " " + idTo, idFrom, idTo, false);

				idFrom = idTo;
			}
		}
	}

	protected void addNode(Point point) {

		String nodeId = point.getId();

		// Add the node if it has not already been done in the process of
		// creating another road (as some points/crossroads are shared).

		if(!this.addedNodeIds.contains(nodeId)) {

			sendNodeAdded(this.sourceId, nodeId);

			this.addedNodeIds.add(nodeId);

			// Place the new node at an appropriate position.

			Coordinate position = getNodePosition(nodeId);
			sendNodeAttributeAdded(this.sourceId, nodeId, "x", position.x);
			sendNodeAttributeAdded(this.sourceId, nodeId, "y", position.y);

			// Bind the attributes.

			for(String key : point.getAttributes().keySet())
				sendNodeAttributeAdded(this.sourceId, nodeId, key, point.getAttribute(key));
		}
	}

}
