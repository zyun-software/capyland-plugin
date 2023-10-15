package com.zyunsoftware.capydevmc.domain.models.linked_account;

public interface LinkedAccountRepository {
  boolean linkDiscord(int user_id, String code);
}
