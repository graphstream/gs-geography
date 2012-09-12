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

/**
 * A Line.
 * 
 * The goal of this class is to represent in the simpler manner possible a line
 * element. Its main features are an optional ID and a list of points.
 * 
 * @author Merwan Achibet
 */
public class Line extends ElementShape {

	/**
	 * The list of points forming the line.
	 */
	protected ArrayList<Vertex> vertices;

	/**
	 * Instantiate a new line.
	 * 
	 * @param id
	 *            The line ID.
	 */
	public Line(Element element) {
		super(element);

		this.type = Type.LINE;
		this.vertices = new ArrayList<Vertex>();
	}

	/**
	 * Add a point to the shape of the line.
	 * 
	 * @param id
	 *            The ID of the new point.
	 * @param x
	 *            The x-axis coordinate of the new point.
	 * @param y
	 *            The y-axis coordinate of the new point.
	 */
	public void addVertex(String id, double x, double y) {

		Vertex vertex = new Vertex(x, y);

		vertex.setId(id);

		this.vertices.add(vertex);
	}

	/**
	 * Give the list of points forming the line.
	 * 
	 * @return The list of points.
	 */
	public ArrayList<Vertex> getVertices() {

		return this.vertices;
	}

	/**
	 * Give the two end points of the line.
	 * 
	 * @return An array of point. The point at index 0 is the starting point,
	 *         the point at index 1 is the ending point.
	 */
	public Vertex[] getEndVertices() {

		return new Vertex[]{
				this.vertices.get(0), this.vertices.get(this.vertices.size() - 1)
		};
	}

	/**
	 * Give all successive pairs of points that compose the line.
	 * 
	 * For example, if the line is A->B->C->D then this method will return
	 * [[A,B][B,C][C,D]].
	 * 
	 * @return All pairs of successive points.
	 */
	public Vertex[][] getVertexPairs() {

		Vertex[][] pairs = new Vertex[this.vertices.size() - 1][];

		for(int i = 0, l = this.vertices.size() - 1; i < l; ++i)
			pairs[i] = new Vertex[]{
					this.vertices.get(i), this.vertices.get(i + 1)
			};

		return pairs;
	}

	@Override
	public boolean equals(Object o) {

		// Check if this is the same instance.

		if(o == this)
			return true;

		// Check if the other shape is a line too.
		ElementShape oShape = (ElementShape)o;

		if(oShape.getType() != Type.LINE)
			return false;

		// Check if the other shape is bound to the same element.

		if(!oShape.getElementId().equals(this.element.getId()))
			return false;

		// Check if the other shape has the same points.

		Line lShape = (Line)oShape;

		if(lShape.getVertices().size() != this.vertices.size())
			return false;

		for(int i = 0, l = lShape.getVertices().size(); i < l; ++i)
			if(!lShape.getVertices().get(i).equals(vertices.get(i)))
				return false;

		return true;
	}

	@Override
	public String toString() {

		String s = new String();

		s += "Line ";

		for(Vertex vertex : this.vertices)
			s += "(" + vertex.getPosition().x + "," + vertex.getPosition().y + ")-";

		return s;
	}

	@Override
	public List<SpatialIndexPoint> toSpatialIndexPoints() {

		List<SpatialIndexPoint> spatialIndexPoints = new ArrayList<SpatialIndexPoint>();

		for(Vertex vertex : this.vertices) {

			SpatialIndexPoint spatialIndexPoint = new SpatialIndexPoint(this, vertex.getId(), vertex.getPosition().x, vertex.getPosition().y);

			spatialIndexPoints.add(spatialIndexPoint);
		}

		return spatialIndexPoints;
	}

}
