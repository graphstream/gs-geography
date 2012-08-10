/*
 * Copyright 2006 - 2011 
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
 * An abstract geometric element.
 * 
 * This class and its implementations are used as intermediary representations
 * for any kind of geographical features coming from an external source. The
 * original input formats can be quite heterogeneous the features they contain
 * are library-dependent.
 * 
 * An Element consists of an identifier and a list of attributes from the
 * original data source.
 * 
 * @author Merwan Achibet
 */
public abstract class Element {

	/**
	 * The ID of the feature.
	 */
	protected String id;

	/**
	 * The category of the feature.
	 */
	protected String category;

	/**
	 * A key/value mapping of attributes.
	 */
	protected HashMap<String, Object> attributes;

	/**
	 * Instantiate a new element.
	 * 
	 * @param id
	 *            The identifier of the element.
	 */
	public Element(String id, String category) {

		this.id = id;
		this.category = category;

		this.attributes = new HashMap<String, Object>();
	}

	public String getId() {

		return new String(this.id);
	}

	public String getCategory() {

		return new String(this.category);
	}

	public Object getAttribute(String key) {

		return this.attributes.get(key);
	}

	public boolean hasAttribute(String key) {

		return this.attributes.containsKey(key);
	}
	
	public boolean hasAttribute(String key, Object value) {

		return this.attributes.containsKey(key) && this.attributes.get(key).equals(value);
	}

	public void addAttribute(String key, Object value) {

		this.attributes.put(key, value);
	}

	public void removeAttribute(String key) {

		this.attributes.remove(key);
	}

	public boolean isPoint() {
		
		return this instanceof Point;
	}

	public boolean isLine() {
		
		return this instanceof Line;
	}
	
	// Abstract

	/**
	 * Check if the element is placed at a given position.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @return True if the element is at position (x,y).
	 */
	public abstract boolean at(double x, double y);

}
