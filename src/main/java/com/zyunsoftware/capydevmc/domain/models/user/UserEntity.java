package com.zyunsoftware.capydevmc.domain.models.user;

import com.zyunsoftware.capydevmc.core.Entity;

public class UserEntity extends Entity<UserModel, UserRepository> {
  public UserEntity(UserModel model, UserRepository repository) {
    super(model, repository);
  }

  public boolean auditPassword(String value) {
    boolean result = _model.password.equals(value);

    return result;
  }

  public UserEntity setCoordinates(int x, int y, int z) {
    _model.x = x;
    _model.y = y;
    _model.z = z;
    save();

    return this;
  }

  public UserEntity setPassword(String value) {
    _model.password = value;
    save();

    return this;
  }

  public UserEntity setApproved(boolean value) {
    _model.approved = value;
    save();

    return this;
  }

  public UserEntity changeBalance(int value) {
    _model.balance += value;
    save();

    return this;
  }

  @Override
  public void save() {
    _model = _repository.save(_model);
  }
}
