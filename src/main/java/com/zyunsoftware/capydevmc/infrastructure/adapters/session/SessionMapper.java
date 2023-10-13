package com.zyunsoftware.capydevmc.infrastructure.adapters.session;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionMapper {
  public static SessionRow resultSetToRow(ResultSet resultSet) throws SQLException {
    String nickname = resultSet.getString("nickname");
    String ip = resultSet.getString("ip");

    SessionRow row = new SessionRow(nickname, ip);

    return row;
  }
}
