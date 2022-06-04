package com.github.mcfongtw.behavioral.visitor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BreadthFiirstTraversalTest extends BreadthFirstVisitor {
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
            System.out.println("'" + node.getImage() + "'" + " >>>: " + node.getId());
        } else if(action == VisitAction.OUT) {

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
        ASTNode num1 = ASTNode.builder().id(1).image("1").type(NodeType.NUMBER).build();
        ASTNode num2 = ASTNode.builder().id(2).image("2").type(NodeType.NUMBER).build();
        ASTNode op = ASTNode.builder().id(3).image("+").type(NodeType.ADD).build();

        op.insert(num1);
        op.insert(num2);


        Visitor visitor = new DecoratorVisitor(new BreadthFiirstTraversalTest());

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
        ASTNode num1 = ASTNode.builder().id(1).image("1").type(NodeType.NUMBER).build();
        ASTNode num2 = ASTNode.builder().id(2).image("2").type(NodeType.NUMBER).build();
        ASTNode op1 = ASTNode.builder().id(3).image("+").type(NodeType.ADD).build();
        ASTNode num3 = ASTNode.builder().id(4).image("3").type(NodeType.NUMBER).build();
        ASTNode op2 = ASTNode.builder().id(5).image("*").type(NodeType.MUL).build();

        op1.insert(num1);
        op1.insert(num2);
        op2.insert(op1);
        op2.insert(num3);


        Visitor visitor = new DecoratorVisitor(new BreadthFiirstTraversalTest());

        op2.accept(visitor);

        Assertions.assertArrayEquals(new Integer[]{5, 3, 4, 1 , 2}, ((DecoratorVisitor) visitor).visitedIdOrder.toArray());



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
        ASTNode num1 = ASTNode.builder().id(1).image("1").type(NodeType.NUMBER).build();
        ASTNode num2 = ASTNode.builder().id(2).image("2").type(NodeType.NUMBER).build();
        ASTNode op1 = ASTNode.builder().id(3).image("+").type(NodeType.ADD).build();
        ASTNode num3 = ASTNode.builder().id(4).image("3").type(NodeType.NUMBER).build();
        ASTNode op2 = ASTNode.builder().id(5).image("*").type(NodeType.MUL).build();
        ASTNode num4 = ASTNode.builder().id(6).image("4").type(NodeType.NUMBER).build();
        ASTNode op3 = ASTNode.builder().id(7).image("+").type(NodeType.ADD).build();

        op1.insert(num1);
        op1.insert(num2);
        op2.insert(op1);
        op2.insert(op3);
        op3.insert(num3);
        op3.insert(num4);


        Visitor visitor = new DecoratorVisitor(new BreadthFiirstTraversalTest());

        op2.accept(visitor);

        Assertions.assertArrayEquals(new Integer[]{5, 3, 7, 1, 2, 4, 6}, ((DecoratorVisitor) visitor).visitedIdOrder.toArray());



    }
}
