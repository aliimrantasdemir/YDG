package com.example.lostfound.repo;

import com.example.lostfound.model.Role;
import com.example.lostfound.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
  private final JdbcTemplate jdbc;
  public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  private static final RowMapper<User> RM = (rs, i) -> new User(
      rs.getLong("id"),
      rs.getString("name"),
      rs.getString("email"),
      rs.getString("password_hash"),
      Role.valueOf(rs.getString("role")),
      rs.getString("created_at")
  );

  public Optional<User> findByEmail(String email) {
    List<User> list = jdbc.query("SELECT * FROM users WHERE email = ?", RM, email);
    return list.stream().findFirst();
  }

  public Optional<User> findById(long id) {
    List<User> list = jdbc.query("SELECT * FROM users WHERE id = ?", RM, id);
    return list.stream().findFirst();
  }

  public long insert(String name, String email, String passwordHash, Role role, String createdAt) {
    jdbc.update("INSERT INTO users(name,email,password_hash,role,created_at) VALUES (?,?,?,?,?)",
        name, email, passwordHash, role.name(), createdAt);
    Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
    return id == null ? -1 : id;
  }
}
