
package com.drive.drive.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response")
public class ErrorResponseDto {
  @Schema(description = "HTTP status code", example = "500")
  private Integer code;
  @Schema(description = "Error data", example = "null")
  private Object data;
  @Schema(description = "Error message", example = "Internal server error")
  private String message;
}
