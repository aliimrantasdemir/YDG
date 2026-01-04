package com.example.lostfound.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class BaseE2ETest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    // ------------------ URL HELPERS ------------------

    protected String baseUrl() {
        String s = System.getProperty("baseUrl");
        if (s != null && !s.isBlank()) return s;

        String env = System.getenv("APP_BASE_URL");
        if (env != null && !env.isBlank()) return env;

        return "http://host.docker.internal:8082";
    }

    protected String seleniumUrl() {
        String s = System.getProperty("seleniumRemoteUrl");
        if (s != null && !s.isBlank()) return ensureWdHub(s);

        String env = System.getenv("SELENIUM_URL");
        if (env != null && !env.isBlank()) return ensureWdHub(env);

        return "http://localhost:4444/wd/hub";
    }

    private String ensureWdHub(String s) {
        s = s.trim();
        if (s.contains("/wd/hub")) return s;
        if (s.endsWith("/")) return s + "wd/hub";
        return s + "/wd/hub";
    }

    // ------------------ DB HELPERS ------------------

    protected String dbPath() {
        String p = System.getProperty("dbPath");
        if (p != null && !p.isBlank()) return normalizePath(p);

        String env1 = System.getenv("IT_DB_PATH");
        if (env1 != null && !env1.isBlank()) return normalizePath(env1);

        String env2 = System.getenv("DB_PATH");
        if (env2 != null && !env2.isBlank()) return normalizePath(env2);

        // stabil bilinen yol
        Path known = Paths.get("C:/lostfound-java-sqlite/data/lostfound.db");
        if (Files.exists(known)) return normalizePath(known.toString());

        // projede yukarı doğru ara
        Path found = findUpwards("data/lostfound.db", 8);
        if (found != null) return normalizePath(found.toString());

        return normalizePath(Paths.get("data/lostfound.db").toAbsolutePath().normalize().toString());
    }

    private String normalizePath(String p) {
        return p.replace("\\", "/");
    }

    private Path findUpwards(String relative, int maxDepth) {
        Path cur = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int i = 0; i <= maxDepth; i++) {
            Path candidate = cur.resolve(relative).normalize();
            if (candidate.toFile().exists()) return candidate;
            cur = cur.getParent();
            if (cur == null) break;
        }
        return null;
    }

    protected boolean dbHasLostTitle(String title) throws Exception {
        String url = "jdbc:sqlite:" + dbPath();
        try (var con = DriverManager.getConnection(url);
             var ps = con.prepareStatement("SELECT 1 FROM lost_reports WHERE title = ? LIMIT 1")) {
            ps.setString(1, title);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    protected boolean waitDbHasLostTitle(String title, int totalSeconds) {
        long end = System.currentTimeMillis() + totalSeconds * 1000L;
        int tryNo = 0;
        while (System.currentTimeMillis() < end) {
            tryNo++;
            try {
                boolean ok = dbHasLostTitle(title);
                System.out.println("[E2E][DB][LOST] try=" + tryNo + " title=" + title + " => " + ok);
                if (ok) return true;
            } catch (Exception e) {
                System.out.println("[E2E][DB][LOST] check failed: " + e.getMessage());
            }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    /**
     * Found tablosu/kolonu projede farklı isimlendirilmiş olabilir diye:
     * 1) found_reports(title) dener
     * 2) olmadı: adı "found" içeren tabloları bulur, title/name benzeri kolonlarda arar
     */
    protected boolean dbHasFoundTitle(String title) throws Exception {
        String url = "jdbc:sqlite:" + dbPath();
        try (Connection con = DriverManager.getConnection(url)) {

            // 1) en olası tablo
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM found_reports WHERE title = ? LIMIT 1")) {
                ps.setString(1, title);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return true;
                }
            } catch (SQLException ignored) {}

            // 2) found* tablolarını keşfet
            List<String> tables = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT name FROM sqlite_master " +
                            "WHERE type='table' AND name NOT LIKE 'sqlite_%' AND lower(name) LIKE '%found%'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) tables.add(rs.getString(1));
                }
            }

            for (String table : tables) {
                List<String> cols = new ArrayList<>();
                try (Statement st = con.createStatement();
                     ResultSet crs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
                    while (crs.next()) cols.add(crs.getString("name"));
                } catch (SQLException ignored) {}

                for (String col : cols) {
                    String c = col.toLowerCase();
                    if (!(c.equals("title") || c.equals("name") || c.contains("title"))) continue;

                    String q = "SELECT 1 FROM " + table + " WHERE " + col + " = ? LIMIT 1";
                    try (PreparedStatement ps = con.prepareStatement(q)) {
                        ps.setString(1, title);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) return true;
                        }
                    } catch (SQLException ignored) {}
                }
            }
            return false;
        }
    }

    protected boolean waitDbHasFoundTitle(String title, int totalSeconds) {
        long end = System.currentTimeMillis() + totalSeconds * 1000L;
        int tryNo = 0;

        while (System.currentTimeMillis() < end) {
            tryNo++;
            try {
                boolean ok = dbHasFoundTitle(title);
                System.out.println("[E2E][DB][FOUND] try=" + tryNo + " title=" + title + " => " + ok);
                if (ok) return true;
            } catch (Exception e) {
                System.out.println("[E2E][DB][FOUND] check failed: " + e.getMessage());
            }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    protected void dumpDbSchemaOnce() {
        try {
            String path = dbPath();
            String url = "jdbc:sqlite:" + path;

            System.out.println("[E2E][DB] SCHEMA DUMP dbPath=" + path);

            try (Connection con = DriverManager.getConnection(url)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String table = rs.getString(1);
                            System.out.println("[E2E][DB] table=" + table);

                            List<String> cols = new ArrayList<>();
                            try (Statement st = con.createStatement();
                                 ResultSet crs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
                                while (crs.next()) cols.add(crs.getString("name"));
                            } catch (SQLException ignored) {}

                            System.out.println("[E2E][DB]   cols=" + cols);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[E2E][DB] SCHEMA DUMP FAILED: " + e.getMessage());
        }
    }

    protected void dumpUsers() {
        try {
            String url = "jdbc:sqlite:" + dbPath();
            try (var con = DriverManager.getConnection(url);
                 var st = con.createStatement();
                 var rs = st.executeQuery("SELECT id,email,role FROM users ORDER BY id")) {
                System.out.println("[E2E][DB] USERS:");
                while (rs.next()) {
                    System.out.println("  id=" + rs.getLong("id") +
                            " email=" + rs.getString("email") +
                            " role=" + rs.getString("role"));
                }
            }
        } catch (Exception e) {
            System.out.println("[E2E][DB] dumpUsers failed: " + e.getMessage());
        }
    }

    // ------------------ SETUP / TEARDOWN ------------------

    @BeforeEach
    void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();

        // SSL sertifikası vs sorunlarında engel olmasın
        options.setAcceptInsecureCerts(true);

        // ✅ Chrome'un HTTP -> HTTPS zorlamasını KESİN kapat
        options.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1280,900",

                // SSL / sertifika (gerekirse)
                "--ignore-certificate-errors",
                "--allow-insecure-localhost",

                // 🔥 KRİTİK: HTTPS-first / HTTPS-only / otomatik upgrade kapansın
                "--disable-features=HttpsOnlyMode,HttpsFirstMode,HttpsUpgrades,AutomaticHttpsUpgrades,PreferHTTPS",

                // bazen ek yardımcı olur (zararı yok)
                "--test-type",
                "--disable-background-networking"
        );

        System.out.println("[E2E] seleniumUrl=" + seleniumUrl());
        System.out.println("[E2E] baseUrl=" + baseUrl());
        System.out.println("[E2E][DB] dbPath=" + dbPath());

        driver = new RemoteWebDriver(new URL(seleniumUrl()), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }




    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    // ------------------ AUTH HELPERS ------------------

    protected void logoutIfPossible() {
        try {
            var btns = driver.findElements(By.id("btnLogout"));
            if (!btns.isEmpty()) {
                btns.get(0).click();
                return;
            }
            open("/logout");
        } catch (Exception ignored) {}
    }
    // ✅ Scenario03 için gerekli: login'e düştüyse tekrar login ol
    protected void ensureLoggedIn(String email, String password) {
        if (onLoginPage()) {
            login(email, password);
        }
        if (onLoginPage()) {
            dumpHtmlAndPng("still_on_login_after_ensure");
            fail("ensureLoggedIn sonrası hala login sayfasında. URL=" + driver.getCurrentUrl()
                    + " | VisibleErrors=" + visibleErrors());
        }
    }

    protected void login(String email, String password) {
        open("/login");
        if (!onLoginPage()) return;

        WebElement emailEl = byId("email");
        WebElement passEl  = byId("password");

        emailEl.clear();
        emailEl.sendKeys(email);
        passEl.clear();
        passEl.sendKeys(password);

        byId("btnLogin").click();

        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d -> {
                        String url = d.getCurrentUrl();
                        if (url == null) return false;
                        if (url.contains("/login?error")) return true;
                        return !url.contains("/login");
                    });
        } catch (TimeoutException te) {
            dumpHtmlAndPng("login_timeout");
            throw te;
        }

        if (driver.getCurrentUrl().contains("/login?error")) {
            dumpHtmlAndPng("login_error");
            fail("Login başarısız görünüyor. URL=" + driver.getCurrentUrl() + " | VisibleErrors=" + visibleErrors());
        }
    }

    protected void openProtected(String path, String email, String pass) {
        open(path);

        if (onLoginPage()) {
            login(email, pass);
            open(path);
        }

        if (onLoginPage()) {
            dumpHtmlAndPng("still_on_login_" + path.replace("/", "_"));
            fail("Protected sayfa açılırken login'e düştü: " + path + " | URL=" + driver.getCurrentUrl());
        }
    }

    protected boolean onLoginPage() {
        String u = driver.getCurrentUrl();
        if (u != null && u.contains("/login")) return true;

        return !driver.findElements(By.id("btnLogin")).isEmpty()
                || !driver.findElements(By.id("email")).isEmpty();
    }

    // ✅ Scenario03 için gerekli
    protected boolean isLoggedIn() {
        return !onLoginPage();
    }

    // ------------------ NAV / FIND HELPERS ------------------

    protected void open(String path) {
        driver.get(baseUrl() + path);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    protected WebElement byId(String id) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        } catch (TimeoutException te) {
            dumpHtmlAndPng("timeout_" + id);
            throw te;
        }
    }

    protected boolean existsById(String id) {
        try { driver.findElement(By.id(id)); return true; }
        catch (NoSuchElementException e) { return false; }
    }

    // ✅ TEK TANE (duplicate yok)
    protected String xpathLiteral(String s) {
        if (s == null) return "''";
        if (!s.contains("'")) return "'" + s + "'";
        if (!s.contains("\"")) return "\"" + s + "\"";
        String[] parts = s.split("'");
        StringBuilder sb = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(", \"'\", ");
            sb.append("'").append(parts[i]).append("'");
        }
        sb.append(")");
        return sb.toString();
    }

    protected boolean existsByText(String exactText) {
        try {
            driver.findElement(By.xpath("//*[normalize-space()=" + xpathLiteral(exactText) + "]"));
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    protected WebElement clickByText(String exactText) {
        By by = By.xpath(
                "//a[normalize-space()=" + xpathLiteral(exactText) + "]" +
                        " | //button[normalize-space()=" + xpathLiteral(exactText) + "]" +
                        " | //input[( @type='submit' or @type='button' ) and normalize-space(@value)=" + xpathLiteral(exactText) + "]"
        );
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
        el.click();
        return el;
    }

    // ------------------ SMART INPUT SET ------------------

    protected void setById(String id, String value) {
        WebElement el = byId(id);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);

        String tag = safeLower(el.getTagName());
        String type = safeLower(el.getAttribute("type"));

        if ("select".equals(tag)) {
            selectByVisibleTextOrValue(el, value);
            return;
        }
        if ("input".equals(tag) && "date".equals(type)) {
            setDateByJs(el, value);
            return;
        }

        try { el.clear(); } catch (Exception ignored) {}
        try {
            el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            el.sendKeys(Keys.DELETE);
        } catch (Exception ignored) {}
        el.sendKeys(value);
    }

    protected void setIfEmptyById(String id, String value) {
        WebElement el = byId(id);
        String v = el.getAttribute("value");
        if (v == null || v.isBlank()) setById(id, value);
    }

    private void selectByVisibleTextOrValue(WebElement selectEl, String value) {
        Select sel = new Select(selectEl);
        try { sel.selectByVisibleText(value); return; } catch (Exception ignored) {}
        try { sel.selectByValue(value); return; } catch (Exception ignored) {}
        selectEl.sendKeys(value);
    }

    private void setDateByJs(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];" +
                        "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                input, value
        );
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    // ------------------ SUBMIT ------------------

    protected void clickSubmitInSameFormOf(String fieldId) {
        WebElement field = byId(fieldId);
        WebElement form = field.findElement(By.xpath("ancestor::form[1]"));

        List<WebElement> submits = form.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
        if (!submits.isEmpty()) {
            WebElement btn = submits.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(btn));
            btn.click();
            return;
        }

        List<WebElement> buttons = form.findElements(By.cssSelector("button, input[type='button']"));
        if (!buttons.isEmpty()) {
            WebElement btn = buttons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(btn));
            btn.click();
            return;
        }

        ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
    }

    /** Scenario04'ün istediği method: formu JS ile zorla submit */
    protected void forceSubmitInSameFormOf(String fieldId) {
        WebElement field = byId(fieldId);
        WebElement form = field.findElement(By.xpath("ancestor::form[1]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
    }

    // ✅ Scenario08 debug: HTML5 validation mesajları
    protected void logHtml5ValidationMessagesInSameFormOf(String fieldId) {
        try {
            WebElement field = byId(fieldId);
            WebElement form = field.findElement(By.xpath("ancestor::form[1]"));
            List<WebElement> inputs = form.findElements(By.cssSelector("input, select, textarea"));

            System.out.println("[E2E][VALIDATION] messages:");
            for (WebElement el : inputs) {
                try {
                    String msg = (String) ((JavascriptExecutor) driver)
                            .executeScript("return arguments[0].validationMessage;", el);
                    if (msg != null && !msg.isBlank()) {
                        System.out.println("  tag=" + el.getTagName()
                                + " id=" + el.getAttribute("id")
                                + " name=" + el.getAttribute("name")
                                + " -> " + msg);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    // ✅ Scenario08 debug: :invalid alanları logla
    protected void logInvalidFieldsOf(String fieldId) {
        try {
            WebElement field = byId(fieldId);
            WebElement form = field.findElement(By.xpath("ancestor::form[1]"));

            @SuppressWarnings("unchecked")
            List<Object> rows = (List<Object>) ((JavascriptExecutor) driver).executeScript(
                    "const f=arguments[0];" +
                            "const bad=[...f.querySelectorAll(':invalid')];" +
                            "return bad.map(e=>({" +
                            "  id:e.id||'', name:e.name||'', type:e.type||e.tagName," +
                            "  value:e.value||'', msg:e.validationMessage||''" +
                            "}));",
                    form
            );

            System.out.println("[E2E][INVALID] count=" + rows.size());
            for (Object r : rows) System.out.println("[E2E][INVALID] " + r);
        } catch (Exception ignored) {}
    }

    // ------------------ WAIT / ASSERT HELPERS ------------------

    protected void waitUrlNotContains(String part, int seconds) {
        new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(d -> !d.getCurrentUrl().contains(part));
    }

    protected boolean waitBodyTextContains(String text, int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds))
                    .until(d -> d.findElement(By.tagName("body")).getText().contains(text));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Scenario04'ün istediği method */
    protected boolean waitBodyTextContainsWithRefresh(String text, int totalSeconds) {
        long end = System.currentTimeMillis() + totalSeconds * 1000L;
        while (System.currentTimeMillis() < end) {
            if (driver.findElement(By.tagName("body")).getText().contains(text)) return true;
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            driver.navigate().refresh();
            try {
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            } catch (Exception ignored) {}
        }
        return false;
    }

    protected String visibleErrors() {
        String css = ".alert, [role='alert'], .invalid-feedback, .text-danger, .error, .errors, .field-error";
        List<WebElement> els = driver.findElements(By.cssSelector(css));
        return els.stream()
                .map(e -> {
                    try { return e.getText(); } catch (Exception ex) { return ""; }
                })
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining(" | "));
    }

    protected void dumpHtmlAndPng(String name) {
        try {
            File dir = new File("target/e2e-artifacts");
            if (!dir.exists()) dir.mkdirs();

            String html = driver.getPageSource();
            Files.writeString(new File(dir, name + ".html").toPath(), html, StandardCharsets.UTF_8);

            if (driver instanceof TakesScreenshot ts) {
                byte[] png = ts.getScreenshotAs(OutputType.BYTES);
                Files.write(new File(dir, name + ".png").toPath(), png);
            }

            System.out.println("[E2E] Dumped HTML/PNG: " + name);
        } catch (IOException e) {
            System.out.println("[E2E] Dump failed: " + e.getMessage());
        }
    }

    /** Scenario04'ün istediği method */
    protected void logBodyText(String tag) {
        try {
            String t = driver.findElement(By.tagName("body")).getText();
            t = (t == null) ? "" : t.trim();
            System.out.println("[E2E][BODY][" + tag + "] " + (t.length() > 800 ? t.substring(0, 800) : t));
        } catch (Exception ignored) {}
    }

    // ------------------ COMPAT / HELPERS ------------------

    protected String unique(String prefix) {
        long n = System.currentTimeMillis() % 100_000_000L;
        return prefix + n;
    }

    protected String text(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).getText();
    }

    protected long findFoundIdByTitle(String title) {
        String xpath = "//tr[td[normalize-space()=" + xpathLiteral(title) + "]]/td[1]";
        String idText = text(By.xpath(xpath));
        return Long.parseLong(idText.trim());
    }

    // ------------------ MULTI-ID HELPERS ------------------

    protected String firstPresentId(int timeoutSeconds, String... ids) {
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < end) {
            for (String id : ids) {
                try {
                    List<WebElement> els = driver.findElements(By.id(id));
                    if (!els.isEmpty()) return id;
                } catch (Exception ignored) {}
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        dumpHtmlAndPng("timeout_firstPresentId");
        throw new TimeoutException("Hiçbir id bulunamadı: " + String.join(", ", ids) +
                " | URL=" + driver.getCurrentUrl());
    }

    protected void setByFirstPresentId(String value, int timeoutSeconds, String... ids) {
        String id = firstPresentId(timeoutSeconds, ids);
        setById(id, value);
    }

    protected void clickIfExistsById(String id) {
        try {
            List<WebElement> els = driver.findElements(By.id(id));
            if (!els.isEmpty()) els.get(0).click();
        } catch (Exception ignored) {}
    }

    protected boolean trySetByFirstPresentId(String value, int timeoutSeconds, String... ids) {
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < end) {
            for (String id : ids) {
                try {
                    List<WebElement> els = driver.findElements(By.id(id));
                    if (!els.isEmpty()) {
                        setById(id, value);
                        return true;
                    }
                } catch (Exception ignored) {}
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    protected String firstPresentEditableId(int totalSeconds, String... ids) {
        long end = System.currentTimeMillis() + totalSeconds * 1000L;

        while (System.currentTimeMillis() < end) {
            for (String id : ids) {
                try {
                    List<WebElement> els = driver.findElements(By.id(id));
                    if (els.isEmpty()) continue;

                    WebElement el = els.get(0);
                    if (!el.isDisplayed() || !el.isEnabled()) continue;

                    String tag = (el.getTagName() == null) ? "" : el.getTagName().toLowerCase();
                    if (!(tag.equals("input") || tag.equals("select") || tag.equals("textarea"))) continue;

                    String type = safeLower(el.getAttribute("type"));
                    if ("hidden".equals(type)) continue;

                    String readonly = el.getAttribute("readonly");
                    if (readonly != null) continue;

                    String name = el.getAttribute("name");
                    if ((name == null || name.isBlank()) && !tag.equals("select")) continue;

                    System.out.println("[E2E] firstPresentEditableId picked=" + id + " tag=" + tag + " name=" + name);
                    return id;
                } catch (Exception ignored) {}
            }

            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        }

        dumpHtmlAndPng("timeout_firstPresentEditableId");
        throw new TimeoutException(
                "Editable+bindable input id bulunamadı: " + String.join(", ", ids) +
                        " | URL=" + driver.getCurrentUrl()
        );
    }

    // --- Backward compatibility (eski testler için) ---
    protected boolean dbHasTitle(String title) throws Exception {
        return dbHasLostTitle(title);
    }
    protected boolean waitDbHasTitle(String title, int totalSeconds) {
        return waitDbHasLostTitle(title, totalSeconds);
    }

}
