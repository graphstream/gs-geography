package org.graphstream.geography.shp;

import java.util.Collection;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.Point;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class DescriptorSHP extends Descriptor {

	public DescriptorSHP(String category, AttributeFilter filter) {
		super(category, filter);
	}
	
	@Override
	public boolean matches(Object o) {

		Point p = (Point)o;

		if(p.hasAttribute("INTRSECT") && p.getAttribute("INTRSECT").equals("Y"))
			return true;
		
		return false;
	}

	@Override
	public boolean isPoint(Object o) {
		
		// TODO: A better way to do this?
		SimpleFeature feature = (SimpleFeature)o;
		return(feature.getType().getGeometryDescriptor().getType().getBinding() == com.vividsolutions.jts.geom.Point.class);
			
	}
	
	protected boolean isPolyline(SimpleFeature feature)
	{
		// TODO: A better way to do this?
		Class<?> binding = feature.getType().getGeometryDescriptor().getType().getBinding();
		
		return( ( binding == com.vividsolutions.jts.geom.MultiLineString.class )
		     || ( binding == com.vividsolutions.jts.geom.LineString.class ) );
	}
	
	@Override
	public Point newPoint(Object o) {
		
		// Cast the object to a GeoTools SimpleFeature.
		
		SimpleFeature feature = (SimpleFeature)o;
		
		// Retrieve its ID.
		
		String id = feature.getID();
		
		// Instantiate a new point.
		
		Point point = new Point(id);
		
		// Bind the position as two "x" and "y" attributes.
		
		Coordinate[] c = ((Geometry)feature.getDefaultGeometry()).getCoordinates();
		point.addAttribute("x", c[0].x);
		point.addAttribute("y", c[0].y);
		
		// Bind the other attributes according to the filter.
		
		Collection<Property> properties = feature.getProperties();
		
		for(Property property : properties)
			if(this.filter == null || this.filter.isKept(property.getName().toString()))
				point.addAttribute(property.getName().toString(), property.getValue());
		
		return point;
	}
	
}
