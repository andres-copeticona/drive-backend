package com.drive.drive.shared.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public abstract class BaseFilter<T> extends Object {
  @Parameter(description = "Sort by", example = "id", required = false)
  protected String sortBy = "id";

  @Parameter(description = "Sort direction", example = "asc", required = false, schema = @Schema(allowableValues = {
      "asc", "desc" }))
  protected String sortDirection = "asc";

  @Parameter(description = "Page number", example = "0", required = false)
  protected int page = 0;

  @Parameter(description = "Page size", example = "10", required = false)
  protected int size = 10;

  @Parameter(description = "Search term", example = "example", required = false)
  protected String searchTerm;

  @Parameter(description = "Show all records", example = "false", required = false)
  protected boolean showAll = false;

  public Sort getSort() {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
    return sort;
  }

  public Pageable getPageable() {
    if (showAll)
      return Pageable.unpaged(getSort());
    else
      return PageRequest.of(page, size, getSort());
  }

  public abstract Specification<T> getSpecification();
}
