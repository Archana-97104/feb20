
package GrafanaSingleCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.ITestContext;
import org.testng.annotations.*;

public class Base {
	private WebDriver driver;
	private String baseDir = "/Users/604550803/Documents/Automation/JDK ARM file(for Apple silicon chip)/SingleGrafana/Data/";
	private String adRequestPath = baseDir + "ActualAdRequest.txt";
	private String adResponsePath = baseDir + "ActualAdResponse.txt";
	private boolean adRequestFetched = false;
	private boolean adResponseFetched = false;
	private Map<String, String> utilityData;

	@BeforeSuite
	public void setUp() throws IOException {
		File directory = new File(baseDir);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		// Reading utility input data from Excel file
		utilityData = readUtilityInput(baseDir + "UtiltyInput.xlsx");

		System.out.println("Utility Data: " + utilityData);
	}

	@BeforeMethod
	public void setUpDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--allow-file-access-from-files");
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
	}

	@Test(dataProvider = "DropdownData", dataProviderClass = Excel.class)
	public void runGrafana(String envInput, String regionInput, String queryInput, String sessionInput, String fromDate,
			String toDate) throws IOException, InterruptedException {
		driver.get("https://d3q7rt0kr5fynf.cloudfront.net/ad-tools/mediatailor-logs-query-builder.html");
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

		// Selecting dropdown values and input fields
		new Select(driver.findElement(By.id("envInput"))).selectByVisibleText(envInput);
		new Select(driver.findElement(By.id("regionInput"))).selectByVisibleText(regionInput);
		new Select(driver.findElement(By.id("queryInput"))).selectByVisibleText(queryInput);
		driver.findElement(By.id("sessionInput")).sendKeys(sessionInput);

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.findElement(By.id("button-mt")).click();

		Set<String> windowHandles = driver.getWindowHandles();
		List<String> allWindows = new ArrayList<>(windowHandles);
		driver.switchTo().window(allWindows.get(1));
//TIME AND DATE 
		driver.findElement(By.xpath("//span[@class='css-1ueg5w']//span[1]")).click();
		System.out.println("Excel From Date: " + fromDate);
		System.out.println("Excel To Date: " + toDate);

		WebElement fromDateInput = driver.findElement(
				By.xpath("//input[@class='css-8tk2dk-input-input' and @aria-label='Time Range from field']"));
		fromDateInput.click();
		fromDateInput.clear();
		fromDateInput.sendKeys(fromDate);

		WebElement toDateInput = driver.findElement(
				By.xpath("//input[@class='css-8tk2dk-input-input' and @aria-label='Time Range to field']"));
		toDateInput.click();
		toDateInput.clear();
		toDateInput.sendKeys(toDate);

		try {
			Thread.sleep(1000);
			System.out.println("From Date after setting: " + fromDateInput.getAttribute("value"));
			System.out.println("To Date after setting: " + toDateInput.getAttribute("value"));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		driver.findElement(By.xpath("//span[text()='Apply time range']")).click();

		// END TIME
		// driver.findElement(By.xpath("//label[text()='Last 7 days']")).click();

		WebElement button = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//button[@class='css-5se5b3 css-1wx8bl8-positionRelative']")));
		button.click();

		String text = "No output fetched.";
		try {
			WebElement resultButton = wait.until(driver -> driver
					.findElement(By.xpath("//button[@class='css-5se5b3 css-1wx8bl8-positionRelative']")));
			text = resultButton.getText();
		} catch (Exception e) {
			System.out.println("No output fetched for this case. Skipping...");
		}

		String resultPath = queryInput.contains("⬅️ Raw Logs - FW Ad Responses") ? adResponsePath : adRequestPath;
		try (FileOutputStream fos = new FileOutputStream(resultPath)) {
			fos.write(text.getBytes());
			fos.flush();
			System.out.println("Content saved to file: " + resultPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (resultPath.equals(adRequestPath)) {
			adRequestFetched = true;
		} else if (resultPath.equals(adResponsePath)) {
			adResponseFetched = true;
		}
		if (resultPath.equals(adRequestPath))
			adRequestFetched = true;
		if (resultPath.equals(adResponsePath))
			adResponseFetched = true;

		// Wait before closing browser
		Thread.sleep(5000);
		driver.quit();
	}

	@AfterSuite
	public void openIndexHtmlOnce(ITestContext context) throws IOException, InterruptedException {
		if (adRequestFetched && adResponseFetched) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--allow-file-access-from-files");
			WebDriver indexDriver = new ChromeDriver(options);
			indexDriver.manage().window().maximize();
			indexDriver.get(
					"file:///Users/604550803/Documents/Automation/JDK ARM file(for Apple silicon chip)/AdresProj/index.html");
			WebDriverWait wait = new WebDriverWait(indexDriver, Duration.ofSeconds(10));
			try {
				Alert alert = wait.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				System.out.println("Alert accepted.");
			} catch (TimeoutException e) {
				System.out.println("No alert present.");
			}
			String coppa = utilityData.get("COPPA");
			String account_type = utilityData.get("AccountType");
			String input1 = utilityData.get("Expected adrequest");

			String vamValue = utilityData.get("VAM");

			System.out.println("Value of input1 (Expected adrequest): " + input1);
			System.out.println("Value of vamValue (VAM): " + vamValue);

			Select coppaDropdown = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.id("coppa"))));
			coppaDropdown.selectByVisibleText(coppa);

			Select accountDropdown = new Select(indexDriver.findElement(By.id("account_type")));
			accountDropdown.selectByVisibleText(account_type);

			if (input1 != null && !input1.isEmpty()) {
				WebElement expectedAdRequestField = indexDriver.findElement(By.cssSelector("textarea#input1"));
				// Ensure the field is clear before sending keys
				expectedAdRequestField.clear();
				expectedAdRequestField.sendKeys(input1);
				System.out.println("Successfully set Expected Ad Request to: " + input1);
			} else {
				System.out.println("Warning: Expected Ad Request is null or empty");
			}

			insertLargeTextIntoTextarea(indexDriver, "input2", adRequestPath);
			insertLargeTextIntoTextarea(indexDriver, "input3", adResponsePath);

			WebElement vamTextArea = indexDriver.findElement(By.cssSelector("textarea#input4"));
			vamTextArea.sendKeys(vamValue); // Enter the value into the textarea

			WebElement button = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@class='submit-btn']")));
			button.click();

		}
	}

	private void insertLargeTextIntoTextarea(WebDriver indexDriver, String elementId, String filePath)
			throws IOException {
		String content = Files.readString(Paths.get(filePath));
		WebElement textArea = indexDriver.findElement(By.xpath("//textarea[@id='" + elementId + "']"));
		JavascriptExecutor js = (JavascriptExecutor) indexDriver;
		js.executeScript("arguments[0].value = arguments[1];", textArea, content);
	}

	public Map<String, String> readUtilityInput(String filePath) {
		Map<String, String> utilityData = new HashMap<>();
		File file = new File(filePath);
		System.out.println("Attempting to load Excel file from: " + file.getAbsolutePath());
	    
	    if (!file.exists()) {
	        System.out.println("Error: Excel file does not exist at " + file.getAbsolutePath());
	        return utilityData;
	    }
		try (FileInputStream fis = new FileInputStream(new File(filePath))) {
			Workbook workbook = WorkbookFactory.create(fis);
			Sheet sheet = workbook.getSheetAt(0);

			Row row = sheet.getRow(1); // Skip header row

			// Reading each cell and trimming any extra spaces
			String coppa = row.getCell(0).getStringCellValue().trim();
			String accountType = row.getCell(1).getStringCellValue().trim();
			String expectedAdRequest = row.getCell(2).getStringCellValue().trim();
			String vam = row.getCell(3).getStringCellValue().trim();

			System.out.println("Reading from Excel - Expected Ad Request: " + expectedAdRequest);

			utilityData.put("COPPA", coppa);
			utilityData.put("AccountType", accountType);
			utilityData.put("VAM", vam);
			// Fix: Store with the exact key name that's used in openIndexHtmlOnce
			utilityData.put("Expected adrequest", expectedAdRequest);

			workbook.close();
		} catch (Exception e) {
			System.out.println("Error reading Excel file: " + e.getMessage());
			e.printStackTrace();
		}
		return utilityData;
	}

	@AfterMethod
	public void tearDown() {
		if (driver != null) {
			// driver.quit();
		}
	}
}
