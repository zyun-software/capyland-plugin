package com.zyunsoftware.capydevmc.app;

import com.zyunsoftware.capydevmc.domain.models.migration.MigrationRepository;
import com.zyunsoftware.capydevmc.domain.models.minecraft.MinecraftRepository;
import com.zyunsoftware.capydevmc.domain.models.session.SessionRepository;
import com.zyunsoftware.capydevmc.domain.models.user.UserRepository;
import com.zyunsoftware.capydevmc.domain.services.AuthorizationService;
import com.zyunsoftware.capydevmc.infrastructure.adapters.migration.MigrationAdapter;
import com.zyunsoftware.capydevmc.infrastructure.adapters.minecraft.MinecraftAdapter;
import com.zyunsoftware.capydevmc.infrastructure.adapters.session.SessionAdapter;
import com.zyunsoftware.capydevmc.infrastructure.adapters.users.UserAdapter;

public class DependencyInjection {
  public static MigrationRepository getMigrationRepository() {
    MigrationAdapter adapter = new MigrationAdapter();

    return adapter;
  }

  public static MinecraftRepository getMinecraftRepository() {
    MinecraftAdapter adapter = new MinecraftAdapter();

    return adapter;
  }

  public static SessionRepository getSessionRepository() {
    SessionAdapter adapter = new SessionAdapter();

    return adapter;
  }

  public static UserRepository getUserRepository() {
    UserAdapter adapter = new UserAdapter();

    return adapter;
  }

  public static AuthorizationService getAuthorizationService() {
    AuthorizationService service = new AuthorizationService(
      getMinecraftRepository(),
      getSessionRepository(),
      getUserRepository()
    );

    return service;
  }
}
