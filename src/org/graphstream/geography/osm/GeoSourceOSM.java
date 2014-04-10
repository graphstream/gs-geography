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

package org.graphstream.geography.osm;

import java.util.HashMap;

import org.graphstream.geography.GeoSource;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Abstract OpenStreetMap source.
 * 
 * This class has the capability to read OpenStreetMap XML files but the
 * accumulated data is not exploited. This work is reserved to more specific
 * implementations.
 * 
 * @author Merwan Achibet
 */
public abstract class GeoSourceOSM extends GeoSource {

	/**
	 * The root of the XML document.
	 */
	protected nu.xom.Element xmlRoot;

	/**
	 * A hash map associating the ID of a node with its position.
	 * 
	 * An OpenStreetMap XML file contains nodes which sole attributes are an ID
	 * and a position. More complex elements such as lines and polygons contain
	 * references (by ID) to these nodes. As a first step, each node position
	 * must thus be recorded in this list for faster access to the positions of
	 * the points forming complex features.
	 */
	protected HashMap<String, Coordinate> nodePositions;

	/**
	 * Instantiate a new OpenStreetMap geographic source.
	 * 
	 * @param fileName
	 *            The path to the input file.
	 */
	public GeoSourceOSM(String... fileNames) {
		super(fileNames);
		
		this.aggregator = new AggregatorOSM(this);
		
		this.nodePositions = new HashMap<String, Coordinate>();
	}
	
	public void addNodePosition(String id, Coordinate position) {
		
		this.nodePositions.put(id, position);
	}

	/**
	 * Return the position of a node with a given ID.
	 * 
	 * @param id
	 *            The ID of the queried node.
	 * @return The coordinates of the node.
	 */
	public Coordinate getNodePosition(String id) {
		Coordinate coo = this.nodePositions.get(id);
		if(coo != null)
			return new Coordinate(this.nodePositions.get(id));
		else {
			System.err.printf("cannot find node position id %s%n", id);
			//return new Coordinate(0, 0);
			return null;
			//throw new RuntimeException("XXX");
		}
	}

}
