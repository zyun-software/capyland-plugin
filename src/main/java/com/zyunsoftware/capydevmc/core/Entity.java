package com.zyunsoftware.capydevmc.core;

public abstract class Entity<TModel extends Model, TRepository extends RepositorySave<?>> {
  protected TModel _model;
  protected TRepository _repository;

  public TModel getModel() {
    return _model;
  }

  public Entity(TModel model, TRepository repository) {
    _model = model;
    _repository = repository;
  }

  public abstract void save();
}
