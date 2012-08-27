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
import java.util.Map.Entry;

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
	 * The geometric type of the element.
	 */
	protected Type type;

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
	 * A key/value mapping of attributes that were removed from the previous
	 * version of the element.
	 */
	protected ArrayList<String> removedAttributes;

	/**
	 * Flag indicating if the element is base version or a diff version.
	 */
	protected boolean diff;

	/**
	 * Instantiate a new element.
	 * 
	 * @param id
	 *            The element ID.
	 */
	public Element(String id) {

		this(id, null, false);
	}

	/**
	 * Instantiate a new element.
	 * 
	 * @param id
	 *            The element ID.
	 * @param category
	 *            The element category.
	 */
	public Element(String id, String category) {

		this(id, category, false);
	}

	/**
	 * Instantiate a new element.
	 * 
	 * @param id
	 *            The element ID.
	 * @param category
	 *            The element category.
	 * @param diff
	 *            True if the element is a diff, false if it is a base.
	 */
	public Element(String id, String category, boolean diff) {

		this.id = new String(id);

		if(category != null)
			this.category = new String(category);

		this.diff = diff;
	}

	public Element(Element other) {

		this.id = new String(other.getId());
		this.category = new String(other.getCategory());
		this.diff = !other.isBase();

		HashMap<String, Object> otherAttributes = other.getAttributes();
		if(otherAttributes != null)
			for(Entry<String, Object> keyValuePair : otherAttributes.entrySet())
				setAttribute(new String(keyValuePair.getKey()), keyValuePair.getValue());

		ArrayList<String> otherRemovedAttributes = other.getRemovedAttributes();
		if(otherRemovedAttributes != null)
			for(String key : otherRemovedAttributes)
				addRemovedAttribute(new String(key));
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
	 * Give the geometric type of the element.
	 * 
	 * @return The geometric type.
	 */
	public Type getType() {

		return this.type;
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

		return this.type == Type.POINT;
	}

	/**
	 * Check if the element is a line.
	 * 
	 * @return True if the element is a line, false otherwise.
	 */
	public boolean isLine() {

		return this.type == Type.LINE;
	}

	/**
	 * Check if the element is a polygon.
	 * 
	 * @return True if the element is a polygon, false otherwise.
	 */
	public boolean isPolygon() {

		return this.type == Type.POLYGON;
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
	 * Add a new attribute to the element or modify its value if it already
	 * exists.
	 * 
	 * @param key
	 *            The key of the attribute.
	 * @param value
	 *            The value of the attribute.
	 */
	public void setAttribute(String key, Object value) {

		// Instantiate the map if has not been done yet.

		if(this.attributes == null)
			this.attributes = new HashMap<String, Object>();

		// Add the attribute to the map.

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
	 * Give the value of an attribute.
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
	 * Give all attributes.
	 * 
	 * @return A list of key/value pairs.
	 */
	public HashMap<String, Object> getAttributes() {

		if(this.attributes == null)
			return null;

		return this.attributes;
	}

	/**
	 * Check if the element possesses a specific attribute.
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
	 * Check if the element possesses a specific attribute AND if it equals the
	 * supplied value.
	 * 
	 * @param key
	 *            The key of the attribute.
	 * @param value
	 *            The value of the attribute.
	 * @return True if the exact same attribute exists, false otherwise.
	 */
	public boolean hasAttribute(String key, Object value) {

		return this.attributes != null && this.attributes.containsKey(key) && this.attributes.get(key).equals(value);
	}

	/**
	 * Add an attribute to the list of attributes removed since the last version
	 * of the element.
	 * 
	 * This list is only populated in diff versions of the element.
	 * 
	 * @param key
	 *            The key of the attribute.
	 */
	public void addRemovedAttribute(String key) {

		// instantiate the list if has not been done yet.

		if(this.removedAttributes == null)
			this.removedAttributes = new ArrayList<String>();

		// Add the key to the list.

		this.removedAttributes.add(key);
	}

	/**
	 * Give all the attributes that have been removed since the last version of
	 * the element.
	 * 
	 * @return A list of keys.
	 */
	public ArrayList<String> getRemovedAttributes() {

		return this.removedAttributes;
	}

	/**
	 * Check if the element is its own base version of a diff version.
	 * 
	 * @return True if the element is its own base version, false otherwise.
	 */
	public boolean isBase() {

		return !this.diff;
	}

	@Override
	public String toString() {

		String s = new String();

		if(isPoint())
			s += "Point";
		else if(isLine())
			s += "Line";
		else if(isPolygon())
			s += "Polygon";

		s += " | " + this.id;

		s += " | attributes: {";
		if(this.attributes != null)
			for(Entry<String, Object> keyValue : this.attributes.entrySet())
				s += " " + keyValue.getKey() + ":" + keyValue.getValue();
		s += " }";

		s += " | removed attributes: {";
		if(this.removedAttributes != null)
			for(String key : this.removedAttributes)
				s += " " + key;
		s += " }";

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
