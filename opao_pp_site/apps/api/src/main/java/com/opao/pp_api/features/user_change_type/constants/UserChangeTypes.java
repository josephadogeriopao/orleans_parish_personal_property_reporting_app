package com.opao.pp_api.features.user_change_type.constants;

import com.opao.pp_api.features.user_change_type.model.UserChangeType;

public final class UserChangeTypes {
    
    private UserChangeTypes() {}

    public static final UserChangeType ACTIVATE = new UserChangeType(1);
    public static final UserChangeType CHANGE_PASSWORD = new UserChangeType(2);
    public static final UserChangeType GET_USERNAME = new UserChangeType(3);
}
