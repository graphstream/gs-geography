package org.graphstream.geography.shp;

import java.util.Collection;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public abstract class DescriptorSHP extends Descriptor {

	public DescriptorSHP(GeoSource source, String category, AttributeFilter filter) {
		super(source, category, filter);
	}

	@Override
	public boolean isPoint(Object o) {

		// TODO: A better way to do this?
		SimpleFeature feature = (SimpleFeature)o;
		return (feature.getType().getGeometryDescriptor().getType().getBinding() == com.vividsolutions.jts.geom.Point.class);

	}

	@Override
	public boolean isLine(Object o) {

		// TODO: A better way to do this?
		SimpleFeature feature = (SimpleFeature)o;
		Class<?> binding = feature.getType().getGeometryDescriptor().getType().getBinding();

		return ((binding == com.vividsolutions.jts.geom.MultiLineString.class) || (binding == com.vividsolutions.jts.geom.LineString.class));
	}

	@Override
	public Point newPoint(Object o) {

		// Cast the object to a GeoTools SimpleFeature.

		SimpleFeature feature = (SimpleFeature)o;

		// Retrieve its ID.

		String id = feature.getID();

		// Instantiate a new point.

		Point point = new Point(id, getCategory());

		// Set the position.
		
		Coordinate[] coord = ((Geometry)feature.getDefaultGeometry()).getCoordinates();
		
		point.setPosition(coord[0].x, coord[0].y);

		// Bind the position as two "x" and "y" attributes.
		// XXX: too soon?
		point.addAttribute("x", point.getPosition().x);
		point.addAttribute("y", point.getPosition().y);

		// Bind the other attributes according to the filter.

		Collection<Property> properties = feature.getProperties();

		for(Property property : properties)
			if(this.filter == null || this.filter.isKept(property.getName().toString()))
				point.addAttribute(property.getName().toString(), property.getValue());

		return point;
	}

	@Override
	public Line newLine(Object o) {

		// Cast the object to a GeoTools SimpleFeature.

		SimpleFeature feature = (SimpleFeature)o;

		// Retrieve its ID.

		String id = feature.getID();

		// Instantiate a new line.

		Line line = new Line(id, getCategory());

		// Shape the line.
		
		Coordinate[] coords = ((Geometry)feature.getDefaultGeometry()).getCoordinates();
		/*
		for(int i = 0; i < coords.length; ++i)
			line.addPoint(coords[i].x, coords[i].y);
		*/
		// Bind the attributes according to the filter.

		Collection<Property> properties = feature.getProperties();

		for(Property property : properties)
			if(this.filter == null || this.filter.isKept(property.getName().toString()))
				line.addAttribute(property.getName().toString(), property.getValue());
		
		return line;
	}

}
