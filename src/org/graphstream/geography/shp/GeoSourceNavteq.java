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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Element;
import org.graphstream.geography.ElementDescriptor;
import org.graphstream.geography.ElementShape;
import org.graphstream.geography.ElementView;
import org.graphstream.geography.FileDescriptor;
import org.graphstream.geography.Line;
import org.graphstream.geography.Vertex;
import org.graphstream.geography.Point;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This geographical source implementation produces a road network from Navteq
 * shapefiles and takes care of the Z-index conflicts.
 * 
 * @author Merwan Achibet
 */
public class GeoSourceNavteq extends GeoSourceSHP {

	/**
	 * Handle to the descriptor matching geographic objects with Z-index points.
	 */
	protected ElementDescriptor zDescriptor;

	/**
	 * Handle to the filter associated with the Z-index point descriptor.
	 */
	protected AttributeFilter zAttributeFilter;

	/**
	 * Handle to the descriptor matching geographic objects with representations
	 * of roads.
	 */
	protected ElementDescriptor roadDescriptor;

	/**
	 * Handle to the filter associated with the road descriptor.
	 */
	protected AttributeFilter roadAttributeFilter;

	/**
	 * A record of nodes already added to the output graph.
	 */
	protected ArrayList<String> addedNodeIds;

	/**
	 * Instantiate a new Navteq source producing a road network.
	 * 
	 * @param fileNames
	 *            A set of paths to the input files. The first file must contain
	 *            the z-indexes and the second file must contain the roads.
	 */
	public GeoSourceNavteq(String... fileNames) {
		super(fileNames);

		FileDescriptor zFileDescriptor = new FileDescriptor(fileNames[0]);
		addFileDescriptor(zFileDescriptor);

		FileDescriptor roadFileDescriptor = new FileDescriptor(fileNames[1]);
		addFileDescriptor(roadFileDescriptor);

		// First: select and filter the Z-index points.

		// We need to keep the link ID to join the data from the two files and
		// the Z level to handle overpasses/underpasses/tunnels/bridges...

		this.zAttributeFilter = new AttributeFilter(AttributeFilter.Mode.KEEP);

		this.zAttributeFilter.addAttribute("LINK_ID");
		this.zAttributeFilter.addAttribute("Z_LEVEL");

		// We are only interested in intersection points.

		this.zDescriptor = new ElementDescriptor(this, "Z", this.zAttributeFilter);

		this.zDescriptor.mustBe(ElementShape.Type.POINT);
		this.zDescriptor.mustHave("INTRSECT", "Y");

		this.zDescriptor.sendElementsToSpatialIndex();

		zFileDescriptor.addDescriptor(this.zDescriptor);

		// Second: select and filter the road points.

		// We need the link ID to join the two files.

		this.roadAttributeFilter = new AttributeFilter(AttributeFilter.Mode.KEEP);

		this.roadAttributeFilter.addAttribute("LINK_ID");

		// We are only interested in line features..

		this.roadDescriptor = new ElementDescriptor(this, "ROAD", this.roadAttributeFilter);

		this.roadDescriptor.mustBe(ElementShape.Type.LINE);

		roadFileDescriptor.addDescriptor(roadDescriptor);
	}

	/**
	 * Give the descriptor matching geographic objects with Z-index points.
	 * 
	 * @return The Z-index point descriptor.
	 */
	public ElementDescriptor getZDescriptor() {

		return this.zDescriptor;
	}

	/**
	 * Give the filter associated with the Z-index point descriptor.
	 * 
	 * @return The Z-index point filter.
	 */
	public AttributeFilter getZAttributeFilter() {

		return this.zAttributeFilter;
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
	 * Give the filter associated with the road descriptor.
	 * 
	 * @return The road filter.
	 */
	public AttributeFilter getRoadAttributeFilter() {

		return this.roadAttributeFilter;
	}

	@Override
	public void nextEvents() {

		this.addedNodeIds = new ArrayList<String>();

		ArrayList<ElementView> roads = getElementViewsAtStep(0);

		for(ElementView road : roads) {

			// Only consider the road elements and not the z-index points.

			if(!road.getCategory().equals("ROAD"))
				continue;

			Line line = (Line)road.getShape();

			// Add the two end points to the graph if necessary.

			Vertex[] ends = line.getEndVertices();

			String idNode1 = addNode(ends[0], line, road);
			String idNode2 = addNode(ends[1], line, road);

			// Draw an edge between the two points.

			if(idNode1 != null && idNode2 != null)
				sendEdgeAdded(this.id, road.getId(), idNode1, idNode2, false);

			// Bind the attributes

			replicateEdgeAttributes(road.getId(), road);

		}
	}

	/**
	 * Add a node that represents an intersection to the output graph.
	 * 
	 * This is where the Z-level conflicts are resolved. We search for all the
	 * Z-level points at the same position as the road point to add. One of
	 * these points shares its link ID with the road and contains its relative
	 * elevation.
	 * 
	 * @param point
	 *            The intersection point to add.
	 * @param line
	 *            The line containing the intersection.
	 * @return The ID of the point used as a graph node.
	 */
	protected String addNode(Vertex point, Line line, ElementView road) {

		// Retrieve the Z-level points at the same position as the road point.

		ArrayList<Element> zPoints = this.index.getElementsAt(point.getPosition().x, point.getPosition().y);

		// Retrieve the views of these z points at the only time step that we
		// consider.

		ArrayList<ElementView> zPointViews = new ArrayList<ElementView>();

		for(Element zPoint : zPoints)
			zPointViews.add(getElementViewAtStep(zPoint.getId(), 0));

		// Among them, find the Z-level point with the same link ID as the road.

		ElementView goodZPointView = null;

		for(ElementView zPointView : zPointViews)
			if(zPointView.getAttribute("LINK_ID").equals(road.getAttribute("LINK_ID")))
				goodZPointView = zPointView;

		if(goodZPointView == null)
			return null;

		// Get the Z level of the Z-level point.

		Object zLevel = goodZPointView.getAttribute("Z_LEVEL");

		// Check if a point at the same position and with the same Z level is
		// already in the output graph.

		ElementView alreadyHerePoint = null;

		for(ElementView zPointView : zPointViews)
			if(zPointView.getAttribute("Z_LEVEL").equals(zLevel) && this.addedNodeIds.contains(zPointView.getId())) {
				alreadyHerePoint = zPointView;
				break;
			}

		// If no valid node is already in the graph, add one.

		if(alreadyHerePoint == null) {

			alreadyHerePoint = goodZPointView;

			sendNodeAdded(this.id, alreadyHerePoint.getId());

			this.addedNodeIds.add(alreadyHerePoint.getId());

			// Place the new node at an appropriate position.

			Coordinate pos = ((Point)alreadyHerePoint.getShape()).getPosition();
			sendNodeAttributeAdded(this.id, alreadyHerePoint.getId(), "x", pos.x);
			sendNodeAttributeAdded(this.id, alreadyHerePoint.getId(), "y", pos.y);

			// Bind the attributes.

			HashMap<String, Object> attributes = alreadyHerePoint.getAttributes();

			if(attributes != null)
				for(Entry<String, Object> entry : attributes.entrySet())
					sendNodeAttributeAdded(this.id, alreadyHerePoint.getId(), entry.getKey(), entry.getValue());
		}

		return alreadyHerePoint.getId();
	}

}
