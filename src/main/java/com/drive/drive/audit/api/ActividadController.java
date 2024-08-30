package com.drive.drive.audit.api;

import com.drive.drive.audit.bl.ActividadBl;
import com.drive.drive.audit.dto.ActividadDto;
import com.drive.drive.audit.dto.ContadorActividadDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadBl actividadBl;

    @Autowired
    public ActividadController(ActividadBl actividadBl) {
        this.actividadBl = actividadBl;
    }

    // Crear actividad
    @PostMapping("/crearactividad")
    public ResponseEntity<ActividadDto> crearActividad(@RequestBody ActividadDto actividadDto) {
        ActividadDto nuevaActividad = actividadBl.crearActividad(actividadDto);
        return new ResponseEntity<>(nuevaActividad, HttpStatus.CREATED);
    }

    // Obtener todas las actividades
    @GetMapping("/todas")
    public ResponseEntity<List<ActividadDto>> obtenerTodasLasActividades(){
        List<ActividadDto> actividades = actividadBl.obtenerTodasLasActividades();
        return new ResponseEntity<>(actividades, HttpStatus.OK);
    }

    // Obtener actividad por id
    @GetMapping("/usuario/{usuarioId}/contador")
    public ResponseEntity<List<ContadorActividadDto>> obtenerActividadesPorUsuario(@PathVariable Long usuarioId){
        List<ContadorActividadDto> conatadorActividades = actividadBl.obtenerContadorActividadesPorUsuario(usuarioId);
        return new ResponseEntity<>(conatadorActividades, HttpStatus.OK);
    }

}