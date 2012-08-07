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

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.shp.DescriptorSHP;
import org.graphstream.geography.shp.FileSourceSHP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.Viewer;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Test the shapefile import.
 * 
 * @author Antoine Dutot
 * @author Merwan Achibet
 */

public class TestFileSourceSHP {

	public static void main(String args[]) {

		new TestFileSourceSHP();
	}

	public static final String GRAPH_ID = "navteq";

	public TestFileSourceSHP() {
		Graph graph = new MultiGraph(GRAPH_ID);

		// Display the resulting graph.

		graph.addAttribute("stylesheet", styleSheetNew);
		graph.removeAttribute("ui.quality");
		graph.removeAttribute("ui.antialias");

		System.setProperty("gs.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		Viewer viewer = graph.display(false);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);

		// Prepare the file import.

		FileSourceSHP src = new FileSourceSHP();

		src.addSink(graph);

		// Filter the attributes to be kept in the final graph.

		AttributeFilter filter = new AttributeFilter(AttributeFilter.Mode.KEEP);

		filter.add("Z_LEVEL");
		filter.add("LINK_ID");
		filter.add("INTRSECT");

		// Filter the features to consider.

		DescriptorSHP descriptor = new DescriptorSHP("TEST", filter) {

			@Override
			public boolean matches(Object o) {

				SimpleFeature feature = (SimpleFeature) o;

				if(Math.random() < 0.1
						&& isPoint(feature)
						&& feature.getProperty("INTRSECT") != null
						&& feature.getProperty("INTRSECT").getValue()
								.equals("Y"))
					return true;

				return false;
			}

		};

		src.addDescriptor(descriptor);

		// Go and read the data.

		try {

			src.begin("/res/Zlevels.shp");
			src.all();

			/*
			 * src.setMergeOperations(mergeOps); src.begin("Streets.shp");
			 * src.all();
			 */
			// equipGraph(graph);

			// src.end();
			// src.release();

			// addStyle(graph);

			System.out.printf("OK%n");
			System.out.println(graph.getNodeCount());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void equipGraph(Graph graph) {
		for(Edge edge : graph.getEachEdge()) {
			if(edge.hasAttribute("length")) {
				double length = edge.getNumber("length");
				double maxCap = length;

				if(edge.hasLabel("SPEED_CAT")) {
					String speed_cat = (String) edge.getLabel("SPEED_CAT");
					double speed = speedCatToKph(speed_cat); // In kilometres
																// per hour.
					double time = (length / 1000) / speed; // In hours.
					time *= 60; // In minutes.
					edge.setAttribute("time", time);
				} else
					System.err.printf("no SPEED_CAT !!%n");

				maxCap /= 5; // A car uses 5 meters.
				if(maxCap < 5)
					maxCap = 5;
				edge.addAttribute("maxCap", maxCap);
			} else
				System.err.printf("no length !!!%n");
		}

		for(Node node : graph) {
			node.setAttribute("ui.label", node.getId());
		}
	}

	protected int speedCatToKph(String cat) {
		int c = Integer.parseInt(cat);

		switch (c) {
		case 1:
			return 150;
		case 2:
			return 130;
		case 3:
			return 100;
		case 4:
			return 90;
		case 5:
			return 70;
		case 6:
			return 50;
		case 7:
			return 30;
		case 8:
			return 11;
		}

		return 5;
	}

	protected void addStyle(Graph graph) {
		for(Edge edge : graph.getEachEdge()) {
			StringBuilder cls = new StringBuilder();
			int cnt = 0;

			if(edge.getLabel("LANE_CAT").equals("2")) {
				cls.append("laneCat2");
				cnt++;
			} else if(edge.getLabel("LANE_CAT").equals("3")) {
				cls.append("laneCat3");
				cnt++;
			}

			if(edge.getLabel("CONTRACC").equals("Y")) {
				if(cnt > 0)
					cls.append(",");
				cls.append("freeway");
				cnt++;
			}

			if(cnt > 0) {
				edge.addAttribute("ui.class", cls.toString());
			}
		}
	}

	protected static final String styleSheetNew = "node { size: 3px; text-visibility-mode: hidden; }"
			+ "edge { shape:polyline; fill-color: #808080; arrow-size: 3px, 3px; }"
			+ "edge.freeway  { size: 2px; stroke-width: 1px; stroke-color: red; shadow-mode: plain; shadow-color: red; shadow-width: 1px; shadow-offset: 0px, 0px; }"
			+ "edge.laneCat2 { fill-color:#E0D040; }"
			+ "edge.laneCat3 { fill-color:#F0F060; }";
}