package datacompressor;

import datacompressor.huffman.HuffmanDataCompressor;
import datacompressor.huffman.HuffmanDataDecompressor;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("Input file path and output file path are required.");
        }

        Boolean isCompress;

        String method = args[0].toLowerCase();

        if (method.equals("compress")) {
            isCompress = true;
        } else if (method.equals("decompress")) {
            isCompress = false;
        } else {
            throw new Exception("Compression methods supported include: [compress, decompress]");
        }

        String inputFilePath = args[1];
        String outputFilePath = "";

        File inputFile = new File(inputFilePath);
        File outputFile;

        HuffmanDataCompressor dataCompressor = new HuffmanDataCompressor(); // Todo: Allow different types of data compressors to be used based on a supplied option.
        HuffmanDataDecompressor dataDecompressor = new HuffmanDataDecompressor();

        if (isCompress) {
            outputFilePath = inputFilePath + ".huf"; // Todo: map extension from some type of static constant/file.
            outputFile = new File(outputFilePath);
            dataCompressor.compress(inputFile, outputFile);
        } else {
           outputFilePath = inputFilePath.substring(0, inputFilePath.length() - 4);
            outputFile = new File(outputFilePath);
            dataDecompressor.decompress(inputFile, outputFile);
        }

        String returnMessage = "File " + inputFilePath + " is " + (isCompress ? "compressed" : "decompressed") + " into " + outputFilePath;

        System.out.println(returnMessage);
    }
}
