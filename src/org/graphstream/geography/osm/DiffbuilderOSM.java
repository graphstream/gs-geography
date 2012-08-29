package org.graphstream.geography.osm;

import java.util.HashMap;
import java.util.Map.Entry;

import org.graphstream.geography.DiffBuilder;
import org.graphstream.geography.Element;
import org.graphstream.geography.ElementShape;
import org.graphstream.geography.ElementShape.Type;
import org.graphstream.geography.ElementState;
import org.graphstream.geography.ElementView;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.graphstream.geography.Polygon;

import com.vividsolutions.jts.geom.Coordinate;

public class DiffbuilderOSM extends DiffBuilder {

	public DiffbuilderOSM(GeoSource source) {
		super(source);
	}

	@Override
	public ElementState diff(Element element, ElementState previousDiff, Integer previousDate, Object o) {

		ElementState nextDiff = null;

		// Cast the object to a XOM element;

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve all of the object attributes.

		HashMap<String, Object> currentAttributes = new HashMap<String, Object>();

		nu.xom.Elements tags = xmlElement.getChildElements("tag");

		for(int i = 0, l = tags.size(); i < l; ++i)
			currentAttributes.put(tags.get(i).getAttributeValue("k"), tags.get(i).getAttributeValue("v"));

		// If there is no previous diff of the element, copy all the attributes.

		if(previousDiff == null) {

			nextDiff = new ElementState(true);

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

			nextDiff = new ElementState();
			
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
