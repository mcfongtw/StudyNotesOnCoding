package com.github.mcfongtw.behavioral.visitor;

import com.google.common.collect.Lists;

import java.util.List;

public class DecoratorVisitor implements Visitor {

    private Visitor visitor;

    public List<Integer> visitedIdOrder = Lists.newArrayList();

    public DecoratorVisitor(Visitor originalVisitor) {
        this.visitor = originalVisitor;
    }

    @Override
    public Object visit(ASTNode node, VisitAction action) {
        this.visitor.visit(node, action);

        visitedIdOrder.add(node.getId());

        return node;
    }

    @Override
    public TraverseStrategy getStrategy() {
        return visitor.getStrategy();
    }
}
