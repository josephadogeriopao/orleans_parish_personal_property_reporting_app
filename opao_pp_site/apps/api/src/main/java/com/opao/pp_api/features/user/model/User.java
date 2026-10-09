package com.opao.pp_api.features.user.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.opao.pp_api.features.user_role.model.UserRole;
import com.opao.pp_api.features.user_status.model.UserStatus;



@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String fullName;      
    private String email;         
    private String phoneNumber;    
    private String password;
    private boolean isActive;
    
    // Added missing relation destination slots for MapStruct flattening
    private UserRole userRoleId;   
    private UserStatus userStatusId; 

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.opao.pp_api.features.user.model.User[ id=" + id + " ]";
    }
}
