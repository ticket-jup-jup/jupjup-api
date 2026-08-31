package org.example.jubjubapi.user.entity;

public enum Role {
    USER;

    public String authority(){
        return "ROLE_" + name();
    }
}
