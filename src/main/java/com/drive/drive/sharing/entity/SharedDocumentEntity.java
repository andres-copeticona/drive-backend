package com.drive.drive.sharing.entity;

import com.drive.drive.file.entity.FileEntity;
import com.drive.drive.user.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Documentos_Compartidos")
public class SharedDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CompartidoID")
    private Long compartidoId;

    @ManyToOne
    @JoinColumn(name = "Receptor_UsuarioID", referencedColumnName = "UsuarioID")
    private Usuario receptor;

    @ManyToOne
    @JoinColumn(name = "Emisor_UsuarioID", referencedColumnName = "UsuarioID")
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "DocumentoID", referencedColumnName = "DocumentoID")
    private FileEntity documento;

    @Column(name = "TipoAcceso")
    private String tipoAcceso;

    @Column(name = "CreatedAt")
    private Date createdAt;

    @Column(name = "LinkDocumento")
    private String linkDocumento;
}