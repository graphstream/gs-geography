package org.graphstream.geography.shp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureIterator;
import org.graphstream.geography.Aggregator;
import org.graphstream.geography.ElementDescriptor;
import org.graphstream.geography.FileDescriptor;
import org.graphstream.geography.GeoSource;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class AggregatorSHP extends Aggregator {

	/**
	 * Iterator on the features from the shapefile.
	 */
	protected FeatureIterator<SimpleFeature> iterator;

	/**
	 * 
	 * @param source
	 */
	public AggregatorSHP(GeoSource source) {
		super(source);
	}

	@Override
	protected void open(FileDescriptor fileDescriptor) {

		try {

			File file = new File(fileDescriptor.getFileName());

			ShapefileDataStore store = new ShapefileDataStore(file.toURI().toURL());

			String type = store.getTypeNames()[0];
			FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource(type);

			this.iterator = source.getFeatures().features();
		}
		catch (IOException e) {

			throw new RuntimeException("I/O error : " + e.getMessage());
		}

	}

	@Override
	protected void traverse(FileDescriptor fileDescriptor, boolean onlyReadId) {

		while(this.iterator.hasNext()) {

			SimpleFeature feature = this.iterator.next();

			for(ElementDescriptor descriptor : fileDescriptor.getDescriptors())
				if(descriptor.matches(feature, this)) {
					
					Integer date = this.source.getTemporalLocator().date(feature);
					
					aggregate(feature, date, onlyReadId);
				}
		}
	}

	@Override
	protected void close(FileDescriptor fileDescriptor) {

		this.iterator.close();
		this.iterator = null;
	}

	@Override
	public String getFeatureId(Object o) {

		// Cast the object to a GeoTools feature.

		SimpleFeature feature = (SimpleFeature)o;

		// Return its ID.

		return feature.getID();
	}

	@Override
	public boolean isPoint(Object o) {

		// Cast the object to a GeoTools feature.

		SimpleFeature feature = (SimpleFeature)o;

		// Check the binding.

		return feature.getType().getGeometryDescriptor().getType().getBinding() == com.vividsolutions.jts.geom.Point.class;
	}

	@Override
	public boolean isLine(Object o) {

		// Cast the object to a GeoTools feature.

		SimpleFeature feature = (SimpleFeature)o;

		// Check the binding.

		Class<?> binding = feature.getType().getGeometryDescriptor().getType().getBinding();

		return binding == com.vividsolutions.jts.geom.MultiLineString.class || binding == com.vividsolutions.jts.geom.LineString.class;
	}

	@Override
	public boolean isPolygon(Object o) {

		// Cast the object to a GeoTools feature.

		SimpleFeature feature = (SimpleFeature)o;

		// Check the binding.

		Class<?> binding = feature.getType().getGeometryDescriptor().getType().getBinding();

		return binding == com.vividsolutions.jts.geom.Polygon.class;
	}

	@Override
	public boolean hasKey(Object o, String key) {

		// Cast the object to a GeoTools feature.

		SimpleFeature feature = (SimpleFeature)o;

		// Go through the feature properties until the same key is found.

		Collection<Property> properties = feature.getProperties();

		for(Property property : properties)
			if(property.getName().toString().equals(key))
				return true;

		return false;
	}

	@Override
	public boolean hasKeyValue(Object o, String key, Object value) {

		// Cast the object to a GeoTools feature.

		SimpleFeature feature = (SimpleFeature)o;

		// Go through the feature properties until the same key and value are
		// found.

		Collection<Property> properties = feature.getProperties();

		for(Property property : properties)
			if(property.getName().toString().equals(key) && property.getValue().equals(value))
				return true;

		return false;
	}

	@Override
	public Object getAttributeValue(Object o, String key) {

		// Cast the object to a GeoTools feature.

		SimpleFeature feature = (SimpleFeature)o;

		// Go through the feature properties until the same key and value are
		// found.

		Collection<Property> properties = feature.getProperties();

		for(Property property : properties)
			if(property.getName().toString().equals(key))
				return property.getValue();

		return null;
	}

}
