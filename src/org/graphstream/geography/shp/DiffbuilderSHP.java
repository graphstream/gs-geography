package org.graphstream.geography.shp;

import java.util.Collection;
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
import org.graphstream.geography.osm.GeoSourceOSM;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class DiffbuilderSHP extends DiffBuilder {

	public DiffbuilderSHP(GeoSource source) {
		super(source);
	}

	@Override
	public ElementState diff(Element element, ElementState previousDiff, Integer previousDate, Object o) {

		ElementState nextDiff = null;

		// Cast the object to a GeoTools feature;

		SimpleFeature feature = (SimpleFeature)o;

		// Retrieve all of the object attributes.

		HashMap<String, Object> currentAttributes = new HashMap<String, Object>();

		Collection<Property> properties = feature.getProperties();

		for(Property property : properties)
			currentAttributes.put(property.getName().toString(), property.getValue());

		// If there is no previous diff of the element, copy all the attributes.

		if(previousDiff == null) {

			nextDiff = new ElementState(true);
			
			for(Entry<String, Object> entry : currentAttributes.entrySet())
				nextDiff.setChangedAttribute(entry.getKey(), entry.getValue());

			ElementShape.Type type = this.source.getAggregator().getType(o);

			ElementShape shape = null;

			if(type == Type.POINT) {

				Point point = new Point(element);

				Coordinate[] coord = ((Geometry)feature.getDefaultGeometry()).getCoordinates();
				point.setPosition(coord[0].x, coord[0].y);

				shape = point;
			}
			else if(type == Type.LINE) {

				Line line = new Line(element);

				// Shape the line.

				Coordinate[] coords = ((Geometry)feature.getDefaultGeometry()).getCoordinates();

//				if(this.onlyLineEndPointsConsidered) {
//					line.addPoint(null, coords[0].x, coords[0].y);
//					line.addPoint(null, coords[coords.length - 1].x, coords[coords.length - 1].y);
//				}
//				else
					for(int i = 0; i < coords.length; ++i)
						line.addPoint(null, coords[i].x, coords[i].y);

				shape = line;
			}
			else if(type == Type.POLYGON) {

				Polygon polygon = new Polygon(element);

				Coordinate[] coords = ((Geometry)feature.getDefaultGeometry()).getCoordinates();

				for(int i = 0; i < coords.length; ++i)
					polygon.addPoint(null, coords[i].x, coords[i].y);

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
