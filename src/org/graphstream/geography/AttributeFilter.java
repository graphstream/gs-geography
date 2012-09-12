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
 * Specify which attributes must be kept/filtered when converting a geographic
 * object from a library-dependent format to a simple geometric element.
 * 
 * A filter has a mode. If it is KEEP, only the matched attributes will be kept
 * in the converted element. If is is FILTER, all attributes except for the
 * matched ones will be kept.
 * 
 * @author Antoine Dutot
 * @author Merwan Achibet
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
	protected ArrayList<String> attributes;

	/**
	 * Instantiate a new filter with the KEEP mode.
	 */
	public AttributeFilter() {

		this.mode = AttributeFilter.Mode.KEEP;
	}

	/**
	 * Instantiate a new filter with the given mode.
	 * 
	 * @param mode
	 *            The filtering mode.
	 */
	public AttributeFilter(Mode mode) {

		this.mode = mode;
	}

	/**
	 * Give a list of filtered attributes from a list of attributes.
	 * 
	 * @param allAttributes
	 *            A list of attributes.
	 * @return A filtered list of attributes.
	 */
	public HashMap<String, Object> filter(HashMap<String, Object> allAttributes) {

		HashMap<String, Object> filtered = new HashMap<String, Object>();

		for(Entry<String, Object> keyValue : allAttributes.entrySet())
			if(isKept(keyValue.getKey()))
				filtered.put(keyValue.getKey(), keyValue.getValue());

		return filtered;
	}

	/**
	 * Check if an attribute must be kept.
	 * 
	 * @param attribute
	 *            The name of the attribute to check.
	 * @return True if the attribute must be kept, false if it must be ignored.
	 */
	public boolean isKept(String attribute) {

		if(this.attributes == null)
			return false;

		return mode == Mode.KEEP ? attributes.contains(attribute) : !attributes.contains(attribute);
	}

	/**
	 * Check if an attribute must be ignored.
	 * 
	 * @param attribute
	 *            The name of the attribute to check.
	 * @return True if the attribute must be ignored, false if it must be kept.
	 */
	public boolean isFiltered(String attribute) {

		if(this.attributes == null)
			return false;

		return mode == Mode.KEEP ? !attributes.contains(attribute) : attributes.contains(attribute);
	}

	/**
	 * Give the filtering mode.
	 * 
	 * @return The filtering mode, KEEP or FILTER.
	 */
	public Mode getMode() {

		return this.mode;
	}

	/**
	 * Add an attribute in the set of kept/filtered attributes.
	 * 
	 * @param attribute
	 *            The attribute name.
	 */
	public void addAttribute(String attribute) {

		if(this.attributes == null)
			this.attributes = new ArrayList<String>();

		this.attributes.add(attribute);
	}

	@Override
	public String toString() {

		String s = new String();

		s += "AttributeFilter";

		s += " | mode: " + this.mode;

		s += " | attributes: {";
		for(String key : this.attributes)
			s += " " + key;
		s += " }";

		return s;
	}

}
