package edu.chop.dgd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.junit.Assume.assumeTrue;

@RunWith(JUnit4.class)

public class SafariDriverTests {

    private WebDriver driver = null;

    private static boolean isSupportedPlatform() {
        Platform current = Platform.getCurrent();
        return Platform.MAC.is(current) || Platform.WINDOWS.is(current);
    }

    @Before
    public void createDriver() {
        assumeTrue(isSupportedPlatform());
        driver = new SafariDriver();
    }

    @After
    public void quitDriver() {
        driver.quit();
    }

    @Test
    public void shouldBeAbleToPerformAGoogleSearch() {
        //driver.get("http://dgdwebapp01.chop.edu/dgdweb/primer/primerReport.html?selectObject=1&start=139000&stop=139000");
        //driver.get("http://127.0.0.1:8080/dgdweb/primer/primerReport.html?selectObject=1&start=1666303&stop=1666304");
        /*driver.findElement(By.name("q")).sendKeys("webdriver");
        driver.findElement(By.name("btnG")).click();*/
        WebElement queryInfoTable = driver.findElement(By.id("queryInfo1"));
        //ExpectedCondition trueCondition = ExpectedConditions.textToBePresentInElement(queryInfoTable, "Genes In Region");
        //IsElementPresent(trueCondition)
        /*if(ExpectedConditions.textToBePresentInElement(queryInfoTable, "Genes In Region").apply(driver).equals("Genes In Region")){
            System.out.println("present!!");
        }*/
        List<WebElement> allSuggestions = driver.findElements(By.xpath("//table//tr/td[@class='queryInfo1']"));

        for(WebElement suggestion : allSuggestions){
            System.out.println(suggestion.getText());
        }

        new WebDriverWait(driver, 3)
                .until(ExpectedConditions.titleIs("Division of Genomic Diagnostics Primer Design Application"));
        System.out.println("hello!!");
    }
}

