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
import org.graphstream.geography.ElementDescriptor;
import org.graphstream.geography.ElementShape;
import org.graphstream.geography.ElementState;
import org.graphstream.geography.ElementView;
import org.graphstream.geography.FileDescriptor;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This geographical source implementation produces a road network from an
 * OpenStreetMap XML file.
 * 
 * @author Merwan Achibet
 */
public class GeoSourceOSM_RoadNetwork extends GeoSourceOSM {

	/**
	 * The descriptor matching geographic objects with representations of roads.
	 */
	protected ElementDescriptor roadDescriptor;

	/**
	 * The attribute filter for roads.
	 */
	protected AttributeFilter roadAttributeFilter;

	/**
	 * A record of nodes already added to the output graph.
	 */
	protected List<String> addedNodeIds;

	/**
	 * A record of edges already added to the output graph.
	 */
	protected List<String> addedEdgeIds;

	/**
	 * Instantiate a new OpenStreetMap source producing a road network graph.
	 * 
	 * @param fileName
	 *            The path to the input file.
	 */
	public GeoSourceOSM_RoadNetwork(String... fileNames) {
		super(fileNames);

		this.addedNodeIds = new ArrayList<String>();
		this.addedEdgeIds = new ArrayList<String>();

		// By default, there are no attribute worth keeping.

		this.roadAttributeFilter = new AttributeFilter();

		// Roads are linear features that possess a "highway" key with whatever
		// value.

		this.roadDescriptor = new ElementDescriptor(this, "ROAD", this.roadAttributeFilter);

		this.roadDescriptor.mustBe(ElementShape.Type.LINE);
		this.roadDescriptor.mustHave("highway");

		// Attach this descriptor to every file.

		for(String fileName : fileNames) {

			FileDescriptor fileDescriptor = new FileDescriptor(fileName);
			fileDescriptor.addDescriptor(this.roadDescriptor);

			this.addFileDescriptor(fileDescriptor);
		}
	}

	/**
	 * Give the descriptor matching geographic objects with representations of
	 * roads
	 * 
	 * @return The road descriptor.
	 */
	public ElementDescriptor getRoadDescriptor() {

		return this.roadDescriptor;
	}

	/**
	 * Give the attribute filter for the roads.
	 * 
	 * @return the attribute filter.
	 */
	public AttributeFilter getRoadAttributeFilter() {

		return this.roadAttributeFilter;
	}

	@Override
	protected void nextEvents() {

		ArrayList<ElementState> elementDiffs = getElementDiffsAtStep(this.currentTimeStep);

		for(ElementState elementDiff : elementDiffs) {

			// If the diff actually is a base, entirely insert the element.

			if(elementDiff.isBase()) {

				Line line = (Line)elementDiff.getShape();

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

					String edgeId = line.getElementId() + "_" + idFrom + "_" + idTo;
					if(!this.addedEdgeIds.contains(edgeId)) {
						sendEdgeAdded(this.id, edgeId, idFrom, idTo, false);
						this.addedEdgeIds.add(edgeId);
					}

					idFrom = idTo;
				}
			}
			
			// Update attributes and shape.
			
			// TODO
		}

	}

	protected void nextEvents2() {

		ArrayList<ElementView> allElements = getElementViewsAtStep(this.currentTimeStep);

		for(ElementView element : allElements) {

			Line line = (Line)element.getShape();

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

				String edgeId = line.getElementId() + "_" + idFrom + "_" + idTo;
				if(!this.addedEdgeIds.contains(edgeId)) {
					sendEdgeAdded(this.id, edgeId, idFrom, idTo, false);
					this.addedEdgeIds.add(edgeId);
				}

				idFrom = idTo;
			}
		}
	}

	/**
	 * Add a node representing part of a road to the output graph.
	 * 
	 * @param point
	 *            The point to transfer to the graph.
	 */
	protected void addNode(Point point) {

		String nodeId = point.getId();

		// Add the node if it has not already been done in the process of
		// creating another road (as some points/crossroads are shared).

		if(!this.addedNodeIds.contains(nodeId)) {

			sendNodeAdded(this.id, nodeId);

			this.addedNodeIds.add(nodeId);

			// Place the new node at an appropriate position.

			Coordinate position = getNodePosition(nodeId);
			sendNodeAttributeAdded(this.id, nodeId, "x", position.x);
			sendNodeAttributeAdded(this.id, nodeId, "y", position.y);
		}
	}

}
