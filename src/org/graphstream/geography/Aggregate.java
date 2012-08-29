package org.graphstream.geography;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Aggregate implements Iterable<Entry<String, HashMap<Integer, Object>>> {

	/**
	 * 
	 */
	protected HashMap<String, HashMap<Integer, Object>> content;
	
	/**
	 * 
	 */
	public Aggregate() {
		
		this.content = new HashMap<String, HashMap<Integer,Object>>();
	}
	
	/**
	 * 
	 * @param id
	 * @param date
	 * @param o
	 */
	public void add(String id, Integer date, Object o) {
	
		HashMap<Integer, Object> objectsAtDate = this.content.get(id);
		
		if(objectsAtDate == null) {
			objectsAtDate = new HashMap<Integer, Object>();
			this.content.put(id, objectsAtDate);
		}
		
		objectsAtDate.put(date,  o);
	}
	
	/**
	 * 
	 * @param id
	 * @param date
	 * @return
	 */
	public Object get(String id, Integer date) {
	
		HashMap<Integer, Object> objectAtDate = content.get(id);
		
		if(objectAtDate == null)
			return null;
		
		return objectAtDate.get(date);
	}

	@Override
	public Iterator<Entry<String, HashMap<Integer, Object>>> iterator() {
		
		return this.content.entrySet().iterator();
	}
	
}
