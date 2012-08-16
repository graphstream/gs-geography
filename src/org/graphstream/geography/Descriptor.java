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
 * Definition of some elements that the user wants to keep in/out of the output
 * graph.
 * 
 * The heart of a descriptor is its matching method. If an object matches the
 * inner definition of a descriptor then it is kept for the next step of the
 * import. The attributes of the element are also filtered using the attribute
 * filter provided via the constructor.
 * 
 * Descriptors are in charge of converting input geographical features into
 * GraphStream geometric elements (simple Points, Lines and Polygons with
 * attributes). As such, the implementation of a descriptor needs to be capable
 * of determining the type of a feature (point, line or polygon) and to
 * instantiate new Points, Lines and Polygons from the input format of the
 * considered objects by means of the isXXX() and newXXX() methods.
 * 
 * @author Merwan Achibet
 */
public abstract class Descriptor {

	/**
	 * The source using this descriptor.
	 */
	protected GeoSource source;

	/**
	 * The ID of the described class of elements.
	 */
	protected String category;

	/**
	 * Filter for the attributes of the described element.
	 */
	protected AttributeFilter filter;

	/**
	 * Optional element type (point, line, polygon) to filter down the features
	 * matched by this descriptor.
	 */
	protected Element.Type type;

	/**
	 * Optional list of attribute keys that a matching feature must have.
	 */
	protected List<String> mustHaveKeys;

	/**
	 * Optional list of attribute keys/values that a matching feature must have.
	 */
	protected HashMap<String, Object> mustHaveValues;

	/**
	 * Instantiate a descriptor.
	 * 
	 * @param source
	 * @param category
	 * @param filter
	 */
	public Descriptor(GeoSource source, String category, AttributeFilter filter) {

		this.source = source;
		this.category = category;
		this.filter = filter;
	}

	/**
	 * Give the name of the category of elements described by the descriptor.
	 * 
	 * @return The name of the element category.
	 */
	public String getCategory() {

		return new String(this.category);
	}

	public void mustBe(Element.Type type) {

		this.type = type;
	}

	public void mustHave(String attributeKey) {

		if(this.mustHaveKeys == null)
			this.mustHaveKeys = new ArrayList<String>();

		this.mustHaveKeys.add(attributeKey);
	}

	public void mustHave(String attributeKey, Object attributeValue) {

		if(this.mustHaveValues == null)
			this.mustHaveValues = new HashMap<String, Object>();

		this.mustHaveValues.put(attributeKey, attributeValue);
	}

	/**
	 * Check if the supplied feature conforms to the inner definition of the
	 * descriptor.
	 * 
	 * @param element
	 *            The considered element.
	 * @return True if the element should be categorized by the descriptor,
	 *         false otherwise.
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

	public String toString() {

		String s = new String();

		s += super.toString() + " ";

		if(this.mustHaveKeys != null) {

			s += "must have keys { ";
			
			for(String key : this.mustHaveKeys)
				s += key + " ";
			
			s += "} ";
		}

		if(this.mustHaveValues != null) {
		
			s += "must have key/value pairs { ";
			
			for(String key : this.mustHaveValues.keySet())
				s += key + "/" + this.mustHaveValues.get(key) + " ";
			
			s += "}";
		}

		return s;
	}

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
	 * Give the considered feature in the GraphStream geometric format.
	 * 
	 * @param o
	 *            The feature to convert.
	 * @return A simple geometric element.
	 */
	public Element newElement(Object o) {
		
		if(isPoint(o))
			return newPoint(o);
		else if(isLine(o))
			return newLine(o);
		else if(isPolygon(o))
			return newPolygon(o);

		// XXX What happens in other cases?

		return null;
	}

	// Abstract

	/**
	 * Check if the supplied feature is a point according to the descriptor
	 * rules.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the feature is a point, false otherwise (line or
	 *         polygon).
	 */
	protected abstract boolean isPoint(Object o);

	/**
	 * Check if the supplied feature is a line according to the descriptor
	 * rules.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the feature is a line, false otherwise (point or
	 *         polygon).
	 */
	protected abstract boolean isLine(Object o);
	
	/**
	 * Check if the supplied feature is a polygon according to the descriptor
	 * rules.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the feature is a polygon, false otherwise (point or
	 *         polygon).
	 */
	protected abstract boolean isPolygon(Object o);

	protected abstract boolean hasKey(String key, Object o);
	
	protected abstract boolean hasKeyValue(String key, Object value, Object o);
	
	/**
	 * Give a GraphStream geometric element based on the supplied feature.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A graphStream geometric element.
	 */
	protected abstract Point newPoint(Object o);
	
	/**
	 * Give a GraphStream geometric element based on the supplied feature.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A graphStream geometric element.
	 */
	protected abstract Line newLine(Object o);

	/**
	 * Give a GraphStream geometric element based on the supplied feature.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A graphStream geometric element.
	 */
	protected abstract Line newPolygon(Object o);

}
