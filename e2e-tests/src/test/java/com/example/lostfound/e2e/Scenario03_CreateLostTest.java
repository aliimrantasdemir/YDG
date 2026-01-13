package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Scenario03_CreateLostTest extends BaseE2ETest {

    @Test
    void staff_creates_lost_post_and_db_has_it() {
        String email = "user@demo.com";
        String pass  = "user123";

        login(email, pass);
        openProtected("/lost/new", email, pass);
        dumpHtmlAndPng("lost_new_open");

        String title = unique("cuzdan");

        setById("lostTitle", title);
        setById("lostDesc", "E2E test açıklama");
        setById("lostCategory", "Cüzdan");
        setById("lostLocation", "Kampüs");
        setIfEmptyById("lostDate", LocalDate.now().toString());

        dumpHtmlAndPng("lost_new_filled");

        clickSubmitInSameFormOf("lostTitle");

        ensureLoggedIn(email, pass);
        dumpHtmlAndPng("after_submit");
        dumpUsers();
        dumpDbSchemaOnce();
        logBodyText("after_submit");
        boolean created = waitDbHasLostTitle(title, 30);


        assertTrue(created,
                "Create DB’de doğrulanamadı. Title bulunamadı: " + title +
                        " | URL=" + driver.getCurrentUrl() +
                        " | VisibleErrors=" + visibleErrors() +
                        " | IsLoggedIn=" + isLoggedIn()
        );
    }
}
