package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

public class Scenario10_PlaceholderTest extends BaseE2ETest {

    @Test
    void claim_requires_proof_and_duplicate_is_blocked() {
        login("staff@demo.com", "staff123");
        byId("linkFoundNew").click();
        String title = unique("FOUND_USB");
        byId("title").sendKeys(title);
        byId("category").sendKeys("USB");
        byId("location").sendKeys("CLASS");
        byId("status").sendKeys("IN_CUSTODY");
        byId("btnFoundCreate").click();
        open("/found/list");
        long foundId = findFoundIdByTitle(title);
        logoutIfPossible();

        login("user@demo.com", "user123");
        open("/claim/new/" + foundId);
        byId("proofText").sendKeys("   ");
        byId("btnClaimCreate").click();
        String err1 = text(By.cssSelector("div.msg.err")).trim();
        assertTrue(err1.contains("proof_required"));

        byId("proofText").clear();
        byId("proofText").sendKeys("My USB has a blue cap.");
        byId("btnClaimCreate").click();

        open("/claim/new/" + foundId);
        byId("proofText").sendKeys("Trying again");
        byId("btnClaimCreate").click();
        String err2 = text(By.cssSelector("div.msg.err")).trim();
        assertTrue(err2.contains("duplicate_claim"));
    }
}
