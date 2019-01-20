package com.github.mcfongtw.behavioral.visitor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Stack;

//FIXME: Recursive BFS algorithm is wrong!
public class BreadthFiirstVerifier extends BreadthFirstVisitor {
    /**
     *
     */
    private static final String TAB = "\t";

    /**
     *
     */
    private int level = -1;

    /**
     *
     */
    private int visitedOrder = 1;

    /**
     *
     */
    private Stack<Integer> visitedOrderStack = new Stack<Integer>();

    /**
     *
     * OUT = IN + (# of SIBLING) + 1
     */
    @Override
    public Object visit(ASTNode root, VisitAction action) {
//        super.visit(root, action);

        if(action == VisitAction.IN) {
            this.visitedOrderStack.push(this.visitedOrder);
            this.visitedOrder++;

            this.level++;
            for(int i = 0; i < this.level; i++) {
                System.out.print(this.TAB);
            }
            System.out.println("'" + root.getImage() + "'" + " >>>: " + (visitedOrder -1));
        } else if(action == VisitAction.OUT) {
            //OUT_degree = IN_degree + (# of descendent) + 1
            int inOrder = this.visitedOrderStack.pop();
            int outOrder = inOrder + 1;
            Assertions.assertEquals(outOrder, this.visitedOrder++);

            for(int i = 0; i < this.level; i++) {
                System.out.print(this.TAB);
            }

            System.out.println("'" + root.getImage() + "'" + " <<<: " +  (visitedOrder -1));
            this.level--;
        }
        return root;
    }

    /*
     *         1(2)
     *        /   \
     *       3(4) 5(6)
     */
    @Test
    public void testSimpleMath() {
        ASTNode num1 = new ASTNode("1", NodeType.NUMBER);
        ASTNode num2 = new ASTNode("2", NodeType.NUMBER);
        ASTNode op = new ASTNode("+", NodeType.ADD);

        op.insert(num1);
        op.insert(num2);


        Visitor visitor = new BreadthFiirstVerifier();

        op.accept(visitor);

    }

    /*
     *         [*]1(2)
     *         /    \
     *    [+]3(4)  [3]5(6)
     *     /   \
     * [1]7(8) [2]9(10)
     */
    @Test
    public void testComplexMath1() {
        ASTNode num1 = new ASTNode("1", NodeType.NUMBER);
        ASTNode num2 = new ASTNode("2", NodeType.NUMBER);
        ASTNode num3 = new ASTNode("3", NodeType.NUMBER);
        ASTNode op1 = new ASTNode("+", NodeType.ADD);
        ASTNode op2 = new ASTNode("*", NodeType.MUL);

        op1.insert(num1);
        op1.insert(num2);
        op2.insert(op1);
        op2.insert(num3);


        Visitor visitor = new BreadthFiirstVerifier();

        op2.accept(visitor);

    }

    /*
     *              [*]1(2)
     *          /            \
     *      [+]3(4)         [+]5(6)
     *      /    \         /         \
     *  [1]7(8) [2]9(10) [3]11(12)  [4]13(14)
     */
    @Test
    public void testComplexMath2() {
        ASTNode num1 = new ASTNode("1", NodeType.NUMBER);
        ASTNode num2 = new ASTNode("2", NodeType.NUMBER);
        ASTNode num3 = new ASTNode("3", NodeType.NUMBER);
        ASTNode num4 = new ASTNode("4", NodeType.NUMBER);
        ASTNode op1 = new ASTNode("+", NodeType.ADD);
        ASTNode op2 = new ASTNode("*", NodeType.MUL);
        ASTNode op3 = new ASTNode("+", NodeType.MUL);

        op1.insert(num1);
        op1.insert(num2);
        op2.insert(op1);
        op2.insert(op3);
        op3.insert(num3);
        op3.insert(num4);


        Visitor visitor = new BreadthFiirstVerifier();

        op2.accept(visitor);

    }
}
