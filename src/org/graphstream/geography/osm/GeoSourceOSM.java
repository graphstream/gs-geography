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

package org.graphstream.geography.osm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.graphstream.geography.BasicSpatialIndex;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.GeoSource;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class GeoSourceOSM extends GeoSource {

	/**
	 * The root of the XML document.
	 */
	protected nu.xom.Element root;

	protected HashMap<String, Coordinate> nodePositions;

	public GeoSourceOSM() {

		this.index = new BasicSpatialIndex();
	}

	public void begin(String fileName) throws IOException {

		try {

			File file = new File(fileName);

			nu.xom.Builder builder = new nu.xom.Builder();
			this.root = builder.build(file).getRootElement();

			// Store the position of every node as they will be referred to by
			// most of the other elements.

			this.nodePositions = new HashMap<String, Coordinate>();

			nu.xom.Elements nodes = this.root.getChildElements("node");

			for(int i = 0, l = nodes.size(); i < l; ++i) {

				nu.xom.Element node = nodes.get(i);

				String id = node.getAttributeValue("id");

				double x = Double.parseDouble(node.getAttributeValue("lon"));
				double y = Double.parseDouble(node.getAttributeValue("lat"));

				this.nodePositions.put(id, new Coordinate(x, y));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void end() throws IOException {
		// Nothing to do.
	}

	public void read() throws IOException {

		nu.xom.Elements elements = this.root.getChildElements();

		for(int i = 0, l = elements.size(); i < l; ++i)
			process(elements.get(i));
	}

	private void process(nu.xom.Element element) {

		for(Descriptor descriptor : this.descriptors)
			if(descriptor.matches(element))
				this.keep(element, descriptor);
	}

	protected void next() throws IOException {

	}

	public Coordinate getNodePosition(String id) {

		return this.nodePositions.get(id);
	}

}
