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

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A Polygon.
 * 
 * The goal of this class is to represent in the simpler manner possible a
 * polygon element. Its main features are an ID and a list of points.
 * 
 * A polygon essentially is a line for which the starting point is the same as
 * the ending point. In other words, a polygon is a closed line.
 * 
 * @author Merwan Achibet
 */
public class Polygon extends Line {

	/**
	 * Instantiate a new polygon by deep-copying another one.
	 * 
	 * @param other
	 *            The polygon to copy.
	 */
	public Polygon(Element element) {
		super(element);

		this.type = Type.POLYGON;
	}

	/**
	 * Give the centroid of the polygon.
	 * 
	 * It can be useful to retrieve this value when in need of an appropriate
	 * position to place a graph node representing the polygon.
	 * 
	 * @return The coordinate of the polygon centroid.
	 */
	public Coordinate getCentroid() {

		Coordinate sum = new Coordinate();

		for(int i = 0; i < this.vertices.size(); ++i) {

			Coordinate p = this.vertices.get(i).getPosition();

			sum.x += p.x;
			sum.y += p.y;
		}

		sum.x /= this.vertices.size();
		sum.y /= this.vertices.size();

		return sum;
	}

	@Override
	public boolean equals(Object o) {

		// Check if this is the same instance.

		if(o == this)
			return true;

		// Check if the other shape is a polygon too.

		ElementShape oShape = (ElementShape)o;

		if(oShape.getType() != Type.POLYGON)
			return false;

		// Check if the other shape is bound to the same element.

		if(!oShape.getElementId().equals(this.element.getId()))
			return false;

		// Check if the other shape has the same points.
		
		Polygon pShape = (Polygon)oShape;

		if(pShape.getVertices().size() != this.vertices.size())
			return false;
		
		for(int i = 0, l = pShape.getVertices().size(); i < l; ++i)
			if(!pShape.getVertices().get(i).equals(vertices.get(i)))
				return false;

		return true;
	}
	
	@Override
	public String toString() {
	
		String s = new String();
		
		s += "Polygon ";
		
		for(Vertex point : this.vertices)
			s += point.getPosition().x + "," + point.getPosition().y + ")-";
		
		return s;
	}
	
}
