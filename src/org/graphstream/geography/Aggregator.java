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

import org.graphstream.geography.ElementShape.Type;

/**
 * An aggregator is a format-specific reader that goes through all input files
 * specified by a geo source and stores (aggregates) the geographic features
 * that match the descriptors of the source.
 * 
 * The aggregated data is structured so that different versions of an element
 * are indexed by the time of their appearance.
 * 
 * It is possible to only aggregate features ID instead of keeping a record of
 * all their data (attributes and shape). This is especially useful to check at
 * which moments a feature appears without wasting memory.
 * 
 * @author Merwan Achibet
 */
public abstract class Aggregator {

	/**
	 * The source using the aggregator.
	 */
	protected GeoSource source;

	/**
	 * 
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
	public Aggregate read(boolean onlyReadId) {

		this.aggregate = new Aggregate();

		ArrayList<FileDescriptor> fileDescriptors = this.source.getFileDescriptors();

		for(FileDescriptor fileDescriptor : fileDescriptors) {

			this.currentFileName = fileDescriptor.getFileName();

			open(fileDescriptor);

			traverse(fileDescriptor, onlyReadId);

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
	abstract protected void traverse(FileDescriptor fileDescriptor, boolean onlyReadId);

	/**
	 * Close the input file described by a file descriptor.
	 * 
	 * @param fileDescriptor
	 */
	abstract protected void close(FileDescriptor fileDescriptor);

	/**
	 * 
	 * @param o
	 * @param onlyReadId
	 */
	protected void aggregate(Object o, Integer date, boolean onlyReadId) {

		String id = getFeatureId(o);

		Object object = onlyReadId ? id : o;

		this.aggregate.add(id, date, object);
	}

	// Abstract

	public abstract String getFeatureId(Object o);

	public abstract boolean isPoint(Object o);

	public abstract boolean isLine(Object o);

	public abstract boolean isPolygon(Object o);

	public boolean isOfType(Object o, ElementShape.Type type) {

		if(type == ElementShape.Type.POINT)
			return isPoint(o);

		if(type == ElementShape.Type.LINE)
			return isLine(o);

		if(type == ElementShape.Type.POLYGON)
			return isPolygon(o);

		return false;
	}

	public ElementShape.Type getType(Object o) {

		if(isPoint(o))
			return Type.POINT;

		if(isLine(o))
			return Type.LINE;

		if(isPolygon(o))
			return Type.POLYGON;

		return null;
	}

	public abstract boolean hasKey(Object o, String key);

	public abstract boolean hasKeyValue(Object o, String key, Object value);

	public abstract Object getAttributeValue(Object o, String key);

	public String getCurrentFileName() {

		return this.currentFileName;
	}

}
