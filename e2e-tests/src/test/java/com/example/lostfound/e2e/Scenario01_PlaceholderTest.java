package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Scenario01_PlaceholderTest extends BaseE2ETest {

    @Test
    void login_success_staff_sees_dashboard() {
        login("staff@demo.com", "staff123");
        dumpHtmlAndPng("scenario01_after_login");
        assertTrue(isLoggedIn(), "Login başarısız. URL=" + driver.getCurrentUrl() + " | errors=" + visibleErrors());
    }

}
