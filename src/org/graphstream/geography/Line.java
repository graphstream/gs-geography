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

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * A Line.
 * 
 * TODO
 * 
 * @author Merwan Achibet
 */
public class Line extends Element {

	private ArrayList<Point> points;

	public Line(String id, String category) {
		super(id, category);

		this.points = new ArrayList<Point>();
	}

	public void addPoint(String id, double x, double y) {

		Point point = new Point(id, null);
		
		point.setPosition(x, y);
		
		this.points.add(point);	
	}

	public ArrayList<Point> getPoints() {

		// TODO copy
		return this.points;
	}
	
	public Point[] getEndPoints() {

		return new Point[]{this.points.get(0), this.points.get(this.points.size() - 1)};
	}

	@Override
	public boolean at(double x, double y) {

		// TODO
		return false;
	}

}