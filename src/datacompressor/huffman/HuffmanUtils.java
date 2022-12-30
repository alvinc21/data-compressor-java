package datacompressor.huffman;

import datacompressor.huffman.model.HuffmanNode;

import java.util.HashMap;
import java.util.Map;

public class HuffmanUtils {
    static Map<String, Byte> getCodes(HuffmanNode tree) {
        HashMap<String, Byte> codes = new HashMap<String, Byte>();
        getCodesHelper(tree, "", codes);

        return codes;
    }
    private static void getCodesHelper(HuffmanNode node, String code, Map<String, Byte> codes) {
        if (node != null) {
            if (node.left != null) {
                if (node.left.containsData) {
                    codes.put(code + "0", node.left.data);
                }
            }
            if (node.right != null) {
                if(node.right.containsData) {
                    codes.put(code + "1", node.right.data);
                }
            }

            getCodesHelper(node.left, code + "0", codes);
            getCodesHelper(node.right, code + "1", codes);
        }
    }
}
