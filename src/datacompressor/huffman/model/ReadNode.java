package datacompressor.huffman.model;

public class ReadNode {
    public ReadNodeType leftType;
    public Byte leftData;
    public ReadNodeType rightType;
    public Byte rightData;

    public ReadNode(ReadNodeType _leftType, Byte _leftData, ReadNodeType _rightType, Byte _rightData) {
        leftType = _leftType;
        leftData = _leftData;
        rightType = _rightType;
        rightData = _rightData;
    }

    @Override
    public String toString(){
        return String.format("[%d,%d,%d,%d]", leftType.value, leftData, rightType.value, rightData);
    }

    public Boolean equals(ReadNode readNode) {
        return this.leftType == readNode.leftType && this.leftData.equals(readNode.leftData) && this.rightType == readNode.rightType && this.rightData.equals(readNode.rightData);
    }
}
