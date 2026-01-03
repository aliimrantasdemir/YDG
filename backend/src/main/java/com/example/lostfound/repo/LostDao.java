package com.example.lostfound.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LostDao {
  private final JdbcTemplate jdbc;
  public LostDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public long insert(long userId, String title, String description, String category, String location, String dateLost, String status, String createdAt) {
    jdbc.update("INSERT INTO lost_reports(user_id,title,description,category,location,date_lost,status,created_at) VALUES (?,?,?,?,?,?,?,?)",
        userId, title, description, category, location, dateLost, status, createdAt);
    Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
    return id == null ? -1 : id;
  }

  public record LostRow(long id, String title, String status, String location, String dateLost) {}

  private static final RowMapper<LostRow> RM = (rs, i) -> new LostRow(
      rs.getLong("id"),
      rs.getString("title"),
      rs.getString("status"),
      rs.getString("location"),
      rs.getString("date_lost")
  );

  public List<LostRow> listByUser(long userId) {
    return jdbc.query("SELECT id,title,status,location,date_lost FROM lost_reports WHERE user_id=? ORDER BY id DESC", RM, userId);
  }
}
