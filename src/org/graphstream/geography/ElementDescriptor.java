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

/**
 * The descriptor is the main tool to select geographic objects, filter them and
 * convert them from a library-dependent format to a more standard
 * representation.
 * 
 * The heart of a descriptor is its matching method. If an object matches the
 * inner definition of a descriptor then it is kept for the next step of the
 * import. The user can specify the geometric type of the objects he wants to
 * consider (points, lines or polygons) and the attributes they should possess.
 * 
 * Descriptors are also in charge of converting input geographical objects into
 * GraphStream geometric elements (simple Points, Lines and Polygons with
 * attributes). As such, the implementation of a descriptor needs to be capable
 * of determining the type of a feature and of instantiating a new Point, Line
 * and Polygon from the input format of the considered object by means of the
 * isXXX(), newXXX() and newXXXDiff() methods.
 * 
 * Moreover, the library-specific object is only converted after it has been
 * matched (for efficiency reasons). Thus, a descriptor must be capable of
 * reading its attribute from its original format using the hasKey() and
 * hasKeyValue() methods.
 * 
 * For all these reasons, each implementation of the Descriptor base class must
 * focus on a single input format.
 * 
 * Note that the attributes of matched elements are reduced to the set of
 * attributes specified by the user through a filtering process. This way, a lot
 * of memory is saved (some geographic files are huge) and the output graph is
 * not cluttered by meaningless data.
 * 
 * @author Merwan Achibet
 */
public abstract class ElementDescriptor {

	/**
	 * How is time considered?
	 * 
	 * NO_TIME: the temporal dimension is ignored.
	 * 
	 * TIME_FILE: each supplied file matches with a time step.
	 * 
	 * TIME_ATTRIBUTE: a specific attribute indicates the time step.
	 * 
	 * @author Merwan Achibet
	 */
	public static enum TimeConsideration {
		NO_TIME, TIME_FILE, TIME_ATTRIBUTE
	};

	/**
	 * The source using this descriptor.
	 */
	protected GeoSource source;

	/**
	 * The category name that will be attached to matched elements.
	 */
	protected String category;

	/**
	 * The filter applied on the attributes of matched elements.
	 */
	protected AttributeFilter filter;

	/**
	 * Optional element type (point, line or polygon) that a matching object
	 * must have.
	 */
	protected Element.Type type;

	/**
	 * Optional list of attribute keys that a matching object must have.
	 */
	protected List<String> mustHaveKeys;

	/**
	 * Optional list of attribute keys/values pairs that a matching object must
	 * have.
	 */
	protected HashMap<String, Object> mustHaveValues;

	/**
	 * The way that this descriptor takes time into account.
	 */
	protected TimeConsideration timeConsideration;

	/**
	 * Should matching elements be stored in the spatial index?
	 * 
	 * In some cases, geographic elements should be referenced in a spatial
	 * index to query them faster and ask questions like
	 * "what point are at (x,y)?" (especially with huge data sets using
	 * position-based relationships). In other cases, a spatial index is useless
	 * and would only slow down the import process.
	 * 
	 * It is the implementation programmer's choice to decide if he needs to
	 * spatially reference the described elements and it really depends on what
	 * he wants to achieve.
	 * 
	 * Note that this flag is descriptor-dependent because a category of
	 * elements (like points representing crossroads) could benefit from being
	 * spatially referenced where an other category of feature matched by an
	 * other descriptor (like lines representing roads) would not.
	 */
	protected boolean toSpatialIndex;

	/**
	 * Should we consider the full line or only its end points?
	 * 
	 * When this flag is set to true, the intermediary points of lines are
	 * ignored and only their extremities are considered. Ignoring these points
	 * can be appropriate when intermediary points only serves to shape the line
	 * and have no meaning other that this esthetic one.
	 */
	protected boolean onlyLineEndPointsConsidered;

	/**
	 * Instantiate a new descriptor.
	 * 
	 * @param source
	 *            The source using this descriptor.
	 * @param category
	 *            The category associated with matching elements.
	 * @param filter
	 *            The filter that will reduce the attributes of matching
	 *            elements.
	 */
	public ElementDescriptor(GeoSource source, String category, AttributeFilter filter) {

		this.source = source;
		this.category = category;
		this.filter = filter;

		this.timeConsideration = TimeConsideration.NO_TIME;

		this.toSpatialIndex = false;
		this.onlyLineEndPointsConsidered = false;
	}

