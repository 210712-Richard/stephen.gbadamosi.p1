package com.revature.controllers;


import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.model.*;
import com.revature.services.EmployeeService;
import com.revature.services.RequestService;
import com.revature.util.S3Util;

import io.javalin.http.Context;



public class EmployeeControllerImpl implements EmployeeController {
	private static Logger log = LogManager.getLogger(EmployeeControllerImpl.class);
	private static EmployeeService empService = new EmployeeService();	// Update to EmployeeService
	private static RequestService reqService = new RequestService();	// Update to RequestService
	
	public void login(Context ctx) {
		log.trace("Login method called");
		log.debug(ctx.body());
		// Try to use a JSON Marshaller to create an object of this type.
		// Javalin does not come with a JSON Marshaller but prefers Jackson. You could also use GSON
		Employee emp = ctx.bodyAsClass(Employee.class);
		log.debug(emp);
		
		// Use the request data to obtain the data requested
		emp = empService.login(emp.getUsername());
		log.debug(emp);
		
		// Create a session if the login was successful
		if(emp != null) {
			// Save the user object as loggedUser in the session
			ctx.sessionAttribute("loggedUser", emp);
			
			// Try to use the JSON Marshaller to send a JSON string of this object back to the client
			ctx.json(emp);
			ctx.status(200);
			return;
		}
		
		// Send a 401 is the login was not successful
		ctx.html("Invalid credentials");
		ctx.status(401);
	}

	@Override
	public void getRequestHistory(Context ctx) {
		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		Employee emp = EmployeeService.empDao.searchEmployees(username);

		if(loggedUser == null) {
			ctx.status(401);
			return;
		}
		
		if(!loggedUser.getUsername().equals(username)) {
			ctx.status(403);
			ctx.html("Cannot view another user's request history");
			return;
		}
				
		if(emp == null) {
			ctx.status(400);
			ctx.html("Invalid User");
			return;
		}
		
		List<Request> history = reqService.viewRequests(emp);
		ctx.status(200);
		ctx.json(history);
		
		if(history.size() == 0) {
			ctx.html("You have not created any requests");
			return;
		}
	}

	@Override
	public void uploadDocs(Context ctx) {
		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		Employee emp = EmployeeService.empDao.searchEmployees(username);

		if(loggedUser == null) {
			ctx.status(401);
			return;
		}
		
		if(!loggedUser.getUsername().equals(username)) {
			ctx.status(403);
			return;
		}
		
		UUID reqID = UUID.fromString(ctx.pathParam("requestId"));
		log.debug(reqID);
		Request request = reqService.searchRequest(reqID);
		log.debug(request);
		
		if(request == null)
		{
			ctx.status(404);
			ctx.html("No request with ID " + ctx.pathParam("requestId") + " found");
			return;
		}
		
		String extension = ctx.header("extension");

		if(extension == null) {
			ctx.status(400);
			ctx.html("Expected filetype in header");
			return;
		}
		String filetype = FileType.toString(FileType.getDocType(extension));
		log.debug(filetype);
		String key = username + "/" + filetype + "/" + ctx.pathParam("requestId") + extension;
		S3Util.getInstance().uploadToBucket(key, ctx.bodyAsBytes());
		request.getDocs().add(key);
		reqService.updateRequest(request, emp);				
	}

	@Override
	public void logout(Context ctx) {
		ctx.req.getSession().invalidate();
		ctx.html("You have successfully logged out of TRMS");
		ctx.status(200);
	}
	
	
}
