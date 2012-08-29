package org.graphstream.geography;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class ElementState {

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

	public ElementState() {

	}

	public ElementState(ElementState other) {

		HashMap<String, Object> otherChangedAttributes = other.getChangedAttributes();
		if(otherChangedAttributes != null)
			for(Entry<String, Object> keyValuePair : otherChangedAttributes.entrySet())
				setChangedAttribute(new String(keyValuePair.getKey()), keyValuePair.getValue());

		ArrayList<String> otherRemovedAttributes = other.getRemovedAttributes();
		if(otherRemovedAttributes != null)
			for(String key : otherRemovedAttributes)
				addRemovedAttribute(new String(key));
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