	/**
	 * Give the name of the category of elements described by the descriptor.
	 * 
	 * @return The name of the element category.
	 */
	public String getCategory() {

		return new String(this.category);
	}

	/**
	 * Set the way that the temporal component is handled.
	 * 
	 * @param timeConsideration
	 *            The time consideration mode.
	 */
	public void setTimeConsideration(TimeConsideration timeConsideration) {

		this.timeConsideration = timeConsideration;
	}

	// TODO not happy with that...
	/**
	 * Give the date associated with a specific element version.
	 * 
	 * @param element
	 *            The element.
	 * @return The date.
	 */
	public Integer getDate(Element element) {

		// If time is not even considered, all events happen at the same time.

		if(this.timeConsideration == TimeConsideration.NO_TIME)
			return 0;

		// If time is file-based, the events happen in the order of the files.

		if(this.timeConsideration == TimeConsideration.TIME_FILE)
			return this.source.getCurrentFileIndex();

		// If time is attribute-based, read the corresponding attribute.

		if(this.timeConsideration == TimeConsideration.TIME_ATTRIBUTE)
			return 0; // TODO if such a file format exists

		return 0;
	}

	/**
	 * Set the descriptor to reference matching elements in a spatial index.
	 */
	public void sendElementsToSpatialIndex() {

		this.toSpatialIndex = true;

		this.source.prepareSpatialIndex();
	}

	/**
	 * Check if matching elements are referenced in a spatial index.
	 * 
	 * @return True if matching elements are spatially referenced, false
	 *         otherwise.
	 */
	public boolean areElementsSentToSpatialIndex() {

		return this.toSpatialIndex;
	}

	/**
	 * Set the descriptor to ignore the middle points of lines.
	 */
	public void onlyConsiderLineEndPoints() {

		this.onlyLineEndPointsConsidered = true;
	}

	/**
	 * Specify the geometric type of geographic objects described by this
	 * descriptor.
	 * 
	 * @param type
	 *            The geometric type of wanted objects.
	 */
	public void mustBe(Element.Type type) {

		this.type = type;
	}

	/**
	 * Specify an attribute key that geographic objects must possess to be kept.
	 * 
	 * These keys are accumulated with each call of this method.
	 * 
	 * @param attributeKey
	 *            The key of the attribute.
	 */
	public void mustHave(String attributeKey) {

		if(this.mustHaveKeys == null)
			this.mustHaveKeys = new ArrayList<String>();

		this.mustHaveKeys.add(attributeKey);
	}

	/**
	 * Specify an attribute key/value pair that geographic objects must possess
	 * to be kept.
	 * 
	 * These keys/values are accumulated with each call of this method.
	 * 
	 * @param attributeKey
	 *            The key of the attribute.
	 * @param attributeValue
	 *            The value of the attribute.
	 */
	public void mustHave(String attributeKey, Object attributeValue) {

		if(this.mustHaveValues == null)
			this.mustHaveValues = new HashMap<String, Object>();

		this.mustHaveValues.put(attributeKey, attributeValue);
	}

	/**
	 * Check if the supplied feature conforms to the inner definition of the
	 * descriptor.
	 * 
	 * If yes, the feature will be kept in memory and converted to a filtered
	 * standard geometric element.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the element conforms to the descriptor definition, false
	 *         otherwise.
	 */
	public boolean matches(Object o) {

		// Check for an optional geometric type condition.

		if(this.type != null && !isOfType(this.type, o))
			return false;

		// Check for optional attribute presence conditions.

		if(this.mustHaveKeys != null)
			for(String key : this.mustHaveKeys)
				if(!hasKey(key, o))
					return false;

		// Check for optional attribute value conditions.

		if(this.mustHaveValues != null)
			for(String key : this.mustHaveValues.keySet())
				if(!hasKeyValue(key, this.mustHaveValues.get(key), o))
					return false;

		return true;
	}

	/**
	 * Check if a geographic feature has a specific geometric type.
	 * 
	 * @param type
	 *            The geometric type.
	 * @param o
	 *            The geographic object.
	 * @return True is the object has the same type, false otherwise?
	 */
	public boolean isOfType(Element.Type type, Object o) {

		if(type == Element.Type.POINT)
			return isPoint(o);

		if(type == Element.Type.LINE)
			return isLine(o);

		if(type == Element.Type.POLYGON)
			return isPolygon(o);

		return false;
	}

