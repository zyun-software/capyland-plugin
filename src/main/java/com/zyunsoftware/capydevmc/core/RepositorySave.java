package com.zyunsoftware.capydevmc.core;

public interface RepositorySave <TModel extends Model> {
  TModel save(TModel model);
}
