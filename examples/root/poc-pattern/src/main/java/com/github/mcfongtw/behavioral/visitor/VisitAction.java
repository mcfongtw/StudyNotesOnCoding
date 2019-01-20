package com.github.mcfongtw.behavioral.visitor;

/**
 * Definition of node-visiting action:
 * 1) IN: it was visited for the first time when traversal begins
 * 2) OUT: it was re-visited when traversal ends
 * 3) SECOND: it was visited again by intention
 * 
 * @author Michael Fong
 *
 */
public enum VisitAction {
	/**
	 * visited when traversal begins
	 */
	IN,
	
	/**
	 * e-visited when traversal ends
	 */
	OUT,
	
	/**
	 * visited again by intention
	 */
	SECOND
}
