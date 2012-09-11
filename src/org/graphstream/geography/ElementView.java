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

import java.util.HashMap;

/**
 * An ElementView represents the complete state of a geographic element at a
 * specific date/
 * 
 * It contains the id, attributes and shape of the represented element and is
 * typically reconstructed from all of its diffs to offer a snapshot view of the
 * object.
 * 
 * @author Merwan Achibet
 */
public class ElementView {

	/**
	 * The associated element.
	 */
	protected Element element;

	/**
	 * The attributes of the element.
	 */
	protected HashMap<String, Object> attributes;

	/**
	 * The shape of the element.
	 */
	protected ElementShape shape;

	/**
	 * Instantiate a new element view.
	 * 
	 * @param id
	 *            The ID of the element.
	 */
	public ElementView(Element element) {

		this.element = element;

		this.attributes = new HashMap<String, Object>();
	}

	/**
	 * Give the ID of the element.
	 * 
	 * @return The element ID.
	 */
	public String getId() {

		return this.element.getId();
	}

	/**
	 * Set an attribute of the element.
	 * 
	 * @param key
	 *            The attribute key.
	 * @param value
	 *            The attribute value.
	 */
	public void setAttribute(String key, Object value) {

		this.attributes.put(key, value);
	}

	/**
	 * Remove an attribute from the element.
	 * 
	 * @param key
	 *            The attribute key.
	 */
	public void removeAttribute(String key) {

		this.attributes.remove(key);
	}

	/**
	 * Give the value of the attribute with the given key.
	 * 
	 * @param key
	 *            The attribute key.
	 * @return The attribute value.
	 */
	public Object getAttribute(String key) {

		return this.attributes.get(key);
	}

	/**
	 * Give the attributes of the element.
	 * 
	 * @return The attributes.
	 */
	public HashMap<String, Object> getAttributes() {

		return this.attributes;
	}

	/**
	 * Give the shape of the element.
	 * 
	 * @return The shape.
	 */
	public ElementShape getShape() {

		return this.shape;
	}
	
	/**
	 * Give the category of the element (given by the descriptor that matched
	 * it).
	 * 
	 * @return The element category.
	 */
	public String getCategory() {

		return this.element.getCategory();
	}
}
