package datacompressor.huffman.model;

public enum ReadNodeType {
    LEAF(0),
    TREE(1);

    int value;

    private ReadNodeType(int value) {
        this.value = value;
    }

    public static ReadNodeType getReadNodeType(int value) {
        switch (value) {
            case 0:
                return LEAF;
            case 1:
                return TREE;
        }
        return null;
    }

    public boolean isLeaf() {
        return this.value == 0;
    }
}
