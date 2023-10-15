package com.zyunsoftware.capydevmc.infrastructure.adapters.linked_account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.zyunsoftware.capydevmc.domain.models.linked_account.LinkedAccountRepository;
import com.zyunsoftware.capydevmc.infrastructure.Api;

public class LinkedAccountAdapter implements LinkedAccountRepository {
  public boolean linkDiscord(int user_id, String code) {
    String deleteQuery = "DELETE FROM capyland_linked_accounts WHERE user_id = ? AND name = 'discord'";
    String insertQuery = "INSERT INTO capyland_linked_accounts (user_id, code, name) VALUES (?, ?, 'discord')";

    try (
      Connection connection = Api.getMysqlConnection();
      PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
      PreparedStatement insertStatement = connection.prepareStatement(insertQuery)
    ) {
      connection.setAutoCommit(false);

      deleteStatement.setInt(1, user_id);
      deleteStatement.executeUpdate();

      insertStatement.setInt(1, user_id);
      insertStatement.setString(2, code);
      insertStatement.executeUpdate();

      connection.commit();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
}
