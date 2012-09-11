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

import java.util.List;

import org.graphstream.geography.index.SpatialIndexPoint;

/**
 * ElementShape represents the shape and position of a geographic element at a
 * given time.
 * 
 * @author Merwan Achibet
 */
public abstract class ElementShape {

	public static enum Type {
		POINT, LINE, POLYGON, UNSPECIFIED
	};

	/**
	 * The element associated with this shape.
	 */
	protected Element element;

	/**
	 * The type of the shape (POINT, LINE or POLYGON).
	 */
	protected Type type;

	/**
	 * Instantiate a new ElementShape
	 * 
	 * @param element
	 *            The element associated with this shape.
	 */
	public ElementShape(Element element) {

		this.element = element;
	}

	/**
	 * Give the element associated with the shape.
	 * 
	 * @return The associated element.
	 */
	public Element getElement() {

		return this.element;
	}

	/**
	 * Give the ID of the element associated with this shape.
	 * 
	 * @return The ID of the associated element.
	 */
	public String getElementId() {

		return this.element.getId();
	}

	/**
	 * Give the geometric type of the shape.
	 * 
	 * @return The geometric type.
	 */
	public Type getType() {

		return this.type;
	}

	/**
	 * Check if the element has a specific geometric type.
	 * 
	 * @param type
	 *            The geometric type.
	 * @return True if the element has the same type, false otherwise.
	 */
	public boolean isType(Type type) {

		if(type == Type.POINT)
			return isPoint();

		if(type == Type.LINE)
			return isLine();

		if(type == Type.POLYGON)
			return isPolygon();

		return false;
	}

	/**
	 * Check if the element is a point.
	 * 
	 * @return True if the element is a point, false otherwise.
	 */
	public boolean isPoint() {

		return this.type == Type.POINT;
	}

	/**
	 * Check if the element is a line.
	 * 
	 * @return True if the element is a line, false otherwise.
	 */
	public boolean isLine() {

		return this.type == Type.LINE;
	}

	/**
	 * Check if the element is a polygon.
	 * 
	 * @return True if the element is a polygon, false otherwise.
	 */
	public boolean isPolygon() {

		return this.type == Type.POLYGON;
	}

	/**
	 * Give special points that spatially represent the shape of the element and
	 * will be stored in a spatial index.
	 * 
	 * @return A list of spatial references to the shape of the element.
	 */
	public abstract List<SpatialIndexPoint> toSpatialIndexPoints();

}
