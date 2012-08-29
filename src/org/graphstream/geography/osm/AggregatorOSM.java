package org.graphstream.geography.osm;

import java.io.File;

import org.graphstream.geography.Aggregator;
import org.graphstream.geography.ElementDescriptor;
import org.graphstream.geography.FileDescriptor;
import org.graphstream.geography.GeoSource;

import com.vividsolutions.jts.geom.Coordinate;

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
	protected void traverse(FileDescriptor fileDescriptor, boolean onlyReadId) {

		nu.xom.Elements xmlElements = this.xmlRoot.getChildElements();

		for(int i = 0, l = xmlElements.size(); i < l; ++i) {

			nu.xom.Element xmlElement = xmlElements.get(i);

			for(ElementDescriptor descriptor : fileDescriptor.getDescriptors())
				if(descriptor.matches(xmlElement, this))
					aggregate(xmlElement, onlyReadId);
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
	public boolean hasKey(Object o, String key) {

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		for(int i = 0, l = xmlTags.size(); i < l; ++i)
			if(xmlTags.get(i).getAttribute("k").getValue().equals(key))
				return true;

		return false;
	}

	@Override
	public boolean hasKeyValue(Object o, String key, Object value) {

		nu.xom.Element xmlElement = (nu.xom.Element)o;

		nu.xom.Elements xmlTags = xmlElement.getChildElements("tag");

		for(int i = 0, l = xmlTags.size(); i < l; ++i) {

			nu.xom.Element xmlTag = xmlTags.get(i);

			if(xmlTag.getAttribute("k").getValue().equals(key) && xmlTag.getAttribute("v").getValue().equals(value))
				return true;
		}

		return false;
	}

}
