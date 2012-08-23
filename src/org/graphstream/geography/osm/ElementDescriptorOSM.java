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

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Element;
import org.graphstream.geography.ElementDescriptor;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.graphstream.geography.Polygon;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Descriptor for OpenStreetMap XML files.
 * 
 * @author Merwan Achibet
 */
public class ElementDescriptorOSM extends ElementDescriptor {

	/**
	 * instantiate a new OpenStreetMap descriptor.
	 * 
	 * @param source
	 *            The source using this descriptor.
	 * @param category
	 *            The category associated with matching elements.
	 * @param filter
	 *            The filter that will reduce the attributes of matching
	 *            elements.
	 */
	public ElementDescriptorOSM(GeoSource source, String category, AttributeFilter filter) {
		super(source, category, filter);
	}

	@Override
	protected boolean isPoint(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// The name of XML entries representing points is "node".

		return xmlElement.getLocalName().equals("node");
	}

	@Override
	protected boolean isLine(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// The name of XML entries representing lines is "way" and it must be
		// open (or it would be a polygon).

		return xmlElement.getLocalName().equals("way") && !isClosed(xmlElement);
	}

	@Override
	protected boolean isPolygon(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// The name of XML entries representing lines is "way" and it must be
		// closed (or it would be a line).

		return xmlElement.getLocalName().equals("way") && isClosed(xmlElement);
	}

	/**
	 * Check if the geographic object shape is looped.
	 * 
	 * Line and polygons are similar in the OpenStreetMap format but polygons
	 * have the same starting and ending point (they are closed).
	 * 
	 * @param xmlElement
	 *            The XML element.
	 * @return True ifthe element is closed, false otherwise.
	 */
	protected boolean isClosed(nu.xom.Element xmlElement) {

		// Get all the nodes of the element.

		nu.xom.Elements xmlNodes = xmlElement.getChildElements("nd");

		// Check that there are child nodes.

		if(xmlNodes.size() == 0)
			return false;

		// Check that the first node is the same as the last.

		String idFirst = xmlNodes.get(0).getAttributeValue("ref");
		String idLast = xmlNodes.get(xmlNodes.size() - 1).getAttributeValue("ref");

		return idFirst.equals(idLast);
	}

	@Override
	protected boolean hasKey(String key, Object o) {

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		for(int i = 0, l = xmlTags.size(); i < l; ++i)
			if(xmlTags.get(i).getAttribute("k").getValue().equals(key))
				return true;

		return false;
	}

	@Override
	protected boolean hasKeyValue(String key, Object value, Object o) {

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		for(int i = 0, l = xmlTags.size(); i < l; ++i) {

			nu.xom.Element xmlTag = xmlTags.get(i);

			if(xmlTag.getAttribute("k").getValue().equals(key) && xmlTag.getAttribute("v").getValue().equals(value))
				return true;
		}

		return false;
	}

	@Override
	public String getElementId(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Return its ID.

		return xmlElement.getAttributeValue("id");
	}

	@Override
	protected Point newPoint(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve its ID.

		String id = xmlElement.getAttributeValue("id");

		// Instantiate a new point.

		Point point = new Point(id, getCategory());

		// Set the position.

		Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(id);

		point.setPosition(coord.x, coord.y);

		// Bind the attributes according to the filter.

		bindAttributesToElement(xmlElement, point);

		return point;
	}

	@Override
	protected Point newPointDiff(Element previousVersion, Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve its ID.

		String id = xmlElement.getAttributeValue("id");

		// Instantiate a new point.

		Point point = new Point(id, getCategory(), true);

		// TODO position? Shape?

		// Bind the attributes according to the filter.

		bindAttributesToElementDiff(previousVersion, xmlElement, point);

		return point;
	}

