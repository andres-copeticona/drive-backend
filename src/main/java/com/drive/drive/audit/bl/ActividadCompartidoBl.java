package com.drive.drive.audit.bl;

import com.drive.drive.audit.dto.ActividadCompartidoDto;
import com.drive.drive.audit.dto.ArchivoContadorDto;
import com.drive.drive.audit.entity.ActividadCompartido;
import com.drive.drive.audit.repository.ActividadCompartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActividadCompartidoBl {
    @Autowired
    private ActividadCompartidoRepository actividadCompartidoRepository;

    // Método para crear una actividad compartida
    public ActividadCompartidoDto createActividadCompartido(ActividadCompartidoDto actividadCompartidoDto) {
        ActividadCompartido actividadCompartido = new ActividadCompartido();
        actividadCompartido.setContador(0.5); // Asegúrate de que `contador` sea de tipo `double` en la entidad
        actividadCompartido.setIdArchivo(actividadCompartidoDto.getIdArchivo());
        actividadCompartido.setIdFolder(actividadCompartidoDto.getIdFolder());
        actividadCompartido.setIdUsuario(actividadCompartidoDto.getIdUsuario());
        actividadCompartido.setNombre(actividadCompartidoDto.getNombre());
        actividadCompartido.setTipo(actividadCompartidoDto.getTipo());

        ActividadCompartido save = actividadCompartidoRepository.save(actividadCompartido);
        return new ActividadCompartidoDto(
                save.getId(),
                save.getContador(),
                save.getIdArchivo(),
                save.getIdFolder(),
                save.getIdUsuario(),
                save.getFecha(),
                save.getNombre(),
                save.getTipo()
        );
    }

    // Método para obtener el total de contador por idFolder y idArchivo
    public double getTotalContador(int idFolder, int idArchivo) {
        List<ActividadCompartido> actividades = actividadCompartidoRepository.findByIdFolderAndIdArchivo(idFolder, idArchivo);
        return actividades.stream().mapToDouble(ActividadCompartido::getContador).sum();
    }

    // Método para obtener todos los archivos con contadores sumados
    public List<ArchivoContadorDto> getAllArchivosConContadoresSumados() {
        List<ActividadCompartido> actividades = actividadCompartidoRepository.findAll();

        Map<Integer, Map<Integer, List<ActividadCompartido>>> archivoFolderContadores = actividades.stream()
                .collect(Collectors.groupingBy(
                        ActividadCompartido::getIdArchivo,
                        Collectors.groupingBy(
                                ActividadCompartido::getIdFolder
                        )
                ));

        return archivoFolderContadores.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .map(folderEntry -> {
                            double contadorSumado = folderEntry.getValue().stream().mapToDouble(ActividadCompartido::getContador).sum();
                            String nombre = folderEntry.getValue().stream().findFirst().map(ActividadCompartido::getNombre).orElse(null);
                            String tipo = folderEntry.getValue().stream().findFirst().map(ActividadCompartido::getTipo).orElse(null);
                            return new ArchivoContadorDto(entry.getKey(), folderEntry.getKey(), contadorSumado, nombre, tipo);
                        })
                ).collect(Collectors.toList());
    }

    // Método para obtener todas las actividades compartidas
    public List<ActividadCompartidoDto> getAllActividades() {
        List<ActividadCompartido> actividades = actividadCompartidoRepository.findAll();
        return actividades.stream().map(actividad -> new ActividadCompartidoDto(
                actividad.getId(),
                actividad.getContador(),
                actividad.getIdArchivo(),
                actividad.getIdFolder(),
                actividad.getIdUsuario(),
                actividad.getFecha(),
                actividad.getNombre(),
                actividad.getTipo()
        )).collect(Collectors.toList());
    }
}
