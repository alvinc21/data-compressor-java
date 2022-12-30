package datacompressor.huffman;

import datacompressor.model.IDataDecompressor;
import datacompressor.huffman.model.HuffmanNode;
import datacompressor.huffman.model.ReadNode;
import datacompressor.huffman.model.ReadNodeType;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static datacompressor.huffman.HuffmanUtils.getCodes;
import static datacompressor.utils.DataUtils.byteToBitSequence;
import static datacompressor.utils.DataUtils.getBitSequences;

public class HuffmanDataDecompressor implements IDataDecompressor {
    public void decompress(File inputFile, File outputFile) throws Exception {
        try {
            int fileReadIndex = 0;
            Path inputFilePath = Paths.get(inputFile.getPath());
            byte[] fileData = Files.readAllBytes(inputFilePath);

            int treeSize = getTreeSizeFromData(fileData, fileReadIndex, fileReadIndex += Integer.BYTES);

            HuffmanNode tree = getTreeFromData(fileData, fileReadIndex, fileReadIndex += treeSize);

            int decompressedDataSize = getDecompressedFileSizeFromData(fileData, fileReadIndex, fileReadIndex += Integer.BYTES);

            byte[] compressedFileData = Arrays.copyOfRange(fileData, fileReadIndex, fileData.length);
            byte[] decompressedFileData = generateDecompressed(tree, compressedFileData, decompressedDataSize);

            Files.write(outputFile.toPath(), decompressedFileData);
        } catch (FileNotFoundException ex) {
            System.out.println("File at " + inputFile.getPath() + " not found.");
            throw ex;
        }
    }

  HuffmanNode generateTree(List<ReadNode> readNodes) {
      if (readNodes.isEmpty()) {
        return null;
    } else {
          HuffmanNode root = new HuffmanNode(Byte.MIN_VALUE, null, null, false);
          ReadNode readNode = readNodes.remove(0);
        if (readNode.leftType.isLeaf()) {
            root.left = new HuffmanNode(readNode.leftData, null, null,true);
        } else {
            root.left = generateTree(readNodes);
        }
        if (readNode.rightType.isLeaf()) {
            root.right = new HuffmanNode(readNode.rightData, null, null, true);
        } else {
            root.right = generateTree(readNodes);
        }

       return root;
    }
  }

  byte[] generateDecompressed(HuffmanNode tree, byte[] data, int size) {
        Map<String, Byte> codes = getCodes(tree);
        ByteBuffer decompressedData = ByteBuffer.allocate(size + 1);
        String code = "";
        List<String> bitSequences = getBitSequences(data);

        for (String bitSequence : bitSequences) {
            for (char bit : bitSequence.toCharArray()) {
                code += bit;
                if (codes.containsKey(code)) {
                    byte decompressedDataChunk = codes.get(code);
                    decompressedData.put(decompressedDataChunk);
                    code = "";
                }
            }
        }

        return Arrays.copyOf(decompressedData.array(), size);
  }

  List<ReadNode> bytesToNodes(byte[] bytes) {
      LinkedList<ReadNode> nodes = new LinkedList<ReadNode>();

      for (int i = 0; i + 3 < bytes.length; i+=4){
          ReadNodeType leftType = ReadNodeType.getReadNodeType((int) bytes[i]);
          byte leftData = bytes[i + 1];

          ReadNodeType rightType = ReadNodeType.getReadNodeType((int) bytes[i + 2]);
          byte rightData = bytes[i + 3];

          ReadNode readNode = new ReadNode(leftType, leftData, rightType, rightData);
          nodes.add(readNode);
      }
      return nodes;
  }

  int getTreeSizeFromData(byte [] fileData, int fileReadIndexStart, int fileReadIndexEnd) {
      byte[] treeSizeBytes = Arrays.copyOfRange(fileData, fileReadIndexStart, fileReadIndexEnd);
      int treeSize = ByteBuffer.wrap(treeSizeBytes).getInt();
      return treeSize;
  }

  int getDecompressedFileSizeFromData(byte[] fileData, int fileReadIndexStart, int fileReadIndexEnd) {
      byte[] sizeBytes = Arrays.copyOfRange(fileData, fileReadIndexStart, fileReadIndexEnd);
      int size = ByteBuffer.wrap(sizeBytes).getInt();
      return size;
  }

  HuffmanNode getTreeFromData(byte[] fileData, int fileReadIndexStart, int fileReadIndexEnd) {
      byte[] treeBytes = Arrays.copyOfRange(fileData, fileReadIndexStart, fileReadIndexEnd);
      List<ReadNode> readNodes = bytesToNodes(treeBytes);
      return generateTree(readNodes);
  }
}
