package com.example.lostfound.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HandoverDao {
  private final JdbcTemplate jdbc;
  public HandoverDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public void insert(long claimId, long custodyItemId, String otp) {
    jdbc.update("INSERT INTO handovers(claim_id,custody_item_id,otp_code) VALUES (?,?,?)",
        claimId, custodyItemId, otp);
  }

  public record HRow(long claimId, long custodyItemId, String otp) {}

  private static final RowMapper<HRow> RM = (rs, i) -> new HRow(
      rs.getLong("claim_id"),
      rs.getLong("custody_item_id"),
      rs.getString("otp_code")
  );

  public List<HRow> listNotDelivered() {
    return jdbc.query("SELECT claim_id,custody_item_id,otp_code FROM handovers WHERE delivered_at IS NULL ORDER BY id DESC", RM);
  }

  public String otpByClaim(long claimId) {
    return jdbc.queryForObject("SELECT otp_code FROM handovers WHERE claim_id=?", String.class, claimId);
  }

  public void deliver(long claimId, String deliveredAt, long deliveredBy, String receiverName) {
    jdbc.update("UPDATE handovers SET delivered_at=?, delivered_by=?, receiver_name=? WHERE claim_id=?",
        deliveredAt, deliveredBy, receiverName, claimId);
    jdbc.update("UPDATE custody_items SET current_status='CLOSED' WHERE id=(SELECT custody_item_id FROM handovers WHERE claim_id=?)", claimId);
  }
}
