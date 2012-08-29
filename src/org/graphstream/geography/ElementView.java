package org.graphstream.geography;

import java.util.HashMap;

public class ElementView {

	public String id;
	
	public HashMap<String, Object> attributes;
	
	public ElementShape shape;
	
	public ElementView(String id) {
		
		this.id = id;
		
		this.attributes = new HashMap<String, Object>();
	}
	
	public void setAttribute(String key, Object value) {
		
		this.attributes.put(key, value);
	}
	
	public void removeAttribute(String key) {
		
		this.attributes.remove(key);
	}

	public HashMap<String, Object> getAttributes() {
		
		return this.attributes;
	}
	
	public ElementShape getShape() {
		
		return this.shape;
	}

}
