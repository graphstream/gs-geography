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

import org.graphstream.geography.index.SpatialIndexPoint;

/**
 * An abstract geometric element.
 * 
 * This class serves as a base for Point, Line and Polygon. These are used as
 * intermediary representations for any kind of geographical features coming
 * from an external source. The original input formats can be quite
 * heterogeneous as the data they contain is library-dependent and we don't want
 * to force the user to learn GeoTools, XOM or whatever other library.
 * 
 * An Element basically consists of a unique identifier and a list of attributes
 * copied from the original format of the feature (potentially filtered).
 * 
 * An element can exist in two flavors. The "base" version is a complete element
 * with some attributes. The "diff" version is a partial element that is based
 * on a "base" element and only contains the differences between these two
 * versions. This duality becomes useful when dealing with several time steps.
 * The change of an element with respect to time is represented by a chain
 * starting with a "base" element and followed by zero or more "diff" elements.
 * 
 * @author Merwan Achibet
 */
public abstract class Element {

	public static enum Type {
		POINT, LINE, POLYGON
	};

	/**
	 * The ID of the feature.
	 */
	protected String id;

	/**
	 * The category of the element (attributed by the descriptor that
	 * instantiated the object from a matching feature).
	 */
	protected String category;

	/**
	 * A key/value mapping of attributes.
	 */
	protected HashMap<String, Object> attributes;

	/**
	 * A key/value mapping of attributes.
	 */
	protected ArrayList<String> removedAttributes;

	/**
	 * 
	 */
	protected boolean diff;

	/**
	 * Instantiate a new element.
	 * 
	 * @param id
	 *            The element ID.
	 * @param category
	 *            The element category.
	 */
	public Element(String id, String category) {

		this.id = id;
		this.category = category;
	}

	/**
	 * Give the ID of the element.
	 * 
	 * @return The ID.
	 */
	public String getId() {

		return new String(this.id);
	}

	/**
	 * Give the category of the element.
	 * 
	 * @return The category.
	 */
	public String getCategory() {

		return new String(this.category);
	}

	/**
	 * Check if the element is of a given category.
	 * 
	 * @param category
	 *            The category.
	 * @return True if the element has the same category, false otherwise.
	 */
	public boolean isCategory(String category) {

		return this.category != null && this.category.equals(category);
	}

	/**
	 * Add an attribute to the element.
	 * 
	 * @param key
	 *            The key of the attribute.
	 * @param value
	 *            The value of the attribute.
	 */
	public void setAttribute(String key, Object value) {

		if(this.attributes == null)
			this.attributes = new HashMap<String, Object>();

		this.attributes.put(key, value);
	}

	/**
	 * Remove an attribute from the element.
	 * 
	 * @param key
	 *            The key of the attribute to remove.
	 */
	public void removeAttribute(String key) {

		if(this.attributes != null)
			this.attributes.remove(key);
	}

	/**
	 * Give the value of a stored attribute.
	 * 
	 * @param key
	 *            The key of the attribute.
	 * @return The value of the attribute or null if it does not exist.
	 */
	public Object getAttribute(String key) {

		if(this.attributes == null)
			return null;

		return this.attributes.get(key);
	}

	/**
	 * Give all of the stored attributes.
	 * 
	 * @return A list of key/value pairs.
	 */
	public HashMap<String, Object> getAttributes() {

		if(this.attributes == null)
			return null;

		return new HashMap<String, Object>(this.attributes);
	}

	/**
	 * Check if the element possesses an attribute.
	 * 
	 * @param key
	 *            The key of the attribute.
	 * @return True if the attribute is possessed by the element, false
	 *         otherwise.
	 */
	public boolean hasAttribute(String key) {

		return this.attributes != null && this.attributes.containsKey(key);
	}

	/**
	 * Check if the element possesses the supplied attribute AND if it equals
	 * the supplied value.
	 * 
	 * @param key
	 *            The key of the attribute.
	 * @param value
	 *            The value that the attribute should have.
	 * @return True if the attribute matches, false otherwise.
	 */
	public boolean hasAttribute(String key, Object value) {

		return this.attributes != null && this.attributes.containsKey(key) && this.attributes.get(key).equals(value);
	}

	/**
	 * Give all of the stored attributes.
	 * 
	 * @return A list of key/value pairs.
	 */
	public ArrayList<String> getRemovedAttributes() {

		return this.removedAttributes;
	}

	/**
	 * Check if the element is its base version of it is based on another one.
	 * 
	 * @return True if the element is its own base version, false otherwise.
	 */
	public boolean isBase() {

		return !this.diff;
	}

	/**
	 * Check if the element has a specific geometric type.
	 * 
	 * @param type
	 *            The geometric type.
	 * @return True if the element has the same type, false otherwise.
	 */
	public boolean isType(Type type) {

		if(type == Type.POINT)
			return isPoint();

		if(type == Type.LINE)
			return isLine();

		if(type == Type.POLYGON)
			return isPolygon();

		return false;
	}

	/**
	 * Check if the element is a point.
	 * 
	 * @return True if the element is a point, false otherwise.
	 */
	public boolean isPoint() {

		return this instanceof Point;
	}

	/**
	 * Check if the element is a line.
	 * 
	 * @return True if the element is a line, false otherwise.
	 */
	public boolean isLine() {

		return this instanceof Line;
	}

	// XXX what does "poly instanceOf Line" return? Does this really work?
	/**
	 * Check if the element is a polygon.
	 * 
	 * @return True if the element is a polygon, false otherwise.
	 */
	public boolean isPolygon() {

		return this instanceof Polygon;
	}

	@Override
	public String toString() {

		String s = new String();

		if(isPoint())
			s += "Point ";
		else if(isLine())
			s += "Line ";
		else if(isPolygon())
			s += "Polygon ";

		s += "[" + this.id + "] ";

		s += "{ ";
		if(this.attributes != null)
			for(String key : this.attributes.keySet())
				s += key + ":" + this.attributes.get(key) + " ";
		s += "}";

		return s;
	}

	// Abstract

	/**
	 * Give special points that spatially represent the shape of the element and
	 * will be stored in a spatial index.
	 * 
	 * @return A list of spatial references to the shape of the element.
	 */
	public abstract List<SpatialIndexPoint> toSpatialIndexPoints();

}
