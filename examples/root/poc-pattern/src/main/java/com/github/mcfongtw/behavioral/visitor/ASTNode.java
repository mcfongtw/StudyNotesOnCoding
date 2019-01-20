package com.github.mcfongtw.behavioral.visitor;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Ints;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class ASTNode extends Node {
 
    private List<ASTNode> children;
    
    private ASTNode parent;
    
    private NodeType type;

    public ASTNode(String name, NodeType type) {
        super(name);
        this.children = new ArrayList<ASTNode>();
        this.parent = null;
        this.type = type;
    }
    
    /**
     ***************
     * Getter/Setter
     ***************
     */
    public ASTNode getParent() {
        return this.parent;
    }
    
    public void setParent(ASTNode node) {
        this.parent = node;
    }
    
    public NodeType getType() {
        return this.type;
    }
     
    /*
     * *************************
     * AST Manipulation
     * *************************
     */

    public void insert( ASTNode node) {
        this.children.add(node);
        
        node.setParent(this);
    }
    
    public void insert(List<ASTNode> nodes) {
        this.children.addAll(nodes);
        
        for(ASTNode node: nodes) {
            node.setParent(this);
        }
    }

    public ASTNode getChildNode(int index) {
    	return this.children.get(index);
    }
    
    public int getChildCount() {
        return this.children.size();
    }
    

    public int getLastChildIndex() {
    	return this.children.size() - 1;
    }
    
    public boolean isRoot() {
        return this.parent == null;
    }
    

    public List<ASTNode> getDescendants() {
    	List<ASTNode> descedents = Lists.newArrayList();
    	
    	for(int i = 0; i < this.getChildCount(); i++) {
    		//Add the grand-children of ith child
    		descedents.add(this.getChildNode(i));
    		descedents.addAll(this.getChildNode(i).getDescendants());
    	}
    	
    	return descedents;
    }
    

    public int getNumOfDescendants() {
    	int count = 0;
    	
    	for(int i = 0; i < this.getChildCount(); i++) {
    		//count the ith child
    		count++;
    		count += this.getChildNode(i).getNumOfDescendants();
    	}
    	
    	return count;
    }

    public int getNumOfSiblings() {
        if(parent == null) {
            return 0;
        } else {
            return parent.children.size() -1;
        }
    }
    

	public Object accept(Visitor visitor) {

		//move to children / siblings
		if(visitor.getStrategy() == TraverseStrategy.DEPTH_FIRST) {
            //IN
            visitor.visit(this, VisitAction.IN);

			this.acceptChildren(visitor);

            //OUT
            visitor.visit(this, VisitAction.OUT);
		} else if (visitor.getStrategy() == TraverseStrategy.BREADTH_FIRST) {

		    int height = getMaximumHeight(this);

            breathFirstSearchRecursively(visitor, this, height);

        }

        return null;
	}
	
	/**
	 * accept all children node of this {@code ASTNode} 
	 * 
	 * @param visitor the way we visit the child node
	 */
	private void acceptChildren(Visitor visitor) {
        for (ASTNode child : this.children) {
            child.accept(visitor);
        }
	}


    private void breathFirstSearchRecursively(Visitor visitor, ASTNode node, int level) {
        //IN
        visitor.visit(node, VisitAction.IN);

        //OUT
        visitor.visit(node, VisitAction.OUT);

        if(level > 1) {
            for(int i = 0; i < node.getChildCount(); i++) {
                ASTNode child = node.getChildNode(i);
                breathFirstSearchRecursively(visitor, child, level -1);
            }
        } else /* level == 1 */  {

        }
    }

    public int getMaximumHeight(ASTNode node) {
	    if(node.getChildCount() == 0) {
	        //leaf node
	        return 1;
        } else {
            /* compute  height of each subtree */
            int[] childrenHeights = new int[node.getChildCount()];

            for(int i = 0; i < node.getChildCount(); i++) {
                childrenHeights[i] = this.getMaximumHeight(node.getChildNode(i));
            }

            return Collections.max(Ints.asList(childrenHeights)).intValue() + 1;
	    }
    }

    @Override
    public String toString() {
    	StringBuilder builder = new StringBuilder();

        builder.append("id : " + this.id);
    	builder.append("image : " + this.image);
    	builder.append("\t\t");
    	builder.append("type : " + this.type.name());
    	
    	return builder.toString();
    }
}

enum NodeType {        
    ADD,
    SUB,
    MUL,
    DIV,
    NEG,
    NUMBER
}
