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
import java.util.Map.Entry;

/**
 * 
 * @author Merwan Achibet
 */
public class ElementDiff {

	/**
	 * The element this diff is associated to.
	 */
	protected Element element;
	
	/**
	 * 
	 */
	protected ElementShape shape;

	/**
	 * A key/value mapping of attributes.
	 */
	protected HashMap<String, Object> changedAttributes;

	/**
	 * A key/value mapping of attributes that were removed from the previous
	 * version of the element.
	 */
	protected ArrayList<String> removedAttributes;
	
	/**
	 * 
	 */
	protected boolean isBaseFlag;

	public ElementDiff(Element element) {
		this(element, false);
	}

	public ElementDiff(Element element, boolean isBaseFlag) {

		this.element = element;
		this.isBaseFlag = isBaseFlag;
	}

	public ElementDiff(ElementDiff other) {

		HashMap<String, Object> otherChangedAttributes = other.getChangedAttributes();
		if(otherChangedAttributes != null)
			for(Entry<String, Object> keyValuePair : otherChangedAttributes.entrySet())
				setChangedAttribute(new String(keyValuePair.getKey()), keyValuePair.getValue());

		ArrayList<String> otherRemovedAttributes = other.getRemovedAttributes();
		if(otherRemovedAttributes != null)
			for(String key : otherRemovedAttributes)
				addRemovedAttribute(new String(key));
	}

	public String getElementId() {
		
		return this.element.getId();
	}

	public void setShape(ElementShape shape) {
		
		this.shape = shape;
	}
	
	public ElementShape getShape() {

		return this.shape;
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
	public void setChangedAttribute(String key, Object value) {

		// Instantiate the map if it has not been done yet.

		if(this.changedAttributes == null)
			this.changedAttributes = new HashMap<String, Object>();

		// Add the attribute to the map.

		this.changedAttributes.put(key, value);
	}

	/**
	 * Remove an attribute from the element.
	 * 
	 * @param key
	 *            The key of the attribute to remove.
	 */
	public void removeChangedAttribute(String key) {

		if(this.changedAttributes != null)
			this.changedAttributes.remove(key);
	}

	/**
	 * Give the value of an attribute.
	 * 
	 * @param key
	 *            The key of the attribute.
	 * @return The value of the attribute or null if it does not exist.
	 */
	public Object getChangedAttribute(String key) {

		if(this.changedAttributes == null)
			return null;

		return this.changedAttributes.get(key);
	}

	/**
	 * Give all attributes.
	 * 
	 * @return A list of key/value pairs.
	 */
	public HashMap<String, Object> getChangedAttributes() {

		if(this.changedAttributes == null)
			return null;

		return this.changedAttributes;
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

		return this.changedAttributes != null && this.changedAttributes.containsKey(key);
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

		return this.changedAttributes != null && this.changedAttributes.containsKey(key) && this.changedAttributes.get(key).equals(value);
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
	 * 
	 * @return
	 */
	public boolean isBase() {
		
		return this.isBaseFlag;
	}
	
	@Override
	public String toString() {

		String s = new String();

		s += "attributes: {";
		if(this.changedAttributes != null)
			for(Entry<String, Object> keyValue : this.changedAttributes.entrySet())
				s += " " + keyValue.getKey() + ":" + keyValue.getValue();
		s += " }";

		s += " | removed attributes: {";
		if(this.removedAttributes != null)
			for(String key : this.removedAttributes)
				s += " " + key;
		s += " }";

		return s;
	}

}
