package GrafanaSingleCase;



import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

public class Excel {

    @DataProvider(name = "DropdownData")
    public Object[][] getData() throws IOException {
        String filePath = "./data/Input.xlsx";
        FileInputStream fileInputStream = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<Object[]> data = new ArrayList<>();
        Iterator<Row> rows = sheet.iterator();

        // Skip header row if present
        if (rows.hasNext()) rows.next();

        // Iterate through each row and read cell data
        while (rows.hasNext()) {
            Row row = rows.next();
            int cellCount = row.getLastCellNum(); 
            String[] rowData = new String[cellCount];

            for (int i = 0; i < cellCount; i++) {
                Cell cell = row.getCell(i);
                rowData[i] = (cell != null) ? cell.toString() : "";
            }

            // Ensure that rowData has at least 6 columns (handling missing values)
            if (rowData.length < 6) {
                String[] extendedRowData = new String[6];
                System.arraycopy(rowData, 0, extendedRowData, 0, rowData.length);
                for (int i = rowData.length; i < 6; i++) {
                    extendedRowData[i] = ""; // Assign empty string if value is missing
                }
                data.add(extendedRowData);
            } else {
                data.add(rowData);
            }
        }

        workbook.close();
        return data.toArray(new Object[0][0]);
    }
TESTRUNNER

    package GrafanaSingleCase;

import java.util.Collections;

import org.testng.TestNG;

public class Testrunner {

	public static void main(String[] args) {
		TestNG testng = new TestNG();
		testng.setTestSuites(Collections.singletonList("testng.xml")); // Path to your TestNG XML file
		testng.run();

	}

}

  
}
