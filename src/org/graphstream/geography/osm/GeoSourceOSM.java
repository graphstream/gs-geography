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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.graphstream.geography.Descriptor;
import org.graphstream.geography.Element;
import org.graphstream.geography.GeoSource;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * An abstract OpenStreetMap source.
 * 
 * It has the capability to read OpenStreetMap XML files but the accumulated
 * data is not exploited. This work is reserved to more specific implementations
 * of this class.
 * 
 * @author Merwan Achibet
 */
public abstract class GeoSourceOSM extends GeoSource {

	protected String fileName;
	
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
	 * must be recorded in this list for faster access to the positions of the
	 * points forming complex features.
	 */
	protected HashMap<String, Coordinate> nodePositions;

	public GeoSourceOSM(String fileName) {

		this.fileName = fileName;
		
		this.nodePositions = new HashMap<String, Coordinate>();
	}
	
	protected void read() {

		try {

			begin(this.fileName);

			traverse();

			end();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void begin(String fileName) throws IOException {

		try {

			File file = new File(fileName);

			// Instantiate a XOM parser.

			nu.xom.Builder builder = new nu.xom.Builder();

			// Save the root of the XML document.

			this.xmlRoot = builder.build(file).getRootElement();

			// Store the position of every node as they will be referred to by
			// most of the other elements.

			nu.xom.Elements nodes = this.xmlRoot.getChildElements("node");

			for(int i = 0, l = nodes.size(); i < l; ++i) {

				nu.xom.Element node = nodes.get(i);

				String id = node.getAttributeValue("id");

				double x = Double.parseDouble(node.getAttributeValue("lon"));
				double y = Double.parseDouble(node.getAttributeValue("lat"));
				Coordinate pos = new Coordinate(x, y);

				this.nodePositions.put(id, pos);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void traverse() {

		nu.xom.Elements xmlElements = this.xmlRoot.getChildElements();

		for(int i = 0, l = xmlElements.size(); i < l; ++i)
			process(xmlElements.get(i));
	}

	public void end() throws IOException {
		// Nothing to do.
	}

	/**
	 * Process a single feature coming from the data source and check if it
	 * suits the user's needs. If it is the case, keep it for a later use,
	 * ignore it otherwise.
	 * 
	 * @param xmlElement
	 *            The XOM XML element to consider.
	 * @throws IOException
	 */
	private void process(nu.xom.Element xmlElement) {

		for(Descriptor descriptor : this.descriptors) {

			Element element = descriptor.newElement(xmlElement);

			if(element != null && descriptor.matches(element))
				this.keep(element, descriptor);
		}
	}

	public Coordinate getNodePosition(String id) {

		return new Coordinate(this.nodePositions.get(id));
	}

}
