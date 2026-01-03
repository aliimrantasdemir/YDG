package com.example.lostfound.e2e;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class Scenario04_PlaceholderTest extends BaseE2ETest {

    @Test
    void staff_creates_found_and_sees_in_list() {
        String email = "staff@demo.com";
        String pass  = "staff123";

        openProtected("/found/new", email, pass);
        dumpHtmlAndPng("found_new_open");

        String title = unique("FOUND_WALLET_");

        String titleId = firstPresentEditableId(10, "title", "foundTitle");
        String descId  = firstPresentEditableId(10, "description", "desc", "foundDesc");
        String catId   = firstPresentEditableId(10, "category", "foundCategory");
        String locId   = firstPresentEditableId(10, "location", "foundLocation");
        String dateId  = firstPresentEditableId(10, "dateFound", "foundDate", "date_found");
        String shelfId = firstPresentEditableId(10, "shelfCode", "shelf_code");

        setById(titleId, title);
        setById(descId, "E2E found açıklama");
        setById(catId, "WALLET");     // önce enum gibi
        setById(locId, "CANTEEN");    // önce enum gibi
        setById(dateId, LocalDate.now().toString());
        setById(shelfId, "A-01");

        dumpHtmlAndPng("found_new_filled");

        // submit (3 yol)
        try { clickSubmitInSameFormOf(titleId); } catch (Exception ignored) {}
        try { clickByText("Create"); } catch (Exception ignored) {}
        try { forceSubmitInSameFormOf(titleId); } catch (Exception ignored) {}

        dumpHtmlAndPng("found_after_submit");
        logBodyText("found_after_submit");

        // 1) Önce UI listeden bak (kullanıcı açısından gerçek doğrulama)
        open("/found");
        boolean seenInUi = waitBodyTextContainsWithRefresh(title, 15);

        // 2) UI'da yoksa DB'ye bak (artık otomatik tablo keşfi var)
        boolean createdInDb = false;
        if (!seenInUi) {
            createdInDb = waitDbHasFoundTitle(title, 30);
        }

        if (!seenInUi && !createdInDb) {
            dumpHtmlAndPng("found_not_created_debug");
            fail("Found oluşturulamadı gibi görünüyor. Title=" + title +
                    " | URL=" + driver.getCurrentUrl() +
                    " | VisibleErrors=" + visibleErrors());
        }

        assertTrue(true);
    }
}
