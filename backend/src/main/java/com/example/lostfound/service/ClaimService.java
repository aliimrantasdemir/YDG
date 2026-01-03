package com.example.lostfound.service;

import com.example.lostfound.repo.*;
import com.example.lostfound.util.OtpUtil;
import com.example.lostfound.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClaimService {
  private final ClaimDao claims;
  private final FoundDao found;
  private final HandoverDao handovers;
  private final AuditRepository audit;

  public ClaimService(ClaimDao claims, FoundDao found, HandoverDao handovers, AuditRepository audit) {
    this.claims = claims;
    this.found = found;
    this.handovers = handovers;
    this.audit = audit;
  }

  public long createClaim(long userId, long foundId, String proofText) {
    if (proofText == null || proofText.isBlank()) throw new IllegalArgumentException("proof_required");
    if (claims.existsActive(userId, foundId)) throw new IllegalStateException("duplicate_claim");
    long id = claims.insert(userId, foundId, proofText, "PENDING", TimeUtil.nowIso());
    audit.log(userId, "CLAIM_CREATED", "claims", id, TimeUtil.nowIso());
    return id;
  }

  public List<ClaimDao.ClaimRow> listPending() {
    return claims.listPending();
  }

  public void approve(long adminId, long claimId) {
    ClaimDao.ClaimRow c = claims.find(claimId);
    if (!"PENDING".equals(c.status())) throw new IllegalStateException("not_pending");
    String now = TimeUtil.nowIso();
    claims.decide(claimId, "APPROVED", now, adminId);

    long custodyId = found.custodyIdByFound(c.foundId());
    String otp = OtpUtil.otp6();
    handovers.insert(claimId, custodyId, otp);

    found.updateFoundStatus(c.foundId(), "CLAIMED");
    audit.log(adminId, "CLAIM_APPROVED", "claims", claimId, now);
  }

  public void reject(long adminId, long claimId) {
    ClaimDao.ClaimRow c = claims.find(claimId);
    if (!"PENDING".equals(c.status())) throw new IllegalStateException("not_pending");
    String now = TimeUtil.nowIso();
    claims.decide(claimId, "REJECTED", now, adminId);
    audit.log(adminId, "CLAIM_REJECTED", "claims", claimId, now);
  }
}
