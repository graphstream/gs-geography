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

package org.graphstream.geography;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.graphstream.geography.ElementShape.Type;
import org.graphstream.geography.index.SpatialIndex;
import org.graphstream.geography.index.SpatialIndexPoint;
import org.graphstream.stream.SourceBase;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * The geo source is the main class used when importing geographic data.
 * 
 * It manages all of the different modules (aggregator, descriptors, diff
 * builder, temporal locator) and stores matching elements from the input files.
 * 
 * @author Merwan Achibet
 */
public abstract class GeoSource extends SourceBase {

	/**
	 * ID of this source.
	 */
	protected String id;

	/**
	 * Paths to the input files.
	 */
	protected ArrayList<String> fileNames;

	/**
	 * Descriptors associated with each input file.
	 */
	protected ArrayList<FileDescriptor> fileDescriptors;

	/**
	 * Aggregator in charge of reading the input files.
	 */
	protected Aggregator aggregator;

	/**
	 * Temporal locator that date elements.
	 */
	protected TemporalLocator temporalLocator;

	/**
	 * Aggregated elements indexed according to their ID.
	 */
	protected HashMap<String, Element> elements;

	/**
	 * 
	 */
	protected SpatialIndex index;

	/**
	 * List of dates.
	 */
	protected ArrayList<Integer> dates;

	/**
	 * The current time step when playing the events.
	 */
	protected int currentTimeStep;

	/**
	 * Instantiate a new geo source with a set of input files.
	 * 
	 * @param fileNames
	 *            The paths to the input files.
	 */
	public GeoSource(String... fileNames) {

		this.id = String.format("<GeoSource %x>", System.nanoTime());

		this.fileNames = new ArrayList<String>();
		for(int i = 0; i < fileNames.length; ++i)
			this.fileNames.add(fileNames[i]);

		this.fileDescriptors = new ArrayList<FileDescriptor>();

		this.temporalLocator = new TemporalLocator(this);

		this.elements = new HashMap<String, Element>();

		this.dates = new ArrayList<Integer>();

		this.currentTimeStep = 0;
	}

	/**
	 * Prepare the spatial index.
	 */
	public void prepareSpatialIndex() {

		if(this.index == null)
			this.index = new SpatialIndex();
	}

	/**
	 * 
	 */
	public void read() {

		TreeSet<Integer> dates = new TreeSet<Integer>();

		/**
		 * First pass: go through all files and instantiate the elements and
		 * their states.
		 */

		// Aggregate the ID of the elements and the times at which they appear.

		Aggregate aggregate = this.aggregator.read();

		for(Entry<String, HashMap<Integer, Object>> entry : aggregate) {

			String id = entry.getKey();

			Element element = this.elements.get(id);

			if(element == null) {

				element = new Element(id);

				this.elements.put(id, element);

				ElementDescriptor descriptorUsed = aggregate.getDescriptorUsed(element.getId());
				element.setDescriptorUsed(descriptorUsed);
			}

			for(Integer date : entry.getValue().keySet()) {

				if(!element.hasDiffAtDate(date))
					element.addDiffAtDate(null, date);

				dates.add(date);
			}
		}

		for(Integer date : dates)
			this.dates.add(date);

		/**
		 * Second pass: go through all files and fill the element states with
		 * attribute and shape data.
		 */

		for(Element element : this.elements.values()) {

			ElementDiff previousDiff = null;
			Integer previousDate = null;

			for(Integer date : element.getDiffs().keySet()) {

				Object currentObject = aggregate.get(element.getId(), date);

				ElementDiff currentDiff = diff(element, previousDiff, previousDate, currentObject);

				element.addDiffAtDate(currentDiff, date);

				previousDiff = currentDiff;
				previousDate = date;

				// Reference the element in the spatial index if necesary.

				if(this.index != null && aggregate.descriptorsUsed.get(currentDiff.getElementId()).areElementsSentToSpatialIndex())
					for(SpatialIndexPoint p : currentDiff.getShape().toSpatialIndexPoints())
						this.index.addPoint(p);
			}
		}
	}

