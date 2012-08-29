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
import java.util.TreeMap;

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
public class Element {

	/**
	 * The ID of the element.
	 */
	protected String id;

	/**
	 * The category of the element (attributed by the descriptor that
	 * instantiated the object from a matching feature).
	 */
	protected String category;

	/**
	 * 
	 */
	protected TreeMap<Integer, ElementState> states;

	/**
	 * Instantiate a new element.
	 * 
	 * @param id
	 *            The element ID.
	 */
	public Element(String id) {

		this.id = id;

		this.states = new TreeMap<Integer, ElementState>();
	}

	/**
	 * 
	 * @param other
	 */
	public Element(Element other) {

		this.id = new String(other.getId());
		this.category = new String(other.getCategory());

		for(Entry<Integer, ElementState> dateStatePair : other.getStates().entrySet())
			this.states.put(new Integer(dateStatePair.getKey()), new ElementState(dateStatePair.getValue()));
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public ElementView getElementViewAtDate(Integer date) {

		// Check that the element exists at this date.

		if(!this.states.containsKey(date))
			return null;

		// Go through each state in ascending order and rebuild the element with
		// its differential states until the date is reached.

		ElementView rebuiltElement = new ElementView(this.id);

		for(Entry<Integer, ElementState> dateElementPair : this.states.entrySet()) {

			// Retrieve the differential state of the element at this date.

			ElementState currentDiff = dateElementPair.getValue();

			// Remove the attributes that disappeared with this diff.

			ArrayList<String> removedAttributes = currentDiff.getRemovedAttributes();

			if(removedAttributes != null)
				for(String key : removedAttributes)
					rebuiltElement.removeAttribute(key);

			// Update the attributes which value changed with this diff.

			HashMap<String, Object> changedAttributes = currentDiff.getChangedAttributes();

			if(changedAttributes != null)
				for(Entry<String, Object> keyValue : changedAttributes.entrySet())
					rebuiltElement.setAttribute(keyValue.getKey(), keyValue.getValue());

			rebuiltElement.shape = currentDiff.shape;

			// Return the rebuilt element if the appropriate date is reached.

			if(dateElementPair.getKey() >= date)
				return rebuiltElement;

		}

		//

		return null;
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

		if(this.category == null)
			return null;

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

	public void addStateAtDate(ElementState state, Integer date) {

		this.states.put(date, state);
	}

	public TreeMap<Integer, ElementState> getStates() {

		return this.states;
	}

	public boolean hasStateAtDate(Integer date) {

		return this.states.containsKey(date);
	}

	@Override
	public String toString() {

		String s = new String();

		s += " Element " + this.id;

		s += " | " + this.states.toString();

		return s;
	}

	/**
	 * Give special points that spatially represent the shape of the element and
	 * will be stored in a spatial index.
	 * 
	 * @return A list of spatial references to the shape of the element.
	 */
	// public abstract List<SpatialIndexPoint> toSpatialIndexPoints();

}
