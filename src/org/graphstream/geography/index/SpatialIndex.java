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

package org.graphstream.geography.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graphstream.geography.Element;
import org.graphstream.geography.ElementShape;
import org.miv.pherd.Particle;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.BarycenterCellData;
import org.miv.pherd.ntree.Cell;
import org.miv.pherd.ntree.CellSpace;
import org.miv.pherd.ntree.QuadtreeCellSpace;

/**
 * A spatial index used to store point references to geometric elements.
 * 
 * An instance of this class is optionally used to speed up spatial querying on
 * huge sets of geographic objects.
 * 
 * XXX This spatial index uses a quadtree implementation (from pherd) that does
 * not accept lines or polygons, only points. So it's not really adapted to
 * complex spatial queries like intersection tests. I think gs-geometry would be
 * better off using another quadtree implementation. Writing one from scratch
 * would be a bit of work but it would be more adapted. Or maybe there are
 * other libraries to achieve this work?
 * 
 * @author Antoine Dutot
 * @author Merwan Achibet
 */
public class SpatialIndex {

	/**
	 * The container for the quadtree.
	 */
	protected ParticleBox box;

	/**
	 * The number of points in a single cell of the quadtree.
	 */
	protected int pointsPerCell = 100;

	/**
	 * The maximum depth of the quadtree.
	 */
	protected int maxDepth = 50;

	/**
	 * The distance at which two points are considered on the same position.
	 */
	protected double distanceOffset = 0.1;

	/**
	 * The number of additions/removals before a reorganization of the quadtree
	 * is needed.
	 */
	protected int stepsbetweenReorganizations = 1000;

	/**
	 * The number of additions/removals that occured since the last
	 * reorganization.
	 */
	protected int modificationsSinceReorganization = 0;

	/**
	 * Instantiate a new spatial index.
	 */
	public SpatialIndex() {

		CellSpace space = new QuadtreeCellSpace(new Anchor(-1, -1, 0), new Anchor(1, 1, 0));

		this.box = new ParticleBox(this.pointsPerCell, space, new BarycenterCellData());

		this.box.getNTree().setDepthMax(this.maxDepth);
	}

	/**
	 * Add references to the points of an element into the spatial index.
	 * 
	 * @param shape
	 *            The element shape to insert.
	 */
	public void addElementPoints(ElementShape shape) {

		List<SpatialIndexPoint> points = shape.toSpatialIndexPoints();

		for(SpatialIndexPoint point : points) {

			this.box.addParticle(point);

			++this.modificationsSinceReorganization;
		}

		checkForReorganization();
	}

	/**
	 * Add a given point to the spatial index.
	 * 
	 * @param point
	 *            The point.
	 */
	public void addPoint(SpatialIndexPoint point) {

		this.box.addParticle(point);

		++this.modificationsSinceReorganization;
		checkForReorganization();
	}

	/**
	 * Recompute the quadtree structure if necessary.
	 * 
	 * If it is called often, the quadtree will be efficiently structured but if
	 * it is called too often the import process will be slown down a lot so an
	 * appropriate middle ground has to be found.
	 */
	protected void checkForReorganization() {

		if(this.modificationsSinceReorganization > this.stepsbetweenReorganizations) {

			// Reorganize the quadtree.

			this.box.step();

			// Reset the counter.

			this.modificationsSinceReorganization = 0;
		}
	}

	/**
	 * Get the number of stored elements.
	 * 
	 * @return The number of elements.
	 */
	public int size() {

		return this.box.getParticleCount();
	}

	/**
	 * Check if an element is already stored in the spatial index.
	 * 
	 * @param element
	 *            The element.
	 * @return True if the element is already in the index, false otherwise.
	 */
	public boolean contains(Element element) {

		return this.box.getParticle(element.getId()) != null;
	}

	/**
	 * Give the elements at a specific position.
	 * 
	 * The position criterion is based on a distance calculation which depends
	 * on the distance offset of the spatial index. This value can be
	 * parameterized and must be chosed appropriately with the scale of the
	 * input data.
	 * 
	 * @param x
	 *            The x-axis coordinate.
	 * @param y
	 *            The y-axis coordinate.
	 * @return A list of elements at this position.
	 */
	public ArrayList<Element> getElementsAt(double x, double y) {

		// Start the descent from the root of the quadtree.

		Cell root = this.box.getNTree().getRootCell();

		return searchInCell(root, x, y);
	}

	/**
	 * Recursively go down the quadtree until a leaf has been found and give the
	 * list of points of that cell that are at a given position.
	 * 
	 * @param cell
	 *            The current cell of the quadtree.
	 * @param x
	 *            The x-axis coordinate.
	 * @param y
	 *            The y-axis coordinate.
	 * @return A list of elements at this position.
	 */
	protected ArrayList<Element> searchInCell(Cell cell, double x, double y) {

		if(cell.isLeaf()) {

			// If we are in a leaf, accurately select the elements at the (x,y)
			// position.

			ArrayList<Element> elements = new ArrayList<Element>();

			Iterator<? extends Particle> iterator = cell.getParticles();

			while(iterator.hasNext()) {

				SpatialIndexPoint point = (SpatialIndexPoint)iterator.next();

				if(point.isAt(x, y, this.distanceOffset))
					elements.add(point.getReferencedElement());
			}

			return elements;
		}
		else {

			// Otherwise, go down the quadtree through the appropriate sub-cell.

			for(int i = 0, divs = cell.getSpace().getDivisions(); i < divs; ++i) {

				Cell subCell = cell.getSub(i);

				if(subCell.contains(x, y, 0))
					return searchInCell(subCell, x, y);
			}
		}

		return null;
	}

}
