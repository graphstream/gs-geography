package org.graphstream.geography.test;

import java.io.IOException;
import java.util.ArrayList;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Element;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;
import org.graphstream.geography.osm.DescriptorOSM;
import org.graphstream.geography.osm.GeoSourceOSM;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.vividsolutions.jts.geom.Coordinate;

public class Test_OSM {

	public static void main(String args[]) {

		Graph graph = new SingleGraph("road network");
		graph.display(false);

		GeoSourceOSM src = new GeoSourceOSM() {

			@Override
			public void transform() {
				
				ArrayList<String> addedIds = new ArrayList<String>();
				System.out.println(index.size());
				for(Element e : this.index)
					if(e.getCategory().equals("ROAD")) {

						Line line = (Line)e;

						ArrayList<Point> points = line.getPoints();

						Point from = points.get(0);
						String idFrom = from.getId();

						if(!addedIds.contains(idFrom)) {

							sendNodeAdded(this.sourceId, idFrom);
							addedIds.add(idFrom);

							Coordinate position = getNodePosition(idFrom);
							sendNodeAttributeAdded(this.sourceId, idFrom, "x", position.x);
							sendNodeAttributeAdded(this.sourceId, idFrom, "y", position.y);
						}

						for(int i = 1, l = points.size(); i < l; ++i) {

							Point to = points.get(i);
							String idTo = to.getId();

							if(!addedIds.contains(idTo)) {

								sendNodeAdded(this.sourceId, idTo);
								addedIds.add(idTo);

								Coordinate position = getNodePosition(idTo);
								sendNodeAttributeAdded(this.sourceId, idTo, "x", position.x);
								sendNodeAttributeAdded(this.sourceId, idTo, "y", position.y);
							}

							sendEdgeAdded(this.sourceId, e.getId() + "_" + idFrom + " " + idTo, idFrom, idTo, false);

							idFrom = idTo;
						}
					}
			}

		};

		src.addSink(graph);

		// Filter.

		AttributeFilter filterRoad = new AttributeFilter();
		filterRoad.add("highway");

		// Select.

		DescriptorOSM descriptorRoad = new DescriptorOSM(src, "ROAD", filterRoad);
		descriptorRoad.mustBe(Element.Type.LINE);
		descriptorRoad.mustHave("highway");
		
		src.addDescriptor(descriptorRoad);

		// Convert.

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
