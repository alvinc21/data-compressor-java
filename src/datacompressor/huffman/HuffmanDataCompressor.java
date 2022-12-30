package datacompressor.huffman;

import datacompressor.model.IDataCompressor;
import datacompressor.huffman.model.HuffmanNode;
import static datacompressor.huffman.HuffmanUtils.getCodes;
import static datacompressor.utils.DataUtils.intToBytes;
import static datacompressor.utils.DataUtils.padBinaryString;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author : alvinchan
 * @created : 2022-12-25
**/

public class HuffmanDataCompressor implements IDataCompressor {
  static String FILE_EXTENSION = "huf";

  // Todo: Consider moving the array writing code into separate functions for readability.
  public void compress(File inputFile, File outputFile) throws Exception {
    try {
      Path inputFilePath = Paths.get(inputFile.getPath());
      byte[] fileData = Files.readAllBytes(inputFilePath);

      Map<Byte, Integer> frequencyDictionary = makeFrequencyDictionary(fileData);
      HuffmanNode tree = createHuffmanTree(frequencyDictionary);
      Map<String, Byte> codes = getCodes(tree);

      byte[] treeBytes = treeToBytes(tree);
      byte[] treeSizeBytes = intToBytes(treeBytes.length);
      byte[] compressedData = generateCompressed(fileData, codes);
      byte[] sizeBytes = intToBytes(fileData.length);
      byte[] compressedFileData = Arrays.copyOf(treeSizeBytes, treeSizeBytes.length + treeBytes.length +  sizeBytes.length + compressedData.length);

      int writeIndex = 0;
      System.arraycopy(treeBytes, 0, compressedFileData, writeIndex += treeSizeBytes.length, treeBytes.length);
      System.arraycopy(sizeBytes, 0, compressedFileData, writeIndex +=  treeBytes.length, sizeBytes.length);
      System.arraycopy(compressedData, 0, compressedFileData, writeIndex += sizeBytes.length, compressedData.length);
      Files.write(outputFile.toPath(), compressedFileData);
    } catch (FileNotFoundException ex) {
      System.out.println("File at " + inputFile.getPath() + " not found.");
      throw ex;
    }
  }

  Map<Byte, Integer> makeFrequencyDictionary(byte[] data) {
    HashMap<Byte, Integer> dictionary = new HashMap<Byte, Integer>();

    for (byte dataChunk : data) {
      if (dictionary.containsKey(dataChunk)) {
        dictionary.put(dataChunk, dictionary.get(dataChunk) + 1);
      } else {
        dictionary.put(dataChunk, 1);
      }
    }
    return dictionary;
  }

  private class HuffmanNodeComparator implements Comparator<Map.Entry<HuffmanNode, Integer>> {
    public int compare(Map.Entry<HuffmanNode, Integer> entry1, Map.Entry<HuffmanNode, Integer> entry2) {
      int entry1Value = entry1.getValue();
      int entry2Value = entry2.getValue();
      if (entry1Value < entry2Value) {
        return -1;
      } else if (entry1Value > entry2Value) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  HuffmanNode createHuffmanTree(Map<Byte, Integer> frequencyDictionary) {
    PriorityQueue<Map.Entry<HuffmanNode, Integer>> frequencyHeap = new PriorityQueue<Map.Entry<HuffmanNode, Integer>>(new HuffmanNodeComparator());
    for (Map.Entry<Byte, Integer> entry : frequencyDictionary.entrySet()) {
      byte data = entry.getKey();
      int frequency = entry.getValue();
      frequencyHeap.add(new AbstractMap.SimpleEntry<HuffmanNode, Integer>(new HuffmanNode(data, null, null, true), frequency));
    }

    while (frequencyHeap.size() > 1) {
      Map.Entry<HuffmanNode, Integer> left = frequencyHeap.remove();
      Map.Entry<HuffmanNode, Integer> right = frequencyHeap.remove();
      HuffmanNode leftNode = left.getKey();
      HuffmanNode rightNode = right.getKey();
      HuffmanNode node = new HuffmanNode(Byte.MIN_VALUE, leftNode, rightNode, false);
      frequencyHeap.add(new AbstractMap.SimpleEntry<HuffmanNode, Integer>(node, left.getValue() + right.getValue()));
    }

    return frequencyHeap.remove().getKey();
  }

  byte[] generateCompressed(byte[] data, Map<String, Byte> codes) {
    List<String> compressedData = getBitSequences(data, codes);

    ByteBuffer byteBuffer = ByteBuffer.allocate(compressedData.size());
    for (String dataChunk : compressedData) {
        byteBuffer.put((byte) Integer.parseInt(dataChunk, 2));
    }

    return byteBuffer.array();
  }

  List<String> getBitSequences(byte[] data, Map<String, Byte> codes) {
    List<String> compressedData = new ArrayList<String>();
    Map<Byte, String> flippedCodes = new HashMap<Byte, String>();

    for (Map.Entry<String, Byte> entry : codes.entrySet()) {
      flippedCodes.put(entry.getValue(), entry.getKey());
    }

    String dataEntry = "";

    for (byte dataChunk : data) {
      if (flippedCodes.containsKey(dataChunk)) {
        String code = flippedCodes.get(dataChunk);
        dataEntry = dataEntry + code;
      } else {
        System.out.println("missing key");
      }
      while (dataEntry.length() >= 8) {
        String compresssedCode = dataEntry.substring(0, 8);
        dataEntry = dataEntry.substring(8);
        compressedData.add(compresssedCode);
      }
    }
    if (!dataEntry.equals("")) {
      compressedData.add(padBinaryString(dataEntry, false));
    }

    return compressedData;
  }

  byte[] treeToBytes(HuffmanNode tree) {
    Stack<HuffmanNode> nodes = new Stack<HuffmanNode>();
    nodes.add(tree);
    ArrayList<Byte> data = new ArrayList<Byte>();

    while (!nodes.isEmpty()) {
      HuffmanNode node = nodes.pop();
      if (node != null) {
        if (node.left != null) {
          if (node.left.isLeaf()) {
            data.add((byte) 0);
            data.add(node.left.data);
          } else {
            data.add((byte) 1);
            data.add(Byte.MIN_VALUE);
          }
        }
        if (node.right != null) {
          if (node.right.isLeaf()) {
            data.add((byte) 0);
            data.add(node.right.data);
          } else {
            data.add((byte) 1);
            data.add(Byte.MIN_VALUE);
          }
        }
        nodes.push(node.right);
        nodes.push(node.left);
      }
    }
    ByteBuffer bytes = ByteBuffer.allocate(data.size());
    for (byte chunk : data) {
      bytes.put(chunk);
    }

    return bytes.array();
  }
}

