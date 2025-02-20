Base-
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

	@Test(dataProvider = "DropdownData", dataProviderClass = Excel.class, threadPoolSize = 5)
	public void runGrafana(String envInput, String regionInput, String queryInput, String sessionInput,String fromDate, String toDate)
			throws IOException, InterruptedException {
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
		
		// Enter From date using updated xpath
		WebElement fromDateInput = driver.findElement(By.xpath("//input[@class='css-8tk2dk-input-input' and @aria-label='Time Range from field']"));
		fromDateInput.click();
		fromDateInput.clear();
		fromDateInput.sendKeys(fromDate);
		
		// Enter To date using updated xpath
		WebElement toDateInput = driver.findElement(By.xpath("//input[@class='css-8tk2dk-input-input' and @aria-label='Time Range to field']"));
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
		
		//END TIME
		//driver.findElement(By.xpath("//label[text()='Last 7 days']")).click();

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
	}

	@AfterSuite
	public void openIndexHtmlOnce(ITestContext context) throws IOException, InterruptedException {
		String coppa = utilityData.get("COPPA");
		String account_type = utilityData.get("AccountType");
		String input1 = utilityData.get("Expected adrequest");

		String vamValue = utilityData.get("VAM");

		// Logging values for debugging
		System.out.println("Value of input1 (Expected adrequest): " + input1);
		System.out.println("Value of vamValue (VAM): " + vamValue);

		if (adRequestFetched && adResponseFetched) {

			// Open index.html in the browser
			driver.get(
					"file:///Users/604550803/Documents/Automation/JDK ARM file(for Apple silicon chip)/AdresProj/index.html");

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
			try {
				WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(10));
				Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
				System.out.println("Alert found: " + alert.getText());
				alert.accept(); // Accept the alert (or use alert.dismiss() to dismiss it)
			} catch (TimeoutException e) {
				System.out.println("No alert present.");
			}
			// Wait for the COPPA field to be visible and fill it in
			// WebElement coppaField =
			// wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='coppa']")));
			Select coppaDropdown = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.id("coppa"))));
			coppaDropdown.selectByVisibleText(coppa);

			// Wait for the Account Type field to be visible and fill it in
			Select accountDropdown = new Select(driver.findElement(By.id("account_type")));
			accountDropdown.selectByVisibleText(account_type);

			// WebElement expectedAdRequestField =
			// wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input1")));
			// expectedAdRequestField.sendKeys(input1);

			if (input1 != null && !input1.isEmpty()) {
				WebElement expectedAdRequestField = driver.findElement(By.cssSelector("textarea#input1"));
				// Ensure the field is clear before sending keys
				expectedAdRequestField.clear();
				expectedAdRequestField.sendKeys(input1);
				System.out.println("Successfully set Expected Ad Request to: " + input1);
			} else {
				System.out.println("Warning: Expected Ad Request is null or empty");
			}

			WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(30));
			// Paste Actual Ad Request content (from ActualAdRequest.txt) into textarea
			insertLargeTextIntoTextarea("input2", adRequestPath);

			// Paste Ad Response content (from ActualAdResponse.txt) into textarea
			insertLargeTextIntoTextarea("input3", adResponsePath);

			// Wait for the VAM field to be visible and fill it in
			WebElement vamTextArea = driver.findElement(By.cssSelector("textarea#input4"));
			vamTextArea.sendKeys(vamValue); // Enter the value into the textarea

			// After filling all fields, click the Compare button
			WebElement button = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@class='submit-btn']")));
			button.click();

			// Wait for results to appear and log them
			try {
				// Wait for result elements to be visible
				WebElement resultElement = wait2.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));
				System.out.println("Comparison Results: " + resultElement.getText());

				// Give some time to view the results
				Thread.sleep(5000);
			} catch (TimeoutException e) {
				System.out.println("Error: Results not displayed within expected timeframe");
				e.printStackTrace();
			}
		}
	}

	private void insertLargeTextIntoTextarea(String elementId, String filePath) throws IOException {
		String content = Files.readString(Paths.get(filePath));

		// Find the element
		WebElement textArea = driver.findElement(By.xpath("//textarea[@id='" + elementId + "']"));

		// Use JavaScript Executor to insert the text
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].value = arguments[1];", textArea, content);
	}

	public Map<String, String> readUtilityInput(String filePath) {
		Map<String, String> utilityData = new HashMap<>();
		try (FileInputStream fis = new FileInputStream(new File(filePath))) {
			Workbook workbook = WorkbookFactory.create(fis);
			Sheet sheet = workbook.getSheetAt(0);

			Row row = sheet.getRow(1); // Skip header row

			// Reading each cell and trimming any extra spaces
			String coppa = row.getCell(0).getStringCellValue().trim();
			String accountType = row.getCell(1).getStringCellValue().trim();
			String expectedAdRequest = row.getCell(2).getStringCellValue().trim();
			String vam = row.getCell(3).getStringCellValue().trim();

			// Debug logging
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
