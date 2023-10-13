package com.zyunsoftware.capydevmc.domain.models.user;

import com.zyunsoftware.capydevmc.core.RepositorySave;

public interface UserRepository extends RepositorySave<UserModel> {
  UserEntity findByNickname(String value);
}
