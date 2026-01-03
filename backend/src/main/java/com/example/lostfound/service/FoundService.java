package com.example.lostfound.service;

import com.example.lostfound.repo.AuditRepository;
import com.example.lostfound.repo.FoundDao;
import com.example.lostfound.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoundService {
  private final FoundDao found;
  private final AuditRepository audit;

  public FoundService(FoundDao found, AuditRepository audit) {
    this.found = found;
    this.audit = audit;
  }

  public long createWithCustody(long staffId, String title, String description, String category, String location, String dateFound, String shelfCode) {
    if (title == null || title.isBlank()) throw new IllegalArgumentException("title_required");
    if (shelfCode == null || shelfCode.isBlank()) throw new IllegalArgumentException("shelf_required");
    String now = TimeUtil.nowIso();
    long foundId = found.insertFound(staffId, title, description, category, location, dateFound, "IN_CUSTODY", now);
    found.insertCustody(foundId, shelfCode, now, "ACTIVE");
    audit.log(staffId, "FOUND_CREATED", "found_reports", foundId, now);
    return foundId;
  }

  public List<FoundDao.FoundRow> list(String category, String location) {
    return found.list(category, location);
  }
}
