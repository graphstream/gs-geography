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

/**
 * 
 * @author Merwan Achibet
 */
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
					
					aggregate(feature, date, descriptor, onlyReadId);
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
