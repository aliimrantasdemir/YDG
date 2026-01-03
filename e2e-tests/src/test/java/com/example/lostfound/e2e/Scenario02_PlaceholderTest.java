package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

public class Scenario02_PlaceholderTest extends BaseE2ETest {

    @Test
    void login_fail_shows_error() {
        open("/login");
        byId("email").sendKeys("staff@demo.com");
        byId("password").sendKeys("WRONGPASS");
        byId("btnLogin").click();

        String err = text(By.cssSelector("div.msg.err"));
        assertTrue(err.toLowerCase().contains("login failed"));
    }
}
