package datacompressor.huffman.model;

import datacompressor.model.IBinaryTree;

public class HuffmanNode implements IBinaryTree<Byte> {
    public byte data;
    public Boolean containsData = false;
    public HuffmanNode left;
    public HuffmanNode right;

    public HuffmanNode(byte _data, HuffmanNode _left, HuffmanNode _right, Boolean _containsData) {
        data = _data;
        containsData = _containsData;
        left = _left;
        right = _right;
    }

    public boolean equals(HuffmanNode node) {
        return this.data == node.data && this.containsData == node.containsData &&
                (this.left == null && node.left == null || this.left.equals(node.left)) &&
                (this.right == null && node.right == null || this.right.equals(node.right));
    }

    public Boolean isLeaf() {
        return this.left == null && this.right == null;
    }

    public HuffmanNode getLeft() {
        return this.left;
    }

    public HuffmanNode getRight() {
        return this.right;
    }

    public Byte getValue() {
        return this.data;
    }
}
