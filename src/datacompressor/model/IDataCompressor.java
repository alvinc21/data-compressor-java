package datacompressor.model;

import java.io.File;

/**
 * @author : alvinchan
 * @created : 2022-12-25
**/
public interface IDataCompressor {
  public void compress(File inputFile, File outputFile) throws Exception;
}
