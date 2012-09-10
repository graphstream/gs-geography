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
	protected ArrayList<LinePoint> points;

	/**
	 * Instantiate a new line.
	 * 
	 * @param id
	 *            The line ID.
	 */
	public Line(Element element) {
		super(element);

		this.type = Type.LINE;
		this.points = new ArrayList<LinePoint>();
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
	public void addPoint(String id, double x, double y) {

		LinePoint point = new LinePoint();

		point.setPosition(x, y);

		this.points.add(point);
	}

	/**
	 * Give the list of points forming the line.
	 * 
	 * @return The list of points.
	 */
	public ArrayList<LinePoint> getPoints() {

		return this.points;
	}

	/**
	 * Give the two end points of the line.
	 * 
	 * @return An array of point. The point at index 0 is the starting point,
	 *         the point at index 1 is the ending point.
	 */
	public LinePoint[] getEndPoints() {

		return new LinePoint[]{
				this.points.get(0), this.points.get(this.points.size() - 1)
		};
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

		if(lShape.getPoints().size() != this.points.size())
			return false;
		
		for(int i = 0, l = lShape.getPoints().size(); i < l; ++i)
			if(!lShape.getPoints().get(i).equals(points.get(i)))
				return false;

		return true;
	}

	/*
	 * @Override public List<SpatialIndexPoint> toSpatialIndexPoints() {
	 * 
	 * List<SpatialIndexPoint> spatialIndexPoints = new
	 * ArrayList<SpatialIndexPoint>();
	 * 
	 * for(Point point : this.points) {
	 * 
	 * SpatialIndexPoint spatialIndexPoint = new SpatialIndexPoint(this,
	 * point.getId(), point.getPosition().x, point.getPosition().y);
	 * 
	 * spatialIndexPoints.add(spatialIndexPoint); }
	 * 
	 * return spatialIndexPoints; }
	 */
}
