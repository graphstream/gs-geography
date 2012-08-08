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
import org.graphstream.geography.shp.GeoSourceSHP;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.Viewer;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Test the shapefile import.
 * 
 * @author Antoine Dutot
 * @author Merwan Achibet
 */

public class TestNavteq {

	public static void main(String args[]) {

		new TestNavteq();
	}

	public static final String GRAPH_ID = "navteq";
	
	protected static final String style = 
			"node { size: 2px; text-visibility-mode: hidden; }" +
			"edge { shape:polyline; fill-color: #808080; arrow-size: 3px, 3px; }" +
			"edge.freeway  { size: 2px; stroke-width: 1px; stroke-color: red; shadow-mode: plain; shadow-color: red; shadow-width: 1px; shadow-offset: 0px, 0px; }";

	public TestNavteq() {

		Graph graph = new MultiGraph(GRAPH_ID);

		// Display the resulting graph.
		
		graph.removeAttribute("ui.quality");
		graph.removeAttribute("ui.antialias");
		graph.addAttribute("stylesheet", TestNavteq.style);

		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		Viewer viewer = graph.display(false);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);
		
		// Prepare the file import.

		GeoSourceSHP src = new GeoSourceSHP();

		src.addSink(graph);

		// Filter the features and attributes to be kept in the final graph.

		AttributeFilter filterZ = new AttributeFilter(AttributeFilter.Mode.KEEP);

		filterZ.add("Z_LEVEL");
		filterZ.add("LINK_ID");
		filterZ.add("INTRSECT");

		DescriptorSHP descriptorZ = new DescriptorSHP("Z", filterZ) {

			@Override
			public boolean matches(Object o) {

				SimpleFeature feature = (SimpleFeature) o;

				return Math.random() < 0.1 && isPoint(feature) && feature.getProperty("INTRSECT") != null && feature.getProperty("INTRSECT").getValue().equals("Y");
			}

		};

		src.addDescriptor(descriptorZ);
		
		// Read the Z level data.

		try {

			src.begin("/res/Zlevels.shp");
			src.all();
			src.end();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Filter the features and attributes to be kept in the final graph.

		AttributeFilter filterRoad = new AttributeFilter(AttributeFilter.Mode.KEEP);

		filterRoad.add("LINK_ID");
		filterRoad.add("SPEED_CAT");
		
		DescriptorSHP descriptorRoad = new DescriptorSHP("ROAD", filterRoad) {

			@Override
			public boolean matches(Object o) {
				
				SimpleFeature feature = (SimpleFeature)o;

				return isLine(feature) && feature.getProperty("SPEED_CAT") != null && feature.getProperty("SPEED_CAT").getValue().equals("7");
			}
			
		};

		src.addDescriptor(descriptorRoad);
		
		// Read the streets data.
		try {
			
			src.begin("/res/Streets.shp");
			src.all();
			src.end();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Final step.
		
		src.transform();
		
		System.out.printf("OK%n");
	}
}
