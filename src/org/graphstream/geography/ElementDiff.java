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
 * An ElementDiff instance represents the differential state of a geographic
 * element at a given time.
 * 
 * Elements are composed of an ordered set of diffs that each contains the
 * differences in attributes and shape from their previous version.
 * 
 * @author Merwan Achibet
 */
public class ElementDiff {

	/**
	 * The element this diff is associated to.
	 */
	protected Element element;

	/**
	 * The shape of the element.
	 */
	protected ElementShape shape;

	/**
	 * The attributes that appear or are added with this diff.
	 */
	protected HashMap<String, Object> changedAttributes;

	/**
	 * The attributes that are removed from the previous diff of the element.
	 */
	protected ArrayList<String> removedAttributes;

	/**
	 * Flag explicitely expressing the disappearance of an element.
	 */
	protected boolean isDeletedFlag;

	/**
	 * Is this diff first in the chain of diffs?
	 */
	protected boolean isBaseFlag;

	/**
	 * Instantiate a new diff for a given element.
	 * 
	 * @param element
	 *            The element this diff is associated to.
	 */
	public ElementDiff(Element element) {
		this(element, false);
	}

	/**
	 * Instantiate a new diff for a given element.
	 * 
	 * @param element
	 *            The element this diff is associated to.
	 * @param isBaseFlag
	 *            True if the diff is a base, false otherwise.
	 */
	public ElementDiff(Element element, boolean isBaseFlag) {

		this.element = element;
		this.isBaseFlag = isBaseFlag;
	}

	/**
	 * Give the element associated with this diff.
	 * 
	 * @return The associated element.
	 */
	public Element getElement() {

		return this.element;
	}

	/**
	 * Give the ID of the element associated with this diff.
	 * 
	 * @return The ID of the associated element.
	 */
	public String getElementId() {

		return this.element.getId();
	}

	/**
	 * Bind a shape to this diff.
	 * 
	 * @param shape
	 *            The shape of the associated element.
	 */
	public void setShape(ElementShape shape) {

		this.shape = shape;
	}

	/**
	 * Give the shape of the associated element.
	 * 
	 * @return The shape of the associated element.
	 */
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
	public void addChangedAttribute(String key, Object value) {

		// Instantiate the map if it has not been done yet.

		if(this.changedAttributes == null)
			this.changedAttributes = new HashMap<String, Object>();

		// Add the attribute to the map.

		this.changedAttributes.put(key, value);
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
	 * State that the element disappears with this diff.
	 */
	public void setDeleted() {

		this.isDeletedFlag = true;
	}

	/**
	 * Check if this diff expresses the disappearance of the element.
	 * 
	 * @return True if the element disappears with this diff, false otherwise.
	 */
	public boolean isDeleted() {

		return this.isDeletedFlag;
	}

	/**
	 * Check if the diff is a base.
	 * 
	 * In other words, check if this diff is the first in the chain of diffs
	 * that represents the associated element.
	 * 
	 * @return True if the diff is a base, false otherwise.
	 */
	public boolean isBase() {

		return this.isBaseFlag;
	}

	/**
	 * Check if the diff is really useful or if it does not represent any
	 * change.
	 * 
	 * @return True if the diff is useless, false it contains differential
	 *         information.
	 */
	public boolean isEmpty() {

		if(this.changedAttributes != null)
			return false;

		if(this.removedAttributes != null)
			return false;

		if(this.shape != null)
			return false;

		if(this.isDeletedFlag)
			return false;

		return true;
	}

	@Override
	public String toString() {

		String s = new String();

		s += "ElementDiff (element " + this.element.getId() + ")";

		s += " | attributes: {";
		if(this.changedAttributes != null)
			for(Entry<String, Object> keyValue : this.changedAttributes.entrySet())
				s += " " + keyValue.getKey() + ":" + keyValue.getValue();
		s += " }";

		s += " | removed attributes: {";
		if(this.removedAttributes != null)
			for(String key : this.removedAttributes)
				s += " " + key;
		s += " }";

		s += " | shape: " + this.shape;
		
		if(this.isDeletedFlag)
			s += " | deletion diff";

		return s;
	}

}
