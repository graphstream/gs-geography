package org.graphstream.geography.osm;

import java.util.HashMap;

import org.graphstream.geography.AttributeFilter;
import org.graphstream.geography.Element;
import org.graphstream.geography.Polygon;

import com.vividsolutions.jts.geom.Coordinate;

public class GeoSourceOSM_Neighborhood extends GeoSourceOSM {

	/**
	 * The neighborhood radius. If two buildings are separated by a distance
	 * less than this radius, then they are considered neighbors.
	 */
	private double radius;

	public GeoSourceOSM_Neighborhood(double radius) {

		this.radius = radius;

		// Prepare filtering.

		AttributeFilter filterRoad = new AttributeFilter();

		filterRoad.add("building");

		// Prepare selection.

		DescriptorOSM descriptorBuilding = new DescriptorOSM(this, "BUILDINGS", null);//filterRoad);

		descriptorBuilding.mustBe(Element.Type.POLYGON);
		descriptorBuilding.mustHave("building", "yes");

		addDescriptor(descriptorBuilding);
	}

	@Override
	public void transform() {

		HashMap<String, Coordinate> placedBuildings = new HashMap<String, Coordinate>();

		for(Element e : this.index) {

			// Compute the center of the current building and add a new node at
			// this position.

			Coordinate centroid = ((Polygon)e).getCentroid();

			sendNodeAdded(this.sourceId, e.getId());

			sendNodeAttributeAdded(this.sourceId, e.getId(), "x", centroid.x);
			sendNodeAttributeAdded(this.sourceId, e.getId(), "y", centroid.y);

			// Draw an edge between the new node and already placed ones if
			// their distance is below the neighborhood radius.

			for(String id : placedBuildings.keySet())
				if(centroid.distance(placedBuildings.get(id)) < this.radius)
					sendEdgeAdded(this.sourceId, e.getId() + id, e.getId(), id, false);

			placedBuildings.put(e.getId(), centroid);
		}
	}

}
