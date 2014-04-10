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

package org.graphstream.geography.osm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.graphstream.geography.Aggregator;
import org.graphstream.geography.ElementDescriptor;
import org.graphstream.geography.FileDescriptor;
import org.graphstream.geography.GeoSource;
import org.graphstream.geography.Vertex;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * @author Merwan Achibet
 */
public class AggregatorOSM extends Aggregator {

	protected nu.xom.Element xmlRoot;

	public AggregatorOSM(GeoSource source) {
		super(source);
	}

	@Override
	protected void open(FileDescriptor fileDescriptor) {

		try {

			File file = new File(fileDescriptor.getFileName());

			// Instantiate a XOM parser.

			nu.xom.Builder builder = new nu.xom.Builder();

			// Save the root of the XML document.

			this.xmlRoot = builder.build(file).getRootElement();

			// Store the position of every node as they will be referred to by
			// most of the other elements.

			nu.xom.Elements nodes = this.xmlRoot.getChildElements("node");

			for(int i = 0, l = nodes.size(); i < l; ++i) {

				nu.xom.Element node = nodes.get(i);

				String id = node.getAttributeValue("id");

				double x = Double.parseDouble(node.getAttributeValue("lon"));
				double y = Double.parseDouble(node.getAttributeValue("lat"));
				Coordinate pos = new Coordinate(x, y);

				((GeoSourceOSM)this.source).addNodePosition(id, pos);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void traverse(FileDescriptor fileDescriptor) {

		nu.xom.Elements xmlElements = this.xmlRoot.getChildElements();

		for(int i = 0, l = xmlElements.size(); i < l; ++i) {

			nu.xom.Element xmlElement = xmlElements.get(i);

			for(ElementDescriptor descriptor : fileDescriptor.getDescriptors())
				if(descriptor.matches(xmlElement, this)) {

					Integer date = this.source.getTemporalLocator().date(xmlElement);

					aggregate(xmlElement, date, descriptor);
				}
		}
	}

	@Override
	protected void close(FileDescriptor fileDescriptor) {

		this.xmlRoot = null;
	}

	@Override
	public String getFeatureId(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Return its ID.

		return xmlElement.getAttributeValue("id");
	}

	@Override
	public boolean hasKey(Object o, String key) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		// Check if the attribute exists.

		for(int i = 0, l = xmlTags.size(); i < l; ++i)
			if(xmlTags.get(i).getAttribute("k").getValue().equals(key))
				return true;

		return false;
	}

	@Override
	public boolean hasKeyValue(Object o, String key, Object value) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Check if the attribute exists and has the correct value.

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		for(int i = 0, l = xmlTags.size(); i < l; ++i) {

			nu.xom.Element xmlTag = xmlTags.get(i);

			if(xmlTag.getAttribute("k").getValue().equals(key) && xmlTag.getAttribute("v").getValue().equals(value))
				return true;
		}

		return false;
	}

	@Override
	public Object getAttributeValue(Object o, String key) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Get the value of the attribute.

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		for(int i = 0, l = xmlTags.size(); i < l; ++i) {

			nu.xom.Element xmlTag = xmlTags.get(i);

			if(xmlTag.getAttribute("k").getValue().equals(key))
				return xmlTag.getAttribute("v").getValue();
		}

		// Return null if the attribute does not exist.

		return null;
	}

	@Override
	public HashMap<String, Object> getAttributes(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve all attributes.

		HashMap<String, Object> attributes = new HashMap<String, Object>();

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		for(int i = 0, l = xmlTags.size(); i < l; ++i)
			attributes.put(xmlTags.get(i).getAttributeValue("k"), xmlTags.get(i).getAttributeValue("v"));

		return attributes;
	}

	@Override
	public boolean isPoint(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// The name of XML entries representing points is "node".

		return xmlElement.getLocalName().equals("node");
	}

	@Override
	public boolean isLine(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// The name of XML entries representing lines is "way" and it must be
		// open (or it would be a polygon).

		return xmlElement.getLocalName().equals("way") && !isClosed(xmlElement);
	}

	@Override
	public boolean isPolygon(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// The name of XML entries representing lines is "way" and it must be
		// closed (or it would be a line).

		return xmlElement.getLocalName().equals("way") && isClosed(xmlElement);
	}

	/**
	 * Check if the geographic object shape is looped.
	 * 
	 * Line and polygons are similar in the OpenStreetMap format but polygons
	 * have the same starting and ending point (they are closed).
	 * 
	 * @param xmlElement
	 *            The XML element.
	 * @return True if the element is closed, false otherwise.
	 */
	protected boolean isClosed(nu.xom.Element xmlElement) {

		// Get all the nodes of the element.

		nu.xom.Elements xmlNodes = xmlElement.getChildElements("nd");

		// Check that there are child nodes.

		if(xmlNodes.size() == 0)
			return false;

		// Check that the first node is the same as the last.

		String idFirst = xmlNodes.get(0).getAttributeValue("ref");
		String idLast = xmlNodes.get(xmlNodes.size() - 1).getAttributeValue("ref");

		return idFirst.equals(idLast);
	}

	@Override
	protected List<Vertex> getPointVertices(Object o) {

		// Retrieve the position of the node.

		List<Vertex> vertices = new ArrayList<Vertex>();

		Coordinate coord = ((GeoSourceOSM)this.source).getNodePosition(getFeatureId(o));
		
		vertices.add(new Vertex(coord.x, coord.y, getFeatureId(o)));

		return vertices;
	}

	@Override
	protected List<Vertex> getLineVertices(Object o) {

		// Cast the object to a XOM element.

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		// Retrieve the positions of all nodes referenced in the path.

		List<Vertex> vertices = new ArrayList<Vertex>();

		nu.xom.Elements xmlNodes = xmlElement.getChildElements("nd");

		GeoSourceOSM sourceOSM = (GeoSourceOSM)this.source;

		for(int i = 0, l = xmlNodes.size(); i < l; ++i) {
			
			String nodeId = xmlNodes.get(i).getAttributeValue("ref");
			
			Coordinate coord = sourceOSM.getNodePosition(nodeId);
			
			if(coord != null)
				vertices.add(new Vertex(coord.x, coord.y, nodeId));
			else System.err.printf("ignoring coord for node %s, referenced in line, but not found%n", nodeId);
		}
		
		return vertices;
	}

	@Override
	protected List<Vertex> getPolygonVertices(Object o) {

		return getLineVertices(o);
	}

}
