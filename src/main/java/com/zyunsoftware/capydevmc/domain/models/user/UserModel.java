package com.zyunsoftware.capydevmc.domain.models.user;

import com.zyunsoftware.capydevmc.core.Model;

public class UserModel extends Model {
  public int id = -1;
  public String nickname = "";
  public String password = null;
  public String firstname = null;
  public String lastname = null;
  public String biography = null;
  public int balance = 0;
  public int credits = 0;
  public Integer x = null;
  public Integer y = null;
  public Integer z = null;
  public boolean approved = false;
}
