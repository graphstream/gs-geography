package org.graphstream.geography.osm;

import nu.xom.Element;
import nu.xom.Elements;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class DescriptorOSM extends Descriptor {

	public DescriptorOSM(GeoSource source, String category, AttributeFilter filter) {
		super(source, category, filter);
	}

	@Override
	public boolean isPoint(Object o) {

		Element element = (Element)o;
		
		return element.getLocalName().equals("node");
	}

	@Override
	public boolean isLine(Object o) {

		Element element = (Element)o;
		
		return element.getLocalName().equals("way");
	}

	@Override
	public Point newPoint(Object o) {

		// Cast the object to a XOM element.

		Element element = (Element)o;
		
		// Retrieve its ID.

		String id = element.getAttributeValue("id");

		// Instantiate a new point.

		Point point = new Point(id, getCategory());

		// Set the position.
		
		Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(id);
		
		point.setPosition(coord.x, coord.y);

		// Bind the position as two "x" and "y" attributes.
		// XXX: too soon?
		point.addAttribute("x", coord.x);
		point.addAttribute("y", coord.y);	

		// Bind the other attributes according to the filter.

		Elements tags = element.getChildElements("tag");

		for(int i = 0, l = tags.size(); i < l; ++i) {

			Element tag = tags.get(i);
			
			String key = tag.getAttributeValue("k");
			String value = tag.getAttributeValue("v");

			if(this.filter == null || this.filter.isKept(key))
				point.addAttribute(key, value);
		}
		
		return point;
	}

	@Override
	public Line newLine(Object o) {

		// Cast the object to a GeoTools SimpleFeature.

		Element element = (Element)o;
		
		// Retrieve its ID.

		String id = element.getAttributeValue("id");

		// Instantiate a new line.

		Line line = new Line(id, getCategory());

		// Shape the line.
		
		Elements lineNodes = element.getChildElements("nd");
		
		for(int i = 0, l = lineNodes.size(); i < l; ++i) {
			
			Element lineNode = lineNodes.get(i);
			
			String lineNodeId = lineNode.getAttributeValue("ref");
			
			Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(lineNodeId);
			
			line.addPoint(lineNodeId, coord.x, coord.y);
		}
		
		// Bind the other attributes according to the filter.

		Elements tags = element.getChildElements("tag");

		for(int i = 0, l = tags.size(); i < l; ++i) {

			Element tag = tags.get(i);

			String key = tag.getAttributeValue("k");
			String value = tag.getAttributeValue("v");

			if(this.filter == null || this.filter.isKept(key))
				line.addAttribute(key, value);
		}

		return line;
	}

}
