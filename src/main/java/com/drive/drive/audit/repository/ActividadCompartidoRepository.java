package com.drive.drive.audit.repository;

import com.drive.drive.audit.entity.ActividadCompartido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadCompartidoRepository extends JpaRepository<ActividadCompartido, Long>{

    // Método para obtener una actividad compartida por idFolder y idArchivo
    List<ActividadCompartido> findByIdFolderAndIdArchivo(int idFolder, int idArchivo);
}
