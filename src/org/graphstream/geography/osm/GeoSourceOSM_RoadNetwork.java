package org.graphstream.geography.osm;

import java.util.ArrayList;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Element;
import org.graphstream.geography.Line;
import org.graphstream.geography.Point;

import com.vividsolutions.jts.geom.Coordinate;

public class GeoSourceOSM_RoadNetwork extends GeoSourceOSM {

	public GeoSourceOSM_RoadNetwork() {

		// Prepare filtering.

		AttributeFilter filterRoad = new AttributeFilter();
		
		filterRoad.add("highway");

		// Prepare selection.

		DescriptorOSM descriptorRoad = new DescriptorOSM(this, "ROAD", filterRoad);
		
		descriptorRoad.mustBe(Element.Type.LINE);
		descriptorRoad.mustHave("highway");

		addDescriptor(descriptorRoad);
	}

	@Override
	public void transform() {

		ArrayList<String> addedIds = new ArrayList<String>();

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

}
