package com.drive.drive.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Wrapper Response")
public class ResponseDto<T> {
  @Schema(description = "HTTP status code", example = "200")
  private Integer code;
  @Schema(description = "Response data")
  private T data;
  @Schema(description = "Response message", example = "Success")
  private String message;
}
