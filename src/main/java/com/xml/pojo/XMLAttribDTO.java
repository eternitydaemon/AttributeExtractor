package com.xml.pojo;

import java.util.List;

public class XMLAttribDTO {

	private String fileName;
	private List<AttributeValDTO> attributes;

	public List<AttributeValDTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeValDTO> attributes) {
		this.attributes = attributes;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