	/**
	 * 
	 */
	public ElementDiff diff(Element element, ElementDiff previousDiff, Integer previousDate, Object o) {

		ElementDiff nextDiff = null;

		// Retrieve all of the object attributes.

		HashMap<String, Object> allAttributes = this.aggregator.getAttributes(o);

		// Retrieve the filter to apply to the attributes.

		AttributeFilter filter = element.getDescriptorUsed().getAttributeFilter();

		// Filter.

		HashMap<String, Object> filteredAttributes = filter.filter(allAttributes);

		// If there is no previous diff, create a "base" diff.

		if(previousDiff == null) {

			nextDiff = new ElementDiff(element, true);

			// Copy all attributes.

			for(Entry<String, Object> entry : filteredAttributes.entrySet())
				nextDiff.addChangedAttribute(entry.getKey(), entry.getValue());

			// Copy the shape.

			ElementShape shape = baseShape(element, o);
			nextDiff.setShape(shape);
		}

		// Otherwise, only copy the changes.

		else {

			nextDiff = new ElementDiff(element);

			ElementView elementAtPreviousDate = element.getElementViewAtDate(previousDate);

			for(Entry<String, Object> entry : elementAtPreviousDate.getAttributes().entrySet())
				if(!filteredAttributes.containsKey(entry.getKey()))
					nextDiff.addRemovedAttribute(entry.getKey());

			for(Entry<String, Object> entry : filteredAttributes.entrySet())
				if(!elementAtPreviousDate.getAttributes().containsKey(entry.getKey()))
					nextDiff.addChangedAttribute(entry.getKey(), entry.getValue());

			for(Entry<String, Object> entry : filteredAttributes.entrySet())
				if(elementAtPreviousDate.getAttributes().containsKey(entry.getKey()) && !elementAtPreviousDate.getAttributes().get(entry.getKey()).equals(entry.getValue()))
					nextDiff.addChangedAttribute(entry.getKey(), entry.getValue());

			// Only copy the shape if it has changed.

			ElementShape newShape = diffShape(element, elementAtPreviousDate, o);
			nextDiff.setShape(newShape);
		}

		return nextDiff;
	}

	protected ElementShape baseShape(Element element, Object o) {

		// Determine the shape type of the element.

		ElementShape.Type type = this.aggregator.getType(o);

		// Instantiate a new shape.

		if(type == Type.POINT) {

			Point point = new Point(element);

			List<Vertex> vertices = this.aggregator.getShapeVertices(o);

			point.setPosition(vertices.get(0).getX(), vertices.get(0).getY());

			return point;
		}
		else if(type == Type.LINE) {

			Line line = new Line(element);

			List<Vertex> vertices = this.aggregator.getShapeVertices(o);
			
			for(Coordinate coord : coords)
				line.addPoint(null, coord.x, coord.y);

			return line;
		}
		else if(type == Type.POLYGON) {

			Polygon polygon = new Polygon(element);

			List<Vertex> vertices = this.aggregator.getShapeVertices(o);

			for(Coordinate coord : coords)
				polygon.addPoint(null, coord.x, coord.y);

			return polygon;
		}

		return null;
	}

	protected ElementShape diffShape(Element element, ElementView elementAtPreviousDate, Object o) {

		// Build a complete shape from the state of the current geographic
		// object.

		ElementShape newShape = baseShape(element, o);

		// Compare the current shape to the previous shape. If they are
		// different, the new shape is returned.

		if(!newShape.equals(elementAtPreviousDate.getShape()))
			return newShape;

		// If they are the same, return null.

		return null;
	}

	/**
	 * Populate the output graph in a single call.
	 * 
	 * All time steps will be played without interruption and the output graph
	 * will immediately be in its final configuration.
	 */
	public void end() {

		do {

		} while(next());
	}

	/**
	 * Populate the output graph from the geometric elements accumulated during
	 * the selection phase.
	 * 
	 * After this method has been executed, all the events occurring during a
	 * single time step (new elements, modified attributes, removed elements,
	 * ...) should be reflected to the output graph.
	 * 
	 * This is were the magic happens. A programmer that wants to build a
	 * specific implementation of GeoSource will do most of its work in this
	 * method. A geographer that simply wants to import geographic data into a
	 * graph will prefer to directly use an implemented use-case.
	 */
	public boolean next() {

		System.out.println(this.currentTimeStep);

		nextEvents();

		return ++this.currentTimeStep < this.dates.size();
	}

	protected abstract void nextEvents();

	/**
	 * Replicate the attribute changes of an element represented as a node in
	 * the output graph.
	 * 
	 * The node must have already been added to the graph prior to any call to
	 * this function.
	 * 
	 * @param nodeId
	 *            The ID of the node.
	 * @param diff
	 *            The element diff representing the node.
	 */
	protected void replicateNodeAttributes(String nodeId, ElementDiff diff) {

		if(diff.getChangedAttributes() != null)
			for(Entry<String, Object> keyValuePair : diff.getChangedAttributes().entrySet())
				sendNodeAttributeChanged(this.id, nodeId, keyValuePair.getKey(), null, keyValuePair.getValue());

		if(diff.getRemovedAttributes() != null)
			for(String key : diff.getRemovedAttributes())
				sendNodeAttributeRemoved(this.id, nodeId, key);
	}

