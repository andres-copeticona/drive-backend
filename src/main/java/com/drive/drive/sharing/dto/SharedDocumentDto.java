package com.drive.drive.sharing.dto;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SharedDocumentDto {
    private Long compartidoId;
    private Long receptorUsuarioId;
    private Long emisorUsuarioId;
    private Long documentoId;
    private String tipoAcceso;
    private Date createdAt;
    private String linkDocumento;
    private String nombreDocumento;
    private String categoria;
    private String emisorNombre;
    private String receptorNombre;

}