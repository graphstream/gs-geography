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
import java.util.Map.Entry;

import org.graphstream.geography.DiffBuilder;
import org.graphstream.geography.Element;
import org.graphstream.geography.ElementShape;
import org.graphstream.geography.ElementShape.Type;
import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.ElementDiff;
import org.graphstream.geography.ElementView;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.graphstream.geography.Polygon;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * @author Merwan Achibet
 */
public class DiffbuilderOSM extends DiffBuilder {

	public DiffbuilderOSM(GeoSource source) {
		super(source);
	}

	@Override
	public ElementDiff diff(Element element, ElementDiff previousDiff, Integer previousDate, Object o) {

		ElementDiff nextDiff = null;

		// Cast the object to a XOM element;

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve all of the object attributes.

		HashMap<String, Object> currentAttributes = new HashMap<String, Object>();

		nu.xom.Elements tags = xmlElement.getChildElements("tag");

		AttributeFilter filter = element.getDescriptorUsed().getAttributeFilter();
		
		for(int i = 0, l = tags.size(); i < l; ++i) {
			
			String key = tags.get(i).getAttributeValue("k");
			
			if(filter.isKept(key))
				currentAttributes.put(key, tags.get(i).getAttributeValue("v"));
		}
		
		// If there is no previous diff of the element, copy all the attributes.

		if(previousDiff == null) {

			nextDiff = new ElementDiff(element, true);

			for(Entry<String, Object> entry : currentAttributes.entrySet())
				nextDiff.setChangedAttribute(entry.getKey(), entry.getValue());

			ElementShape.Type type = this.source.getAggregator().getType(o);

			ElementShape shape = null;

			if(type == Type.POINT) {

				Point point = new Point(element);

				Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(element.getId());

				point.setPosition(coord.x, coord.y);

				shape = point;
			}
			else if(type == Type.LINE) {

				Line line = new Line(element);

				// Shape the line.

				nu.xom.Elements lineNodes = xmlElement.getChildElements("nd");

				/*
				 * if(this.onlyLineEndPointsConsidered) { addPointToLine(line,
				 * lineNodes, 0); addPointToLine(line, lineNodes,
				 * lineNodes.size() - 1); } else
				 */// TODO
				for(int i = 0, l = lineNodes.size(); i < l; ++i)
					addPointToLine(line, lineNodes, i);

				shape = line;
			}
			else if(type == Type.POLYGON) {

				Polygon polygon = new Polygon(element);

				// Shape the polygon. We can use addPointToLine(...) because a
				// polygon
				// extends a line. The last node is not added as a point as it
				// is the
				// same as the first node.

				nu.xom.Elements polygonNodes = xmlElement.getChildElements("nd");

				for(int i = 0, l = polygonNodes.size() - 1; i < l; ++i)
					addPointToLine(polygon, polygonNodes, i);

				shape = polygon;
			}

			nextDiff.setShape(shape);
		}

		//

		else {

			nextDiff = new ElementDiff(element);
			
			ElementView elementAtPreviousDate = element.getElementViewAtDate(previousDate);

			for(Entry<String, Object> entry : elementAtPreviousDate.getAttributes().entrySet())
				if(!currentAttributes.containsKey(entry.getKey()))
					nextDiff.addRemovedAttribute(entry.getKey());

			for(Entry<String, Object> entry : currentAttributes.entrySet())
				if(!elementAtPreviousDate.getAttributes().containsKey(entry.getKey()))
					nextDiff.setChangedAttribute(entry.getKey(), entry.getValue());

			for(Entry<String, Object> entry : currentAttributes.entrySet())
				if(elementAtPreviousDate.getAttributes().containsKey(entry.getKey()) && !elementAtPreviousDate.getAttributes().get(entry.getKey()).equals(entry.getValue()))
					nextDiff.setChangedAttribute(entry.getKey(), entry.getValue());

			nextDiff.setShape(previousDiff.getShape());
		}

		return nextDiff;
	}

	/**
	 * Add a node from the OpenStreetMap XML format to the geometric
	 * representation of the line.
	 * 
	 * @param line
	 *            The line to complete.
	 * @param xmlNodes
	 *            The list of XML nodes shaping the line in the input format.
	 * @param index
	 *            The index of the input format node that must be added to the
	 *            line.
	 */
	protected void addPointToLine(Line line, nu.xom.Elements xmlNodes, int index) {

		// Select the appropriate sub-node.

		nu.xom.Element xmlNode = xmlNodes.get(index);

		// Retrieve its ID and then its position.

		String nodeId = xmlNode.getAttributeValue("ref");

		Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(nodeId);

		// Complete the line shape.
		line.addPoint(nodeId, coord.x, coord.y);
	}

}
