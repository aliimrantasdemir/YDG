package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class Scenario08_PlaceholderTest extends BaseE2ETest {

    @Test
    void staff_delivers_item_using_otp() {
        // 1) Staff creates FOUND
        logoutIfPossible();
        login("staff@demo.com", "staff123");

        open("/found/new");

        String title = unique("FOUND_CARD_");

        String titleId = firstPresentEditableId(10, "title", "foundTitle");
        String catId   = firstPresentEditableId(10, "category", "foundCategory");
        String locId   = firstPresentEditableId(10, "location", "foundLocation");

        // status bazı projelerde var, bazılarında yok -> varsa set et
        String statusId = null;
        try { statusId = firstPresentEditableId(2, "status", "foundStatus"); } catch (Exception ignored) {}

        // date/shelf varsa validation yemesin diye doldur (yoksa try-catch ile geçer)
        String dateId = null;
        try { dateId = firstPresentEditableId(2, "dateFound", "foundDate", "date_found"); } catch (Exception ignored) {}
        String shelfId = null;
        try { shelfId = firstPresentEditableId(2, "shelfCode", "shelf_code"); } catch (Exception ignored) {}

        setById(titleId, title);

        // Backend enum bekliyorsa bunlar genelde çalışır
        setById(catId, "CARD");
        setById(locId, "OFFICE");
        if (statusId != null) setById(statusId, "IN_CUSTODY");

        if (dateId != null) {
            setById(dateId, LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        }
        if (shelfId != null) {
            setById(shelfId, "B-08");
        }

        // Submit: önce id ile dene, yoksa Create text, yoksa form submit
        boolean clicked = false;
        try {
            driver.findElement(By.id("btnFoundCreate")).click();
            clicked = true;
        } catch (Exception ignored) {}
        if (!clicked) {
            try { clickByText("Create"); clicked = true; } catch (Exception ignored) {}
        }
        if (!clicked) {
            clickSubmitInSameFormOf(titleId);
        }

        // Hâlâ /found/new ise create olmamıştır
        if (driver.getCurrentUrl().contains("/found/new")) {
            dumpHtmlAndPng("sc08_found_still_new");
            logHtml5ValidationMessagesInSameFormOf(titleId);
            logInvalidFieldsOf(titleId);
            fail("Found create sonrası hala /found/new. VisibleErrors=" + visibleErrors());
        }

        long foundId = waitDbFoundIdByTitle(title, 30);
        if (foundId <= 0) {
            dumpHtmlAndPng("sc08_found_id_not_in_db");
            fail("DB’de FOUND kaydı bulunamadı (id çekilemedi). title=" + title);
        }

        // 2) User creates CLAIM
        logoutIfPossible();
        login("user@demo.com", "user123");

        open("/claim/new/" + foundId);

        String proofId;
        try {
            proofId = firstPresentEditableId(10, "proofText", "proof", "claimProof");
        } catch (TimeoutException te) {
            dumpHtmlAndPng("sc08_claim_proof_not_found");
            throw te;
        }

        setById(proofId, "It has my name written with pen.");

        try {
            driver.findElement(By.id("btnClaimCreate")).click();
        } catch (Exception e) {
            try { clickByText("Create"); }
            catch (Exception ex) { clickSubmitInSameFormOf(proofId); }
        }

        // 3) Admin approves CLAIM
        logoutIfPossible();
        login("admin@demo.com", "admin123");

        open("/admin/claims");

        String claimIdXpath = "//tr[td[normalize-space()='" + foundId + "']]/td[1]";
        long claimId;
        try {
            claimId = Long.parseLong(text(By.xpath(claimIdXpath)).trim());
        } catch (Exception e) {
            dumpHtmlAndPng("sc08_claim_id_not_found");
            fail("ClaimId bulunamadı/parse edilemedi. foundId=" + foundId + " URL=" + driver.getCurrentUrl());
            return;
        }

        try {
            byId("btnApprove_" + claimId).click();
        } catch (Exception e) {
            dumpHtmlAndPng("sc08_approve_btn_missing");
            fail("Approve butonu bulunamadı. claimId=" + claimId + " URL=" + driver.getCurrentUrl());
            return;
        }

        // 4) Staff delivers using OTP
        logoutIfPossible();
        login("staff@demo.com", "staff123");

        open("/staff/handovers");

        String otp = readOtpFromRow(claimId);
        if (otp == null || otp.isBlank()) {
            dumpHtmlAndPng("sc08_otp_not_found");
            fail("OTP sayfada bulunamadı. claimId=" + claimId + " URL=" + driver.getCurrentUrl());
            return;
        }

        // otp ve receiver input id’leri projeye göre değişebilir -> olası id’lerle bul
        String otpInputId = firstPresentEditableId(8,
                "otp_" + claimId, "otp", "handoverOtp", "otpCode_" + claimId);

        String receiverId = firstPresentEditableId(8,
                "receiver_" + claimId, "receiver", "handoverReceiver", "deliveredTo_" + claimId);

        setById(otpInputId, otp);
        setById(receiverId, "Ali Imran");

        // deliver butonu
        boolean deliveredClicked = false;
        try {
            byId("btnDeliver_" + claimId).click();
            deliveredClicked = true;
        } catch (Exception ignored) {}
        if (!deliveredClicked) {
            try { clickByText("Deliver"); deliveredClicked = true; } catch (Exception ignored) {}
        }
        if (!deliveredClicked) {
            dumpHtmlAndPng("sc08_deliver_btn_missing");
            fail("Deliver butonu bulunamadı. claimId=" + claimId + " URL=" + driver.getCurrentUrl());
            return;
        }

        // tekrar listele -> deliver butonu kalkmış olmalı
        open("/staff/handovers");
        assertFalse(existsById("btnDeliver_" + claimId),
                "Deliver sonrası buton hâlâ görünüyor. claimId=" + claimId + " URL=" + driver.getCurrentUrl());
    }

    // -------- helpers --------

    /** DB’den title -> foundId çekip poll eder */
    private long waitDbFoundIdByTitle(String title, int totalSeconds) {
        long end = System.currentTimeMillis() + totalSeconds * 1000L;
        int tryNo = 0;

        while (System.currentTimeMillis() < end) {
            tryNo++;
            try {
                long id = dbFindFoundIdByTitle(title);
                System.out.println("[E2E][DB][FOUND_ID] try=" + tryNo + " title=" + title + " => id=" + id);
                if (id > 0) return id;
            } catch (Exception e) {
                System.out.println("[E2E][DB][FOUND_ID] check failed: " + e.getMessage());
            }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
        }
        return -1;
    }

    private long dbFindFoundIdByTitle(String title) throws Exception {
        String url = "jdbc:sqlite:" + dbPath();

        try (Connection con = DriverManager.getConnection(url)) {
            // en olası tablo
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id FROM found_reports WHERE title = ? ORDER BY id DESC LIMIT 1")) {
                ps.setString(1, title);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            } catch (SQLException ignored) {}

            // fallback: found içeren tabloları tara
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT name FROM sqlite_master " +
                            "WHERE type='table' AND name NOT LIKE 'sqlite_%' AND lower(name) LIKE '%found%'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String table = rs.getString(1);

                        String idCol = null;
                        String titleCol = null;

                        try (Statement st = con.createStatement();
                             ResultSet crs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
                            while (crs.next()) {
                                String col = crs.getString("name");
                                String lc = col.toLowerCase();
                                if (idCol == null && (lc.equals("id") || lc.endsWith("_id"))) idCol = col;
                                if (titleCol == null && (lc.equals("title") || lc.contains("title") || lc.equals("name"))) titleCol = col;
                            }
                        } catch (SQLException ignored2) {}

                        if (idCol == null || titleCol == null) continue;

                        String q = "SELECT " + idCol + " FROM " + table + " WHERE " + titleCol + "=? ORDER BY " + idCol + " DESC LIMIT 1";
                        try (PreparedStatement ps2 = con.prepareStatement(q)) {
                            ps2.setString(1, title);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) return rs2.getLong(1);
                            }
                        } catch (SQLException ignored2) {}
                    }
                }
            }
        }

        return -1;
    }

    /** Handover satırından OTP’yi okumaya çalışır (code varsa ordan; yoksa 4-8 haneli sayı regex’i) */
    private String readOtpFromRow(long claimId) {
        String rowXpath = "//tr[td[normalize-space()='" + claimId + "']]";

        // 1) <code> içinde ise
        try {
            String otp = text(By.xpath(rowXpath + "//code")).trim();
            if (!otp.isBlank()) return otp;
        } catch (Exception ignored) {}

        // 2) class/id içinde otp geçen elementlerden
        try {
            String otp = text(By.xpath(rowXpath + "//*[contains(@class,'otp') or contains(@id,'otp')]")).trim();
            if (!otp.isBlank()) return otp;
        } catch (Exception ignored) {}

        // 3) satır text’inden regex ile çek
        try {
            String rowText = driver.findElement(By.xpath(rowXpath)).getText();
            Matcher m = Pattern.compile("\\b\\d{4,8}\\b").matcher(rowText);
            if (m.find()) return m.group();
        } catch (NoSuchElementException ignored) {}

        return null;
    }
}
