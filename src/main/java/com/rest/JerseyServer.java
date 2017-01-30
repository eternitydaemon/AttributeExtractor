package com.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dataProcessor.UploadedDataProcessor;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.xml.pojo.AttributeValDTO;
import com.xml.pojo.XMLAttribDTO;

@Path("/")
public class JerseyServer {

	Logger log = LoggerFactory.getLogger(JerseyServer.class);
	static String UPLOAD_PATH = "k:/temp/";

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayHTMLHello() {
		return "Test Jersey Stub";
	}

	@POST
	@Path("/upload")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public List<XMLAttribDTO> uploadXMLFile(@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception {

		// FileUtils.deleteDirectory(new File(UPLOAD_PATH));
		List<XMLAttribDTO> xmlFileAttribList = null;
		try {
			int read = 0;
			byte[] bytes = new byte[1024];
			System.out.println(UPLOAD_PATH + fileMetaData.getFileName());
			OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + fileMetaData.getFileName()));
			while ((read = fileInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
			UploadedDataProcessor dataProcessor = new UploadedDataProcessor();
			xmlFileAttribList = dataProcessor.getXMLFileAttribData(UPLOAD_PATH);
		} catch (IOException e) {
			log.error("Error while uploading file. Please try again !!");
			System.out.println("ERROR");
			return null;
		}
		System.out.println("success");
		// return Response.status(201).entity(obj).build();

		// check if pojo conforms to json or not
		return xmlFileAttribList;
	}

	/**
	 * Method to fetch the attributes for parsing the XML
	 * 
	 * @param data
	 * @return
	 */
	@POST
	@Path("/getAttributes")
	//@Produces(MediaType.APPLICATION_OCTET_STREAM)
	//@Produces("text/plain")
	@Produces("application/vnd.ms-excel")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAttributesForXML(String data) {

		ObjectMapper mapper = new ObjectMapper();
		String fileUrl = null;
		UploadedDataProcessor dataProcessor = new UploadedDataProcessor();
		// list of flat file and atribute mapping since jackson is adaptable to
		// that
		List<AttributeValDTO> attribValDTOList = null;
		try {
			attribValDTOList = mapper.readValue(data, TypeFactory.collectionType(List.class, AttributeValDTO.class));
			System.out.println(attribValDTOList);
		} catch (JsonParseException e) {
			log.error("exception while parsing pojo" + e.getMessage());
		} catch (JsonMappingException e) {
			log.error("exception while parsing pojo" + e.getMessage());
		} catch (IOException e) {
			log.error("exception while parsing pojo" + e.getMessage());
		}

		if (CollectionUtils.isNotEmpty(attribValDTOList)) {

			// list of file vs attribute list to be supplied for writing to
			// excel
			List<XMLAttribDTO> xmlDtoList = new ArrayList<>();

			// iterating through the attribValDTOList to generate the xmlDtoList
			for (AttributeValDTO attribVal : attribValDTOList) {
				boolean isFilePresent = false;
				XMLAttribDTO attribDTO = new XMLAttribDTO();
				List<AttributeValDTO> attribValDtoXmlAttribList = new ArrayList<>();
				for (XMLAttribDTO xmlAttribDTO : xmlDtoList) {
					if (attribVal.getFileName().equalsIgnoreCase(xmlAttribDTO.getFileName())) {
						isFilePresent = true;

						break;
					}
				}
				if (!isFilePresent) {

					attribDTO.setFileName(attribVal.getFileName());
					AttributeValDTO attributeValDTO = new AttributeValDTO();
					attributeValDTO.setAttribName(attribVal.getAttribName());
					attributeValDTO.setColName(attribVal.getColName());
					attributeValDTO.setDataValidation(attribVal.getDataValidation());
					attribValDtoXmlAttribList.add(attributeValDTO);

				} else {
					for (XMLAttribDTO xmlDto : xmlDtoList) {
						if (attribVal.getFileName().equalsIgnoreCase(xmlDto.getFileName())) {
							AttributeValDTO attributeValDTO = new AttributeValDTO();
							attributeValDTO.setAttribName(attribVal.getAttribName());
							attributeValDTO.setColName(attribVal.getColName());
							attributeValDTO.setDataValidation(attribVal.getDataValidation());
							// attribValDtoXmlAttribList.add(attributeValDTO);
							xmlDto.getAttributes().add(attributeValDTO);
						}
					}
				}
				attribDTO.setAttributes(attribValDtoXmlAttribList);
				xmlDtoList.add(attribDTO);
			}

			// receives attribute name vs list of attribute values
			Map<String, List<String>> attribNameValMap = dataProcessor.parseXMLtoAtrribValue(UPLOAD_PATH, xmlDtoList);
			if (MapUtils.isNotEmpty(attribNameValMap)) {
				// send the map to generate the excel spreadsheet
				fileUrl = dataProcessor.writeDataToExcel(UPLOAD_PATH, attribNameValMap);
				System.out.println("excel parsing:" + fileUrl);
			}
		}
		if (null != fileUrl) {
			// send the list to generate the excel spreadsheet for attribute
			// data validation map
			File parsedExcel = new File(fileUrl);
			ResponseBuilder response = Response.ok((Object) parsedExcel);
			//response.header("Content-Type", "application/vnd.ms-excel");
			System.out.println(parsedExcel.getName());
			response.header("Content-Disposition", "attachment; filename="+parsedExcel.getName());

			if (dataProcessor.writeAttribDataToExcel(fileUrl, attribValDTOList)) {
				// return Response.status(200).build();
				return response.build();

			} else {
				return Response.status(500).build();
			}
		} else {
			return Response.status(500).build();
		}
	}
}
