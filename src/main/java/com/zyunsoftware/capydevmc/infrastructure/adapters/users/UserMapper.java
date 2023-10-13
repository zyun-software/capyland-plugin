package com.zyunsoftware.capydevmc.infrastructure.adapters.users;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zyunsoftware.capydevmc.domain.models.user.UserModel;

public class UserMapper {
  public static UserModel resultSetToModel(ResultSet resultSet) throws SQLException {
    UserModel row = new UserModel();

    row.id = resultSet.getInt("id");
    row.nickname = resultSet.getString("nickname");
    row.password = resultSet.getString("password");
    row.firstname = resultSet.getString("firstname");
    row.lastname = resultSet.getString("lastname");
    row.biography = resultSet.getString("biography");
    row.balance = resultSet.getInt("balance");
    row.credits = resultSet.getInt("credits");
    if (resultSet.getObject("x") != null) {
      row.x = resultSet.getInt("x");
    }
    if (resultSet.getObject("y") != null) {
      row.y = resultSet.getInt("y");
    }
    if (resultSet.getObject("z") != null) {
      row.z = resultSet.getInt("z");
    }
    row.approved = resultSet.getBoolean("approved");

    return row;
  }

  public static void setUpsertPreparedStatement(
    PreparedStatement preparedStatement,
    UserModel model
  ) throws SQLException {
    preparedStatement.setString(1, model.nickname);
    preparedStatement.setString(2, model.password);
    preparedStatement.setString(3, model.firstname);
    preparedStatement.setString(4, model.lastname);
    preparedStatement.setString(5, model.biography);
    preparedStatement.setInt(6, model.balance);
    preparedStatement.setInt(7, model.credits);
    preparedStatement.setObject(8, model.x);
    preparedStatement.setObject(9, model.y);
    preparedStatement.setObject(10, model.z);
    preparedStatement.setBoolean(11, model.approved);
    if (model.id != -1) {
      preparedStatement.setInt(12, model.id);
    }
  }
}
