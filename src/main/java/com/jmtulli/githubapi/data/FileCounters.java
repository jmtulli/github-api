package com.jmtulli.githubapi.data;

public class FileCounters {

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
    return size;
  }

  public void addLines(int newLines) {
    this.lines += newLines;
  }

  public void addSize(double newSize) {
    this.size += newSize;
  }
}
