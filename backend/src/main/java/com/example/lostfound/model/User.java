package com.example.lostfound.model;

public record User(long id, String name, String email, String passwordHash, Role role, String createdAt) {}
