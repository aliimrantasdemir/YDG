package com.example.lostfound.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FoundDao {
  private final JdbcTemplate jdbc;
  public FoundDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public long insertFound(long staffId, String title, String description, String category, String location, String dateFound, String status, String createdAt) {
    jdbc.update("INSERT INTO found_reports(staff_id,title,description,category,location,date_found,status,created_at) VALUES (?,?,?,?,?,?,?,?)",
        staffId, title, description, category, location, dateFound, status, createdAt);
    Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
    return id == null ? -1 : id;
  }

  public void insertCustody(long foundId, String shelfCode, String intakeAt, String currentStatus) {
    jdbc.update("INSERT INTO custody_items(found_report_id,shelf_code,intake_at,current_status) VALUES (?,?,?,?)",
        foundId, shelfCode, intakeAt, currentStatus);
  }

  public record FoundRow(long id, String title, String category, String location, String status) {}

  private static final RowMapper<FoundRow> RM = (rs, i) -> new FoundRow(
      rs.getLong("id"),
      rs.getString("title"),
      rs.getString("category"),
      rs.getString("location"),
      rs.getString("status")
  );

  public List<FoundRow> list(String category, String location) {
    StringBuilder sql = new StringBuilder("SELECT id,title,category,location,status FROM found_reports WHERE 1=1");
    List<Object> params = new ArrayList<>();
    if (category != null && !category.isBlank()) { sql.append(" AND category=?"); params.add(category); }
    if (location != null && !location.isBlank()) { sql.append(" AND location LIKE ?"); params.add("%"+location+"%"); }
    sql.append(" ORDER BY id DESC");
    return jdbc.query(sql.toString(), RM, params.toArray());
  }

  public void updateFoundStatus(long foundId, String status) {
    jdbc.update("UPDATE found_reports SET status=? WHERE id=?", status, foundId);
  }

  public long custodyIdByFound(long foundId) {
    Long id = jdbc.queryForObject("SELECT id FROM custody_items WHERE found_report_id=?", Long.class, foundId);
    return id == null ? -1 : id;
  }
}
