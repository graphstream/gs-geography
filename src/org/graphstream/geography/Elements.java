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
 * A container holding all the geometric elements gathered from the input data
 * source.
 * 
 * Elements are grouped in blocks corresponding to specific times. An element is
 * recorded in a block if it just appeared (it is then a base element) or if it
 * has changed (it is then a diff element).
 * 
 * It the progress of time is not considered, all elements are stored at time 0
 * as base versions.
 * 
 * @author Merwan Achibet
 */
public class Elements {

	/**
	 * The map of element blocks, ordered by time.
	 */
	protected TreeMap<Integer, ArrayList<Element>> elementsByDate;

	/**
	 * Instantiate a new element container.
	 */
	public Elements() {

		this.elementsByDate = new TreeMap<Integer, ArrayList<Element>>();
	}

	/**
	 * Store an element at a specific time.
	 * 
	 * @param element
	 *            The element to store.
	 * @param date
	 *            The date at which the element appears/changes.
	 */
	public void addElement(Element element, Integer date) {

		// Retrieve the block of elements representing the current
		// configuration.

		ArrayList<Element> elementsAtDate = this.elementsByDate.get(date);

		// Instantiate the block if has not been done yet.

		if(elementsAtDate == null) {

			elementsAtDate = new ArrayList<Element>();

			this.elementsByDate.put(date, elementsAtDate);
		}

		// Add the element to the list.

		elementsAtDate.add(element);
	}

	/**
	 * Give all the elements appearing at a specific time step.
	 * 
	 * @param step
	 *            The time step.
	 * @return The list of elements at this time step or null if the step does
	 *         not exist.
	 */
	public ArrayList<Element> getElementsAtStep(int step) {

		// Element blocks are ordered by date so advance to the 'step'-th block
		// and return it.

		int s = 0;
		for(Integer d : this.elementsByDate.keySet()) {

			if(s == step)
				return getElementsAtDate(d);

			++s;
		}

		return null;
	}

	/**
	 * Give all the elements at a specific date.
	 * 
	 * @param date
	 *            The date.
	 * @return A list of elements at this date.
	 */
	public ArrayList<Element> getElementsAtDate(Integer date) {

		return rebuildElements(date);
	}

	/**
	 * Give all the elements at the first date. In other words, give the
	 * starting configuration.
	 * 
	 * @return A list of all elements in their starting state.
	 */
	public ArrayList<Element> getElementsAtBeginning() {

		return rebuildElements(this.elementsByDate.firstEntry().getKey());
	}

	/**
	 * Give all the elements at the last date. In other words, give the final
	 * configuration.
	 * 
	 * @return A list of all elements in their final state.
	 */
	public ArrayList<Element> getElementsAtEnd() {

		return rebuildElements(this.elementsByDate.lastEntry().getKey());
	}

	/**
	 * Give the first version of an Element.
	 * 
	 * This element should be a base version.
	 * 
	 * @param id
	 *            The element ID.
	 * @return The base of the element, or null if the element never appears.
	 */
	public Element getElementFirstVersion(String id) {

		// Go though each time block until the element appears for the first
		// time.

		for(Entry<Integer, ArrayList<Element>> entry : this.elementsByDate.entrySet())
			for(Element e : entry.getValue())
				if(e.getId().equals(id))
					return e;

		return null;
	}

	/**
	 * Give the last version of an Element.
	 * 
	 * This element could be a base version if it only appears during a single
	 * time step or a diff version if it remains present across several time
	 * steps.
	 * 
	 * @param id
	 *            The element ID.
	 * @return The last version of the element, or null if the element never
	 *         appears.
	 */
	public Element getElementLastVersion(String id) {

		if(this.elementsByDate.size() == 0)
			return null;

		// Rebuild entirely the element from its diff versions.

		return rebuildElement(id, this.elementsByDate.lastEntry().getKey());
	}

	// TODO hashmap <- arraylist
	/**
	 * Rebuild an element from its diff versions.
	 * 
	 * @param id
	 *            The element ID.
	 * @param date
	 *            The date of the rebuilt version of the element.
	 * @return A base element rebuilt from its diff to a specific date or null
	 *         if the date does not exist.
	 */
	protected Element rebuildElement(String id, Integer date) {

		Element rebuiltElement = null;

		// Go though each date block in the ascending order until the date is
		// reached.

		for(Entry<Integer, ArrayList<Element>> dateElements : this.elementsByDate.entrySet()) {

			for(Element nextDiff : dateElements.getValue()) {

				// If the element to rebuild has not been found yet, look for
				// its base version.

				if(rebuiltElement == null && nextDiff.getId().equals(id))
					rebuiltElement = nextDiff; // COPY? XXX

				// Otherwise, update it with its diff version.

				else if(nextDiff.getId().equals(id)) {

					// Remove the attributes that disappeared with this diff.

					ArrayList<String> removedAttributes = nextDiff.getRemovedAttributes();
					
					if(removedAttributes != null)
						for(String key : removedAttributes)
							rebuiltElement.removeAttribute(key);

					// Set the attributes which value have changed or that are
					// entirely new.

					HashMap<String, Object> attributes = nextDiff.getAttributes();
					
					if(attributes != null)
						for(Entry<String, Object> keyValue : attributes.entrySet())
							rebuiltElement.setAttribute(keyValue.getKey(), keyValue.getValue());
				}
			}

			// Return the rebuilt element if the appropriate date is reached.

			if(dateElements.getKey() >= date)
				return rebuiltElement;

		}

		return null;
	}

	// XXX what if an element disappears and then reappears?
	
	/**
	 * Rebuild all elements from their diff versions.
	 * 
	 * @param date
	 *            The date of the rebuilt versions of the elements.
	 * @return A list of base elements rebuilt from their diffs to a specific
	 *         date or null if the date does not exist.
	 */
	protected ArrayList<Element> rebuildElements(Integer date) {

		HashMap<String, Element> rebuiltElements = new HashMap<String, Element>();

		// Go though each date block in the ascending order until the date is
		// reached.

		for(Entry<Integer, ArrayList<Element>> entry : this.elementsByDate.entrySet()) {

			for(Element nextDiff : entry.getValue()) {

				// If the element to rebuild has not been found yet, look for
				// its base version.

				if(rebuiltElements.get(nextDiff.getId()) == null)
					rebuiltElements.put(nextDiff.getId(), nextDiff);

				// Otherwise, update it with its diff version.

				else {

					// Retrieve the last version of the element.

					Element rebuiltElement = rebuiltElements.get(nextDiff.getId());

					// Remove the attributes that disappeared with this diff.

					ArrayList<String> removedAttributes = nextDiff.getRemovedAttributes();

					if(removedAttributes != null)
						for(String key : removedAttributes)
							rebuiltElement.removeAttribute(key);

					// Set the attributes which value have changed or that are
					// entirely new.

					HashMap<String, Object> attributes = nextDiff.getAttributes();

					if(attributes != null)
						for(Entry<String, Object> keyValue : attributes.entrySet())
							rebuiltElement.setAttribute(keyValue.getKey(), keyValue.getValue());

					// TODO shape? position?
				}
			}
			
			// Return the rebuilt element if the appropriate date is reached.

			if(entry.getKey() >= date)
				return new ArrayList<Element>(rebuiltElements.values());
		}

		return null;
	}
	
}
