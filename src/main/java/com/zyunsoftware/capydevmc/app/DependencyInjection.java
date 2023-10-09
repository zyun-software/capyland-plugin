package com.zyunsoftware.capydevmc.app;

import com.zyunsoftware.capydevmc.domain.models.migration.MigrationRepository;
import com.zyunsoftware.capydevmc.infrastructure.adapters.migration.MigrationAdapter;

public class DependencyInjection {
  public static MigrationRepository getMigrationRepository() {
    MigrationAdapter adapter = new MigrationAdapter();

    return adapter;
  }
}
