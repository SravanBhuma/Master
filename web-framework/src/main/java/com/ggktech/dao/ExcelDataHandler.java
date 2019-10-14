package com.ggktech.dao;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ggktech.utils.ConfigConstants;

/**
 * Class that contains functions for excel operations.
 */
public class ExcelDataHandler {

	private static final Logger LOGGER = Logger.getLogger(ExcelDataHandler.class);

	/**
	 * @param sheetName
	 *            : Sheet name where data needs to be retrieved
	 * @param sExcelFiPath
	 *            : Excel document file path
	 * @return worksheet : returns sheet
	 * @throws IOException
	 */
	public static synchronized Sheet getSheetData(String sheetName, String sExcelFiPath) throws IOException {
		Workbook workbook = getWorkBook(ConfigConstants.PARENTFOLDER_PATH + sExcelFiPath);
		Sheet worksheet = null;
		try {
			worksheet = workbook.getSheet(sheetName);
		} finally {
			try {
				if (workbook != null) {
					workbook.close();
				}

			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
		return worksheet;
	}

	/**
	 * @param sheetName
	 *            : Sheet name where data needs to be stored
	 * @param rowNumber
	 *            : Row number
	 * @param cellNumber
	 *            : Cell number
	 * @param value
	 *            : Value to be stored
	 * @param sExcelFiPath
	 *            : Excel document location
	 * @throws IOException
	 */
	public static synchronized void setSheetData(String sheetName, int rowNumber, int cellNumber, String value,
			String sExcelFiPath) throws IOException {
		Workbook moduleWorkbook = null;
		FileOutputStream fos = null;
		try {
			moduleWorkbook = getWorkBook(ConfigConstants.PARENTFOLDER_PATH + sExcelFiPath);
			Sheet moduleSheet = moduleWorkbook.getSheet(sheetName);
			Row rowa;
			if (moduleSheet.getRow(rowNumber) == null) {
				rowa = moduleSheet.createRow(rowNumber);
			} else {
				rowa = moduleSheet.getRow(rowNumber);
			}
			Cell cella = rowa.createCell(cellNumber);
			cella.setCellValue(value);
			fos = new FileOutputStream(ConfigConstants.PARENTFOLDER_PATH + sExcelFiPath);
			moduleWorkbook.write(fos);
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (moduleWorkbook != null) {
					moduleWorkbook.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}

	public static Workbook getWorkBook(String path) {
		Workbook book = null;
		try (FileInputStream fis = new FileInputStream(path)) {
			if (path.endsWith(".xlsx")) {
				book = new XSSFWorkbook(fis);
			} else {
				book = new HSSFWorkbook(fis);
			}

		} catch (IOException e) {
			LOGGER.error(e);
		}
		return book;
	}

	/**
	 * @param sSheetName
	 *            : Sheet name where data needs to be retrieved
	 * @param filePath
	 *            : Excel document location
	 * @param sColName
	 *            : Column from which data needs to be get.
	 * @param scriptName
	 *            : Testscript name
	 * @return sVal : Retrievs value of the specified cell
	 */
	public String getExcelValue(String sSheetName, String filePath, String sColName, String scriptName) {
		String sVal = null;
		Sheet sheet = null;
		Row row = null;
		Cell cell = null;
		try {
			sheet = getSheetData(sSheetName, filePath);
			row = getRowTestScriptName(sheet, scriptName);
			cell = getCellFromRow(row, sColName, sheet);
			sVal = cell.getStringCellValue();
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return sVal;
	}

	/**
	 * @param sheetName
	 *            : Sheet name where data needs to be retrieved
	 * @param sTestScript
	 *            : Testscript name
	 * @return record : returns all values associated with the given script from
	 *         given sheet
	 */
	public Map<String, String> getValueFromExcel(Sheet sheetName, String sTestScript) {
		Row row = getRowTestScriptName(sheetName, sTestScript);
		Map<String, String> record = new HashMap<>();
		int lastCellNumber = row.getLastCellNum();
		for (int k = 1; k < lastCellNumber; k++) {
			record.put(sheetName.getRow(0).getCell(k).getStringCellValue(), row.getCell(k).getStringCellValue());
		}
		return record;

	}

	/**
	 * @param sheet
	 *            : Sheet name where data needs to be retrieved
	 * @param scriptName
	 *            : Testscript name
	 * @return iRow : returns the row where given scriptname matched
	 */
	public Row getRowTestScriptName(Sheet sheet, String scriptName) {
		Row iRow = null;
		int lastRowNumber = sheet.getLastRowNum();
		for (int i = 1; i <= lastRowNumber; i++) {
			Row row = sheet.getRow(i);
			if (row.getCell(1).getStringCellValue().equals(scriptName)) {
				iRow = sheet.getRow(i);
				break;
			}
		}
		return iRow;
	}

	/**
	 * @param mRow
	 *            : row number
	 * @param colName
	 *            : column number
	 * @param sheet
	 *            : Sheet name where data needs to be retrieved
	 * @return mCell : returns the Cell from specified sheet
	 */
	public Cell getCellFromRow(Row mRow, String colName, Sheet sheet) {
		Cell mCell = null;
		Row iRow = sheet.getRow(0);
		int lastCellNum = iRow.getLastCellNum();
		for (int i = 1; i <= lastCellNum; i++) {
			if (iRow.getCell(i) != null && iRow.getCell(i).getStringCellValue().equals(colName)) {
				mCell = mRow.getCell(i);
				break;
			}
		}
		return mCell;

	}

	public static String getCellValueFromSheet(Sheet sheet, int rowIndex, int columnIndex) {
		Cell cell = sheet.getRow(rowIndex).getCell(columnIndex);
		cell.setCellType(CellType.STRING);
		return cell.getStringCellValue();
	}
}
