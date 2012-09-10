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

/**
 * A FileDescriptor describes what geographic data will be extracted from a
 * single file.
 * 
 * @author Merwan Achibet
 */
public class FileDescriptor {

	/**
	 * The path to the input file.
	 */
	protected String fileName;

	/**
	 * A list of descriptors to categorize the objects from the file.
	 */
	protected ArrayList<ElementDescriptor> descriptors;

	/**
	 * Instantiate a new FileDescriptor.
	 * 
	 * @param fileName
	 *            The path to the file.
	 */
	public FileDescriptor(String fileName) {

		this.fileName = fileName;
		
		this.descriptors = new ArrayList<ElementDescriptor>();
	}

	/**
	 * Give the path to the file.
	 * 
	 * @return The path.
	 */
	public String getFileName() {

		return new String(this.fileName);
	}

	/**
	 * Add a descriptor.
	 * 
	 * @param descriptor
	 *            The descriptor to add.
	 */
	public void addDescriptor(ElementDescriptor descriptor) {

		this.descriptors.add(descriptor);
	}

	/**
	 * Give the descriptors associated with this file.
	 * 
	 * @return A list of descriptors.
	 */
	public ArrayList<ElementDescriptor> getDescriptors() {

		return this.descriptors;
	}

	@Override
	public String toString() {
	
		String s = new String();
		
		s += "FileDescriptor\n";
		
		for(ElementDescriptor descriptor : this.descriptors)
			s += "\t" + descriptor.toString();
		
		return s;
	}
	
}
