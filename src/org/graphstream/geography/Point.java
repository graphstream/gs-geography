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
import java.util.List;

import org.graphstream.geography.index.SpatialIndexPoint;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A point.
 * 
 * The goal of this class is to represent in the simpler manner possible a point
 * element. Its main features are an optional ID and a position.
 * 
 * @author Merwan Achibet
 */
public class Point extends ElementShape {

	/**
	 * The Cartesian position of the point in the studied space.
	 */
	protected Coordinate position;

	public Point(Element element) {
		super(element);

		this.type = Type.POINT;
		this.position = new Coordinate();
	}

	public Point(Element element, String id) {
		this(element);
	}

	/**
	 * Change the Cartesian position of the point.
	 * 
	 * @param x
	 *            The x-axis coordinate.
	 * @param y
	 *            The y-axis coordinate.
	 */
	public void setPosition(double x, double y) {

		this.position.x = x;
		this.position.y = y;
	}

	/**
	 * Give the Cartesian position of the point.
	 * 
	 * @return The point position.
	 */
	public Coordinate getPosition() {

		return new Coordinate(this.position);
	}

	public double getX() {

		return this.position.x;
	}

	public double getY() {

		return this.position.y;
	}

	@Override
	public boolean equals(Object o) {

		// Check if this is the same instance.

		if(o == this)
			return true;

		// Check if the other shape is a point too.

		ElementShape oShape = (ElementShape)o;

		if(oShape.getType() != Type.POINT)
			return false;

		// Check if the other shape is bound to the same element.

		if(!oShape.getElementId().equals(this.element.getId()))
			return false;

		// Check if the other shape is at the same position.

		Point pShape = (Point)oShape;

		if(!this.position.equals(pShape.getPosition()))
			return false;

		return true;
	}

	@Override
	public String toString() {

		String s = new String();

		s += "Point (" + this.position.x + "," + this.position.y + ")";

		return s;
	}

	public List<SpatialIndexPoint> toSpatialIndexPoints() {

		List<SpatialIndexPoint> spatialIndexPoints = new ArrayList<SpatialIndexPoint>();

		spatialIndexPoints.add(new SpatialIndexPoint(this, this.getElementId()+Math.random(), this.position.x, this.position.y));

		return spatialIndexPoints;
	}

}
