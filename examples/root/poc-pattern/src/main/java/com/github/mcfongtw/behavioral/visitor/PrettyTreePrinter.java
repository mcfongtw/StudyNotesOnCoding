package com.github.mcfongtw.behavioral.visitor;

import java.io.PrintStream;

/**
 * {@code PrettyTreePrinter} is an abstract class implemented to visit an tree
 * 
 * @author Michael Fong
 * 
 */
public class PrettyTreePrinter extends DepthFirstVisitor {

    /**
     * sequence degree
     */
    private static int degree = 0;

    /**
     * output stream
     */
    private final PrintStream dottyStream;

    /**
     * Constructor
     *
     * @param dottyStream output stream
     */
    public PrettyTreePrinter(PrintStream dottyStream) {
        this.dottyStream = dottyStream;
    }


    /**
     * Override this method, if you do not want to visit individual AST node, but to handle them
     * indifferently.
     */

    public Object visit(ASTNode node, VisitAction action) {
        Object visitResult;

        switch (node.getType()) {

            /*
             * subtree: computational operators
             */
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case NEG:
                visitResult = this.visit_MATH(node, action);
                break;

            case NUMBER:
                visitResult = this.visit_NUMBER(node, action);
                break;

            default:
                visitResult = this.visit_DEFAULT(node, action);
        }

        return visitResult;
    }

    /**
     * Visit the {@code ASTNode} MATH
     *
     * @param node   the AST node to be visited
     * @param action the action to take
     * @return visited result
     */
    protected Object visit_MATH(ASTNode node, VisitAction action) {
        if (action == VisitAction.IN) {
            StringBuffer sb = new StringBuffer();
            sb.append("\t" + this.getNodeName(node) + " [");
            sb.append("label = \"" + node.getImage());
//			sb.append("\\n " + "IN:" + (degree++));
            //You may add other lines of label
            sb.append("\", ");
            sb.append("style = filled, fillcolor = lawngreen, ");
            sb.append("shape=Mcircle");
            sb.append("]");
            this.dottyStream.println(sb.toString());
        } else if (action == VisitAction.OUT) {
            //connecting to child nodes
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < node.getChildCount(); i++) {
                ASTNode child = (ASTNode) node.getChildNode(i);
                sb.append("\t" + this.getNodeName(node));
                sb.append(" -> " + this.getNodeName(child) + "\r\n");
            }

            this.dottyStream.println(sb.toString());
        }
        return node;
    }


    /**
     * Visit the {@code ASTNode} NUMBER
     *
     * @param node   the AST node to be visited
     * @param action the action to take
     * @return visited result
     */
    protected Object visit_NUMBER(ASTNode node, VisitAction action) {

        if (action == VisitAction.IN) {
            StringBuffer sb = new StringBuffer();
            sb.append("\t" + this.getNodeName(node) + " [");
            sb.append("label = \"" + node.getImage());
            if(node.getChildCount() > 0) {
                ASTNode child = (ASTNode) node.getChildNode(0);
                if (child != null && child.getImage().equals("ONE")) {
                    sb.append("\\n" + "value: 1");
                } else {
                    sb.append("\\n" + "value: " + child.getImage());
                }
            }
//			sb.append("\\n " + "IN:" + (degree++));
            //You may add other lines of label
            sb.append("\", ");
            sb.append("color = brown, ");
            sb.append("shape=egg");
            sb.append("]");
            this.dottyStream.println(sb.toString());
        } else if (action == VisitAction.OUT) {
            //leaf node
        }
        return node;
    }

    /**
     * Visit all other {@code ASTNode}s
     *
     * @param node   the AST node to be visited
     * @param action the action to take
     * @return visited result
     */
    protected Object visit_DEFAULT(ASTNode node, VisitAction action) {

        //Only process ROOT node
//		if( ((ASTNode)node).getStartToken().getType() == CustomTokenType.ROOT.ordinal()) {
        if (node.getImage().equals("ROOT")) {
            if (action == VisitAction.IN) {
                StringBuffer sb = new StringBuffer();
                sb.append("\t" + this.getNodeName(node) + " [");
                sb.append("label = \"" + node.getImage());
//				sb.append("\\n " + "IN:" + (degree++));
                //You may add other lines of label
                sb.append("\", ");
                sb.append("style = filled, fillcolor = red, ");
                sb.append("shape=triangle");
                sb.append("]");
                this.dottyStream.println(sb.toString());
            } else if (action == VisitAction.OUT) {
                //connecting to child nodes
                StringBuffer sb = new StringBuffer();

                for (int i = 0; i < node.getChildCount(); i++) {
                    ASTNode child = (ASTNode) node.getChildNode(i);
                    sb.append("\t" + this.getNodeName(node));
                    sb.append(" -> " + this.getNodeName(child) + "\r\n");
                }

                this.dottyStream.println(sb.toString());
            }
        }
        return node;
    }

    private String getNodeName(ASTNode node) {
        StringBuffer sb = new StringBuffer();
        sb.append("node_");
        sb.append(node.getImage());
        return sb.toString();
    }

}
