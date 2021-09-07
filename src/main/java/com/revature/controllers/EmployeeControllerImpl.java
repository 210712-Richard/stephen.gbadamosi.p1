package com.revature.controllers;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.model.Employee;
import com.revature.model.FileType;
import com.revature.model.Request;
import com.revature.model.Role;
import com.revature.model.Status;
import com.revature.services.EmployeeService;
import com.revature.services.RequestService;
import com.revature.util.S3Util;

import io.javalin.http.Context;

public class EmployeeControllerImpl implements EmployeeController {
	private static Logger log = LogManager.getLogger(EmployeeControllerImpl.class);
	
	private static EmployeeService empService = new EmployeeService(); // Update to EmployeeService
	private static RequestService reqService = new RequestService(); // Update to RequestService

	public void login(Context ctx) {
		log.trace("Login method called");
		log.debug(ctx.body());
		// Try to use a JSON Marshaller to create an object of this type.
		// Javalin does not come with a JSON Marshaller but prefers Jackson. You could
		// also use GSON
		Employee emp = ctx.bodyAsClass(Employee.class);
		log.debug(emp);

		// Use the request data to obtain the data requested
		emp = empService.login(emp.getUsername());
		log.debug(emp);

		// Create a session if the login was successful
		if(emp != null) {
			// Save the user object as loggedUser in the session
			ctx.sessionAttribute("loggedUser", emp);

			// Try to use the JSON Marshaller to send a JSON string of this object back to
			// the client
			ctx.json(emp);
			ctx.status(200);
			return;
		}

		// Send a 401 is the login was not successful
		ctx.html("Invalid credentials");
		ctx.status(401);
	}

	@Override
	public void viewRequest(Context ctx) {
		log.trace("viewRequest method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in user: " + loggedUser);
		Employee emp = empService.searchEmployees(username);

		if(loggedUser == null) {
			ctx.status(401);
			return;
		}

		if(!loggedUser.getUsername().equals(username)) {
			ctx.status(403);
			ctx.html("Cannot view another user's request details");
			return;
		}

		if(emp == null) {
			ctx.status(400);
			ctx.html("Invalid User");
			return;
		}	
		
		UUID req = UUID.fromString(ctx.pathParam("requestId"));	
		Request request = reqService.searchRequest(req);
		
		if(request == null) {
			ctx.status(404);
			ctx.html("Could not find request matching request ID");
			return;	
		}

		ctx.status(200);
		ctx.json(request);
	}

	@Override
	public void getRequestHistory(Context ctx) {
		log.trace("getRequestHistory method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in user: " + loggedUser);
		Employee emp = empService.searchEmployees(username);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if(!loggedUser.getUsername().equals(username) || loggedUser.getRole() == Role.COORDINATOR) {
			ctx.status(403);
			ctx.html("Cannot view another user's request history");
			return;
		}

		if (emp == null) {
			ctx.status(400);
			ctx.html("Invalid User");
			return;
		}

		List<Request> history = reqService.viewRequests(emp);
		ctx.status(200);
		ctx.json(history);

		if (history.size() == 0) {
			ctx.html("You have not created any requests");
			return;
		}
	}

