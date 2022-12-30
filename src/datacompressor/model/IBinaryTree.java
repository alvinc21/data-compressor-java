package datacompressor.model;

public interface IBinaryTree<T> {
    public IBinaryTree getLeft();
    public IBinaryTree getRight();
    public T getValue();
}
