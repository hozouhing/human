package com.jeff.mvc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadWapper extends HttpServletRequestWrapper {

	private Set<UploadFile> uploadFiles;

	public UploadWapper(HttpServletRequest request) {
		super(request);
		uploadFiles = new HashSet<UploadFile>();
		setFileMap(request);
	}

	private void setFileMap(HttpServletRequest request) {
		boolean tag = ServletFileUpload.isMultipartContent(request);
		if (tag) {
			boolean isMul = ServletFileUpload.isMultipartContent(request);
			if (isMul) {
				try {
					InputStream is;
					ByteArrayOutputStream baos;
					ServletFileUpload upload = new ServletFileUpload();
					FileItemIterator iter = upload.getItemIterator(request);
					while (iter.hasNext()) {
						FileItemStream fis = iter.next();
						is = fis.openStream();
						if (fis.isFormField()) {
							continue;
						} else {
							/**
							 * 将一个文件输入流转换为字节数组需要通过ByteArrayoutputStream
							 */
							baos = new ByteArrayOutputStream();
							int len = 0;
							byte[] buf = new byte[1024];
							while ((len = is.read(buf)) > 0) {
								// 这里就可以把输入流输出到一个直接数组流中
								baos.write(buf, 0, len);
							}
							byte[] fs = baos.toByteArray();
							String fileName = fis.getName();
							String formName = fis.getFieldName();
							//System.out.println(fileName);
							uploadFiles.add(new UploadFile(fs, fileName,formName));
						}
					}
				} catch (Exception e) {
				}
			}
			this.setAttribute("uploadFiles", uploadFiles);
		}
	}

}
