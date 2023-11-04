package com.zyunsoftware.capydevmc.domain.services;

import java.util.List;

import com.zyunsoftware.capydevmc.app.DependencyInjection;
import com.zyunsoftware.capydevmc.domain.models.api.ApiRepository;
import com.zyunsoftware.capydevmc.domain.models.linked_account.LinkedAccountRepository;
import com.zyunsoftware.capydevmc.domain.models.minecraft.MinecraftCoordinatesModel;
import com.zyunsoftware.capydevmc.domain.models.minecraft.MinecraftRepository;
import com.zyunsoftware.capydevmc.domain.models.session.SessionRepository;
import com.zyunsoftware.capydevmc.domain.models.user.UserEntity;
import com.zyunsoftware.capydevmc.domain.models.user.UserModel;
import com.zyunsoftware.capydevmc.domain.models.user.UserRepository;

public class AuthorizationService {
  private ApiRepository _apiRepository;
  private LinkedAccountRepository _linkedAccountRepository;
  private MinecraftRepository _minecraftRepository;
  private SessionRepository _sessionRepository;
  private UserRepository _userRepository;

  public AuthorizationService(
    ApiRepository apiRepository,
    LinkedAccountRepository linkedAccountRepository,
    MinecraftRepository minecraftRepository,
    SessionRepository sessionRepository,
    UserRepository userRepository
  ) {
    _apiRepository = apiRepository;
    _linkedAccountRepository = linkedAccountRepository;
    _minecraftRepository = minecraftRepository;
    _sessionRepository = sessionRepository;
    _userRepository = userRepository;
  }

  public MinecraftRepository getMinecraftRepository() {
    return _minecraftRepository;
  }

  public boolean audit() {
    _sessionRepository.load();
    String nickname = _minecraftRepository.getNickname();
    String ip = _minecraftRepository.getIp();
    boolean hasActiveSession = _sessionRepository.has(nickname, ip);
    if (!hasActiveSession) {
      return false;
    }

    return true;
  }

  public void discord() {
    String[] args = _minecraftRepository.getArgs();
    if (args.length != 2) {
      _minecraftRepository.showMessage("message.command.discord.args");
      return;
    }
    
    String code = args[0];
    String password = args[1];

    _sessionRepository.load();
    String nickname = _minecraftRepository.getNickname();
    String ip = _minecraftRepository.getIp();
    boolean hasActiveSession = _sessionRepository.has(nickname, ip);
    if (!hasActiveSession) {
      _minecraftRepository.showMessage("message.command.logout.unauthorized");
      return;
    }

    UserEntity userEntity = _userRepository.findByNickname(nickname);

    _minecraftRepository.setPassword(password);

    password = _minecraftRepository.getPassword();
    boolean auditPassword = userEntity.auditPassword(password);
    if (!auditPassword) {
      _minecraftRepository.showMessage("message.command.login.invalid-password");
      return;
    }

    UserModel model = userEntity.getModel();
    boolean linkDiscord = _linkedAccountRepository.linkDiscord(model.id, code);

    String text = "message.command.discord." + (linkDiscord ? "success" : "error");
    _minecraftRepository.showMessage(text);
  }

  public void setApproved(String nickname, boolean approved) {
    UserRepository userRepository = DependencyInjection.getUserRepository();
    UserEntity userEntity = userRepository.findByNickname(nickname);
    if (userEntity == null) {
      _minecraftRepository.showMessage("message.hidden-command.approve-player.not-found");
      return;
    }

    UserModel userModel = userEntity.getModel();
    if (userModel.approved == approved) {
      _minecraftRepository.showMessage("message.hidden-command.approve-player.not-changed");
      return;
    }

    userEntity.setApproved(approved);

    String message = "message.hidden-command.approve-player." + (approved ? "approved" : "canceled");
    _minecraftRepository.showMessage(message);
  }

  public void teleportToMain() {
    _sessionRepository.load();

    boolean inLobby = _minecraftRepository.inLobby();
    if (!inLobby) {
      return;
    }
     
    String nickname = _minecraftRepository.getNickname();
    String ip = _minecraftRepository.getIp();
    
    boolean hasActiveSession = _sessionRepository.has(nickname, ip);
    if (!hasActiveSession) {
      return;
    }
    
    UserEntity userEntity = _userRepository.findByNickname(nickname);
    if (userEntity == null) {
      return;
    }

    UserModel model = userEntity.getModel();
    if (!model.approved) {
      _minecraftRepository.showTitle("title.portal.unapproved", "", 0, 5, 0);
      return;
    }

    if (model.x == null || model.y == null || model.z == null) {
      MinecraftCoordinatesModel coordinates = _minecraftRepository.getMainWorldSpawnCoordinates();
      userEntity.setCoordinates(coordinates.x, coordinates.y, coordinates.z);
      model = userEntity.getModel();
    }

    _minecraftRepository.teleportToMain(model.x, model.y, model.z);
  }

  public void teleportToLobby() {
    boolean inLobby = _minecraftRepository.inLobby();
    if (inLobby) {
      _minecraftRepository.showTitle("title.lobby", "", 0, 5, 0);
      return;
    }
  
    String nickname = _minecraftRepository.getNickname();
    
    UserEntity userEntity = _userRepository.findByNickname(nickname);
    if (userEntity == null) {
      return;
    }

    _toLobby(userEntity);
  }

