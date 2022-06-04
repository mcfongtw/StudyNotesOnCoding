package com.github.mcfongtw.behavioral.visitor;



/**
 * Any tree-traversing class should implement this {@code Visitor} interface.  
 * 
 * @author Michael Fong
 *
 */
public interface Visitor {
	
	/**
	 * Define what to do with the {@code ASTNode} when a {@code VisitAction} comes.
	 * 
	 * @param node an AST node to be visited.
	 * @param action an action to take to visit the node
	 * 
	 * @return visited result
	 */
	Object visit(ASTNode node, VisitAction action);

	TraverseStrategy getStrategy();
}

class DepthFirstVisitor implements Visitor {

	@Override
	public Object visit(ASTNode node, VisitAction action) {
		return null;
	}

	@Override
    public TraverseStrategy getStrategy() {
        return TraverseStrategy.DEPTH_FIRST;
    }
}

class BreadthFirstVisitor implements Visitor {

	@Override
	public Object visit(ASTNode node, VisitAction action) {
		return null;
	}

	@Override
    public TraverseStrategy getStrategy() {
        return TraverseStrategy.BREADTH_FIRST;
    }
}