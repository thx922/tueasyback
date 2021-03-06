package com.hiyun.tueasy.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hiyun.tueasy.model.Cell;
import com.hiyun.tueasy.model.Excel;
import com.hiyun.tueasy.model.MyExcel;
import com.hiyun.tueasy.model.MySheet;
import com.hiyun.tueasy.model.Sheet;
import com.hiyun.tueasy.util.JsonUtil;
import com.hiyun.tueasy.util.UUIDGenerator;

public class PoiReadExcelHelper {

	private static Map<String,String> map = new HashMap<String,String>();
	
	public static Excel readExcel(String fileName) {
		Excel excel = new Excel();
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		try {
			File f = new File(fileName);
			excel.setUuid(UUIDGenerator.getUUID());
			excel.setExcelName(f.getName());
			file = new FileInputStream(f);

			// Get the workbook instance for XLS file
			workbook = new HSSFWorkbook(file);
			List<Sheet> sheetList = new ArrayList<Sheet>();

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				HSSFSheet sheet = workbook.getSheetAt(i);
				if (sheet == null) {
					continue;
				}
				Sheet sh = new Sheet();
				sh.setSheetName(workbook.getSheetName(i));
				List<Cell> cellList = new ArrayList<Cell>();
				// Iterate through each rows from first sheet
				Iterator<Row> rowIterator = sheet.iterator();
				int rowNum = 0;
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					rowNum++;
					// For each row, iterate through each columns
					Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row
							.cellIterator();
					int columnNum = 0;
					while (cellIterator.hasNext()) {
						org.apache.poi.ss.usermodel.Cell cell = cellIterator
								.next();
						columnNum++;
						Cell myCell = new Cell();
						myCell.setRowNum(rowNum);
						myCell.setColumnNum(columnNum);
						switch (cell.getCellType()) {
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
							myCell.setType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
							myCell.setDoubleContent(cell.getNumericCellValue());
							break;
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
							myCell.setType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING);
							myCell.setStringContent(cell.getStringCellValue());
							break;

						}
						cellList.add(myCell);
					}
				}
				sh.setCell(cellList);
				sheetList.add(sh);
			}

			excel.setSheet(sheetList);

			workbook.close();
			file.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return excel;

	}

	public static String getFileName(String uuid){
		String fileName = "";
		if(StringUtils.isEmpty(uuid) || map.isEmpty())
			return fileName;
		else
			return map.get(uuid);
	}
	public static List<MyExcel> parseExcel(String... fileName) {
		List<MyExcel> list = new ArrayList<MyExcel>();
		FileInputStream file = null;
		Workbook workbook = null;
		
		try {
			for (String strfile : fileName) {
				File f = new File(strfile);
				MyExcel excel = new MyExcel();
				excel.setSchemaName(f.getName());
				String uuid = UUIDGenerator.getUUID();
				excel.setUuid(uuid);
				map.put(uuid, strfile);
				if(FilenameUtils.getExtension(strfile).equalsIgnoreCase("xls")) {
					workbook = new HSSFWorkbook(new FileInputStream(f));
				}else if(FilenameUtils.getExtension(strfile).equalsIgnoreCase("xlsx")) {
					workbook = new XSSFWorkbook(new FileInputStream(f));
				}else {
					
				}
				List<MySheet> tables = new ArrayList<MySheet>();
				for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
					org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
					if (sheet == null) {
						continue;
					}
					String sheetName = workbook.getSheetName(i);
					MySheet mysheet = new MySheet();
					mysheet.setTableName(sheetName);
					List<Object> titleList = new ArrayList<Object>();
					Iterator<Row> rowIterator = sheet.iterator();
					if (rowIterator.hasNext()) {
						Row row = rowIterator.next();
						// For each row, iterate through each columns
						Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row
								.cellIterator();
						while (cellIterator.hasNext()) {
							org.apache.poi.ss.usermodel.Cell cell = cellIterator
									.next();
							switch (cell.getCellType()) {
							case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
								titleList.add(cell.getNumericCellValue());
								break;

							case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
								titleList.add(cell.getStringCellValue());
								break;
							}
						}
					}
					mysheet.setColumns(titleList);
					tables.add(mysheet);

				}
				excel.setTables(tables);
				list.add(excel);

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		return list;
	}

	/**
	 * 返回一个json字符串
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getExcelAsJson(String... fileName) {
		String json = "";
		json = JsonUtil.Object2Json(parseExcel(fileName));

		return json;
	}

	public static Map<String, Object> parseExcel(String fileName) {
		Map<String, Object> mapObject = new HashMap<String, Object>();
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		try {
			File f = new File(fileName);
			mapObject.put("uuid", UUIDGenerator.getUUID());
			mapObject.put("excelName", f.getName());

			Map<String, Object> map = new HashMap<String, Object>();

			// Get the workbook instance for XLS file
			file = new FileInputStream(f);
			workbook = new HSSFWorkbook(file);
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				HSSFSheet sheet = workbook.getSheetAt(i);
				if (sheet == null) {
					continue;
				}
				String sheetName = workbook.getSheetName(i);
				List<Object> titleList = new ArrayList<Object>();

				Iterator<Row> rowIterator = sheet.iterator();
				if (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					// For each row, iterate through each columns
					Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row
							.cellIterator();
					while (cellIterator.hasNext()) {
						org.apache.poi.ss.usermodel.Cell cell = cellIterator
								.next();
						switch (cell.getCellType()) {
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
							titleList.add(cell.getNumericCellValue());
							break;

						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
							titleList.add(cell.getStringCellValue());
							break;
						}
					}
				}

				map.put(sheetName, titleList);

			}

			mapObject.put("tableName", map);
			workbook.close();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		return mapObject;
	}

	/**
	 * 返回一个json字符串
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getExcelAsJson(String fileName) {
		String json = "";
		json = JsonUtil.Object2Json(parseExcel(fileName));

		return json;
	}

	/**
	 * TODO:根据uuid从redis得到上传文件的路径（fileName）
	 * 
	 * @param uuid
	 * @param sheetName
	 * @param columnName
	 * @param fileName
	 * @return
	 */
	public static List<Object> parseExcelByColumn(String uuid,
			String sheetName, String columnName) {
		List<Object> list = new ArrayList<Object>();
		FileInputStream file = null;
		HSSFWorkbook workbook = null;

		try {
			if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(sheetName)
					|| StringUtils.isEmpty(columnName)) {
				return list;
			}
			// columnName对应的在excel中的columnIndex
			int columnIndex = 0;
			String fileName = map.get(uuid);
			File f = new File(fileName);
			// Get the workbook instance for XLS file
			file = new FileInputStream(f);
			workbook = new HSSFWorkbook(file);

			HSSFSheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				return list;
			}

			Iterator<Row> rowIterator = sheet.iterator();
			boolean flag = false;
			int rowIndex = 0;
			while (rowIterator.hasNext()) {
				rowIndex++;
				Row row = rowIterator.next();
				// For each row, iterate through each columns
				Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row
						.cellIterator();
				if (flag != true) {
					// 找到columnName对应的columnIndex
					while (cellIterator.hasNext()) {
						org.apache.poi.ss.usermodel.Cell cell = cellIterator
								.next();
						switch (cell.getCellType()) {
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
							if (String.valueOf(cell.getNumericCellValue())
									.equals(columnName)) {
								columnIndex = cell.getColumnIndex();
								flag = true;
							}
							break;

						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
							if (cell.getStringCellValue().equals(columnName)) {
								columnIndex = cell.getColumnIndex();
								flag = true;
							}
							break;
						}
						if (flag == true)
							break;

					}
				}
				// 如果没有找到对应的columIndex
				if (flag == false)
					return list;
				else if (rowIndex == 1)
					continue;
				else {
					org.apache.poi.ss.usermodel.Cell cell = row
							.getCell(columnIndex);
					if (cell != null) {
						switch (cell.getCellType()) {
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
							list.add(cell.getNumericCellValue());
							break;

						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
							list.add(cell.getStringCellValue());
							break;
							
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA:
						{
							 // 判断当前的cell是否为Date
			                  if (HSSFDateUtil.isCellDateFormatted(cell)){
			                	  list.add( cell.getDateCellValue().toString());
			                	  
			                  }else{
			                	  list.add(cell.getNumericCellValue());
			                  }
			                	  
			                  break;
						}
							
						}
						

					}
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return list;
	}

	/**
	 * 
	 * @param uuid
	 * @param sheetName
	 * @param columnName
	 * @param fileName
	 * @return
	 */
	public static String getExcelJsonByColumn(String uuid, String sheetName,
			String columnName) {
		String json = "";
		json = JsonUtil.Object2Json(parseExcelByColumn(uuid, sheetName,
				columnName));

		return json;
	}
}
