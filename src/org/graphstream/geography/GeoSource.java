package org.graphstream.geography;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.graphstream.stream.SourceBase;

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
	 * 
	 */
	protected DiffBuilder diffBuilder;

	/**
	 * Aggregated elements indexed according to their ID.
	 */
	protected HashMap<String, Element> elements;

	/**
	 * 
	 */
	protected TreeSet<Integer> dates;
	
	/**
	 * 
	 */
	protected int currentTimeStep;
	
	/**
	 * 
	 * @param fileNames
	 */
	public GeoSource(String... fileNames) {

		this.id = String.format("<GeoSource %x>", System.nanoTime());

		this.fileNames = new ArrayList<String>();
		for(int i = 0; i < fileNames.length; ++i)
			this.fileNames.add(fileNames[i]);

		this.fileDescriptors = new ArrayList<FileDescriptor>();

		this.elements = new HashMap<String, Element>();
		
		this.dates = new TreeSet<Integer>();
		
		this.currentTimeStep = 0;
	}

	/**
	 * 
	 */
	public void read() {

		/**
		 * First pass: go through all files and instantiate the elements and
		 * their states.
		 */

		Aggregate aggregatedIds = this.aggregator.read(this, true);

		for(Entry<String, HashMap<Integer, Object>> entry : aggregatedIds) {

			String id = entry.getKey();

			Element element = this.elements.get(id);

			if(element == null) {
				element = new Element(id);
				this.elements.put(id, element);
			}

			for(Integer date : entry.getValue().keySet()) {
				
				if(!element.hasStateAtDate(date))
					element.addStateAtDate(null, date);

				if(!dates.contains(date))
					dates.add(date);
			}
		}

		/**
		 * Second pass: go through all files and fill the element states with
		 * attribute and shape data.
		 */

		Aggregate aggregate = this.aggregator.read(this, false);

		for(Element element : this.elements.values()) {

			ElementState previousDiff = null;
			Integer previousDate = null;

			for(Integer date : element.getStates().keySet()) {

				Object currentObject = aggregate.get(element.getId(), date);

				ElementState currentDiff = this.diffBuilder.diff(element, previousDiff, previousDate, currentObject);

				element.addStateAtDate(currentDiff, date);

				previousDiff = currentDiff;
				previousDate = date;
			}
		}
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
	
	public ArrayList<ElementView> getElementsAtStep(int step) {

		// TODO step != date

		ArrayList<ElementView> elementsAtStep = new ArrayList<ElementView>();

		for(Element element : this.elements.values()) {

			// Retrieve the element view at this step.

			ElementView elementAtStep = element.getElementViewAtDate(step);

			if(elementAtStep != null)
				elementsAtStep.add(elementAtStep);
		}

		return elementsAtStep;
	}

	public void addFileDescriptor(FileDescriptor fileDescriptor) {

		this.fileDescriptors.add(fileDescriptor);
	}

	public Aggregator getFeatureAggregator() {

		return this.aggregator;
	}

	public ArrayList<FileDescriptor> getFileDescriptors() {

		return this.fileDescriptors;
	}

}
