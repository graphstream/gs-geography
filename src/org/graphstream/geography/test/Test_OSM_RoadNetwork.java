package org.graphstream.geography.test;

import org.graphstream.geography.osm.GeoSourceOSM;
import org.graphstream.geography.osm.GeoSourceOSM_RoadNetwork;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class Test_OSM_RoadNetwork {

	public static void main(String args[]) {

		Graph graph = new SingleGraph("road network");
		graph.display(false);

		GeoSourceOSM src = new GeoSourceOSM_RoadNetwork();
		src.addSink(graph);

		src.read("/home/merwan/map.osm");
	}

}
