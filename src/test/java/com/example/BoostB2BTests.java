package com.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BoostB2BTests {

    private WebDriver driver;

    @BeforeMethod
    public void setUp(){
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.get("https://boostb2b.com");
    }

    @AfterMethod
    public void tearDown(){
        if (driver != null){
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void scenarioOneTest(){
        List<WebElement> tabs = driver.findElements(By.xpath("(//nav[contains(@class,'elementor-nav-menu--main')]/ul)[3]/li/a"));
        List<String> headers = tabs.stream().map(tab -> tab.getText().trim()).collect(Collectors.toList());
        saveToTxtFile(headers, "BoostB2B_HeadersListing.txt");
    }

    @Test
    public void scenarioTwoTest(){
        WebElement companyLink = driver.findElement(By.linkText("Company"));
        Actions actions = new Actions(driver);
        actions.moveToElement(companyLink).click().click().perform();

        WebElement globalFootPrint = driver.findElement(By.xpath("//h2[text()='Global Footprint']"));
        actions.scrollToElement(globalFootPrint).build().perform();

        List<String> actualCountries = driver.findElements(By.xpath("//h2[text()='Global Footprint']/../../following-sibling::section//h2"))
                .stream().map(country -> country.getText().trim()).collect(Collectors.toList());

        List<String> expectedCountries = readCountryNamesFromFile("CountryNames.txt");

        Assert.assertEquals(actualCountries.size(), expectedCountries.size());

        // Check if the UI contains a country not on the expected list
        for (String country : actualCountries) {
            Assert.assertTrue(expectedCountries.contains(country), "UI contains unexpected country: " + country);
        }

        // Check if the expected list contains a country not found on the UI
        for (String country : expectedCountries) {
            Assert.assertTrue(actualCountries.contains(country), "Expected country not found on the UI: " + country);
        }
    }

    @Test
    public void scenarioThreeTest() throws InterruptedException {
        driver.findElement(By.xpath("(//a[text()='Get Started'])[5]")).click();

        driver.switchTo().frame(0);

        WebElement firstNameInput = driver.findElement(By.xpath("//div[contains(@class,'first_name')]/input"));
        firstNameInput.sendKeys("Ana");

        WebElement lastNameInput = driver.findElement(By.xpath("//div[contains(@class,'last_name')]/input"));
        lastNameInput.sendKeys("Rusu");

        WebElement emailInput = driver.findElement(By.xpath("//div[contains(@class,'email')]/input"));
        emailInput.sendKeys("anarususerg@gmail.com");

        WebElement titleInput = driver.findElement(By.xpath("//div[contains(@class,'title')]/input"));
        titleInput.sendKeys("Testing Technical Assessment");

        WebElement companyInput = driver.findElement(By.xpath("//div[contains(@class,'company')]/input"));
        companyInput.sendKeys("Testing Technical Assessment");

        WebElement countryDropDown = driver.findElement(By.xpath("//div[contains(@class,'country')]/select"));
        Select countrySelect = new Select(countryDropDown);
        countrySelect.selectByVisibleText("United States");

        WebElement stateDropDown = driver.findElement(By.xpath("//div[contains(@class,'state')]/select"));
        Select stateSelect = new Select(stateDropDown);
        stateSelect.selectByVisibleText("NY");

        WebElement commentsInput = driver.findElement(By.xpath("//div[contains(@class,'comment')]/input"));
        commentsInput.sendKeys(getCurrentDateFromApi());

        driver.switchTo().parentFrame();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

        driver.switchTo().frame(0);

        WebElement careerOpprCheckbox = driver.findElement(By.xpath("//label[text()='Career Opportunities']/preceding-sibling::input"));
        careerOpprCheckbox.click();

        //Pause for 20 seconds for user to click on captcha
        Thread.sleep(20000);

        WebElement sendBtn = driver.findElement(By.xpath("//input[@value='Send']"));
        sendBtn.click();

        WebElement thankYou = driver.findElement(By.xpath("//span[contains(text(),'Thank you!')]"));
        Assert.assertTrue(thankYou.isDisplayed());

    }

    private String getCurrentDateFromApi(){
        String baseUrl = "https://timeapi.io/api/Time/current/zone";
        Response response = RestAssured.given()
                .queryParam("timeZone", "America/New_York")
                .contentType("application/json")  // Set the Content-Type header
                .get(baseUrl);

        Assert.assertEquals(response.getStatusCode(), 200, "Unexpected status code");

        String date = response.then().extract().path("date");

        Assert.assertNotNull(date, "Date field is not present in the response");

        return convertToFormattedDate(date);
    }

    private String convertToFormattedDate(String inputDate) {
        LocalDate localDate = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        return localDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }


    private void saveToTxtFile(List<String> headers, String fileName){
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (String header : headers) {
                writer.println(header);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> readCountryNamesFromFile(String filePath) {
        List<String> countryNames = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                countryNames.add(line.trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return countryNames;
    }
}
