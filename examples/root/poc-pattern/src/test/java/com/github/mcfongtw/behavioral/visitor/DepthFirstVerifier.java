package com.github.mcfongtw.behavioral.visitor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DepthFirstVerifier extends DepthFirstVisitor {

    /**
     *
     */
    private static final String TAB = "\t";

    /**
     *
     */
    private int level = -1;



    /**
     * OUT = IN + (# of DESC) * 2 + 1
     */
    @Override
    public Object visit(ASTNode node, VisitAction action) {
        if(action == VisitAction.IN) {

            this.level++;
            for(int i = 0; i < this.level; i++) {
                System.out.print(this.TAB);
            }
            System.out.println("'" + node.getImage() + "'" + " >>>: " + (node.getId()));
        } else if(action == VisitAction.OUT) {
            for(int i = 0; i < this.level; i++) {
                System.out.print(this.TAB);
            }
            this.level--;
            System.out.println("'" + node.getImage() + "'" + " <<<: " +  (node.getId()));
        }
        return node;
    }

    /*
     *          1(6)
     *        /   \
     *       2(3) 4(5)
     *
     */
    @Test
    public void testSimpleMath() {
        ASTNode num1 = new ASTNode("1", NodeType.NUMBER);
        ASTNode num2 = new ASTNode("2", NodeType.NUMBER);
        ASTNode op = new ASTNode("+", NodeType.ADD);

        op.insert(num1);
        op.insert(num2);


        Visitor visitor = new DecoratorVisitor(new DepthFirstVerifier());

        op.accept(visitor);

        Assertions.assertArrayEquals(new Integer[]{3, 1, 1, 2, 2, 3}, ((DecoratorVisitor) visitor).visitedIdOrder.toArray());

    }

    /*
     *         1(10)
     *        /   \
     *       2(7) 8(9)
     *     /  \
     *   3(4) 5(6)
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


        Visitor visitor = new DecoratorVisitor(new DepthFirstVerifier());

        op2.accept(visitor);

        Assertions.assertArrayEquals(new Integer[]{8, 7, 4, 4, 5, 5, 7, 6, 6, 8}, ((DecoratorVisitor) visitor).visitedIdOrder.toArray());


    }

    /*
     *              [*]1(14)
     *          /            \
     *      [+]2(7)         [+]8(13)
     *      /    \         /         \
     *  [1]3(4) [2]5(6) [3]9(10)  [4]11(12)
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


        Visitor visitor = new DecoratorVisitor(new DepthFirstVerifier());

        op2.accept(visitor);

        Assertions.assertArrayEquals(new Integer[]{14, 13, 9, 9, 10, 10, 13, 15, 11, 11, 12, 12, 15, 14}, ((DecoratorVisitor) visitor).visitedIdOrder.toArray());


    }

}
