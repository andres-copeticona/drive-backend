package com.drive.drive.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "List response DTO")
public class ListResponseDto<T> {
  @Schema(description = "elements in the list")
  T data;

  @Schema(description = "total elements in the list")
  Long total;
}
