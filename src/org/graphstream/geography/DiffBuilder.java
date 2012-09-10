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

/**
 * The main task of a diff builder is to create a differential version of a
 * geographic object from its previous version.
 * 
 * It must acknowledges the addition of new attributes, the removal of
 * attributes, the modification of existing attributes and the changes in the
 * shape and position of the geographic object.
 * 
 * @author Merwan Achibet
 */
public abstract class DiffBuilder {

	/**
	 * The source using the diff builder.
	 */
	protected GeoSource source;

	/**
	 * Instantiate a new diff builder.
	 * 
	 * @param source
	 *            The source using the diff builder.
	 */
	public DiffBuilder(GeoSource source) {

		this.source = source;
	}

	/**
	 * Give a differential version of a geographic element based on its previous
	 * version and its current library-specific form.
	 * 
	 * @param element
	 *            The element.
	 * @param previousDiff
	 *            The previous diff or the element or null if it does not exist
	 *            yet.
	 * @param previousDate
	 *            The date of the previous version of the element.
	 * @param o
	 *            The library-specific form of the element.
	 * @return A differential version of the element.
	 */
	abstract public ElementDiff diff(Element element, ElementDiff previousDiff, Integer previousDate, Object o);

}
