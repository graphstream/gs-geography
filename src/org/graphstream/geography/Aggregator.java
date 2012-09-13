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

package org.graphstream.geography;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.graphstream.geography.ElementShape.Type;

/**
 * An aggregator is a format-specific reader that goes through all input files
 * specified by a geo source and stores (aggregates) the geographic features
 * that match the descriptors of the source.
 * 
 * The aggregated data is structured so that different versions of an element
 * are indexed by the time of their appearance.
 * 
 * @author Merwan Achibet
 */
public abstract class Aggregator {

	/**
	 * The source using the aggregator.
	 */
	protected GeoSource source;

	/**
	 * The results aggregated while the file are read.
	 */
	protected Aggregate aggregate;

	/**
	 * The name of the file currently traversed.
	 */
	protected String currentFileName;

	/**
	 * Instantiate a new aggregator.
	 * 
	 * @param source
	 *            The source using the aggregator.
	 */
	public Aggregator(GeoSource source) {

		this.source = source;
	}

	/**
	 * Go in order through the input files specified by the geo source and
	 * aggregate relevant features.
	 * 
	 * If the onlyReadId flag is set to true then only the IDs of aggregated
	 * features will be in the final aggregate.
	 * 
	 * @param onlyReadId
	 *            The flag to only aggregate IDs.
	 * @return An aggregate containing the relevant features from the input
	 *         files.
	 */
	public Aggregate read() {

		this.aggregate = new Aggregate();

		ArrayList<FileDescriptor> fileDescriptors = this.source.getFileDescriptors();

		for(FileDescriptor fileDescriptor : fileDescriptors) {

			this.currentFileName = fileDescriptor.getFileName();

			open(fileDescriptor);

			traverse(fileDescriptor);

			close(fileDescriptor);
		}

		return this.aggregate;
	}

	/**
	 * Open the input file described by a file descriptor.
	 * 
	 * @param fileDescriptor
	 */
	abstract protected void open(FileDescriptor fileDescriptor);

	/**
	 * Go through the content of the input file described by a file descriptor
	 * and aggregate the appropriate data.
	 * 
	 * @param fileDescriptor
	 *            The file descriptor.
	 * @param onlyReadId
	 *            The flag to only aggregate IDs.
	 */
	abstract protected void traverse(FileDescriptor fileDescriptor);

	/**
	 * Close the input file described by a file descriptor.
	 * 
	 * @param fileDescriptor
	 */
	abstract protected void close(FileDescriptor fileDescriptor);

	/**
	 * Aggregate a library-specific geographic object appearing at a specific
	 * date.
	 * 
	 * @param o
	 *            The geographic object in its library-dependent form (XOM,
	 *            GeoTools, ...).
	 * @param date
	 *            The date of appearance of the object.
	 * @param descriptor
	 *            The descriptor that matched the object.
	 */
	protected void aggregate(Object o, Integer date, ElementDescriptor descriptor) {

		String id = getFeatureId(o);

		this.aggregate.add(id, date, o);

		this.aggregate.setDescriptorUsed(id, descriptor);
	}

	/**
	 * Get the name of the cile being currently read.
	 * 
	 * @return The file name.
	 */
	public String getCurrentFileName() {

		return this.currentFileName;
	}

	// Abstract

	/*
	 * All of the following abstract methods must be overridden in the
	 * format-specific implementations of Aggregator. They answer to questions
	 * about the shape of a geographic object and its attributes.
	 */

	/**
	 * Give the ID of the geographic object (Typically the same as in the file
	 * it is coming from).
	 * 
	 * @param o
	 *            The geographic object.
	 * @return The ID of the object.
	 */
	public abstract String getFeatureId(Object o);

	/**
	 * Check if the geographic object has a given key within its attributes.
	 * 
	 * @param o
	 *            The geographic object.
	 * @param key
	 *            The attribute key.
	 * @return True if the object has an attribute with this key, false
	 *         otherwise.
	 */
	public abstract boolean hasKey(Object o, String key);

	/**
	 * Check if the geographic object has a given key/value pair within its
	 * attribute.
	 * 
	 * @param o
	 *            The geographic object.
	 * @param key
	 *            The attribute key.
	 * @param value
	 *            The attribute value.
	 * @return True if the object has an attribute with these key and value,
	 *         false otherwise.
	 */
	public abstract boolean hasKeyValue(Object o, String key, Object value);

	/**
	 * Give the value of the attribute of a geometric object with a given key.
	 * 
	 * @param o
	 *            The geographic object.
	 * @param key
	 *            The attribute key.
	 * @return The attribute value or null if there is no attribute with such a
	 *         key.
	 */
	public abstract Object getAttributeValue(Object o, String key);

	/**
	 * Give the attributes of the given geographic object.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return The attributes of the object.
	 */
	public abstract HashMap<String, Object> getAttributes(Object o);

	/**
	 * Check if the geographic object is a point.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return True if the object is a point, false otherwise.
	 */
	public abstract boolean isPoint(Object o);

	/**
	 * Check if the geographic object is a line.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return True if the object is a line, false otherwise.
	 */
	public abstract boolean isLine(Object o);

	/**
	 * Check if the geographic object is a polygon.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return True if the object is a polygon, false otherwise.
	 */
	public abstract boolean isPolygon(Object o);

	/**
	 * Check if the shape of a geographic object is the same as the given shape
	 * type.
	 * 
	 * @param o
	 *            The geographic object.
	 * @param type
	 *            The shape type.
	 * @return True if the object has the same shape, false otherwise.
	 */
	public boolean isOfType(Object o, ElementShape.Type type) {

		if(type == ElementShape.Type.POINT)
			return isPoint(o);

		if(type == ElementShape.Type.LINE)
			return isLine(o);

		if(type == ElementShape.Type.POLYGON)
			return isPolygon(o);

		return false;
	}

	/**
	 * Give the type of shape of a given geographic object.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return The shape type of the object.
	 */
	public ElementShape.Type getShapeType(Object o) {

		if(isPoint(o))
			return Type.POINT;

		if(isLine(o))
			return Type.LINE;

		if(isPolygon(o))
			return Type.POLYGON;

		return Type.UNSPECIFIED;
	}

	/**
	 * Give a list of vertices describing the shape of the given geographic
	 * object.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return The vertices describing the shape.
	 */
	public List<Vertex> getShapeVertices(Object o) {

		if(isPoint(o))
			return getPointVertices(o);

		if(isLine(o))
			return getLineVertices(o);

		if(isPolygon(o))
			return getPolygonVertices(o);

		return null;
	}

	protected abstract List<Vertex> getPointVertices(Object o);

	protected abstract List<Vertex> getLineVertices(Object o);

	protected abstract List<Vertex> getPolygonVertices(Object o);

}