  public void register() {
    String[] args = _minecraftRepository.getArgs();

    if (args.length != 2) {
      _minecraftRepository.showMessage("message.command.register.args");
      return;
    }
    
    String password = args[0];
    String confirmPassword = args[1];

    if (!password.equals(confirmPassword)) {
      _minecraftRepository.showMessage("message.command.register.different-passwords");
      return;
    }

    _sessionRepository.load();
    String nickname = _minecraftRepository.getNickname();
    UserEntity userEntity = _userRepository.findByNickname(nickname);
    if (userEntity != null) {
      _minecraftRepository.showMessage(_minecraftRepository.getConfigString("message.command.register.already-registered"));
      return;
    }

    _minecraftRepository.setPassword(password);

    UserModel userModel = new UserModel();

    userModel.nickname = nickname;
    userModel.password = _minecraftRepository.getPassword();

    _userRepository.save(userModel);

    _minecraftRepository.setArgs(new String[] { password });

    login();
  }

  public void login() {
    String[] args = _minecraftRepository.getArgs();

    if (args.length != 1) {
      _minecraftRepository.showMessage("message.command.login.args");
      return;
    }

    _sessionRepository.load();
    String nickname = _minecraftRepository.getNickname();
    UserEntity userEntity = _userRepository.findByNickname(nickname);
    if (userEntity == null) {
      _minecraftRepository.showMessage("message.command.login.registration-is-required");
      return;
    }
    
    String ip = _minecraftRepository.getIp();
    boolean hasActiveSession = _sessionRepository.has(nickname, ip);

    if (hasActiveSession) {
      _minecraftRepository.showMessage(_minecraftRepository.getConfigString("message.command.login.already-authorized"));
      return;
    }

    _minecraftRepository.setPassword(args[0]);
    String password = _minecraftRepository.getPassword();
    boolean auditPassword = userEntity.auditPassword(password);

    if (!auditPassword) {
      _minecraftRepository.showMessage("message.command.login.invalid-password");
      return;
    }
    
    _sessionRepository.create(nickname, ip);

    _minecraftRepository.showMessage("message.command.login.success");

    controlSelected();
  }

  public void logout() {
    _sessionRepository.load();
    String nickname = _minecraftRepository.getNickname();
    String ip = _minecraftRepository.getIp();
    boolean hasActiveSession = _sessionRepository.has(nickname, ip);
    if (!hasActiveSession) {
      _minecraftRepository.showMessage("message.command.logout.unauthorized");
      return;
    }

    _sessionRepository.remove(nickname);

    controlSelected();
  }

  public void changePassword() {
    String[] args = _minecraftRepository.getArgs();

    if (args.length != 3) {
      _minecraftRepository.showMessage("message.command.change-password.args");
      return;
    }
    
    String newPassword = args[0];
    String confirmNewPassword = args[1];
    String oldPassword = args[2];

    if (!newPassword.equals(confirmNewPassword)) {
      _minecraftRepository.showMessage("message.command.register.different-passwords");
      return;
    }

    _sessionRepository.load();
    String nickname = _minecraftRepository.getNickname();
    String ip = _minecraftRepository.getIp();
    boolean hasActiveSession = _sessionRepository.has(nickname, ip);
    if (!hasActiveSession) {
      _minecraftRepository.showMessage(_minecraftRepository.getConfigString("message.command.logout.unauthorized"));
      return;
    }

    UserEntity userEntity = _userRepository.findByNickname(nickname);

    _minecraftRepository.setPassword(oldPassword);

    String password = _minecraftRepository.getPassword();
    boolean auditPassword = userEntity.auditPassword(password);
    if (!auditPassword) {
      _minecraftRepository.showMessage(_minecraftRepository.getConfigString("message.command.login.invalid-password"));
      return;
    }

    _minecraftRepository.setPassword(newPassword);
    password = _minecraftRepository.getPassword();

    userEntity.setPassword(password);

    _minecraftRepository.showMessage("message.command.change-password.changed");
  }

  public void controlAll() {
    _sessionRepository.load();
    List<String> list = _minecraftRepository.getOnlineNicknames();

    for (String nickname : list) {
      _control(nickname, false);
    }
  }

  public void controlSelected() {
    _sessionRepository.load();
    String nickname = _minecraftRepository.getNickname();
    _control(nickname, true);
  }

  private void _control(String nickname, boolean api) {
    _minecraftRepository.selectPlayer(nickname);

    boolean inLobby = _minecraftRepository.inLobby();
    // перевірка чи зареєстрований
    UserEntity userEntity = _userRepository.findByNickname(nickname);
    if (userEntity == null) {
      if (!inLobby) {
        _minecraftRepository.teleportToLobby();
      }

      _minecraftRepository.showTitle("title.register.text", "title.register.sub", 0, 5, 0);
      return;
    }

    // перевірка чи авторизований
    String ip = _minecraftRepository.getIp();
    boolean hasActiveSession = _sessionRepository.has(nickname, ip);

    if (!hasActiveSession) {
      if (!inLobby) {
        _toLobby(userEntity);
      }

      _minecraftRepository.showTitle("title.login.text", "title.login.sub", 0, 5, 0);

      if (api) {
        _apiRepository.sendAuthorizationRequest(nickname, ip);
      }

      return;
    } else if (inLobby) {
      _minecraftRepository.clearTitle();
    }

    if (!userEntity.getModel().approved && !inLobby) {
      _minecraftRepository.showTitle("title.portal.unapproved", "", 0, 5, 0);
      _toLobby(userEntity);
    }
  }

  private void _toLobby(UserEntity userEntity) {
    MinecraftCoordinatesModel coordinates = _minecraftRepository.getCoordinates();
    userEntity.setCoordinates(coordinates.x, coordinates.y, coordinates.z);
    _minecraftRepository.teleportToLobby();
  }
}
