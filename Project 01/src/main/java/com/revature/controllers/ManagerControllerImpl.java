package com.revature.controllers;

import java.io.InputStream;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.model.Employee;
import com.revature.model.FileType;
import com.revature.model.Request;
import com.revature.model.Role;
import com.revature.services.EmployeeService;
import com.revature.services.RequestService;
import com.revature.util.S3Util;

import io.javalin.http.Context;

public class ManagerControllerImpl implements ManagerController {
	private static Logger log = LogManager.getLogger(EmployeeControllerImpl.class);
	private static EmployeeService empService = new EmployeeService();	// Update to EmployeeService
	private static RequestService reqService = new RequestService();	// Update to RequestService

	public void reviewRequest(Context ctx) {
		
	}

	@Override
	public void updateRequest(Context ctx) {
		
	}

	@Override
	public void getDoc(Context ctx) {
		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		Employee emp = EmployeeService.empDao.searchEmployees(username);

		if(loggedUser == null) {
			ctx.status(401);
			return;
		}
		
		if(!loggedUser.getUsername().equals(username) || loggedUser.getRole() == Role.COORDINATOR) {
			ctx.status(403);
			return;
		}
		
		UUID reqID = UUID.fromString(ctx.pathParam("requestId"));
		Request request = reqService.searchRequest(reqID);
		String extension = ctx.header("extension");

		if(extension == null) {
			ctx.status(400);
			ctx.html("Expected filetype in header");
			return;
		}
		
		String filetype = FileType.toString(FileType.getDocType(extension));
		String docURL = "";
		for(String docsURL : request.getDocs()) {
			if(docsURL.contains(filetype))
				docURL = docsURL;
		}
		
		if(docURL.equals("")) {
			ctx.status(404);
			ctx.html("File not found for Request ID: " + reqID);
			return;
		}
		
		try {
			InputStream doc = S3Util.getInstance().getObject(docURL);
			ctx.result(doc);
		} catch (Exception e) {
			ctx.status(500);
			ctx.html("Unable to download document");
		}
		
	}
	
}
