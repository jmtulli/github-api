package com.jmtulli.githubapi.util;

public final class ApplicationConstants {

  public static final Integer RETRY_TIME_AFTER_TOO_MANY_REQUEST = 60000;
  public static final String HEADER_NAME = "X-Requested-With";
  public static final String HEADER_VALUE = "XMLHttpRequest";
  public static final String PATTERN_BRANCH_NAME = ">(.*)<\\/a>";
  public static final String PATTERN_CLASS_BRANCH_NAME = "<a class=\"branch-name";
  public static final String PATTERN_CLASS_RESULT = "<div class=\"text-mono f6 flex-auto pr-3 flex-order-2 flex-md-order-1 mt-2 mt-md-0\">";
  public static final String PATTERN_CLASS_TREE_FINDER = "class=\"js-tree-finder\"";
  public static final String PATTERN_FILE_LINES = "\\s*(\\d*?)\\sline";
  public static final String PATTERN_FILE_SIZE = "\\s*(\\d*?.\\d*?)\\s(Byte|KB|MB|GB)";
  public static final String PATTERN_TREE_LIST = "data-url=\"(.*?)\"";
  public static final String STRING_NO_EXTENSION = "NO_EXTENSION";
  public static final String URL_ALL_BRANCHES = "/branches/all";
  public static final String URL_BLOB = "/blob/";
  public static final String URL_FIND = "/find/";
  public static final String URL_GITHUB = "https://github.com";
  public static final String URL_GITHUB_API = "https://github.com/jmtulli/github-api";

}
