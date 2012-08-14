package org.graphstream.geography.test;

import java.io.IOException;

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

		try {
			src.begin("/home/merwan/map.osm");
			src.read();
			src.end();

			src.transform();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
