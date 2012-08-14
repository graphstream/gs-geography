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
import org.graphstream.geography.Element;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.graphstream.geography.shp.DescriptorSHP;
import org.graphstream.geography.shp.GeoSourceSHP;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.Viewer;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Test the shapefile import.
 * 
 * @author Antoine Dutot
 * @author Merwan Achibet
 */

public class Test_Navteq {

	protected static final String style = "node { size: 2px; text-visibility-mode: hidden; } edge { shape:polyline; fill-color: #808080; }";

	public static void main(String args[]) {

		Graph graph = new SingleGraph("navteq");

		// Display the resulting graph.

		graph.removeAttribute("ui.quality");
		graph.removeAttribute("ui.antialias");
		graph.addAttribute("stylesheet", Test_Navteq.style);

		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		Viewer viewer = graph.display(false);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);

		// Prepare the file import.

		GeoSourceSHP src = new GeoSourceSHP() {

			@Override
			public void transform() {

				// Add the roads to the graph as edges. The Z
				// points of the spatial index are used to resolve the Z level
				// conflicts.

				ArrayList<String> addedIds = new ArrayList<String>();

				for(Element e : this.index)
					if(e.getCategory().equals("ROAD")) {

						// TODO take care of the Z index issue.

						Line line = (Line)e;

						Point[] endPoints = line.getEndPoints();

						Point from = endPoints[0];
						String idFrom = null;
						Coordinate positionFrom = from.getPosition();
						ArrayList<Element> here = this.index.getElementsAt(positionFrom.x, positionFrom.y);
						if(here.size() > 0)
							idFrom = here.get(0).getId();

						if(idFrom != null && !addedIds.contains(idFrom)) {
							sendNodeAdded(this.sourceId, idFrom);
							sendNodeAttributeAdded(this.sourceId, idFrom, "x", positionFrom.x);
							sendNodeAttributeAdded(this.sourceId, idFrom, "y", positionFrom.y);
							addedIds.add(idFrom);
						}

						Point to = endPoints[1];
						String idTo = null;
						Coordinate positionTo = to.getPosition();
						here = this.index.getElementsAt(positionTo.x, positionTo.y);
						if(here.size() > 0)
							idTo = here.get(0).getId();

						if(idTo != null && !addedIds.contains(idTo)) {
							sendNodeAdded(this.sourceId, idTo);
							sendNodeAttributeAdded(this.sourceId, idTo, "x", positionTo.x);
							sendNodeAttributeAdded(this.sourceId, idTo, "y", positionTo.y);
							addedIds.add(idTo);
						}

						if(idFrom != null && idTo != null)
							sendEdgeAdded(this.sourceId, e.getId(), idFrom, idTo, false);
					}
			}

		};

		src.addSink(graph);

		// Filter the features and attributes to be kept in the final graph.

		AttributeFilter filterZ = new AttributeFilter(AttributeFilter.Mode.KEEP);

		filterZ.add("Z_LEVEL");
		filterZ.add("LINK_ID");

		DescriptorSHP descriptorZ = new DescriptorSHP(src, "Z", filterZ) {

			@Override
			public boolean matches(Element e) {

				return Math.random() < 0.1 && e.hasAttribute("INTRSECT", "Y");
			}

		};

		src.addDescriptor(descriptorZ);

		// Read the Z level data.

		try {

			src.begin("/res/Zlevels.shp");
			src.read();
			src.end();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Filter the features and attributes to be kept in the final graph.

		AttributeFilter filterRoad = new AttributeFilter(AttributeFilter.Mode.KEEP);

		filterRoad.add("LINK_ID");
		filterRoad.add("SPEED_CAT");

		DescriptorSHP descriptorRoad = new DescriptorSHP(src, "ROAD", filterRoad) {

			@Override
			public boolean matches(Element e) {

				return e.isLine() && e.hasAttribute("SPEED_CAT", "4");
			}

		};

		src.addDescriptor(descriptorRoad);

		// Read the streets data.

		try {

			src.begin("/res/Streets.shp");
			src.read();
			src.end();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		src.transform();

		System.out.printf("OK%n");
		System.out.println(graph.getNodeCount());
	}
}
