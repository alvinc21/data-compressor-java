package datacompressor.model;

import java.io.File;

public interface IDataDecompressor {
    public void decompress(File inputFile, File outFile) throws Exception;
}
