package org.graphstream.geography;

import java.io.IOException;
import java.util.ArrayList;

import org.graphstream.stream.SourceBase;

public abstract class FileSourceAbstract extends SourceBase {

	/**
	 * The ID of this source. 
	 */
	protected String sourceId;
	
	/**
	 * Descriptors for the input features.
	 */
	protected ArrayList<Descriptor> descriptors;
	
	//
	protected ArrayList<Element> elements;
	
	public FileSourceAbstract() {
		
		this.sourceId = String.format("<DGS stream %x>", System.nanoTime());
		
		this.descriptors = new ArrayList<Descriptor>();
	}
	
	public void addDescriptor(Descriptor descriptor) {

		this.descriptors.add(descriptor);
	}
	
	// Abstract
	
	public abstract void begin(String fileName) throws IOException;
	
	public abstract void all() throws IOException;
	
	protected abstract boolean next() throws IOException;
	
	protected abstract void keep(Object o, Descriptor descriptor);
	
	protected abstract void transform();
}
