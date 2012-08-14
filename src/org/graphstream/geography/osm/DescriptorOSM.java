package org.graphstream.geography.osm;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.Element;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.graphstream.geography.Polygon;

import com.vividsolutions.jts.geom.Coordinate;

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

		// The name of XML entries representing lines is "way".

		return xmlElement.getLocalName().equals("way");
	}
	
	@Override
	protected boolean isPolygon(Object o) {

		// Cast the object to a XOM element.
		
		nu.xom.Element xmlElement = (nu.xom.Element)o;
		
		// A polygon is a way...
		
		if(!xmlElement.getLocalName().equals("way"))
			return false;
		
		// ... That is closed (start and end points are the same).
		
		nu.xom.Elements xmlNodes = xmlElement.getChildElements("nd");
		String idFirst = xmlNodes.get(0).getAttributeValue("ref");
		String idLast = xmlNodes.get(0).getAttributeValue("ref");
		
		return idFirst.equals(idLast);
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

		// Bind the position as two "x" and "y" attributes.
		// XXX: too soon?
		point.addAttribute("x", coord.x);
		point.addAttribute("y", coord.y);

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
	protected Line newPolygon(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve its ID.

		String id = xmlElement.getAttributeValue("id");

		// Instantiate a new line.

		Polygon polygon = new Polygon(id, getCategory());

		// Shape the line.

		nu.xom.Elements lineNodes = xmlElement.getChildElements("nd");

		for(int i = 0, l = lineNodes.size(); i < l; ++i) {

			nu.xom.Element lineNode = lineNodes.get(i);

			String lineNodeId = lineNode.getAttributeValue("ref");

			Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(lineNodeId);

			polygon.addPoint(lineNodeId, coord.x, coord.y);
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