	@Override
	public void uploadDocs(Context ctx) {
		log.trace("uploadDocs method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in user: " + loggedUser);
		Employee emp = empService.searchEmployees(username);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if(!loggedUser.getUsername().equals(username)) {
			ctx.html("Invalid request: you cannot upload docs to another user's request");
			ctx.status(403);
			return;
		}

		UUID reqID = UUID.fromString(ctx.pathParam("requestId"));
		log.debug(reqID);
		Request request = reqService.searchRequest(reqID);
		log.debug(request);

		if (request == null) {
			ctx.status(404);
			ctx.html("No request with ID " + ctx.pathParam("requestId") + " found");
			return;
		}

		if (request.getStatus() == Status.APPROVED || request.getStatus() == Status.DENIED) {
			ctx.status(400);
			ctx.html("This request has already processed");
			return;
		}
						
		String extension = ctx.header("extension");

		if (extension == null) {
			ctx.status(400);
			ctx.html("Expected filetype in header");
			return;
		}
		
		String filetype = FileType.toString(FileType.getDocType(extension));
		log.debug(filetype);
		
		String approver = "";
		// Email approver so check for username in header
		Employee mgr = null;		
		if(filetype.equalsIgnoreCase("email")) {
			approver =  ctx.pathParam("approver");
			if(approver == null || approver.equals("")) {
				ctx.status(400);
				ctx.html("Expected email approver username to be specified in request header");
				return;
			}
			
			for(String nextapprover : emp.getApprovalChain()) {
				if(nextapprover.equals(approver))
					mgr = empService.searchEmployees(approver);		

			}
			
			if(mgr == null) {
				ctx.status(400);
				ctx.html("Invalid username for email approver");
				return;
			}
		}
		
		else {
			mgr = empService.searchEmployees(emp.getManager());
		}
		
		String key = "";
		key = username + "/" + mgr.getUsername() + "/" + filetype + "/" + ctx.pathParam("requestId") + extension;
		
		S3Util.getInstance().uploadToBucket(key, ctx.bodyAsBytes());
		request.getDocs().add(key);
				
		// if request in pending review queue for user, move to first on approval chain
		// add user to list of requestees
		// set request status to In-progress
		request.getRequestees().add(username);
		request.setStatus(Status.IN_PROGRESS);
		
		if(request.getComment() != null || !request.getComment().equals("")) {
			request.getCommHistory().append(System.getProperty("line.separator"));
			request.getCommHistory().append(request.getComment());			
		}
		
		if(filetype.equalsIgnoreCase("email") || extension.equalsIgnoreCase(".msg")) {
			reqService.setNextApprover(request, emp);
			reqService.addRequest(reqID, mgr);
			String comment = mgr.getUsername() + " has approved the request. Moving to next approver: " + request.getNextApprover();
			request.setComment(comment);
			
		}
		
		else {
			String comment = emp.getUsername() + " has uploaded requested documents, please review";
			request.setComment(comment);
			mgr.setMessage(comment);
			request.setNextApprover(mgr.getUsername());
		}
				
		empService.updateEmployee(emp);
		empService.updateEmployee(mgr);
		reqService.updateRequest(request, emp);
		ctx.status(204);
	}

	@Override
	public void confirmRequest(Context ctx) {
		log.trace("confirmRequest method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in user: " + loggedUser);

		if(loggedUser == null) {
			ctx.status(401);
			return;
		}

		if(!loggedUser.getUsername().equals(username)) {
			ctx.status(403);
			ctx.html("Cannot modify another user's reimbursement request");
			return;
		}
		
		UUID req = UUID.fromString(ctx.pathParam("requestId"));	
		Request request = reqService.searchRequest(req);
		
		if(request == null || !request.getRequestor().equals(loggedUser.getUsername())) {
			ctx.status(404);
			ctx.html("Could not find request matching username and request ID");
			return;	
		}
		
		request.setStatus(Status.IN_PROGRESS);
		if(request.getComment() != null || !request.getComment().equals("")) {
			request.getCommHistory().append(System.getProperty("line.separator"));
			request.getCommHistory().append(request.getComment());
		}
		
		String comment = username + " has confirmed reimbursement request - pending BenCo sign off";
		request.setComment(comment);
		
		ctx.status(200);
		ctx.html("Request confirmed by user");

	}
	
	@Override
	public void cancelRequest(Context ctx) {
		log.trace("cancelRequest method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");

		if(loggedUser == null) {
			ctx.status(401);
			return;
		}

		if(!loggedUser.getUsername().equals(username)) {
			ctx.status(403);
			ctx.html("Cannot modify another user's reimbursement request");
			return;
		}
		
		UUID req = UUID.fromString(ctx.pathParam("requestId"));	
		Request request = reqService.searchRequest(req);
		
		if(request == null || !request.getRequestor().equals(loggedUser.getUsername())) {
			ctx.status(404);
			ctx.html("Could not find request matching username and request ID");
			return;	
		}
		
		request.setStatus(Status.CANCELLED);
		if(request.getComment() != null || !request.getComment().equals("")) {
			request.getCommHistory().append(System.getProperty("line.separator"));
			request.getCommHistory().append(request.getComment());
		}
		
		Employee approver = empService.searchEmployees(request.getNextApprover());
		String comment = username + " has cancelled reimbursement request";
		request.setComment(comment);
		request.getCommHistory().append(System.getProperty("line.separator"));
		request.getCommHistory().append(request.getComment());
		
		approver.setMessage(comment);
		loggedUser.setMessage(comment);
		
		reqService.removeRequest(request, loggedUser);
		for(String a : loggedUser.getApprovalChain()) {
			approver = empService.searchEmployees(a);
			reqService.removeRequest(request, approver);
			empService.updateEmployee(approver);
		}
		
		
		ctx.status(200);
		ctx.html("Request cancelled by user");

	}

	@Override
	public void logout(Context ctx) {
		log.trace("logout method called");

		ctx.req.getSession().invalidate();
		ctx.html("You have successfully logged out of TRMS");
		ctx.status(200);
	}

}
