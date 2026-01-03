package com.example.lostfound.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClaimDao {
  private final JdbcTemplate jdbc;
  public ClaimDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public boolean existsActive(long userId, long foundId) {
    Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM claims WHERE user_id=? AND found_report_id=? AND status IN ('PENDING','APPROVED')",
        Integer.class, userId, foundId);
    return n != null && n > 0;
  }

  public long insert(long userId, long foundId, String proofText, String status, String createdAt) {
    jdbc.update("INSERT INTO claims(user_id,found_report_id,proof_text,status,created_at) VALUES (?,?,?,?,?)",
        userId, foundId, proofText, status, createdAt);
    Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
    return id == null ? -1 : id;
  }

  public record ClaimRow(long id, long userId, long foundId, String status) {}

  private static final RowMapper<ClaimRow> RM = (rs, i) -> new ClaimRow(
      rs.getLong("id"),
      rs.getLong("user_id"),
      rs.getLong("found_report_id"),
      rs.getString("status")
  );

  public List<ClaimRow> listPending() {
    return jdbc.query("SELECT id,user_id,found_report_id,status FROM claims WHERE status='PENDING' ORDER BY id DESC", RM);
  }

  public ClaimRow find(long id) {
    return jdbc.queryForObject("SELECT id,user_id,found_report_id,status FROM claims WHERE id=?", RM, id);
  }

  public void decide(long id, String status, String decidedAt, long decidedBy) {
    jdbc.update("UPDATE claims SET status=?, decided_at=?, decided_by=? WHERE id=?", status, decidedAt, decidedBy, id);
  }
}
