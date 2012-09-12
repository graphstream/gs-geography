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
 * The descriptor is the main tool to select geographic objects and filter them.
 * 
 * The heart of a descriptor is its matching method. If an object matches the
 * inner definition of a descriptor then it is kept for the next step of the
 * import. The user can specify the geometric type of the objects he wants to
 * consider (points, lines or polygons) and the attributes they should possess.
 * 
 * Note that the attributes of matched elements are reduced to the set of
 * attributes specified by the user through a filtering process. This way, a lot
 * of memory is saved (some geographic files are huge) and the output graph is
 * not cluttered by meaningless data.
 * 
 * @author Merwan Achibet
 */
public class ElementDescriptor {

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
	protected ElementShape.Type mustBeType;

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
	 * Optional list of attribute keys that a matching object must not have.
	 */
	protected List<String> mustNotHaveKeys;

	/**
	 * Optional list of attribute keys/values pairs that a matching object must
	 * not have.
	 */
	protected HashMap<String, Object> mustNotHaveValues;

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

		this.mustBeType = ElementShape.Type.UNSPECIFIED;

		this.toSpatialIndex = false;
	}
	
	/**
	 * Specify the geometric type of geographic objects described by this
	 * descriptor.
	 * 
	 * @param type
	 *            The geometric type of wanted objects.
	 */
	public void mustBe(ElementShape.Type type) {

		this.mustBeType = type;
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
	 * Specify an attribute key that geographic objects must possess to be kept.
	 * 
	 * These keys are accumulated with each call of this method.
	 * 
	 * @param attributeKey
	 *            The key of the attribute.
	 */
	public void mustNotHave(String attributeKey) {

		if(this.mustNotHaveKeys == null)
			this.mustNotHaveKeys = new ArrayList<String>();

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
	public void mustNotHave(String attributeKey, Object attributeValue) {

		if(this.mustNotHaveValues == null)
			this.mustNotHaveValues = new HashMap<String, Object>();

		this.mustNotHaveValues.put(attributeKey, attributeValue);
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
	public boolean matches(Object o, Aggregator aggregator) {

		// Check for the geometric type.

		if(this.mustBeType != ElementShape.Type.UNSPECIFIED && !aggregator.isOfType(o, this.mustBeType))
			return false;

		// Check for forbidden attribute keys.

		if(this.mustNotHaveKeys != null)
			for(String key : this.mustNotHaveKeys)
				if(aggregator.hasKey(o, key))
					return false;

		// Check for forbidden attribute key/value pairs.

		if(this.mustNotHaveValues != null)
			for(String key : this.mustNotHaveValues.keySet())
				if(aggregator.hasKeyValue(o, key, this.mustNotHaveValues.get(key)))
					return false;

		// Check for required attribute keys.

		if(this.mustHaveKeys != null)
			for(String key : this.mustHaveKeys)
				if(!aggregator.hasKey(o, key))
					return false;

		// Check for required attribute key/value pairs.

		if(this.mustHaveValues != null)
			for(String key : this.mustHaveValues.keySet())
				if(!aggregator.hasKeyValue(o, key, this.mustHaveValues.get(key)))
					return false;

		return true;
	}

	/**
	 * Give the name of the category of elements described by the descriptor.
	 * 
	 * @return The name of the element category.
	 */
	public String getCategory() {

		return new String(this.category);
	}

	public AttributeFilter getAttributeFilter() {

		return this.filter;
	}

	/**
	 * Set the descriptor to reference matching elements in a spatial index.
	 */
	public void sendElementsToSpatialIndex() {

		this.toSpatialIndex = true;

		this.source.prepareSpatialIndex();
	}

	/**
	 * 
	 * @return
	 */
	public boolean areElementsSentToSpatialIndex() {

		return this.toSpatialIndex;
	}

	@Override
	public String toString() {

		String s = new String();

		s += "Descriptor";

		if(this.mustBeType != null)
			s += " | type: " + this.mustBeType.toString();

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

		if(this.toSpatialIndex)
			s += " | matching elements stored in a spatial index";

		return s;
	}

}
