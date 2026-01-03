package com.example.lostfound.service;

import com.example.lostfound.repo.AuditRepository;
import com.example.lostfound.repo.HandoverDao;
import com.example.lostfound.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HandoverService {
  private final HandoverDao handovers;
  private final AuditRepository audit;

  public HandoverService(HandoverDao handovers, AuditRepository audit) {
    this.handovers = handovers;
    this.audit = audit;
  }

  public List<HandoverDao.HRow> listNotDelivered() {
    return handovers.listNotDelivered();
  }

  public void deliver(long staffId, long claimId, String otpInput, String receiverName) {
    String realOtp = handovers.otpByClaim(claimId);
    if (otpInput == null || !otpInput.equals(realOtp)) throw new IllegalArgumentException("otp_invalid");
    if (receiverName == null || receiverName.isBlank()) throw new IllegalArgumentException("receiver_required");
    String now = TimeUtil.nowIso();
    handovers.deliver(claimId, now, staffId, receiverName);
    audit.log(staffId, "HANDOVER_DELIVERED", "handovers", claimId, now);
  }
}
