package com.example.lostfound.it;

import com.example.lostfound.LostFoundApplication;
import com.example.lostfound.model.Role;
import com.example.lostfound.repo.UserRepository;
import com.example.lostfound.util.TimeUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LostFoundApplication.class)
@ActiveProfiles("it")
class ClaimFlowIT {

  @Autowired JdbcTemplate jdbc;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;

  long adminId, staffId, userId;

  @BeforeEach
  void clean() {
    jdbc.update("DELETE FROM handovers");
    jdbc.update("DELETE FROM claims");
    jdbc.update("DELETE FROM custody_items");
    jdbc.update("DELETE FROM found_reports");
    jdbc.update("DELETE FROM lost_reports");
    jdbc.update("DELETE FROM audit_logs");
    jdbc.update("DELETE FROM users");

    adminId = users.insert("Admin","admin@x.com", encoder.encode("x"), Role.ADMIN, TimeUtil.nowIso());
    staffId = users.insert("Staff","staff@x.com", encoder.encode("x"), Role.STAFF, TimeUtil.nowIso());
    userId  = users.insert("User","user@x.com",  encoder.encode("x"), Role.USER,  TimeUtil.nowIso());
  }

  @Test
  void sqlite_is_working() {
    Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
    assertNotNull(n);
    assertEquals(3, n.intValue());
  }
}
