package com.github.mcfongtw.behavioral.visitor;

import java.io.PrintStream;

/*
 * Visitor pattern is used when we have to perform an operation on a group of similar kind of
 * Objects. With the help of visitor pattern, we can move the operational logic from the objects to
 * another class.
 */
public class Application {

    public static void main(String[] args) {
        ASTNode num1 = ASTNode.builder().id(1).image("1").type(NodeType.NUMBER).build();
        ASTNode num2 = ASTNode.builder().id(2).image("2").type(NodeType.NUMBER).build();
        ASTNode op = ASTNode.builder().id(3).image("+").type(NodeType.ADD).build();

        op.insert(num1);
        op.insert(num2);


        Visitor visitor = new PrettyTreePrinter(new PrintStream(System.out));

        op.accept(visitor);

    }

}
