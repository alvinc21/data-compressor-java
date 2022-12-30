package datacompressor.huffman;

import datacompressor.huffman.model.HuffmanNode;
import datacompressor.huffman.model.ReadNode;
import datacompressor.utils.BinaryTreeUtils;
import datacompressor.utils.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static datacompressor.huffman.HuffmanDataCompressor.FILE_EXTENSION;
import static datacompressor.utils.DataUtils.getBitSequences;

public class HuffmanDataSpec extends TestCase {
    HuffmanDataCompressor compressor = new HuffmanDataCompressor();
    HuffmanDataDecompressor decompressor = new HuffmanDataDecompressor();
    String inputFilePath = "./test_resources/Silhouette-Partitura-completa.pdf";
    File inputFile = new File(inputFilePath);
    File outputFile = FileUtils.addFileExtension(inputFile, FILE_EXTENSION);
    File decompressedFile;
    byte[] inputFileData;
    byte[] outputFileData;
    byte[] decompressedFileData;

    HuffmanNode inputTree;
    Map<Byte, Integer> frequencyDictionary;

    int TREE_SIZE_INDEX = 0;
    int TREE_INDEX = TREE_SIZE_INDEX + Integer.BYTES;
    int TREE_DATA_SIZE;
    int FILE_SIZE_INDEX;
    int DECOMPRESSED_DATA_INDEX;


    @Override
    protected void setUp() throws Exception {
        try {
            inputFileData = Files.readAllBytes(Paths.get(inputFilePath));
            frequencyDictionary = compressor.makeFrequencyDictionary(inputFileData);
            inputTree = compressor.createHuffmanTree(frequencyDictionary);
            TREE_DATA_SIZE = compressor.treeToBytes(inputTree).length;
            FILE_SIZE_INDEX = TREE_INDEX + TREE_DATA_SIZE;
            DECOMPRESSED_DATA_INDEX = FILE_SIZE_INDEX + Integer.BYTES;

            compressor.compress(inputFile, outputFile);
            outputFileData = Files.readAllBytes(Paths.get(outputFile.getPath()));

            String fileExtension = com.google.common.io.Files.getFileExtension(inputFilePath);
            String decompressedFilePath = outputFile.getPath() + "." + fileExtension;
            decompressedFile = new File(decompressedFilePath);
            decompressor.decompress(outputFile, decompressedFile);
            decompressedFileData = Files.readAllBytes(Paths.get(decompressedFilePath));
        } catch (FileNotFoundException ex) {
            throw ex;
        }
    }

    @Test
    public void testTreeSizeEquality() {
        byte[] treeBytes = compressor.treeToBytes(inputTree);
        int inputTreeSize = treeBytes.length;
        int outputTreeSize = decompressor.getTreeSizeFromData(outputFileData, TREE_SIZE_INDEX, TREE_INDEX);
        Assert.assertEquals("Asserting that inputTreeSize: " + inputTreeSize + " equals outputTreeSize: " + outputTreeSize, inputTreeSize, outputTreeSize);
    }

    @Test
    public void testDecompressedDataSizeEquality() {
        int inputDataSize = inputFileData.length;
        int outputDecompressedDataSize = decompressor.getDecompressedFileSizeFromData(outputFileData, FILE_SIZE_INDEX, DECOMPRESSED_DATA_INDEX);
        Assert.assertEquals(inputDataSize, outputDecompressedDataSize);
    }

    @Test
    public void testTreeEquality() {
        byte[] readNodeBytes = Arrays.copyOfRange(outputFileData, TREE_INDEX, FILE_SIZE_INDEX);
        List<ReadNode> readNodes = decompressor.bytesToNodes(readNodeBytes);
        HuffmanNode outputTree = decompressor.generateTree(readNodes);

//        BinaryTreeUtils.print(System.out, inputTree);
//        BinaryTreeUtils.print(System.out, outputTree);
        Assert.assertTrue("Assert that input tree and output tree are equal", inputTree.equals(outputTree));
    }

    @Test
    public void testReadNodesEquality() {
        byte[] outputReadNodeBytes = Arrays.copyOfRange(outputFileData, TREE_INDEX, FILE_SIZE_INDEX);
        byte[] inputReadNodeBytes = compressor.treeToBytes(inputTree);
        List<ReadNode> inputReadNodes = decompressor.bytesToNodes(inputReadNodeBytes);
        List<ReadNode> outputReadNodes = decompressor.bytesToNodes(outputReadNodeBytes);

        Boolean listsAreEqual = true;
        for (int i = 0; i < inputReadNodes.size(); i++) {
            ReadNode inputReadNode = inputReadNodes.get(i);
            ReadNode outputReadNode = outputReadNodes.get(i);
            if (!inputReadNode.equals(outputReadNode)) {
                listsAreEqual = false;
                break;
            }
        }
        Assert.assertTrue(listsAreEqual);
    }

