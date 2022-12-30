package datacompressor.utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class DataUtils {

    public static List<String> getBitSequences(byte[] data) {
        List<String> bitSequences = new ArrayList<String>();
        for (byte dataChunk : data) {
            String bitSequence = byteToBitSequence(dataChunk, true);
            bitSequences.add(bitSequence);
        }
        return bitSequences;
    }
    public static String byteToBitSequence(Byte data, Boolean leftPadded) {
        return padBinaryString(Integer.toBinaryString(Byte.toUnsignedInt(data)), leftPadded);
    }

    public static String padBinaryString(String binaryString, Boolean leftPadded) {
        String padding = "0".repeat(8 - binaryString.length());
        if (leftPadded) {
            return padding + binaryString;
        } else {
            return binaryString + padding;
        }
    }

    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
    }
}
