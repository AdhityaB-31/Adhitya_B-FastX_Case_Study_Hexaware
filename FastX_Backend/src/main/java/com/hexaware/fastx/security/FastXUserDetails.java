package com.hexaware.fastx.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class FastXUserDetails implements UserDetails {

    private final String username;   // the email address used to log in
    private final String password;
    private final String role;       // stored internally as "ROLE_USER", "ROLE_BUS_OPERATOR", etc.
    private final Long entityId;     // userId (for User) or operatorId (for BusOperator)
    private final String displayName; // full name for User, company name for BusOperator

    public FastXUserDetails(String email, String password, String roleName, Long entityId, String displayName) {
        this.username = email;
        this.password = password;
        this.role = roleName.toUpperCase().startsWith("ROLE_")
                ? roleName.toUpperCase()
                : "ROLE_" + roleName.toUpperCase();
        this.entityId = entityId;
        this.displayName = displayName;
    }

    /**
     * Returns the primary-key ID of the logged-in account.
     * For ROLE_USER      → this is the userId from the `users` table.
     * For ROLE_BUS_OPERATOR → this is the operatorId from the `bus_operators` table.
     * Used in service-layer ownership checks (e.g. "can this user access booking X?").
     */
    public Long getEntityId() {
        return entityId;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;  // email is the username in FastX
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
