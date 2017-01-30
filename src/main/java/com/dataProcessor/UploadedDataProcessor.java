package com.dataProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.xml.pojo.AttributeValDTO;
import com.xml.pojo.XMLAttribDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UploadedDataProcessor {
	Logger log = LoggerFactory.getLogger(UploadedDataProcessor.class);

	/**
	 * 
	 * @param uploadPath
	 * @param xmlDto
	 * @return
	 */
	public Map<String, List<String>> parseXMLtoAtrribValue(String uploadPath, List<XMLAttribDTO> xmlDto) {
		if (log.isDebugEnabled()) {
			log.debug("parseXMLtoJAXBObjects:entry");
		}

		List<String> uploadFilePaths = getFilePathsInUploadDir(uploadPath);
		Map<File, List<AttributeValDTO>> fileMap = getFileObjsMap(uploadFilePaths, xmlDto);
		// System.out.println(fileList);

		// to parse the XML with DOM model and generate the attributeList
		Map<String, List<String>> attribNameValMap = parseXMLWithDOM(fileMap);

		if (log.isDebugEnabled()) {
			log.debug("parseXMLtoJAXBObjects:exit");
		}
		return attribNameValMap;
	}

	public List<XMLAttribDTO> getXMLFileAttribData(String uploadPath) {
		if (log.isDebugEnabled()) {
			log.debug("parseXMLtoJAXBObjects:entry");
		}

		List<String> uploadFilePaths = getFilePathsInUploadDir(uploadPath);
		List<File> fileList = getFileObjsList(uploadFilePaths);
		List<XMLAttribDTO> xmlAttribList = parseXMLWithDOMForXMLFileAttrib(fileList);
		return xmlAttribList;

	}

	public String writeDataToExcel(String UPLOAD_PATH, Map<String, List<String>> attribNameValMap) {
		boolean isWriteSuccess = true;
		String fileUrl = null;
		// for first sheet
		XSSFWorkbook workbook = new XSSFWorkbook();
		String extractedFileName = "Extracted_Attributes_" + System.currentTimeMillis() + ".xlsx";
		String attribSheetName = "Attributes vs Data";
		XSSFSheet attributeVsDatasheet = workbook.createSheet(attribSheetName);
		int rowNos = 1;
		int cellNos = 0;
		// for first header row
		Row row = attributeVsDatasheet.createRow(rowNos);
		for (String attribNameValKey : attribNameValMap.keySet()) {
			Cell headerCell = row.createCell(cellNos++);
			headerCell.setCellValue(attribNameValKey);
		}
		cellNos = 0;
		for (Map.Entry<String, List<String>> attribNameValObj : attribNameValMap.entrySet()) {
			for (String attribVal : attribNameValObj.getValue()) {
				int rowNumber = ++rowNos;
				Row dataRow = null;
				if (null != attributeVsDatasheet.getRow(rowNumber)) {
					dataRow = attributeVsDatasheet.getRow(rowNumber);
				} else {
					dataRow = attributeVsDatasheet.createRow(rowNumber);
				}
				Cell cell = dataRow.createCell(cellNos);
				cell.setCellValue(attribVal);
			}
			++cellNos;
			rowNos = 1;
		}

		try (FileOutputStream outputStream = new FileOutputStream(UPLOAD_PATH + extractedFileName)) {
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			isWriteSuccess = false;
			log.error("exception while writing to file:" + e.getMessage());
		}
		if (isWriteSuccess) {
			fileUrl = UPLOAD_PATH + extractedFileName;
		}
		return fileUrl;
	}

	public boolean writeAttribDataToExcel(String fileUrl, List<AttributeValDTO> attribValDTOList) {
		boolean isWriteSuccess = true;
		XSSFWorkbook workbook = null;
		FileInputStream file = null;
		try {
			file = new FileInputStream(new File(fileUrl));
			workbook = new XSSFWorkbook(file);
		} catch (FileNotFoundException e) {
			isWriteSuccess = false;
			log.error("exception while searching file for spreadsheet processing" + e.getMessage());
		} catch (IOException e) {
			isWriteSuccess = false;
			log.error("exception while searching file for spreadsheet processing" + e.getMessage());
		}
		if (null != workbook) {
			String colDataSheetName = "Attributes vs Data Valiation";
			XSSFSheet colDataSheet = workbook.createSheet(colDataSheetName);
			int rowNos = 0;
			Row row = colDataSheet.createRow(++rowNos);
			int cellNos = 0;

			// setting the initial rows as header
			setCellValue(row, cellNos++, "Attribute Name");
			setCellValue(row, cellNos++, "Column Name");
			setCellValue(row, cellNos++, "Data Validation");
			setCellValue(row, cellNos++, "Input XML");
			cellNos = 0;

			for (AttributeValDTO attribData : attribValDTOList) {
				row = colDataSheet.createRow(++rowNos);
				cellNos = 0;

				// setting the value
				setCellValue(row, cellNos++, attribData.getAttribName());
				setCellValue(row, cellNos++, attribData.getColName());
				setCellValue(row, cellNos++, attribData.getDataValidation());
				setCellValue(row, cellNos++, attribData.getFileName());
			}

			try (FileOutputStream outputStream = new FileOutputStream(fileUrl)) {
				workbook.write(outputStream);
				workbook.close();
			} catch (Exception e) {
				isWriteSuccess = false;
				log.error("exception while writing to file:" + e.getMessage());
			}

		} else {
			isWriteSuccess = false;
		}
		return isWriteSuccess;
	}

	private List<String> getFilePathsInUploadDir(String uploadPath) {
		if (log.isDebugEnabled()) {
			log.debug("getFilePathsInUploadDir:entry");
		}
		File folder = new File(uploadPath);
		List<String> filesPaths = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (!file.isDirectory()) {
				filesPaths.add(file.getAbsolutePath());
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("getFilePathsInUploadDir:exit");
		}
		return filesPaths;
	}

	private Map<File, List<AttributeValDTO>> getFileObjsMap(List<String> filesPaths, List<XMLAttribDTO> xmlDto) {

		if (log.isDebugEnabled()) {
			log.debug("getFileArrObjs:entry");
		}
		Map<File, List<AttributeValDTO>> fileAttribMap = new TreeMap<>();
		for (String path : filesPaths) {
			for (XMLAttribDTO xmlAttribDTO : xmlDto) {
				File file = new File(path);
				if (xmlAttribDTO.getFileName().equalsIgnoreCase(file.getName())) {
					fileAttribMap.put(file.getAbsoluteFile(), xmlAttribDTO.getAttributes());
					break;
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("getFileArrObjs:exit");
		}
		return fileAttribMap;
	}

	private List<File> getFileObjsList(List<String> filesPaths) {

		if (log.isDebugEnabled()) {
			log.debug("getFileArrObjs:entry");
		}
		List<File> fileList = new ArrayList<>();
		for (String path : filesPaths) {
			File file = new File(path);
			fileList.add(file.getAbsoluteFile());
		}

		if (log.isDebugEnabled()) {
			log.debug("getFileArrObjs:exit");
		}
		return fileList;
	}

	private Map<String, List<String>> parseXMLWithDOM(Map<File, List<AttributeValDTO>> fileAtribMap) {
		Map<String, List<String>> attribNameValMap = null;
		for (File file : fileAtribMap.keySet()) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			try {
				String xmlString = FileUtils.readFileToString(file);
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
				if (null != doc) {
					attribNameValMap = new HashMap<>();
					for (AttributeValDTO attribVal : fileAtribMap.get(file)) {
						List<String> attribValList = getValueForAttribute(doc.getDocumentElement(),
								attribVal.getAttribName());
						if (CollectionUtils.isNotEmpty(attribValList)) {
							attribNameValMap.put(attribVal.getAttribName(), attribValList);
						}
					}
				}

			} catch (ParserConfigurationException e) {
				log.error("Error while parsing XML:" + e.getMessage());
			} catch (SAXException e) {
				log.error("Error while parsing XML:" + e.getMessage());
			} catch (IOException e) {
				log.error("Error while parsing XML:" + e.getMessage());
			}
		}
		return attribNameValMap;
	}

	private List<XMLAttribDTO> parseXMLWithDOMForXMLFileAttrib(List<File> fileList) {
		List<XMLAttribDTO> xmlDataList = new ArrayList<>();
		for (File file : fileList) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			try {
				String xmlString = FileUtils.readFileToString(file);
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));

				Node node = doc.getDocumentElement();
				XMLAttribDTO xmlAttribDto = new XMLAttribDTO();
				Set<String> nodeSet = new TreeSet<>();
				listNodeAttribRecur(node, nodeSet);

				if (CollectionUtils.isNotEmpty(nodeSet)) {
					List<AttributeValDTO> attribDtoList = new ArrayList<>();
					for (String nodeName : nodeSet) {
						AttributeValDTO attribDto = new AttributeValDTO();
						attribDto.setAttribName(nodeName);
						attribDtoList.add(attribDto);
					}
					xmlAttribDto.setAttributes(attribDtoList);
				}
				xmlAttribDto.setFileName(file.getName());
				xmlDataList.add(xmlAttribDto);

			} catch (ParserConfigurationException e) {
				log.error("Error while parsing XML:" + e.getMessage());
			} catch (SAXException e) {
				log.error("Error while parsing XML:" + e.getMessage());
			} catch (IOException e) {
				log.error("Error while parsing XML:" + e.getMessage());
			}
		}
		return xmlDataList;
	}

	private List<String> getValueForAttribute(Node node, String attribName) {
		/*
		 * // for root node List<String> valueList = listNodes(node,
		 * attribName);
		 * 
		 * // for children nodes while (node.hasChildNodes()) { NodeList list =
		 * node.getChildNodes(); if (list.getLength() > 0) { for (int i = 0; i <
		 * list.getLength(); i++) { List<String> nodesList =
		 * listNodes(list.item(i), attribName); if
		 * (CollectionUtils.isNotEmpty(nodesList)) {
		 * valueList.addAll(nodesList); } } } }
		 */
		List<String> valueList = new ArrayList<>();
		listNodeAttrib(node, attribName, valueList);
		return valueList;

	}

	private List<String> listNodes(Node node, String attribName) {
		List<String> valueList = null;
		// when the node is not the root node
		if (node.hasAttributes()) {
			valueList = new ArrayList<>();
			// System.out.println(indent + " Element Attributes are:");
			NamedNodeMap attrs = node.getAttributes();
			for (int index = 0; index < attrs.getLength(); index++) {
				Attr attribute = (Attr) attrs.item(index);
				if (attribName.equalsIgnoreCase(attribute.getName())) {
					System.out.println(attribute.getName() + " = " + attribute.getValue());
					valueList.add(attribute.getValue());
				}
			}
		}
		return valueList;
	}

	private void listNodeAttribRecur(Node node, Set<String> nodeList) {
		if (node.hasAttributes()) {
			NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Attr attribute = (Attr) attrs.item(i);
				nodeList.add(attribute.getName());
			}
		}

		NodeList list = node.getChildNodes();
		if (list.getLength() > 0) {
			for (int i = 0; i < list.getLength(); i++) {
				listNodeAttribRecur(list.item(i), nodeList);
			}
		}
	}

	private void listNodeAttrib(Node node, String attribName, List<String> nodesList) {
		if (node.hasAttributes()) {
			NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Attr attribute = (Attr) attrs.item(i);
				if (attribName.equalsIgnoreCase(attribute.getName())) {
					nodesList.add(attribute.getValue());
				}
			}
		}

		NodeList list = node.getChildNodes();
		if (list.getLength() > 0) {
			for (int i = 0; i < list.getLength(); i++) {
				listNodeAttrib(list.item(i), attribName, nodesList);
			}
		}
	}

	private void setCellValue(Row row, int cellNos, String rowVal) {
		Cell cell = row.createCell(cellNos++);
		cell.setCellValue(rowVal);

	}
}
