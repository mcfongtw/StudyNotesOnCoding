package com.github.mcfongtw.behavioral.visitor;

/**
 * Tree traversal pattern
 * 
 * @author Michael Fong
 *
 */
public enum TraverseStrategy {
	
	/**
	 * depth-first traversal
	 */
	DEPTH_FIRST,

	/**
	 * iterative depth-first traversal
	 */
	ITERATIVE_DEPTH_FIRST,
	
	/**
	 * breadth-first traversal
	 */
	BREADTH_FIRST;
}
