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

package org.graphstream.geography.shp;

import java.io.IOException;
import java.net.URL;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureIterator;
import org.graphstream.geography.BasicSpatialIndex;
import org.graphstream.geography.Descriptor;
import org.graphstream.geography.Element;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Line;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class GeoSourceSHP extends GeoSource {

	/**
	 * Iterator on the shapefile features.
	 */
	protected FeatureIterator<SimpleFeature> iterator;

	public GeoSourceSHP() {

		this.elements = new BasicSpatialIndex();
	}

	public void begin(String fileName) throws IOException {

		if(this.iterator == null) {

			try {

				URL url = this.getClass().getResource(fileName);

				ShapefileDataStore store = new ShapefileDataStore(url);

				String type = store.getTypeNames()[0];
				FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource(type);

				this.iterator = source.getFeatures().features();
			}
			catch (IOException e) {

				throw new RuntimeException("I/O error : " + e.getMessage());
			}
		}
	}

	public void end() throws IOException {
		// Nothing to do.
	}

	public void all() throws IOException {

		while(this.iterator != null && this.iterator.hasNext()) {

			next();

			Thread.yield();
		}

		this.iterator = null;
	}

	protected void next() throws IOException {

		// Get the current feature.

		SimpleFeature feature = iterator.next();

		// Check if the feature can be categorized among the user's interests.

		for(Descriptor descriptor : this.descriptors)
			if(descriptor.matches(feature))
				this.keep(feature, descriptor);
	}

	protected void keep(Object o, Descriptor descriptor) {

		Element element = descriptor.newElement(o);

		this.elements.add(element);
	}

	public void transform() {

		// TODO: take care of the Z index issue.
		// TODO: a spatial index would be way better to store elements and query
		// them faster. Only later should the elements be transfered to the graph.
		
		for(Element e : this.elements)
			if(e.getCategory().equals("Z")) {

				sendNodeAdded(this.sourceId, e.getId());

				sendNodeAttributeAdded(this.sourceId, e.getId(), "x", e.getAttribute("x"));
				sendNodeAttributeAdded(this.sourceId, e.getId(), "y", e.getAttribute("y"));

				// for(String key : e.getAttributes().keySet())
				// sendNodeAttributeAdded(this.sourceId, e.getId(), key,
				// e.getAttribute(key));
			}

		for(Element e : this.elements)
			if(e.getCategory().equals("ROAD")) {

				String idFrom = System.nanoTime() + "";
				sendNodeAdded(this.sourceId, idFrom);
				sendNodeAttributeAdded(this.sourceId, idFrom, "x", ((Line)e).getEndPositions()[0].x);
				sendNodeAttributeAdded(this.sourceId, idFrom, "y", ((Line)e).getEndPositions()[0].y);

				String idTo = System.nanoTime() + "";
				sendNodeAdded(this.sourceId, idTo);
				sendNodeAttributeAdded(this.sourceId, idTo, "x", ((Line)e).getEndPositions()[1].x);
				sendNodeAttributeAdded(this.sourceId, idTo, "y", ((Line)e).getEndPositions()[1].y);

				sendEdgeAdded(this.sourceId, e.getId(), idFrom, idTo, false);
			}
	}

}
