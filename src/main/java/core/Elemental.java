package core;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.*;
import java.time.Duration;
import java.util.Properties;

public class  Elemental {

    public static WebDriver driver;
    public WebElement element;
    public static String browserName;
    public static DataParser locatorParser;
    public static LoadEnvProps loadEnvProps;
    public static Capabilities caps;
    public static String embURL=null;
    public static boolean ghSignInState=false;
    public static boolean bbSignInState=false;

    /*To Do:Use maven dependency to create driver*/
    public String getBrowserName(String browser) {
        browserName = browser;
        return browserName;
    }

    public Elemental()
    {
        loadEnvProps = new LoadEnvProps();
    }
    public void setWebDriver() {
        String browser = getBrowserName(browserName);
        if (browser.equalsIgnoreCase("chrome")) {
            System.setProperty("webdriver.chrome.driver", "./src/main/resources/executables/chromedriver.exe");
            ChromeOptions options = new ChromeOptions();
            //WebDriverManager.chromedriver().setup();
            options.addArguments("start-maximized");
            options.addArguments("--disable-gpu");
            options.addArguments("enable-automation");
            /*Avoid using headless mode. Some tests are failing in headless mode.*/
            //options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-browser-side-navigation");
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addArguments("--test-type");
            options.addArguments("--disable-popup-blocking");
            driver = new ChromeDriver(options);
        } else if (browser.equalsIgnoreCase("firefox")) {
            System.setProperty("webdriver.gecko.driver", "./src/main/resources/executables/geckodriver.exe");
            //WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver();
            driver.manage().window().maximize();
        } else if (browser.equalsIgnoreCase("edge")) {
            System.setProperty("webdriver.edge.driver", "./src/main/resources/executables/msedgedriver.exe");
            //WebDriverManager.edgedriver().setup();
            driver = new EdgeDriver();
            driver.manage().window().maximize();
        }
    }

    public void OpenPlatform(String appURL) {
        embURL=appURL;
        driver.get(embURL);
        waitTillWholePageIsLoaded();
    }

    public String getPropertyValue(String key)
    {
        return (String) loadEnvProps.loadPropertiesFile("appConfig.properties").get(key);
    }

    public void waitTillWholePageIsLoaded()
    {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
    }


    public void WaitTillElementIsClickable(String elementLocator) {
        By element = locatorParser.getElementLocator(elementLocator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void WaitTillPresenceOfElementIsLocated(String elementLocator) {
        By element = locatorParser.getElementLocator(elementLocator);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(element));
    }

    public void FluentWaitForWebElement(String elementLocator)
    {
        By element = locatorParser.getElementLocator(elementLocator);
        Wait<WebDriver> fluentWait = new FluentWait<>(driver)
                /*Define total time you can wait for*/
                .withTimeout(Duration.ofSeconds(5))
                /*Define polling frequency*/
                .pollingEvery(Duration.ofSeconds(1))
                /*Define Exceptions to be ignored*/
                .ignoring(NoSuchElementException.class, TimeoutException.class);
        WebElement ele = fluentWait.until(driver -> {
            /*Define subjected conditions for which we need to wait for*/
            return driver.findElement(element);
        });
    }

    public void WaitTillTextFieldIsReady(String elementLocator) {
        By element = locatorParser.getElementLocator(elementLocator);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOf(driver.findElement(element)));
    }
    public void tearDown() {
        try {
            caps = ((RemoteWebDriver) driver).getCapabilities();
            //Currently done only for Google chrome.
            //To DO: Generate Env Props for Firefox and MS Edge
            CreateEnvironmentProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
        driver.manage().deleteAllCookies();
        driver.quit();
    }

    public void CreateEnvironmentProperties() throws IOException {
        File envProps = new File(".//allure-results//environment.properties");
        if(envProps.exists() && !envProps.isDirectory())
        {
            envProps.createNewFile();
        }
        FileOutputStream propFile = new FileOutputStream(envProps, false);
            try
            {
                Properties prop = new Properties();
                prop.setProperty("Embold URL",embURL);
                prop.setProperty("Platform", System.getProperty("os.name"));
                prop.setProperty("BrowserName", String.valueOf(caps.getCapability("browserName")));
                prop.setProperty("BrowserVersion", String.valueOf(caps.getCapability("browserVersion")));
                prop.setProperty("ScreenResolution",String.valueOf(driver.manage().window().getSize()));
                prop.setProperty("ExecutedBy",System.getProperty("user.name"));
                prop.store(propFile, null);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
}