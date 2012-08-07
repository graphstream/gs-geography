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

import java.util.HashSet;

/**
 * Specify which attributes are kept or filtered when importing or exporting
 * data.
 * 
 * @author Antoine Dutot
 */
public class AttributeFilter {

	/**
	 * Keep or filter the specified attributes?
	 */
	public static enum Mode {
		KEEP, FILTER
	};

	/**
	 * The filtering mode.
	 */
	protected Mode mode;

	/**
	 * The set of attributes to keep or filter.
	 */
	protected HashSet<String> attributes = new HashSet<String>();

	/**
	 * New filter with the given mode.
	 * 
	 * @param keep
	 *            If true, the set contains attributes to preserve, else it
	 *            contains attributes to ignore.
	 */
	public AttributeFilter(boolean keep) {

		this.mode = keep ? Mode.KEEP : Mode.FILTER;
	}

	/**
	 * New filter with the given mode.
	 * 
	 * @param mode
	 *            The mode (Mode.KEEP or Mode.FILTER).
	 */
	public AttributeFilter(Mode mode) {

		this.mode = mode;
	}

	// Access

	/**
	 * True if the attribute must be ignored.
	 * 
	 * @param attribute
	 *            The attribute to test.
	 * @return False if the attribute must be stored.
	 */
	public boolean isFiltered(String attribute) {

		if(mode == Mode.KEEP)
			return !attributes.contains(attribute);

		return attributes.contains(attribute);
	}

	/**
	 * True if the attribute must not be considered.
	 * 
	 * @param attribute
	 *            The attribute to test.
	 * @return False if the attribute must be stored.
	 */
	public boolean isKept(String attribute) {

		if(mode == Mode.KEEP)
			return attributes.contains(attribute);

		return !attributes.contains(attribute);
	}

	/**
	 * True if the set of attributes defines the attributes to conserve and
	 * store.
	 * 
	 * @return False if the set of attributes define what must be ignored.
	 */
	public boolean isKeepMode() {
		
		return mode == Mode.KEEP;
	}

	/**
	 * True if the set of attributes defines the attributes to filter.
	 * 
	 * @return False if the set of attributes define what must be kept.
	 */
	public boolean isFilterMode() {
		
		return mode == Mode.FILTER;
	}

	// Command

	/**
	 * Add an attribute in the set of kept or filtered attributes.
	 * 
	 * @param attribute
	 *            The attribute to add to the set.
	 */
	public void add(String attribute) {
		
		attributes.add(attribute);
	}

	/**
	 * Remove and attribute of the set of kept or filtered attributes.
	 * 
	 * @param attribute
	 *            The attribute to remove from the set.
	 */
	public void remove(String attribute) {
		
		attributes.remove(attribute);
	}
	
}