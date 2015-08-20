package com.hiyun.tueasy.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.hiyun.tueasy.core.PoiReadExcelHelper;
import com.hiyun.tueasy.util.FileUtils;

@Controller
@RequestMapping(value = "/common")
public class CommonController {

	@Value("${uploadFilePath}")
	private String uploadFilePath;
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public String upload(HttpServletRequest request,
			HttpServletResponse response) {
		List<String> fileNames = new ArrayList<String>();
		try {
			CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
					request.getSession().getServletContext());
			if (multipartResolver.isMultipart(request)) { // 判断request是否有文件上传
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				Iterator<String> it = multiRequest.getFileNames();
				while (it.hasNext()) {
					MultipartFile file = multiRequest.getFile(it.next());
					if (file != null) {
						String originalFileName = URLDecoder.decode(file.getOriginalFilename(), "UTF-8");
						File localFile = new File(this.uploadFilePath,
								originalFileName);
						int index = 1;
						while(localFile.exists()){
							localFile = new File(this.uploadFilePath,
									FileUtils.renameName(originalFileName, index++));
						}
						fileNames.add(localFile.getAbsolutePath());
						file.transferTo(localFile); // 将上传文件写到服务器上指定的文件
					}
				}
				
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return PoiReadExcelHelper.getExcelAsJson(fileNames.toArray(new String[fileNames.size()]));
	}
	
	@RequestMapping(value = "/getDataByColumn", method = RequestMethod.GET)
	@ResponseBody
	public String getDataByColumn(@RequestParam String uuid,@RequestParam String tableName,@RequestParam String column,HttpServletRequest request,
			HttpServletResponse response) {
		
		try {
			tableName = URLDecoder.decode(tableName,"UTF-8");
			column =  URLDecoder.decode(column,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return PoiReadExcelHelper.getExcelJsonByColumn(uuid, tableName, column);
				
		
	}
	

}
