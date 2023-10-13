package com.zyunsoftware.capydevmc.infrastructure.adapters.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zyunsoftware.capydevmc.app.CapylandPlugin;
import com.zyunsoftware.capydevmc.domain.models.migration.MigrationRepository;
import com.zyunsoftware.capydevmc.infrastructure.Api;

public class MigrationAdapter implements MigrationRepository {
  private Logger _logger;

  public MigrationAdapter() {
    _logger = CapylandPlugin.getInstance().getLogger();
  }

  @Override
  public void migrate() {
    try (
      Connection connection = Api.getMysqlConnection();
      Statement statement = connection.createStatement()
    ) {
      String[] createTableSqls = {
        "CREATE TABLE IF NOT EXISTS capyland_users ("
        + "id INT AUTO_INCREMENT PRIMARY KEY,"
        + "nickname VARCHAR(16) UNIQUE NOT NULL,"
        + "password VARCHAR(32) NULL,"
        + "firstname VARCHAR(16) NULL,"
        + "lastname VARCHAR(16) NULL,"
        + "biography TEXT NULL,"
        + "balance INT NOT NULL,"
        + "credits INT NOT NULL,"
        + "x INT NULL,"
        + "y INT NULL,"
        + "z INT NULL,"
        + "approved BOOLEAN NOT NULL,"
        + "INDEX idx_nickname (nickname)"
        + ");",

        "CREATE TABLE IF NOT EXISTS capyland_sessions ("
        + "user_id INT NOT NULL,"
        + "ip VARCHAR(15) NOT NULL,"
        + "application VARCHAR(255) NOT NULL,"
        + "PRIMARY KEY (user_id, ip, application),"
        + "FOREIGN KEY (user_id) REFERENCES capyland_users(id),"
        + "INDEX idx_application (application)"
        + ");",

        "CREATE TABLE IF NOT EXISTS capyland_linked_accounts ("
        + "user_id INT NOT NULL,"
        + "code VARCHAR(255) NOT NULL,"
        + "name VARCHAR(255) NOT NULL,"
        + "PRIMARY KEY (user_id, code, name),"
        + "FOREIGN KEY (user_id) REFERENCES capyland_users(id)"
        + ");"
      };

      for (String createTableSql : createTableSqls) {
        statement.executeUpdate(createTableSql);  
      }

      _logger.info("Міграції виконано успішно");
    } catch (SQLException e) {
      _logger.log(Level.SEVERE, "Помилка під час виконання міграцій", e);
    }
  }
}
