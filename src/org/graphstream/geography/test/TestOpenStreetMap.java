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

package org.graphstream.geography.test;

import java.util.ArrayList;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.Element;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.graphstream.geography.osm.DescriptorOSM;
import org.graphstream.geography.osm.GeoSourceOSM;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.Viewer;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Test the shapefile import.
 * 
 * @author Merwan Achibet
 */

public class TestOpenStreetMap {

	public static void main(String args[]) {

		new TestOpenStreetMap();
	}

	public TestOpenStreetMap() {

		Graph graph = new MultiGraph("osm");

		// Display the resulting graph.

		graph.removeAttribute("ui.quality");
		graph.removeAttribute("ui.antialias");

		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		Viewer viewer = graph.display(false);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);

		// Prepare the file import.

		GeoSourceOSM src = new GeoSourceOSM() {

			@Override
			protected void keep(Object o, Descriptor descriptor) {

				// Convert the object to a GraphStream geometric element.

				Element element = descriptor.newElement(o);

				// Add it to the spatial index.

				if(element != null)
					this.elements.add(element);
			}

			@Override
			public void transform() {

				ArrayList<String> addedIds = new ArrayList<String>();

				for(Element e : this.elements)
					if(e.getCategory().equals("ROAD")) {

						Line line = (Line)e;

						ArrayList<Point> points = line.getPoints();

						for(int i = 0, l = points.size() - 1; i < l; ++i) {
							
							Point from = points.get(i);
							String idFrom = from.getId();

							if(!addedIds.contains(idFrom)) {

								sendNodeAdded(this.sourceId, idFrom);
								addedIds.add(idFrom);

								Coordinate position = getNodePosition(idFrom);
								sendNodeAttributeAdded(this.sourceId, idFrom, "x", position.x);
								sendNodeAttributeAdded(this.sourceId, idFrom, "y", position.y);
							}

							Point to = points.get(i+1);
							String idTo = to.getId();

							if(!addedIds.contains(idTo)) {

								sendNodeAdded(this.sourceId, idTo);
								addedIds.add(idTo);

								Coordinate position = getNodePosition(idTo);
								sendNodeAttributeAdded(this.sourceId, idTo, "x", position.x);
								sendNodeAttributeAdded(this.sourceId, idTo, "y", position.y);
							}

							sendEdgeAdded(this.sourceId, e.getId()+"_"+idFrom+" "+idTo, idFrom, idTo, false);
						}
					}
			}

		};

		src.addSink(graph);

		// Filter the features and attributes to be kept in the final graph.

		AttributeFilter filterRoad = new AttributeFilter(AttributeFilter.Mode.KEEP);

		// filterRoad.add("highway");

		DescriptorOSM descriptorRoad = new DescriptorOSM(src, "ROAD", filterRoad) {

			@Override
			public boolean matches(Object o) {

				nu.xom.Element element = (nu.xom.Element)o;

				boolean r = false;
				nu.xom.Elements tags = element.getChildElements("tag");

				for(int i = 0, l = tags.size(); i < l; ++i)
					if(tags.get(i).getAttributeValue("k").equals("highway"))
						r = true;

				return element.getLocalName().equals("way") && r;
			}

		};

		src.addDescriptor(descriptorRoad);

		// Read the data.

		try {

			src.begin("/home/merwan/map.osm");
			src.all();
			src.end();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		src.transform();

		System.out.println("OK " + graph.getNodeCount());
	}
}
