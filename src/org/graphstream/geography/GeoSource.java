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

/**
 * The geo source is the main class used when importing geographic data.
 * 
 * It manages all of the different modules (aggregator, descriptors, temporal
 * locator, stores matching elements from the input files and populate the
 * output graph.
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
	 * The spatial index optionally used to store spatial references.
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
	 * 
	 * This method is only called if a descriptor is setup to store matching
	 * elements in a spatial index.
	 */
	public void prepareSpatialIndex() {

		this.index = new SpatialIndex();
	}

	/**
	 * Go through the input files, aggregate the relevant data and convert the
	 * geographic objects to standard geometric elements.
	 */
	public void read() {

		/**
		 * First pass: go through all files and instantiate the elements.
		 */

		// All the dates will be accumulated in this ordered set.

		TreeSet<Integer> dates = new TreeSet<Integer>();

		// Aggregate the geographic objects and the dates at which they appear.

		Aggregate aggregate = this.aggregator.read();

		// For each accumulated geographic object...

		for(Entry<String, HashMap<Integer, Object>> entry : aggregate) {

			// Create the corresponding element.

			String id = entry.getKey();

			Element element = this.elements.get(id);

			if(element == null) {

				element = new Element(id);

				element.setDescriptorUsed(aggregate.getDescriptorUsed(element.getId()));

				this.elements.put(id, element);
			}

			// Create an empty slot for a diff each time the element appears.

			for(Integer date : entry.getValue().keySet()) {

				element.addDiffAtDate(null, date);

				dates.add(date);
			}
		}

		// Transfer the content of the ordered set of dates into our list of
		// dates.

		for(Integer date : dates)
			this.dates.add(date);

		/**
		 * Second pass: fill the element diffs with attribute and shape data.
		 */

		for(Element element : this.elements.values()) {

			Integer previousDate = null;

			for(Integer date : element.getDiffs().keySet()) {

				Object currentObject = aggregate.get(element.getId(), date);

				ElementDiff currentDiff = diff(element, previousDate, currentObject);

				// Only add the diff to the diff chain if the element changed
				// since the last date.

				if(!currentDiff.isEmpty()) {

					element.addDiffAtDate(currentDiff, date);

					// Reference the element in the spatial index if necessary.

					if(this.index != null && aggregate.descriptorsUsed.get(currentDiff.getElementId()).areElementsSentToSpatialIndex())
						for(SpatialIndexPoint p : currentDiff.getShape().toSpatialIndexPoints())
							this.index.addPoint(p);
				}

				//

				previousDate = date;
			}

			// Add a diff at the end of the diff chain to express the
			// disappearance of the element.

			// Special case: we don't add a deletion diff if the element still
			// exists at the last time step or we would end with an empty graph.

			Integer elementLastDate = element.getDiffs().lastKey();
			Integer globalLastDate = this.dates.get(this.dates.size() - 1);

			if(!elementLastDate.equals(this.dates.get(globalLastDate))) {

				ElementDiff deletionDiff = new ElementDiff(element);
				deletionDiff.setDeleted();

				Integer deletionDate = this.dates.get(this.dates.indexOf(elementLastDate) + 1);

				element.addDiffAtDate(deletionDiff, deletionDate);
			}

			// Remove the empty diff slots that were not filled up because their
			// diff was useless.

			ArrayList<Integer> datesToBeDeleted = new ArrayList<Integer>();

			for(Entry<Integer, ElementDiff> dateDiffPair : element.getDiffs().entrySet())
				if(dateDiffPair.getValue() == null)
					datesToBeDeleted.add(dateDiffPair.getKey());

			for(Integer dateToBeDeleted : datesToBeDeleted)
				element.removeDiffAtDate(dateToBeDeleted);
		}
	}

	/**
	 * Build an element diff.
	 * 
	 * @param element
	 *            The element.
	 * @param previousDiff
	 *            The previous diff of the element.
	 * @param previousDate
	 *            The date of the previous diff.
	 * @param o
	 *            The geographic object to convert to a diff.
	 * @return A diff representing the changes that occured since the last diff.
	 */
	public ElementDiff diff(Element element, Integer previousDate, Object o) {

		ElementDiff nextDiff = null;

		// Retrieve all of the object attributes.

		HashMap<String, Object> allAttributes = this.aggregator.getAttributes(o);

		// Retrieve the filter to apply to the attributes.

		AttributeFilter filter = element.getDescriptorUsed().getAttributeFilter();

		// Filter.

		HashMap<String, Object> filteredAttributes = filter.filter(allAttributes);

		// If there is no previous diff, create a "base" diff.

		if(!element.hasBaseDiff()) {

			nextDiff = new ElementDiff(element, true);

			// Copy the filtered attributes.

			for(Entry<String, Object> entry : filteredAttributes.entrySet())
				nextDiff.addChangedAttribute(entry.getKey(), entry.getValue());

			// Copy the shape.

			nextDiff.setShape(baseShape(element, o));
		}

		// Otherwise, only copy the changes since the last diff.

		else {

			nextDiff = new ElementDiff(element, false);

			ElementView elementAtPreviousDate = element.getElementViewAtDate(previousDate);

			// Check for attributes that were removed since the previous date.

			for(Entry<String, Object> entry : elementAtPreviousDate.getAttributes().entrySet())
				if(!filteredAttributes.containsKey(entry.getKey()))
					nextDiff.addRemovedAttribute(entry.getKey());

			// Check for attributes that were added since the previous date.

			for(Entry<String, Object> entry : filteredAttributes.entrySet())
				if(!elementAtPreviousDate.getAttributes().containsKey(entry.getKey()))
					nextDiff.addChangedAttribute(entry.getKey(), entry.getValue());

			// Check for attributes that were modified since the previous date.

			for(Entry<String, Object> entry : filteredAttributes.entrySet())
				if(elementAtPreviousDate.getAttributes().containsKey(entry.getKey()) && !elementAtPreviousDate.getAttributes().get(entry.getKey()).equals(entry.getValue()))
					nextDiff.addChangedAttribute(entry.getKey(), entry.getValue());

			// Only copy the shape if it has changed.

			ElementShape newShape = diffShape(element, elementAtPreviousDate, o);

			nextDiff.setShape(newShape);
		}

		return nextDiff;
	}

	/**
	 * Build the the shape of an element from the geographic object it
	 * represents.
	 * 
	 * @param element
	 *            The element.
	 * @param o
	 *            The geographic object.
	 * @return The shape of the geographic object.
	 */
	protected ElementShape baseShape(Element element, Object o) {

		// Determine the geometric type of the element.

		ElementShape.Type type = this.aggregator.getShapeType(o);

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

			for(Vertex vertex : vertices)
				line.addVertex(vertex.getId(), vertex.getX(), vertex.getY());

			return line;
		}
		else if(type == Type.POLYGON) {

			Polygon polygon = new Polygon(element);

			List<Vertex> vertices = this.aggregator.getShapeVertices(o);

			for(Vertex vertex : vertices)
				polygon.addVertex(vertex.getId(), vertex.getX(), vertex.getY());

			return polygon;
		}

		return null;
	}

	/**
	 * Give the new shape of an element if it changed since the last diff.
	 * 
	 * @param element
	 *            The element.
	 * @param elementAtPreviousDate
	 *            The complete state of the element at the previous date.
	 * @param o
	 *            The geographic object at the current date.
	 * @return The new shape if it is different from its previous diff,
	 *         otherwise null.
	 */
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
	 */
	public boolean next() {

		System.out.println("step " + this.currentTimeStep + " (date: " + stepToDate(this.currentTimeStep) + ")");

		nextEvents();

		return ++this.currentTimeStep < this.dates.size();
	}

	/**
	 * This is were the magic happens. A programmer that wants to build a
	 * specific implementation of GeoSource will do most of its work in this
	 * method. A geographer that simply wants to import geographic data into a
	 * graph will prefer to directly use an implemented use-case.
	 */
	protected abstract void nextEvents();

	/**
	 * Replicate the attribute changes of an element represented as a node in
	 * the output graph.
	 * 
	 * The node must have already been added to the graph prior to any call to
	 * this method.
	 * 
	 * @param nodeId
	 *            The node ID.
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
	 * Replicate the attribute changes of an element represented as an edge in
	 * the output graph.
	 * 
	 * The edge must have already been added to the graph prior to any call to
	 * this method.
	 * 
	 * @param edgeId
	 *            The edge ID.
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
	 * this method.
	 * 
	 * @param edgeId
	 *            The edge ID.
	 * @param view
	 *            The element view representing the edge.
	 */
	protected void replicateEdgeAttributes(String edgeId, ElementView view) {

		if(view.getAttributes() != null)
			for(Entry<String, Object> keyValuePair : view.getAttributes().entrySet())
				sendNodeAttributeChanged(this.id, edgeId, keyValuePair.getKey(), null, keyValuePair.getValue());
	}

	/**
	 * Give the date associated with a given time step.
	 * 
	 * In some cases, the time step number and the date are the same (for
	 * example, when each time step corresponds to a different file: step 0 =
	 * date 0, step 1 = date 1, ...) but they can be different (step 0 = date
	 * 1950, step 1 = date 1965, ...).
	 * 
	 * @param date
	 *            The date.
	 * @return The time step associated with this date.
	 */
	public Integer dateToStep(Integer date) {

		return this.dates.indexOf(date);
	}

	/**
	 * Give the time step associated with a given date.
	 * 
	 * @param step
	 *            The time step index.
	 * @return The date associated with this time step.
	 */
	public Integer stepToDate(int step) {

		return this.dates.get(step);
	}

	/**
	 * Give the complete state of an element at a given date.
	 * 
	 * @param id
	 *            The element ID.
	 * @param step
	 *            The time step.
	 * @return The state of the element at this step.
	 */
	public ElementView getElementViewAtStep(String id, int step) {

		// Get the real date from the time step index.

		Integer date = stepToDate(step);

		// Check that the element exists.

		Element element = this.elements.get(id);
		if(element == null)
			return null;

		// Return the state of the element at this date.

		return element.getElementViewAtDate(date);
	}

	/**
	 * Give the complete state of all elements at a given date.
	 * 
	 * @param step
	 *            The time step.
	 * @return The state of all elements at this step.
	 */
	public ArrayList<ElementView> getElementViewsAtStep(int step) {

		// Get the real date from the time step index.

		Integer date = stepToDate(step);

		//

		ArrayList<ElementView> elementViewsAtStep = new ArrayList<ElementView>();

		for(Element element : this.elements.values()) {

			// Retrieve the element view at this step.

			ElementView elementAtDate = element.getElementViewAtDate(date);

			if(elementAtDate != null)
				elementViewsAtStep.add(elementAtDate);
		}

		return elementViewsAtStep;
	}

	/**
	 * Give the diff of an element at a given date.
	 * 
	 * @param id
	 *            The element ID.
	 * @param step
	 *            The time step.
	 * @return The diff of the element at this step or null if it does not exist
	 *         at this time.
	 */
	public ElementDiff getElementDiffAtStep(String id, int step) {

		// Get the real date from the time step index.

		Integer date = stepToDate(step);

		Element element = this.elements.get(id);
		
		if(element == null)
			return null;
		
		ElementDiff diffAtDate = element.getDiffAtDate(date);
		
		return diffAtDate;
	}

	/**
	 * Give the diff of all elements at a given date.
	 * 
	 * @param step
	 *            The time step.
	 * @return The diffs of the elements at this step.
	 */
	public ArrayList<ElementDiff> getElementDiffsAtStep(int step) {

		// Get the real date from the time step index.

		Integer date = stepToDate(step);

		//

		ArrayList<ElementDiff> elementDiffsAtStep = new ArrayList<ElementDiff>();

		for(Element element : this.elements.values()) {

			// Retrieve the element diff at this step.

			ElementDiff elementAtDate = element.getElementDiffAtDate(date);

			if(elementAtDate != null)
				elementDiffsAtStep.add(elementAtDate);
		}

		return elementDiffsAtStep;
	}

	/**
	 * Give the list of elements deleted at a given time step.
	 * 
	 * @param step
	 *            The time step.
	 * @return The elements that disappear at this time step.
	 */
	protected ArrayList<Element> getDeletedElements(int step) {

		// Get the real date from the step index.

		Integer date = stepToDate(step);

		// Build a list of elements deleted at this date.

		ArrayList<Element> deletedElements = new ArrayList<Element>();

		for(Element element : this.elements.values()) {

			ElementDiff diffAtDate = element.getDiffAtDate(date);

			if(diffAtDate != null && diffAtDate.isDeleted())
				deletedElements.add(diffAtDate.getElement());
		}

		return deletedElements;
	}

	/**
	 * Time will not be considered.
	 */
	public void noTime() {

		this.temporalLocator = new TemporalLocator(this);
	}

	/**
	 * Each file will correspond to a different time step.
	 */
	public void timeDependsOnFile() {

		this.temporalLocator = new TemporalLocatorByFile(this);
	}

	/**
	 * The date of an element will depend on a special attribute.
	 * 
	 * @param attributeName
	 *            The name of the date attribute.
	 */
	public void timeDependsOnAttribute(String attributeName) {

		this.temporalLocator = new TemporalLocatorByAttribute(this, attributeName);
	}

	/**
	 * Add a file descriptor containing element descriptors to this source.
	 * 
	 * @param fileDescriptor
	 *            The descriptor.
	 */
	public void addFileDescriptor(FileDescriptor fileDescriptor) {

		this.fileDescriptors.add(fileDescriptor);
	}

	/**
	 * Give the file descriptors associated with this source.
	 * 
	 * @return The file descriptors.
	 */
	public ArrayList<FileDescriptor> getFileDescriptors() {

		return this.fileDescriptors;
	}

	/**
	 * Give the paths to the input files.
	 * 
	 * @return The paths.
	 */
	public ArrayList<String> getFileNames() {

		return this.fileNames;
	}

	/**
	 * Give the aggregator used by this source.
	 * 
	 * @return The aggregator.
	 */
	public Aggregator getAggregator() {

		return this.aggregator;
	}

	/**
	 * Give the temporal locator used by this source.
	 * 
	 * @return The temporal locator.
	 */
	public TemporalLocator getTemporalLocator() {

		return this.temporalLocator;
	}

}
