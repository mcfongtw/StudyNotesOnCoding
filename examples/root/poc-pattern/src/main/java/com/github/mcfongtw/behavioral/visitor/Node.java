package com.github.mcfongtw.behavioral.visitor;

public abstract class Node {

    protected int id;

    protected String image;

    private static int ID_GENERATOR = 1;

    public Node(String name) {
        id = ID_GENERATOR++;
        this.image = name;
    }

    /**
     * Retrieve the image of the {@code ASTNode}
     *
     * @return the image of the node
     */
    public String getImage() {
        return this.image;
    }

    public int getId() { return this.id; }
}