	/**
	 * Replicate the attribute changes of an element represented as a node in
	 * the output graph.
	 * 
	 * The node must have already been added to the graph prior to any call to
	 * this function.
	 * 
	 * @param nodeId
	 *            The ID of the node.
	 * @param diff
	 *            The element view representing the node.
	 */
	protected void replicateNodeAttributes(String nodeId, ElementView view) {

		if(view.getAttributes() != null)
			for(Entry<String, Object> keyValuePair : view.getAttributes().entrySet())
				sendNodeAttributeChanged(this.id, nodeId, keyValuePair.getKey(), null, keyValuePair.getValue());
	}

	/**
	 * Replicate the attribute changes of an element represented as an edge in
	 * the output graph.
	 * 
	 * The edge must have already been added to the graph prior to any call to
	 * this function.
	 * 
	 * @param edgeId
	 *            The ID of the edge.
	 * @param diff
	 *            The element diff representing the edge.
	 */
	protected void replicateEdgeAttributes(String edgeId, ElementDiff diff) {

		if(diff.getChangedAttributes() != null)
			for(Entry<String, Object> keyValuePair : diff.getChangedAttributes().entrySet())
				sendNodeAttributeChanged(this.id, edgeId, keyValuePair.getKey(), null, keyValuePair.getValue());

		if(diff.getRemovedAttributes() != null)
			for(String key : diff.getRemovedAttributes())
				sendNodeAttributeRemoved(this.id, edgeId, key);
	}

	/**
	 * Replicate the attribute changes of an element represented as an edge in
	 * the output graph.
	 * 
	 * The edge must have already been added to the graph prior to any call to
	 * this function.
	 * 
	 * @param edgeId
	 *            The ID of the edge.
	 * @param view
	 *            The element view representing the edge.
	 */
	protected void replicateEdgeAttributes(String edgeId, ElementView view) {

		if(view.getAttributes() != null)
			for(Entry<String, Object> keyValuePair : view.getAttributes().entrySet())
				sendNodeAttributeChanged(this.id, edgeId, keyValuePair.getKey(), null, keyValuePair.getValue());
	}

	/**
	 * 
	 */
	protected ArrayList<Element> getExpiredElements() {

		ArrayList<Element> expiredElements = new ArrayList<Element>();

		for(Element element : this.elements.values())
			if(element.hasExpiredByDate(this.currentTimeStep))
				expiredElements.add(element);

		return expiredElements;
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public Integer dateToStep(Integer date) {

		return this.dates.indexOf(date);
	}

	/**
	 * 
	 * @param step
	 * @return
	 */
	public Integer stepToDate(int step) {

		return this.dates.get(step);
	}

	/**
	 * 
	 * @param id
	 * @param step
	 * @return
	 */
	public ElementView getElementViewAtStep(String id, int step) {

		Integer date = stepToDate(step);

		Element element = this.elements.get(id);
		if(element == null)
			return null;

		ElementView elementViewAtDate = element.getElementViewAtDate(date);

		return elementViewAtDate;
	}

	/**
	 * 
	 * @param step
	 * @return
	 */
	public ArrayList<ElementView> getElementViewsAtStep(int step) {

		ArrayList<ElementView> elementViewsAtStep = new ArrayList<ElementView>();

		for(Element element : this.elements.values()) {

			// Retrieve the element view at this step.

			Integer date = stepToDate(step);
			ElementView elementAtDate = element.getElementViewAtDate(date);

			if(elementAtDate != null)
				elementViewsAtStep.add(elementAtDate);
		}

		return elementViewsAtStep;
	}

	/**
	 * 
	 * @param step
	 * @return
	 */
	public ArrayList<ElementDiff> getElementDiffsAtStep(int step) {

		ArrayList<ElementDiff> elementDiffsAtStep = new ArrayList<ElementDiff>();

		for(Element element : this.elements.values()) {

			// Retrieve the element diff at this step.

			Integer date = stepToDate(step);
			ElementDiff elementAtDate = element.getElementDiffAtDate(date);

			if(elementAtDate != null)
				elementDiffsAtStep.add(elementAtDate);
		}

		return elementDiffsAtStep;
	}

	/**
	 * 
	 */
	public void noTime() {

		this.temporalLocator = new TemporalLocator(this);
	}

	/**
	 * 
	 */
	public void timeDependsOnFile() {

		this.temporalLocator = new TemporalLocatorByFile(this);
	}

	/**
	 * 
	 * @param attributeName
	 */
	public void timeDependsOnAttribute(String attributeName) {

		this.temporalLocator = new TemporalLocatorByAttribute(this, attributeName);
	}

	public void addFileDescriptor(FileDescriptor fileDescriptor) {

		this.fileDescriptors.add(fileDescriptor);
	}

	public ArrayList<FileDescriptor> getFileDescriptors() {

		return this.fileDescriptors;
	}

	public ArrayList<String> getFileNames() {

		return this.fileNames;
	}

	public Aggregator getAggregator() {

		return this.aggregator;
	}

	public TemporalLocator getTemporalLocator() {

		return this.temporalLocator;
	}

}
