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

import org.miv.pherd.Particle;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.BarycenterCellData;
import org.miv.pherd.ntree.Cell;
import org.miv.pherd.ntree.CellSpace;
import org.miv.pherd.ntree.QuadtreeCellSpace;

/**
 * A spatial index used to store geographical elements.
 * 
 * TODO
 * 
 * @author Antoine Dutot
 * @author Merwan Achibet
 */
public class SpatialIndex {

	protected ParticleBox box;

	protected int pointsPerCell = 10;

	protected int maxDepth = 20;

	protected double distanceOffset = 0.1;

	protected int stepsbetweenReorganizations = 10;

	public SpatialIndex() {

		CellSpace space = new QuadtreeCellSpace(new Anchor(-1, -1, 0), new Anchor(1, 1, 0));

		this.box = new ParticleBox(this.pointsPerCell, space, new BarycenterCellData());

		this.box.getNTree().setDepthMax(this.maxDepth);
	}

	/**
	 * Add an element to the spatial index.
	 * 
	 * @param element
	 *            The geographical element to add.
	 */
	public void add(Element element) {

		SpatialIndexPoint particle = element.toSpatialIndexPoint();

		this.box.addParticle(particle);

		checkForReorganization();
	}

	/**
	 * Remove an element from the spatial index.
	 * 
	 * @param element
	 *            The geographical element to remove.
	 */
	public void remove(Element element) {

		this.box.removeParticle(element.getId());

		checkForReorganization();
	}

	/**
	 * TODO
	 */
	protected void checkForReorganization() {

		if(this.box.getParticleCount() % this.stepsbetweenReorganizations == 0)
			this.box.step();
	}

	/**
	 * Count the stored elements.
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
	 *            The queried element.
	 * @return True if the element is already in the index, false otherwise.
	 */
	public boolean contains(Element element) {

		return this.box.getParticle(element.getId()) != null;
	}

	/**
	 * TODO
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ArrayList<Element> getElementsAt(double x, double y) {

		Cell root = this.box.getNTree().getRootCell();

		return searchInCell(root, x, y);
	}

	/**
	 * TODO
	 * 
	 * @param cell
	 * @param x
	 * @param y
	 * @return
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
