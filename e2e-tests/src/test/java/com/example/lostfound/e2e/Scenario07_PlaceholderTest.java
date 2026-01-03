package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

public class Scenario07_PlaceholderTest extends BaseE2ETest {

    @Test
    void admin_approves_claim_and_found_status_becomes_claimed() {
        login("staff@demo.com", "staff123");
        byId("linkFoundNew").click();
        String title = unique("FOUND_NOTEBOOK");
        byId("title").sendKeys(title);
        byId("category").sendKeys("NOTEBOOK");
        byId("location").sendKeys("LAB");
        byId("status").sendKeys("IN_CUSTODY");
        byId("btnFoundCreate").click();

        open("/found/list");
        long foundId = findFoundIdByTitle(title);
        logoutIfPossible();

        login("user@demo.com", "user123");
        open("/claim/new/" + foundId);
        byId("proofText").sendKeys("There is a sticker on the back.");
        byId("btnClaimCreate").click();
        logoutIfPossible();

        login("admin@demo.com", "admin123");
        open("/admin/claims");

        String claimIdXpath = "//tr[td[normalize-space()='" + foundId + "']]/td[1]";
        long claimId = Long.parseLong(text(By.xpath(claimIdXpath)).trim());

        String approveBtnId = "btnApprove_" + claimId;
        byId(approveBtnId).click();

        open("/admin/claims");
        assertFalse(existsById(approveBtnId));

        open("/found/list");
        String statusXpath = "//tr[td[normalize-space()='" + foundId + "']]/td[5]";
        String status = text(By.xpath(statusXpath));
        assertTrue(status.contains("CLAIMED"));
    }
}
