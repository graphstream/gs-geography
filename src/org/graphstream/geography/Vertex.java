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
 * @author Merwan Achibet
 */
public class Vertex {

	/**
	 * Coordinates of the point.
	 */
	protected Coordinate position;

	/**
	 * Optional ID.
	 * 
	 * Can be useful when a line element must refer to point elements.
	 */
	protected String id;

	/**
	 * Instantiate a new point at coordinates (0,0).
	 */
	public Vertex() {

		this.position = new Coordinate(0, 0);
	}

	/**
	 * Instantiate a new point at coordinates (x,y).
	 * 
	 * @param x
	 *            The x-axis coordinate.
	 * @param y
	 *            The y-axis coordinate.
	 */
	public Vertex(double x, double y) {

		this.position = new Coordinate(x, y);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param id
	 */
	public Vertex(double x, double y, String id) {
		
		this(x, y);
		
		this.id = id;
	}

	/**
	 * Set the position of the point to the coordinates (x,y).
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

	public void setX(double x) {
		
		this.position.x = x;
	}
	
	public void setY(double y) {
		
		this.position.y = y;
	}

	/**
	 * Give the coordinates of the point.
	 * 
	 * @return The coordinates.
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
	
	/**
	 * Associate an ID with the point.
	 * 
	 * @param id
	 *            The ID.
	 */
	public void setId(String id) {

		this.id = id;
	}

	/**
	 * Give the optional ID associated with the point.
	 * 
	 * @return The ID.
	 */
	public String getId() {

		if(this.id == null)
			return null;
		
		return new String(this.id);
	}

	@Override
	public boolean equals(Object o) {

		// Check if this is the same instance.

		if(o == this)
			return true;

		// Check if the other shape has the same position.

		Vertex point = (Vertex)o;

		if(this.position.equals(point.getPosition()))
			return true;

		return false;
	}
	
}
