package com.opao.pp_api.features.user_change.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.opao.pp_api.features.user.model.UserEntity;
import com.opao.pp_api.features.user_change.constants.UserChangeConstants;
import com.opao.pp_api.features.user_change_type.model.UserChangeTypeEntity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data // Generates Getters, Setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a public no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Table(name = "user_change")
public class UserChangeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false) // Removed @NotNull to let MySQL handle database generation smoothly
    @Column(name = "USER_CHANGE_ID")
    private Integer userChangeId;

    @Basic(optional = false)
    @NotNull
    @Size(min = UserChangeConstants.VERIFICATION_CODE_MIN_LENGTH, max = UserChangeConstants.VERIFICATION_CODE_MAX_LENGTH)
    @Column(name = "VERIFICATION_CODE")
    private String verificationCode;

    @Basic(optional = false)
    @NotNull
    @Column(name = "INITIATED_TIME")  
    private LocalDateTime initiatedTime;

    @JoinColumn(name = "USER_CHANGE_TYPE_ID", referencedColumnName = "USER_CHANGE_TYPE_ID")
    @ManyToOne(optional = false)
    private UserChangeTypeEntity userChangeTypeId;

    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private UserEntity userId;

    /**
     * 💡 JPA Lifecycle Hook
     * Guarantees that initiatedTime is never null right before saving, 
     * bypassing any Lombok constructor assignment issues.
     */
    @PrePersist
    protected void onCreate() {
        if (this.initiatedTime == null) {
            this.initiatedTime = LocalDateTime.now();
        }
    }

    // Secondary constructors kept for legacy compatibility if called elsewhere in your project
    public UserChangeEntity(Integer userChangeId) {
        this.userChangeId = userChangeId;
    }

    public UserChangeEntity(Integer userChangeId, String verificationCode, LocalDateTime initiatedTime) {
        this.userChangeId = userChangeId;
        this.verificationCode = verificationCode;
        this.initiatedTime = initiatedTime;
    }

        @Override
    public int hashCode() {
        int hash = 0;
        hash += (userChangeId != null ? userChangeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserChangeEntity)) {
            return false;
        }
        UserChangeEntity other = (UserChangeEntity) object;
        if ((this.userChangeId == null && other.userChangeId != null) || (this.userChangeId != null && !this.userChangeId.equals(other.userChangeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.opao.pp_api.features.user_change.model.UserChangeEntity[ userChangeId=" + userChangeId + " ]";
    }
    
}
