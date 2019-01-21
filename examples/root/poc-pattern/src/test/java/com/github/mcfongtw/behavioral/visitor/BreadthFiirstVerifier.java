package com.github.mcfongtw.behavioral.visitor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
     * OUT = IN + (# of SIBLING) + 1
     */
    @Override
    public Object visit(ASTNode node, VisitAction action) {
        if(action == VisitAction.IN) {

            this.level++;
            for(int i = 0; i < this.level; i++) {
                System.out.print(this.TAB);
            }
            System.out.println("'" + node.getImage() + "'" + " >>>: " + node.getId());
        } else if(action == VisitAction.OUT) {

            for(int i = 0; i < this.level; i++) {
                System.out.print(this.TAB);
            }

            System.out.println("'" + node.getImage() + "'" + " <<<: " +  node.getId());
            this.level--;
        }
        return node;
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


        Visitor visitor = new DecoratorVisitor(new BreadthFiirstVerifier());

        op.accept(visitor);

        Assertions.assertArrayEquals(new Integer[]{3, 1, 2}, ((DecoratorVisitor) visitor).visitedIdOrder.toArray());


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


        Visitor visitor = new DecoratorVisitor(new BreadthFiirstVerifier());

        op2.accept(visitor);

        Assertions.assertArrayEquals(new Integer[]{8, 7, 6, 4, 5}, ((DecoratorVisitor) visitor).visitedIdOrder.toArray());



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


        Visitor visitor = new DecoratorVisitor(new BreadthFiirstVerifier());

        op2.accept(visitor);

        Assertions.assertArrayEquals(new Integer[]{14, 13, 15, 9, 10, 11, 12}, ((DecoratorVisitor) visitor).visitedIdOrder.toArray());



    }
}
