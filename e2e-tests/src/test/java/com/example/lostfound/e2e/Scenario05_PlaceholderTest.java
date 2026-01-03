package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

public class Scenario05_PlaceholderTest extends BaseE2ETest {

    @Test
    void filter_found_list_by_category_and_location() {
        login("staff@demo.com", "staff123");

        byId("linkFoundNew").click();
        String t1 = unique("FOUND_PHONE");
        byId("title").sendKeys(t1);
        byId("category").sendKeys("PHONE");
        byId("location").sendKeys("LIBRARY");
        byId("status").sendKeys("IN_CUSTODY");
        byId("btnFoundCreate").click();

        byId("btnFoundNew").click();
        String t2 = unique("FOUND_KEYS");
        byId("title").sendKeys(t2);
        byId("category").sendKeys("KEYS");
        byId("location").sendKeys("PARK");
        byId("status").sendKeys("IN_CUSTODY");
        byId("btnFoundCreate").click();

        open("/found/list");
        byId("queryCat").sendKeys("PHONE");
        byId("queryLoc").sendKeys("LIBRARY");
        byId("btnSearch").click();

        assertTrue(driver.findElements(By.xpath("//tr[td[normalize-space()='" + t1 + "']]")).size() > 0);
        assertEquals(0, driver.findElements(By.xpath("//tr[td[normalize-space()='" + t2 + "']]")).size());
    }
}