    @Test
    public void testCodesEquality() {
        Map<String, Byte> inputCodes = HuffmanUtils.getCodes(inputTree);
        int inputCodesSize = inputCodes.size();

        byte[] readNodeBytes = Arrays.copyOfRange(outputFileData, TREE_INDEX, FILE_SIZE_INDEX);
        List<ReadNode> readNodes = decompressor.bytesToNodes(readNodeBytes);
        HuffmanNode outputTree = decompressor.generateTree(readNodes);
        Map<String, Byte> outputCodes = HuffmanUtils.getCodes(outputTree);
        int outputCodesSize = outputCodes.size();

        Boolean codesMatch = true;
        for (Map.Entry<String, Byte> inputCode : inputCodes.entrySet()) {
            if (!outputCodes.get(inputCode.getKey()).equals(inputCode.getValue())) {
                codesMatch = false;
                break;
            }
        }

        Assert.assertTrue("Asserting that input Huffman codes equals output Huffman codes", inputCodesSize == outputCodesSize && codesMatch);
    }

    @Test
    public void testCodesDoNotMatchByPrefix() {
        Map<String, Byte> inputCodes = HuffmanUtils.getCodes(inputTree);

        int maxKeySize = 0;
        for (String key : inputCodes.keySet()) {
            maxKeySize = (key.length() > maxKeySize) ? key.length() : maxKeySize;
        }

        Set<String> keys = new HashSet<String>();

        for (String key : inputCodes.keySet()) {
            for (int i=0; i < key.length() - 1; i++) {
               String wildCard = key.substring(0, i + 1) + "*".repeat(maxKeySize - (i + 1));
               keys.add(wildCard);
            }
        }

        Boolean containsMatchPrefix = false;
        for (String key : inputCodes.keySet()) {
            String searchKey = key + "*".repeat(maxKeySize - key.length());
            if (keys.contains(searchKey)) {
                containsMatchPrefix = true;
                break;
            }
        }

        Assert.assertFalse("Codes do not match each other by prefix", containsMatchPrefix);
    }

    @Test
    public void testCompressedDataEquality() {
        Map<String, Byte> inputCodes = HuffmanUtils.getCodes(inputTree);
        byte[] inputCompressedData = compressor.generateCompressed(inputFileData, inputCodes);
        byte[] outputCompressedData = Arrays.copyOfRange(outputFileData, DECOMPRESSED_DATA_INDEX, outputFileData.length);

        Assert.assertArrayEquals("Assert that input compressed data corresponds to output compressed data", inputCompressedData, outputCompressedData);
    }

    @Test
    public void testBitSequencesEquality() {
        Map<String, Byte> inputCodes = HuffmanUtils.getCodes(inputTree);
        List<String> inputBitSequences = compressor.getBitSequences(inputFileData, inputCodes);
        byte[] compressedData = Arrays.copyOfRange(outputFileData, DECOMPRESSED_DATA_INDEX, outputFileData.length);
        List<String> outputBitSequences = getBitSequences(compressedData);

        Assert.assertEquals("Assert that input bit sequences are equal to output bit sequences", inputBitSequences, outputBitSequences);
    }

    // Todo: Consider removing this function in favor of testDecompressedFile().
    @Test
    public void testDecompressedFileEquality() {
        byte[] readNodeBytes = Arrays.copyOfRange(outputFileData, TREE_INDEX, FILE_SIZE_INDEX);
        List<ReadNode> readNodes = decompressor.bytesToNodes(readNodeBytes);
        HuffmanNode outputFileTree = decompressor.generateTree(readNodes);
        byte[] compressedData = Arrays.copyOfRange(outputFileData, DECOMPRESSED_DATA_INDEX, outputFileData.length);
        byte[] decompressedData = decompressor.generateDecompressed(outputFileTree, compressedData, inputFileData.length);

        Assert.assertArrayEquals(inputFileData, decompressedData);
    }

    // Todo: Move decompression setup code into this test function.
    @Test
    public void testDecompressedFile(){
        Assert.assertArrayEquals("Assert decompressed file is equal to uncompressed file", inputFileData, decompressedFileData);
    }
}
