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

/**
 * This temporal locator implementation determines the date of appearance of a
 * feature according to a special attribute that each dated feature must
 * possess.
 * 
 * @author Merwan Achibet
 */
public class TemporalLocatorByAttribute extends TemporalLocator {

	/**
	 * The key of the attribute specifying the date.
	 */
	protected String attributeKey;

	/**
	 * Instantiate a new temporal locator using a specific attribute to
	 * determine the date of appearance.
	 * 
	 * @param source
	 *            The source using the temporal locator.
	 * @param attributeKey
	 *            The key of the date attribute.
	 */
	public TemporalLocatorByAttribute(GeoSource source, String attributeKey) {
		super(source);

		this.attributeKey = attributeKey;
	}

	/**
	 * Give the date of appearance of a given geographic object.
	 * 
	 * Here, we use a predefined attribute to determine the date.
	 * 
	 * XXX This is kind of experimental, so this may be written differently
	 * when a geographic format really using this presentation appears.
	 * 
	 * @param o
	 *            The geographic object.
	 * @return The date of appearance of the object.
	 */
	@Override
	public Integer date(Object o) {

		// Retrieve the aggregator that read the input file(s).

		Aggregator aggregator = this.source.getAggregator();

		// Get the date attribute from the feature.

		String dateString = aggregator.getAttributeValue(o, this.attributeKey).toString();

		// Check that the date attribute is specified.

		if(dateString == null)
			return null;

		// Return the date as an Integer.

		return Integer.parseInt(dateString);
	}

}
