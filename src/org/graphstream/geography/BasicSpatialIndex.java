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
import java.util.Iterator;

import org.graphstream.geography.Element;
import org.graphstream.geography.SpatialIndex;

/**
 * The most basic form of spatial index: a dumb list of geometric elements.
 * 
 * Should not be used, it only serves as a placeholder until a more elaborate
 * structure is coded.
 * 
 * @author Merwan Achibet
 */
public class BasicSpatialIndex implements SpatialIndex {

	private ArrayList<Element> elements;

	public BasicSpatialIndex() {

		this.elements = new ArrayList<Element>();
	}

	@Override
	public void add(Element element) {

		this.elements.add(element);
	}

	@Override
	public ArrayList<Element> getElementsAt(double x, double y) {

		ArrayList<Element> selection = new ArrayList<Element>();

		for(Element element : this.elements)
			if(element.at(x, y))
				selection.add(element);

		return selection;
	}

	@Override
	public int size() {

		return this.elements.size();
	}

	@Override
	public Iterator<Element> iterator() {

		return this.elements.iterator();
	}

}
