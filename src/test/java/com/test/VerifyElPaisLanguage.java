package com.test;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class VerifyElPaisLanguage {
    private WebDriver driver;
    String url = "https://elpais.com/";

    @BeforeClass
    public void setup() {
        System.out.println("Setting up WebDriver...");
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void checkLanguage() {
        try {
            String url = "https://elpais.com/";
            driver.get(url);

            // Explicit wait for the HTML element to be available
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement htmlElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("html")));

            // Retrieve the lang attribute
            String langAttribute = htmlElement.getAttribute("lang");

            // Assertion
            Assert.assertEquals(langAttribute.trim(), "es-ES");
            // Wait for the "Opinión" section link to be visible
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            WebElement opinionSection = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, 'elpais.com/opinion')]")
            ));
            Thread.sleep(1000); // Small delay to ensure it's visible
            opinionSection.click();

            JavascriptExecutor js = (JavascriptExecutor) driver;
            //Locate the first five articles
            List<WebElement> articles = driver.findElements(By.xpath("//article[contains(@class, 'c-o c-d')]"));
            int count = Math.min(articles.size(), 5);  // Ensure we get at most 5 articles

            String[] articleTitlesES = new String[5];
            for (int i = 0; i < count; i++) {

                WebElement article = articles.get(i);
                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", article);
                Thread.sleep(1000);
                // Extract the title
                WebElement webTitle = article.findElement(By.xpath(".//h2[contains(@class, 'c_t c_t-i')]/a"));
                String title = webTitle.getText();
                articleTitlesES[i] = title;

                // Extract the content (summary) if available
                String content = "";
                try {
                    WebElement contentElement = article.findElement(By.xpath(".//p[contains(@class, 'c_d')]"));
                    content = contentElement.getText();
                } catch (Exception e) {
                    content = "No content available";
                }

                // Check if article has an image inside <figure> → <a> → <img>
                String imageUrl = null;
                try {
                    WebElement imgElement = article.findElement(By.xpath(".//figure/a/img"));
                    imageUrl = imgElement.getAttribute("src");
                } catch (NoSuchElementException e) {
                    System.out.println("No image found for article " + (i + 1));
                }

                // If image exists, download it
                if (imageUrl != null) {
                    try (InputStream in = new URL(imageUrl).openStream()) {
                        Files.copy(in, Paths.get("Downloads/" + "article" + (i + 1) + ".jpg"), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Image downloaded: " + "article" + (i + 1) + ".jpg");
                    } catch (IOException e) {
                        System.out.println("Failed to download image: " + e.getMessage());
                    }
                }

                // Print the article details
                System.out.println("Article " + (i + 1) + ":");
                System.out.println("Title: " + title);
                System.out.println("Content: " + content);
                System.out.println("-----------------------------------");

            }
            List<String> translatedTitlesList = new ArrayList<>();
            for (String title : articleTitlesES) {
                String translatedTitle = RapidAPITranslator.translate(title, "en");
                translatedTitlesList.add(translatedTitle);
                System.out.println("Original: " + title);
                System.out.println("Translated: " + translatedTitle);
                System.out.println("----------------------------------");
            }
            // Call method to count repeated words
            Map<String, Integer> wordCounts = Words.countRepeatedWords(translatedTitlesList);

            // Print words appearing more than twice
            System.out.println("Repeated words (more than twice):");
            for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
                if (entry.getValue() >= 2) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }


    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
