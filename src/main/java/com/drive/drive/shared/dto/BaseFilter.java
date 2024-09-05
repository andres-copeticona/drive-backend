package com.drive.drive.shared.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseFilter {
  int page = 0;
  int size = 10;
  String searchTerm = "";

  @Override
  public String toString() {
    return "{" +
        "page=" + page +
        ", size=" + size +
        ", searchTerm='" + searchTerm + "'}";
  }

}
