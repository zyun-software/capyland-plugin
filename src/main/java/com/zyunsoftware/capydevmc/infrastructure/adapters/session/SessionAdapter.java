package com.zyunsoftware.capydevmc.infrastructure.adapters.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.zyunsoftware.capydevmc.domain.models.session.SessionRepository;
import com.zyunsoftware.capydevmc.infrastructure.Api;

public class SessionAdapter implements SessionRepository {
  private List<SessionRow> _sessions = new ArrayList<>();

  @Override
  public void load() {
    try (
      Connection connection = Api.getMysqlConnection();
      Statement statement = connection.createStatement()
    ) {
      _sessions.clear();

      String loadSql =
        "SELECT u.nickname, s.ip " + 
        "FROM capyland_sessions s " +
        "JOIN capyland_users u ON s.user_id = u.id " +
        "WHERE s.application = 'minecraft'";
      
      ResultSet resultSet = statement.executeQuery(loadSql);

      while (resultSet.next()) {
        SessionRow _row = SessionMapper.resultSetToRow(resultSet);
        _sessions.add(_row);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean has(String nickname, String ip) {
    for (SessionRow row : _sessions) {
      if (row.nickname.equals(nickname) && row.ip.equals(ip)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void create(String nickname, String ip) {
    try (Connection connection = Api.getMysqlConnection()) {
      String insertSql =
        "INSERT INTO capyland_sessions (user_id, ip, application) " + 
        "VALUES ((SELECT id FROM capyland_users WHERE nickname = ?), ?, 'minecraft')";

      PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
      preparedStatement.setString(1, nickname);
      preparedStatement.setString(2, ip);

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void remove(String nickname) {
    try (Connection connection = Api.getMysqlConnection()) {
      String removeSql =
        "DELETE s " + 
        "FROM capyland_sessions s " +
        "JOIN capyland_users u ON s.user_id = u.id " +
        "WHERE u.nickname = ? AND s.application = 'minecraft'";

      PreparedStatement preparedStatement = connection.prepareStatement(removeSql);
      preparedStatement.setString(1, nickname);

      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
