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

package org.graphstream.geography.test;

import java.util.Random;

import org.graphstream.geography.index.SpatialIndex;
import org.graphstream.geography.index.SpatialIndexPoint;

/**
 * Basic test of the spatial index structure.
 * 
 * @author Merwan Achibet
 */
public class Test_SpatialIndex {

	public static void main(String[] args) {

		// Instantiate the spatial index.

		SpatialIndex index = new SpatialIndex();

		// Add a lot of randomly placed points.

		Random rnd = new Random(12345);

		for(int i = 0; i < 1000; ++i) {
			SpatialIndexPoint p = new SpatialIndexPoint(null, ""+rnd.nextDouble(), rnd.nextDouble() * 100, rnd.nextDouble() * 100);
			index.addPoint(p);
		}

		// Add specific points.

		SpatialIndexPoint a = new SpatialIndexPoint(null, "A", 0, 0);
		index.addPoint(a);

		SpatialIndexPoint b = new SpatialIndexPoint(null, "B", 0.00001, 0.00001);
		index.addPoint(b);

		SpatialIndexPoint c = new SpatialIndexPoint(null, "C", 1, 1);
		index.addPoint(c);

		System.out.println(index.size());

		System.out.println(index.getElementsAt(0, 0));
	}

}
