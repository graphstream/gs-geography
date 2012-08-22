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
 * recorded in a block if it just appeared (it is then a "base" element) or if
 * it has changed (it is then a "diff" element).
 * 
 * It the progress of time is not considered, all elements are stored at time 0
 * as "base" versions.
 * 
 * @author Merwan Achibet
 */
public class Elements {

	/**
	 * The ordered map of element blocks.
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
	public void add(Element element, Integer date) {

		ArrayList<Element> elementsAtDate = this.elementsByDate.get(date);

		if(elementsAtDate == null) {
			elementsAtDate = new ArrayList<Element>();
			this.elementsByDate.put(date, elementsAtDate);
		}
System.out.println(element + " " + date);
		elementsAtDate.add(element);
	}

	/**
	 * Give the states of all the elements at a specific time.
	 * 
	 * @param date
	 *            The date.
	 * @return A list of elements at this time.
	 */
	public ArrayList<Element> getElementsAtDate(Integer date) {

		return accumulate(date);
	}

	/**
	 * Give the states of all the elements at the last time.
	 * 
	 * @return A list of elements in their final form.
	 */
	public ArrayList<Element> getElementsAtEnd() {
		
		for(Entry<Integer, ArrayList<Element>> entry : this.elementsByDate.entrySet())
			System.out.println(entry.getKey() + " " + entry.getValue());
		
		return accumulate(this.elementsByDate.lastEntry().getKey());
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	protected ArrayList<Element> accumulate(Integer date) {

		// This list will hold the progressive state of each element and will be
		// updated with diffs through each time step.

		HashMap<String, Element> accumulatedElements = new HashMap<String, Element>();

		for(Entry<Integer, ArrayList<Element>> entry : this.elementsByDate.entrySet()) {

			for(Element elementDiff : entry.getValue()) {

				// If the element is a "base", just add it to the accumulated
				// elements.

				if(elementDiff.isBase())
					accumulatedElements.put(elementDiff.getId(), elementDiff);

				// If the element is a "diff", update its accumulated version.

				else {

					// Retrieve the last version of the element.

					Element previousElementDiff = accumulatedElements.get(elementDiff.getId());

					// Delete attributes that have been removed.

					for(String key : elementDiff.getRemovedAttributes())
						previousElementDiff.removeAttribute(key);

					// Update attributes which values have been changed and add
					// new attributes.

					for(Entry<String, Object> entry2 : elementDiff.getAttributes().entrySet())
						previousElementDiff.setAttribute(entry2.getKey(), entry2.getValue());

					// TODO shape? position?
				}
			}

			if(entry.getKey() >= date)
				return new ArrayList<Element>(accumulatedElements.values());
		}

		return new ArrayList<Element>(accumulatedElements.values());
	}
}
