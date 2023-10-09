package com.zyunsoftware.capydevmc.infrastructure.adapters.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zyunsoftware.capydevmc.app.CapylandPlugin;
import com.zyunsoftware.capydevmc.domain.models.migration.MigrationRepository;
import com.zyunsoftware.capydevmc.infrastructure.utilities.ConfigUtility;

public class MigrationAdapter implements MigrationRepository {
  private Logger _logger;

  public MigrationAdapter() {
    _logger = CapylandPlugin.getInstance().getLogger();
  }

  @Override
  public void migrate() {
    try (Connection connection = DriverManager.getConnection(
      ConfigUtility.getMysqlUrl(),
      ConfigUtility.getMysqlUser(),
      ConfigUtility.getMysqlPassword()
    );
      Statement statement = connection.createStatement()) {
      String[] createTableSqls = {
        "CREATE TABLE IF NOT EXISTS capyland_users ("
        + "id INT AUTO_INCREMENT PRIMARY KEY,"
        + "name VARCHAR(255) UNIQUE NOT NULL,"
        + "password VARCHAR(32) NOT NULL,"
        + "balance INT NOT NULL,"
        + "reserve INT NOT NULL,"
        + "credits INT NOT NULL,"
        + "approved BOOLEAN NOT NULL,"
        + "description TEXT NULL,"
        + "INDEX idx_name (name)"
        + ");",

        "CREATE TABLE IF NOT EXISTS capyland_sessions ("
        + "user_id INT NOT NULL,"
        + "ip VARCHAR(15) NOT NULL,"
        + "application VARCHAR(255) NOT NULL,"
        + "approved BOOLEAN NOT NULL,"
        + "last_used DATETIME NULL,"
        + "PRIMARY KEY (user_id, ip, application),"
        + "FOREIGN KEY (user_id) REFERENCES capyland_users(id),"
        + "INDEX idx_approved (approved)"
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
