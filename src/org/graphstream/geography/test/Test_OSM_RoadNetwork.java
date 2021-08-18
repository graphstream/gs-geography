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

import java.io.IOException;

import org.graphstream.geography.osm.GeoSourceOSM;
import org.graphstream.geography.osm.GeoSourceOSM_RoadNetwork;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDGS;

/**
 * Test the import of a road network from an OpenStreetMap XML file.
 * 
 * @author Merwan Achibet
 */
public class Test_OSM_RoadNetwork {

	public static void main(String args[]) {

		String n= "node {size: 1px;	fill-color: #777;text-mode: hidden;z-index: 0;}";
		String e = "edge { shape: line; fill-color: #222; arrow-size: 3px, 2px; }";
		String e1="edge.tollway { size: 2px; stroke-color: red; stroke-width: 1px; stroke-mode: plain; }";
		String e2="edge.tunnel { stroke-color: blue; stroke-width: 1px; stroke-mode: plain; }";
		String e3="edge.bridge { stroke-color: yellow; stroke-width: 1px; stroke-mode: plain; }";
		
		Graph graph = new SingleGraph("road network");
		graph.setAttribute("ui.stylesheet",n+e+e1+e2+e3);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		//System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		GeoSourceOSM src = new GeoSourceOSM_RoadNetwork(System.getProperty("user.dir")+"/data/aquila.osm");//roads.osm");
		src.addSink(graph);

		src.read();
		
		src.end();
		
		for(Edge edge: graph.getEachEdge()) {
			if(edge.hasAttribute("isTollway")) {
				edge.addAttribute("ui.class", "tollway");
			} else if(edge.hasAttribute("isTunnel")) {
				edge.addAttribute("ui.class", "tunnel");
			} else if(edge.hasAttribute("isBridge")) {
				edge.addAttribute("ui.class", "bridge");
			}
		}
		

		graph.display(false);
		try {
			FileSink fs= new FileSinkDGS();
			fs.writeAll(graph, System.getProperty("user.dir")+"/data/aquila2");
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
	}

}
