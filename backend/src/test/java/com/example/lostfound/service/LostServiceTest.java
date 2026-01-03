package com.example.lostfound.service;

import com.example.lostfound.repo.AuditRepository;
import com.example.lostfound.repo.LostDao;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class LostServiceTest {

  @Test
  void create_requires_title() {
    LostDao lostDao = Mockito.mock(LostDao.class);
    AuditRepository audit = Mockito.mock(AuditRepository.class);
    LostService svc = new LostService(lostDao, audit);

    assertThrows(IllegalArgumentException.class, () ->
        svc.create(1L, " ", "desc", "cat", "loc", "2025-12-27"));
  }
}
