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
    return Math.round(size*100.0)/100.0;
  }

  public void addLines(int newLines) {
    this.lines += newLines;
  }

  public void addSize(double newSize) {
    this.size += newSize;
  }
}