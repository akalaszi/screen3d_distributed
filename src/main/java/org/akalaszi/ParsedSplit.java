package org.akalaszi;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.mapreduce.InputSplit;

public class ParsedSplit extends InputSplit {

	private final List<Element> elements;
	private final String[] locations;

	@Override
	public long getLength() throws IOException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	public ParsedSplit(List<Element> elements, String[] locations) {
		this.elements = elements;
		this.locations = locations;
	}

	@Override
	public String[] getLocations() throws IOException, InterruptedException {
		return locations;
	}

	public List<Element> getElements() {
		return elements;
	}

	public static class Element {
		private final String id;
		private final String value;

		public Element(String id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getId() {
			return id;
		}

		public String getValue() {
			return value;
		}
	}

}
