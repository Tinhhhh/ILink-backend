package com.exe201.ilink.Util;

import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.entity.Role;
import com.exe201.ilink.model.enums.RoleName;
import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class AccountSpecification {
    public Specification<Account> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public Specification<Account> hasPhone(String phone) {
        return (root, query, cb) -> {
            if (phone == null) return null;

            return cb.like(cb.lower(root.get("phone")),
                "%" + phone.toLowerCase() + "%"
            );
        };
    }

    public Specification<Account> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null) return null;

            return cb.like(
                cb.concat(cb.concat(cb.lower(root.get("firstName")), " "), cb.lower(root.get("lastName"))),
                "%" + name.toLowerCase() + "%"
            );
        };
    }

    public Specification<Account> notAdmin() {
        return (root, query, cb) -> {

            Join<Account, Role> role = root.join("role");
            return cb.notEqual((role.get("roleName")), RoleName.ADMIN.getRoleName());
        };
    }

    public Specification<Account> hasRole(String role) {
        return (root, query, cb) -> {
            String upperRole;
            if (role != null) {
                upperRole = role.toUpperCase();
            } else {
                return null;
            }
            if (!RoleName.contains(upperRole)) return null;
            Join<Account, Role> roleJoin = root.join("role");
            return cb.equal(roleJoin.get("roleName"), upperRole);
        };
    }
}

