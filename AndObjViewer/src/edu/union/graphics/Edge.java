package edu.union.graphics;

import java.io.Serializable;

/**
 * A class representing an edge between two vertices.
 * @author bburns
 */
public class Edge implements Serializable {
	int start, end;
	int hs;

	/**
	 * Constructor
	 * @param s Starting vertex index.
	 * @param e Ending vertex index.
	 */
	public Edge(int s, int e) {
		this.start = (s<e)?s:e;
		this.end = (s<e)?e:s;
		this.hs = (start+"."+end).hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return hs;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object o) {
		if (o instanceof Edge) {
			Edge e = (Edge)o;
			return (e.start == start && e.end == end) ||
			(e.start == end && e.end == start);
		}
		return false;
	}
}