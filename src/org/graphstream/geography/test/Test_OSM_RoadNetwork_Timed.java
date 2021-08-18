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

import org.graphstream.geography.osm.GeoSourceOSM_RoadNetwork;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Test the import of a road network from an OpenStreetMap XML file.
 * 
 * @author Merwan Achibet
 */
public class Test_OSM_RoadNetwork_Timed {

	public static void main(String args[]) {

		Graph graph = new SingleGraph("road network");

		GeoSourceOSM_RoadNetwork src = new GeoSourceOSM_RoadNetwork(System.getProperty("user.dir")+"/data/roads_t0.osm", System.getProperty("user.dir")+"/data/roads_t1.osm", System.getProperty("user.dir")+"/data/roads_t2.osm");
		src.addSink(graph);
		
		src.timeDependsOnFile();

		src.getRoadAttributeFilter().addAttribute("highway");

		src.read();

		graph.display(false);

		do {

			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		} while(src.next());

	}
}