	/**
	 * Give a simple geometric element (point, line or polygon with attributes)
	 * based on a library-dependent object.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A simple geometric element representing the object.
	 */
	public Element newElement(Object o) {

		if(isPoint(o))
			return newPoint(o);
		else if(isLine(o))
			return newLine(o);
		else if(isPolygon(o))
			return newPolygon(o);

		System.out.println("Warning: feature not recognized -> "+o);

		return null;
	}

	/**
	 * Give a simple geometric element representing the difference between the
	 * current state of a library-dependent object and a version from a previous
	 * date.
	 * 
	 * @param previousVersion
	 *            The previous version of the element.
	 * @param o
	 *            The new version of the object to convert.
	 * @return A simple geometric element representing the difference between
	 *         the two dates.
	 */
	public Element newElementDiff(Element previousVersion, Object o) {

		if(isPoint(o))
			return newPointDiff(previousVersion, o);
		else if(isLine(o))
			return newLineDiff(previousVersion, o);
		else if(isPolygon(o))
			return newPolygonDiff(previousVersion, o);

		System.out.println("Warning: feature not recognized -> "+o);

		return null;
	}

	@Override
	public String toString() {

		String s = new String();

		s += "Descriptor";

		if(this.type != null)
			s += " | type: " + this.type.toString();

		if(this.mustHaveKeys != null) {

			s += " | keys: { ";

			for(String key : this.mustHaveKeys)
				s += key + " ";

			s += "}";
		}

		if(this.mustHaveValues != null) {

			s += " | key/value pairs: { ";

			for(String key : this.mustHaveValues.keySet())
				s += key + "/" + this.mustHaveValues.get(key) + " ";

			s += "}";
		}

		if(this.onlyLineEndPointsConsidered)
			s += " | only line end points are considered";

		if(this.toSpatialIndex)
			s += " | matching elements stored in a spatial index";

		return s;
	}

	// Abstract

	/**
	 * Give the ID of a library-dependent geographic object.
	 * 
	 * @param o
	 *            The object.
	 * @return The ID of the object from its original format.
	 */
	public abstract String getElementId(Object o);

	/**
	 * Check if a geographic object, in its library-dependent form, represents a
	 * point.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return True if the object is a point, false otherwise.
	 */
	protected abstract boolean isPoint(Object o);

	/**
	 * Check if a geographic object, in its library-dependent form, represents a
	 * line.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return True if the object is a line, false otherwise.
	 */
	protected abstract boolean isLine(Object o);

	/**
	 * Check if a geographic object, in its library-dependent form, represents a
	 * polygon.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return True if the object is a polygon, false otherwise.
	 */
	protected abstract boolean isPolygon(Object o);

	/**
	 * Check if a geographic object, in its library-dependent form, possess a
	 * specific attribute key.
	 * 
	 * @param key
	 *            The attribute key.
	 * @param o
	 *            The geographic object.
	 * @return True if the object has the attribute key, false otherwise.
	 */
	protected abstract boolean hasKey(String key, Object o);

	/**
	 * Check if a geographic object, in its library-dependent form, possess a
	 * specific attribute key and a matching value.
	 * 
	 * @param key
	 *            The attribute key.
	 * @param value
	 *            The attribute value.
	 * @param o
	 *            The geographic object.
	 * @return True if the object has the attribute key and the same value,
	 *         false otherwise.
	 */
	protected abstract boolean hasKeyValue(String key, Object value, Object o);

	/**
	 * Give a simple point representation based on the supplied
	 * library-dependent geographical object.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A standard point.
	 */
	protected abstract Point newPoint(Object o);

	
	protected abstract Point newPointDiff(Element previousVersion, Object o);

	/**
	 * Give a simple line representation based on the supplied library-dependent
	 * geographical object.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A standard line.
	 */
	protected abstract Line newLine(Object o);

	protected abstract Line newLineDiff(Element previousVersion, Object o);

	/**
	 * Give a simple polygon representation based on the supplied
	 * library-dependent geographical object.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A standard polygon.
	 */
	protected abstract Polygon newPolygon(Object o);

	protected abstract Polygon newPolygonDiff(Element previousVersion, Object o);

}
