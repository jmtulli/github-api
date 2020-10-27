package com.jmtulli.githubapi.data;

import java.io.Serializable;

/**
 * Keep track of the quantity of lines and the total size (in bytes) of the files for a given file
 * extension.
 * 
 * @author Jose Tulli
 *
 */
public class FileCounters implements Serializable {

  private int lines;
  private double size;

  public FileCounters(int lines, double size) {
    this.lines = lines;
    this.size = size;
  }

  public int getLines() {
    return lines;
  }

  public double getSize() {
    return Math.round(size * 100.0) / 100.0;
  }

  public void addLines(int newLines) {
    this.lines += newLines;
  }

  public void addSize(double newSize) {
    this.size += newSize;
  }
}
