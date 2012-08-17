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

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.Element;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.graphstream.geography.Polygon;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A descriptor for features coming from OpenStreetMap files.
 * 
 * @author Merwan Achibet
 */
public class DescriptorOSM extends Descriptor {

	public DescriptorOSM(GeoSource source, String category, AttributeFilter filter) {
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
		// open (otherwise it would be a polygon).

		return xmlElement.getLocalName().equals("way") && !isClosed(xmlElement);
	}

	@Override
	protected boolean isPolygon(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// A polygon is a closed way.

		return xmlElement.getLocalName().equals("way") && isClosed(xmlElement);
	}

	protected boolean isClosed(nu.xom.Element xmlElement) {

		nu.xom.Elements xmlNodes = xmlElement.getChildElements("nd");

		String idFirst = xmlNodes.get(0).getAttributeValue("ref");
		String idLast = xmlNodes.get(xmlNodes.size() - 1).getAttributeValue("ref");

		return idFirst.equals(idLast);
	}

	@Override
	protected boolean hasKey(String key, Object o) {

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		for(int i = 0, l = xmlTags.size(); i < l; ++i) {

			nu.xom.Element xmlTag = xmlTags.get(i);

			if(xmlTag.getAttribute("k").getValue().equals(key))
				return true;
		}

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

			nu.xom.Element firstNode = lineNodes.get(0);
			String firstNodeId = firstNode.getAttributeValue("ref");
			Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(firstNodeId);
			line.addPoint(firstNodeId, coord.x, coord.y);
			
			nu.xom.Element lastNode = lineNodes.get(lineNodes.size()-1);
			String lastNodeId = lastNode.getAttributeValue("ref");
			coord = ((GeoSourceOSM)this.source).getNodePosition(lastNodeId);
			line.addPoint(lastNodeId, coord.x, coord.y);
		}
		else
			for(int i = 0, l = lineNodes.size(); i < l; ++i) {

				nu.xom.Element lineNode = lineNodes.get(i);
				String lineNodeId = lineNode.getAttributeValue("ref");
				Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(lineNodeId);
				line.addPoint(lineNodeId, coord.x, coord.y);
			}

		// Bind the attributes according to the filter.

		bindAttributesToElement(xmlElement, line);

		return line;
	}

	@Override
	protected Polygon newPolygon(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve its ID.

		String id = xmlElement.getAttributeValue("id");

		// Instantiate a new polygon.

		Polygon polygon = new Polygon(id, getCategory());

		// Shape the polygon.

		nu.xom.Elements polygonNodes = xmlElement.getChildElements("nd");

		for(int i = 0, l = polygonNodes.size() - 1; i < l; ++i) {

			nu.xom.Element polygonNode = polygonNodes.get(i);

			String polygonNodeId = polygonNode.getAttributeValue("ref");

			Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(polygonNodeId);

			polygon.addPoint(polygonNodeId, coord.x, coord.y);
		}

		// Bind the attributes according to the filter.

		bindAttributesToElement(xmlElement, polygon);

		return polygon;
	}

	/**
	 * Copy the attributes of an element from its input format to the simple
	 * geometric format. This is where a potential attribute filter is applied.
	 * 
	 * @param xmlElement
	 *            The XOM element where attributes are copied from.
	 * @param element
	 *            The simple geometric element where filtered attributes are
	 *            copied to.
	 */
	protected void bindAttributesToElement(nu.xom.Element xmlElement, Element element) {

		nu.xom.Elements tags = xmlElement.getChildElements("tag");

		for(int i = 0, l = tags.size(); i < l; ++i) {

			nu.xom.Element tag = tags.get(i);

			String key = tag.getAttributeValue("k");
			String value = tag.getAttributeValue("v");

			if(this.filter == null || this.filter.isKept(key))
				element.addAttribute(key, value);
		}
	}

}
