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
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An aggregate is the result of the aggregation of the content of one or
 * several input files.
 * 
 * Its storage structure is two-dimensional and depends on the ID of a stored
 * feature and its date of appearance. This way we can store different versions
 * of a same geographic object when time progression is taken into account.
 * 
 * @author Merwan Achibet
 */
public class Aggregate implements Iterable<Entry<String, HashMap<Integer, Object>>> {

	/**
	 * The aggregated geographic objects, indexed by ID and by date of
	 * appearance.
	 */
	protected HashMap<String, HashMap<Integer, Object>> content;

	/**
	 * A mapping of the descriptor that matched each geographic object.
	 */
	protected HashMap<String, ElementDescriptor> descriptorsUsed;

	/**
	 * Instantiate a new Aggregate.
	 */
	public Aggregate() {

		this.content = new HashMap<String, HashMap<Integer, Object>>();

		this.descriptorsUsed = new HashMap<String, ElementDescriptor>();
	}

	/**
	 * Add an object to the aggregate (typically a geographic object in its
	 * library-dependent form).
	 * 
	 * @param id
	 *            The object ID.
	 * @param date
	 *            The date of appearance of the object.
	 * @param o
	 *            The object.
	 */
	public void add(String id, Integer date, Object o) {

		// Get the slot for all the versions of the object.
		
		HashMap<Integer, Object> objectVersions = this.content.get(id);

		// If the slot does not exist yet, add it.
		
		if(objectVersions == null) {
			
			objectVersions = new HashMap<Integer, Object>();
			
			this.content.put(id, objectVersions);
		}
		
		// Finally, add the object at the given date.

		objectVersions.put(date, o);
	}

	/**
	 * Give the object of the aggregate with a specific ID and a specific date.
	 * 
	 * @param id
	 *            The object ID.
	 * @param date
	 *            The date of the object.
	 * @return The object or null if it isn't aggregated.
	 */
	public Object get(String id, Integer date) {

		// Retrieve all of the versions of the object appearing at different
		// dates.

		HashMap<Integer, Object> objectVersions = this.content.get(id);

		if(objectVersions == null)
			return null;

		// Return the version of the object at the given date.
		
		return objectVersions.get(date);
	}

	/**
	 * Specify the descriptor that matched a given geographic object.
	 * 
	 * @param id
	 *            The object ID.
	 * @param descriptor
	 *            The descriptor that matched the object.
	 */
	public void setDescriptorUsed(String id, ElementDescriptor descriptor) {

		this.descriptorsUsed.put(id, descriptor);
	}

	/**
	 * Give the descriptor that matched an aggregated geographic object.
	 * 
	 * @param id
	 *            The object ID.
	 * @return The descriptor that matched the object.
	 */
	public ElementDescriptor getDescriptorUsed(String id) {

		return this.descriptorsUsed.get(id);
	}

	@Override
	public Iterator<Entry<String, HashMap<Integer, Object>>> iterator() {

		return this.content.entrySet().iterator();
	}

}
