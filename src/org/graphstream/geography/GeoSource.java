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
import java.util.Map.Entry;
import java.util.TreeSet;

import org.graphstream.stream.SourceBase;

/**
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
	 * 
	 */
	protected DiffBuilder diffBuilder;

	/**
	 * 
	 */
	protected TemporalLocator temporalLocator;

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

		this.temporalLocator = new TemporalLocator(this);

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

		Aggregate aggregatedIds = this.aggregator.read(true);

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

		Aggregate aggregate = this.aggregator.read(false);

		for(Element element : this.elements.values()) {

			ElementDiff previousDiff = null;
			Integer previousDate = null;

			for(Integer date : element.getStates().keySet()) {

				Object currentObject = aggregate.get(element.getId(), date);

				ElementDiff currentDiff = this.diffBuilder.diff(element, previousDiff, previousDate, currentObject);

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

	public ArrayList<ElementView> getElementViewsAtStep(int step) {

		ArrayList<ElementView> elementViewsAtStep = new ArrayList<ElementView>();

		for(Element element : this.elements.values()) {

			// Retrieve the element view at this step.

			Integer date = step; // TODO step -> date
			ElementView elementAtDate = element.getElementViewAtDate(date);

			if(elementAtDate != null)
				elementViewsAtStep.add(elementAtDate);
		}

		return elementViewsAtStep;
	}

	public ArrayList<ElementDiff> getElementDiffsAtStep(int step) {

		ArrayList<ElementDiff> elementDiffsAtStep = new ArrayList<ElementDiff>();

		for(Element element : this.elements.values()) {

			// Retrieve the element diff at this step.

			Integer date = step; // TODO step -> date
			ElementDiff elementAtDate = element.getElementDiffAtDate(date);

			if(elementAtDate != null)
				elementDiffsAtStep.add(elementAtDate);
		}

		return elementDiffsAtStep;
	}

	public void noTime() {

		this.temporalLocator = new TemporalLocator(this);
	}

	public void timeDependsOnFile() {
		
		this.temporalLocator = new TemporalLocatorByFile(this);	
	}
	
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
