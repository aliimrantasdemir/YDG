package com.example.lostfound.service;

import com.example.lostfound.repo.AuditRepository;
import com.example.lostfound.repo.LostDao;
import com.example.lostfound.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LostService {
  private final LostDao lost;
  private final AuditRepository audit;

  public LostService(LostDao lost, AuditRepository audit) {
    this.lost = lost;
    this.audit = audit;
  }

  public long create(long userId, String title, String description, String category, String location, String dateLost) {
    if (title == null || title.isBlank()) throw new IllegalArgumentException("title_required");
    if (description == null || description.isBlank()) throw new IllegalArgumentException("desc_required");
    long id = lost.insert(userId, title, description, category, location, dateLost, "OPEN", TimeUtil.nowIso());
    audit.log(userId, "LOST_CREATED", "lost_reports", id, TimeUtil.nowIso());
    return id;
  }

  public List<LostDao.LostRow> myLost(long userId) {
    return lost.listByUser(userId);
  }
}
