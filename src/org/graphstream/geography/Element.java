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
 * This class serves as a standard representation for geographic objects coming
 * from an external source. The original input formats can be quite
 * heterogeneous as the data they contain is library-dependent and we don't want
 * to force the user to learn GeoTools, XOM or whatever other library.
 * 
 * An Element basically consists of a unique identifier and a list of states (or
 * diffs) that represent the change it is subject to when time progresses.
 * 
 * @author Merwan Achibet
 */
public class Element {

	/**
	 * The ID of the element.
	 */
	protected String id;

	/**
	 * The descriptor to which the element has been matched.
	 */
	protected ElementDescriptor descriptorUsed;

	/**
	 * The states (or diffs) of this element, indexed by time.
	 */
	protected TreeMap<Integer, ElementDiff> diffs;

	/**
	 * Instantiate a new element.
	 * 
	 * @param id
	 *            The ID of the element.
	 */
	public Element(String id) {

		this.id = id;

		this.diffs = new TreeMap<Integer, ElementDiff>();
	}

	/**
	 * Get the complete state of the element at a given date.
	 * 
	 * @param date
	 *            The date of the returned element.
	 * @return The element as it is at a given date.
	 */
	public ElementView getElementViewAtDate(Integer date) {

		// Check that the element exists at this date.

		if(!this.diffs.containsKey(date))
			return null;

		// Go through each state in ascending order and rebuild the element with
		// its differential states until the date is reached.

		ElementView rebuiltElement = new ElementView(this);

		for(Entry<Integer, ElementDiff> dateElementPair : this.diffs.entrySet()) {

			// Retrieve the differential state of the element at this date.

			ElementDiff currentDiff = dateElementPair.getValue();

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

			// Update the shape if it has changed.
			
			if(currentDiff.shape != null)
				rebuiltElement.shape = currentDiff.shape;

			// Return the rebuilt element if the appropriate date is reached.

			if(dateElementPair.getKey() >= date)
				return rebuiltElement;

		}

		//

		return null;
	}

	/**
	 * Give the diff of this element at a given date.
	 * 
	 * @param date
	 *            The date.
	 * @return The element diff at the given date.
	 */
	public ElementDiff getElementDiffAtDate(Integer date) {

		return this.diffs.get(date);
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
	 * Record the descriptor that matched the element.
	 * 
	 * @param descriptorUsed
	 *            The descriptor.
	 */
	public void setDescriptorUsed(ElementDescriptor descriptorUsed) {

		this.descriptorUsed = descriptorUsed;
	}

	/**
	 * Give the descriptor that matched the element.
	 * 
	 * @return The descriptor.
	 */
	public ElementDescriptor getDescriptorUsed() {

		return this.descriptorUsed;
	}

	/**
	 * Give the category of the element.
	 * 
	 * @return The category.
	 */
	public String getCategory() {

		return this.descriptorUsed.getCategory();
	}

	/**
	 * Check if the element is of a given category.
	 * 
	 * @param category
	 *            The category.
	 * @return True if the element has the same category, false otherwise.
	 */
	public boolean isCategory(String category) {

		return this.descriptorUsed.getCategory().equals(category);
	}

	/**
	 * Bind a specific element diff to a given date.
	 * 
	 * @param diff
	 *            The element diff.
	 * @param date
	 *            The date.
	 */
	public void addDiffAtDate(ElementDiff diff, Integer date) {

		this.diffs.put(date, diff);
	}

	/**
	 * Give all the diffs of the element.
	 * 
	 * @return The diffs.
	 */
	public TreeMap<Integer, ElementDiff> getDiffs() {

		return this.diffs;
	}

	/**
	 * Check if the element has a diff at the given date.f
	 * 
	 * @param date
	 *            The date.
	 * @return True if the element has a diff at the date, false otherwise.
	 */
	public boolean hasDiffAtDate(Integer date) {

		return this.diffs.containsKey(date);
	}

	/**
	 * Check if the element still exists at the given date.
	 * 
	 * @param date
	 *            The date.
	 * @return True if the element still exists at the date, false otherwise.
	 */
	public boolean hasExpiredByDate(Integer date) {

		return this.diffs.lastKey() < date;
	}

	@Override
	public String toString() {

		String s = new String();

		s += " Element " + this.id;

		s += " | " + this.diffs.toString();

		return s;
	}

}
