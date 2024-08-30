package com.drive.drive.audit.api;

import com.drive.drive.audit.bl.ActividadCompartidoBl;
import com.drive.drive.audit.dto.ActividadCompartidoDto;
import com.drive.drive.audit.dto.ArchivoContadorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actividad-compartido")
public class ActividadCompartidoController {
    @Autowired
    private ActividadCompartidoBl actividadCompartidoBl;

    // Crear actividad compartida
    @PostMapping("/create")
    public ResponseEntity<ActividadCompartidoDto> createActividadCompartido(@RequestBody ActividadCompartidoDto actividadCompartidoDto) {
        ActividadCompartidoDto createActividadCompartido = actividadCompartidoBl.createActividadCompartido(actividadCompartidoDto);
        return ResponseEntity.ok(createActividadCompartido);
    }

    // Obtener actividad compartida por id
    @GetMapping("/total-contador")
    public ResponseEntity<Double> getTotalContadorByIdFolderAndIdArchivo(@RequestParam int idFolder, @RequestParam int idArchivo){
        double totalContador = actividadCompartidoBl.getTotalContador(idFolder, idArchivo);
        return ResponseEntity.ok(totalContador);
    }

    // Obtener todos los archivos con contadores sumados
    @GetMapping("/all-archivos-contadores")
    public ResponseEntity<List<ArchivoContadorDto>> getAllArchivosConContadoresSumados() {
        List<ArchivoContadorDto> archivosConContadores = actividadCompartidoBl.getAllArchivosConContadoresSumados();
        return ResponseEntity.ok(archivosConContadores);
    }

    // Obtener todas las actividades compartidas
    @GetMapping("/all")
    public ResponseEntity<List<ActividadCompartidoDto>> getAllActividades() {
        List<ActividadCompartidoDto> actividades = actividadCompartidoBl.getAllActividades();
        return ResponseEntity.ok(actividades);
    }
}
