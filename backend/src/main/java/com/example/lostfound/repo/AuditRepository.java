package com.example.lostfound.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditRepository {
  private final JdbcTemplate jdbc;
  public AuditRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public void log(long actorUserId, String action, String entityType, long entityId, String createdAt) {
    jdbc.update("INSERT INTO audit_logs(actor_user_id, action, entity_type, entity_id, created_at) VALUES (?,?,?,?,?)",
        actorUserId, action, entityType, entityId, createdAt);
  }
}
