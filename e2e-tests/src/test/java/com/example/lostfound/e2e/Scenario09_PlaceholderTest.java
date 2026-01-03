package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Scenario09_PlaceholderTest extends BaseE2ETest {

    @Test
    void user_cannot_access_staff_page_found_new() {
        login("user@demo.com", "user123");
        open("/found/new");
        String body = driver.getPageSource().toLowerCase();
        assertTrue(body.contains("403") || body.contains("forbidden") || body.contains("access is denied"));
    }
}