	@Override
	protected Line newLine(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve its ID.

		String id = xmlElement.getAttributeValue("id");

		// Instantiate a new line.

		Line line = new Line(id, getCategory());

		// Shape the line.

		nu.xom.Elements lineNodes = xmlElement.getChildElements("nd");

		if(this.onlyLineEndPointsConsidered) {
			addPointToLine(line, lineNodes, 0);
			addPointToLine(line, lineNodes, lineNodes.size() - 1);
		}
		else
			for(int i = 0, l = lineNodes.size(); i < l; ++i)
				addPointToLine(line, lineNodes, i);

		// Bind the attributes according to the filter.

		bindAttributesToElement(xmlElement, line);

		return line;
	}

	@Override
	protected Line newLineDiff(Element previousVersion, Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve its ID.

		String id = xmlElement.getAttributeValue("id");

		// Instantiate a new line.

		Line line = new Line(id, getCategory(), true);

		// Bind the attributes according to the filter.

		bindAttributesToElementDiff(previousVersion, xmlElement, line);

		return line;
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

	@Override
	protected Polygon newPolygon(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve its ID.

		String id = xmlElement.getAttributeValue("id");

		// Instantiate a new polygon.

		Polygon polygon = new Polygon(id, getCategory());

		// Shape the polygon. We can use addPointToLine(...) because a polygon
		// extends a line. The last node is not added as a point as it is the
		// same as the first node.

		nu.xom.Elements polygonNodes = xmlElement.getChildElements("nd");

		for(int i = 0, l = polygonNodes.size() - 1; i < l; ++i)
			addPointToLine(polygon, polygonNodes, i);

		// Bind the attributes according to the filter.

		bindAttributesToElement(xmlElement, polygon);

		return polygon;
	}

	@Override
	protected Polygon newPolygonDiff(Element previousVersion, Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve its ID.

		String id = xmlElement.getAttributeValue("id");

		// Instantiate a new polygon.

		Polygon polygon = new Polygon(id, getCategory(), true);

		// Bind the attributes according to the filter.

		bindAttributesToElementDiff(previousVersion, xmlElement, polygon);

		return polygon;
	}

	/**
	 * Copy the attributes of an element from its input format to the simple
	 * geometric format.
	 * 
	 * This is where an optional attribute filter is applied.
	 * 
	 * @param xmlElement
	 *            The XML element where attributes are copied from.
	 * @param element
	 *            The simple geometric element where filtered attributes are
	 *            copied to.
	 */
	protected void bindAttributesToElement(nu.xom.Element xmlElement, Element element) {

		// Retrieve all of the element attributes.

		nu.xom.Elements tags = xmlElement.getChildElements("tag");

		// Only keep the ones that are explicitly asked for.

		for(int i = 0, l = tags.size(); i < l; ++i) {

			nu.xom.Element tag = tags.get(i);

			String key = tag.getAttributeValue("k");
			String value = tag.getAttributeValue("v");

			if(this.filter == null || this.filter.isKept(key))
				element.setAttribute(key, value);
		}
	}

	protected void bindAttributesToElementDiff(Element previousVersion, nu.xom.Element xmlElement, Element element) {
		System.out.println("a " + previousVersion);
		// Retrieve all of the element attributes.

		HashMap<String, Object> previousVersionAttributes = previousVersion.getAttributes();

		nu.xom.Elements tags = xmlElement.getChildElements("tag");
		HashMap<String, Object> newVersionAttributes = new HashMap<String, Object>();
		for(int i = 0, l = tags.size(); i < l; ++i)
			newVersionAttributes.put(tags.get(i).getAttributeValue("k"), tags.get(i).getAttributeValue("v"));

		for(Entry<String, Object> entry : previousVersionAttributes.entrySet())
			if(!newVersionAttributes.containsKey(entry.getKey()))
				element.addRemovedAttribute(entry.getKey());
		
		for(Entry<String, Object> entry : newVersionAttributes.entrySet())
			if(!previousVersionAttributes.containsKey(entry.getKey()))
				element.setAttribute(entry.getKey(), entry.getValue());
		
		for(Entry<String, Object> entry : newVersionAttributes.entrySet())
			if(previousVersionAttributes.containsKey(entry.getKey()) && !previousVersionAttributes.get(entry.getKey()).equals(entry.getValue()))
				element.setAttribute(entry.getKey(), entry.getValue());

		System.out.println("b " + element);
	}
}
