package com.drive.drive.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import com.drive.drive.modules.user.entities.RoleEntity;
import com.drive.drive.modules.user.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RoleSeeder implements ApplicationRunner {
  private final RoleRepository roleRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (args.getOptionValues("seeder") != null) {
      List<String> seeder = Arrays.asList(args.getOptionValues("seeder").get(0).split(","));
      if (seeder.contains("role")) {
        seedRoles();
        log.info("Success run role seeder");
      }
    } else {
      log.info("Role seeder skipped");
    }
  }

  private void seedRoles() {
    List<String> roles = new ArrayList<>();

    roles.add("Administrador");
    roles.add("Super Administrador");

    var index = 0;
    for (var role : roles) {
      AtomicLong id = new AtomicLong(index + 1L);

      roleRepository.findById(id.getPlain()).ifPresentOrElse(
          (value) -> {
            log.info("Role {} already exists", role);
          },
          () -> {
            var newRole = new RoleEntity();
            newRole.setId(id.getPlain());
            newRole.setName(role);
            roleRepository.save(newRole);
            log.info("Success run RoleSeeder {}", role);
          });

      index++;
    }
  }
}
