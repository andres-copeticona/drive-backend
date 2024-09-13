package com.drive.drive.modules.activities.services;

import com.drive.drive.modules.activities.dto.ActivityDto;
import com.drive.drive.modules.activities.dto.ActivityFilter;
import com.drive.drive.modules.activities.dto.CreateActivityDto;
import com.drive.drive.modules.activities.entities.ActivityEntity;
import com.drive.drive.modules.activities.mappers.ActivityMapper;
import com.drive.drive.modules.activities.repositories.ActivityRepository;
import com.drive.drive.modules.user.entities.UserEntity;
import com.drive.drive.modules.user.repositories.UserRepository;
import com.drive.drive.shared.dto.ListResponseDto;
import com.drive.drive.shared.dto.ResponseDto;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ActivityService {

  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private UserRepository userRepository;

  public ResponseDto<ListResponseDto<List<ActivityDto>>> listActivities(ActivityFilter filter) {
    try {
      Specification<ActivityEntity> spec = filter.getSpecification();
      Sort sort = filter.getSort();
      Pageable pageable = filter.getPageable();

      List<ActivityEntity> activities;
      Long total = 0L;

      if (pageable == null) {
        activities = activityRepository.findAll(spec, sort);
        total = Long.valueOf(activities.size());
      } else {
        var res = activityRepository.findAll(spec, pageable);
        activities = res.getContent();
        total = res.getTotalElements();
      }

      List<ActivityDto> dtos = activities.stream().map(ActivityMapper::entityToDto).collect(Collectors.toList());
      return new ResponseDto<>(200, new ListResponseDto<>(dtos, total), "Lista de actividades obtenida correctamente.");
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseDto<>(500, null, "Error obteniendo la lista de actividades");
    }
  }

  public void createActivity(CreateActivityDto createActivityDto) {
    ActivityEntity log = new ActivityEntity();
    log.setActivityType(createActivityDto.getActivityType());
    log.setIp(createActivityDto.getIp());
    UserEntity user = userRepository.findById(createActivityDto.getUserId()).get();
    log.setUser(user);
    log.setName(createActivityDto.getDescription());
    activityRepository.save(log);
  }

  // @Transactional
  // public ActividadDto createActivity(ActividadDto actividadDto) {
  // if (actividadDto.getUsuarioId() == null) {
  // throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
  // }
  //
  // Actividad actividad = new Actividad();
  // actividad.setNombre(actividadDto.getNombre());
  // actividad.setFecha(actividadDto.getFecha());
  // actividad.setIp(actividadDto.getIp());
  //
  // // Crear un usuario con el ID proporcionado
  // UserEntity usuario = new UserEntity();
  // usuario.setId(actividadDto.getUsuarioId());
  //
  // actividad.setUsuario(usuario);
  // actividad.setTipoActividad(actividadDto.getTipoActividad());
  //
  // Actividad nuevaActividad = activityRepository.save(actividad);
  //
  // return convertirAActividadDto(nuevaActividad);
  // }
  //
  // // Método para obtener todas las actividades
  // @Transactional
  // public List<ActividadDto> obtenerTodasLasActividades() {
  // return activityRepository.findAll().stream()
  // .map(this::convertirAActividadDto)
  // .collect(Collectors.toList());
  // }
  //
  // // Método para obtener actividades por usuario
  // @Transactional
  // public List<ContadorActividadDto> obtenerContadorActividadesPorUsuario(Long
  // usuarioId) {
  // List<Actividad> actividades = activityRepository.findByUsuario_id(usuarioId);
  //
  // Map<String, Long> contadorporTipo = actividades.stream()
  // .collect(Collectors.groupingBy(Actividad::getTipoActividad,
  // Collectors.counting()));
  //
  // return contadorporTipo.entrySet().stream()
  // .map(entry -> new ContadorActividadDto(entry.getKey(), entry.getValue()))
  // .collect(Collectors.toList());
  // }
  //
  // // Método para convertir Actividad a ActividadDto
  // private ActividadDto convertirAActividadDto(Actividad actividad) {
  // return new ActividadDto(
  // actividad.getId(),
  // actividad.getNombre(),
  // actividad.getFecha(),
  // actividad.getIp(),
  // actividad.getUsuario().getId(), // Cambiado a ID de usuario
  // actividad.getTipoActividad());
  // }
}
