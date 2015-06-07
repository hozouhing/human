package com.jeff.mvc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

public class UploadFile {
	private byte[] source;
	private String fileName;
	private String formName;
	private String extensionName;

	public UploadFile(byte[] source, String fileName, String formName) {
		super();
		this.source = source;
		this.fileName = FilenameUtils.getName(fileName);
		this.formName = formName;
		this.extensionName = FilenameUtils.getExtension(fileName);
	}

	/**
	 * 
	 * @param path
	 */
	public void save(String path) {
		try {
			@SuppressWarnings("resource")
			FileOutputStream fs = new FileOutputStream(path + "\\"
					+ this.getFileName());
			fs.write(this.getSource(), 0, this.getSource().length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void save() {
		try {
			@SuppressWarnings("resource")
			FileOutputStream fs = new FileOutputStream(MvcContext.getInstance()
					.getDefaultUploadPath() + "\\" + this.getFileName());
			fs.write(this.getSource(), 0, this.getSource().length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public byte[] getSource() {
		return source;
	}

	public void setSource(byte[] source) {
		this.source = source;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

}
