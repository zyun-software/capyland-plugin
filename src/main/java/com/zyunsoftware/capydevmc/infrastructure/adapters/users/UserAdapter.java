package com.zyunsoftware.capydevmc.infrastructure.adapters.users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zyunsoftware.capydevmc.domain.models.user.UserEntity;
import com.zyunsoftware.capydevmc.domain.models.user.UserModel;
import com.zyunsoftware.capydevmc.domain.models.user.UserRepository;
import com.zyunsoftware.capydevmc.infrastructure.Api;

public class UserAdapter implements UserRepository {
  @Override
  public UserModel save(UserModel model) {
    try (Connection connection = Api.getMysqlConnection()) {
      UserModel userModel;
      if (model.id == -1) {
        String insertQuery = "INSERT INTO capyland_users (nickname, password, firstname, lastname, biography, balance, credits, x, y, z, approved) " +
          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

        userModel = _upsert(insertQuery, model);
      } else {
        String updateQuery = "UPDATE capyland_users " +
          "SET nickname = ?, password = ?, firstname = ?, lastname = ?, biography = ?, " +
          "balance = ?, credits = ?, x = ?, y = ?, z = ?, approved = ? " +
          "WHERE id = ? ";

        userModel = _upsert(updateQuery, model);
      }

      return userModel;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public UserEntity findByNickname(String value) {
    try (Connection connection = Api.getMysqlConnection()) {
      String selectQuery = "SELECT * FROM capyland_users WHERE nickname = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

      preparedStatement.setString(1, value);

      ResultSet resultSet = preparedStatement.executeQuery();

      if (resultSet.next()) {
        UserModel model = UserMapper.resultSetToModel(resultSet);
        UserEntity entity = new UserEntity(model, this);

        return entity;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  private UserModel _upsert(String query, UserModel model) throws SQLException {
    Connection connection = Api.getMysqlConnection();

    PreparedStatement preparedStatement = connection.prepareStatement(query);
    UserMapper.setUpsertPreparedStatement(preparedStatement, model);
    
    int affectedRows = preparedStatement.executeUpdate();

    if (affectedRows > 0) {
      UserEntity entity = findByNickname(model.nickname);
      if (entity != null) {
        return entity.getModel();
      }
    }

    return null;
  }
}
