package com.revature.controllers;

import java.io.InputStream;
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

public class ManagerControllerImpl implements ManagerController {
	private static Logger log = LogManager.getLogger(ManagerControllerImpl.class);

	private static EmployeeService empService = new EmployeeService(); // Update to EmployeeService
	private static RequestService reqService = new RequestService(); // Update to RequestService

	public void reviewRequest(Context ctx) {

	}

	@Override
	public void getDoc(Context ctx) {
		log.trace("getDoc method called");

//		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
//		Employee emp = empService.searchEmployees(username);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getRole() == Role.COORDINATOR) {
			ctx.status(403);
			return;
		}

		UUID reqID = UUID.fromString(ctx.pathParam("requestId"));
		Request request = reqService.searchRequest(reqID);

		if (request == null) {
			ctx.status(404);
			ctx.html("No request matching requestID [" + reqID + "] found in DB");
			return;
		}
		String extension = ctx.header("extension");

		if (extension == null) {
			ctx.status(400);
			ctx.html("Expected filetype in header");
			return;
		}

		String filetype = FileType.toString(FileType.getDocType(extension));
		String fileURL = "";
		for (String docsURL : request.getDocs()) {
			if (docsURL.contains(filetype))
				fileURL = docsURL;
		}

		if (fileURL.equals("")) {
			ctx.status(404);
			ctx.html("File not found for Request ID: " + reqID);
			return;
		}

		try {
			InputStream doc = S3Util.getInstance().getObject(fileURL);
			ctx.result(doc);
		} catch (Exception e) {
			ctx.status(500);
			ctx.html("Unable to download document");
		}

	}

	@Override
	public void requestDoc(Context ctx) {
		log.trace("requestDoc method called");

		// Expecting the following variables in request
		// username, reqID, requestee and extension

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in user: " + loggedUser);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getRole() == Role.COORDINATOR) {
			ctx.status(403);
			return;
		}

		Employee emp = empService.searchEmployees(username);
		String requestee_user = ctx.header("requestee");
		Employee requestee = null;

		if (requestee_user == null) {
			ctx.html("Requestee wasn't specified in request header");
			ctx.status(400);
			return;
		}

		UUID reqID = UUID.fromString(ctx.pathParam("requestId"));
		// if request in pending review queue for manager, send back to queue for
		// requestee
		Request pending = loggedUser.getPendingReview().stream().filter((req) -> req.getReqID().equals(reqID))
				.findFirst().orElse(null);
		log.debug(pending);

		if (pending == null) {
			ctx.status(403);
			ctx.html("This request isn't pending your approval");
			return;
		}

		if (!requestee_user.equalsIgnoreCase(username) && !pending.getRequestees().stream()
				.anyMatch((appchain) -> appchain.equalsIgnoreCase(requestee_user))) {
			ctx.status(400);
			ctx.html("Requestee must be in chain of approvers for this request");
			return;
		}

		requestee = empService.searchEmployees(requestee_user);
		if (requestee == null) {
			ctx.status(404);
			ctx.html("Invalid username for requestee");
			return;
		}

		String extension = ctx.header("extension");
		log.debug(extension);

		if (extension == null) {
			ctx.status(400);
			ctx.html("Expected File type in header");
			return;
		}

		String filetype = FileType.toString(FileType.getDocType(extension));
		log.debug(filetype);

		if (pending.getStatus() == Status.APPROVED || pending.getStatus() == Status.DENIED) {
			ctx.status(400);
			ctx.html("This request has already processed");
			return;
		}

		String comment = loggedUser.getName() + " is requesting additional information for approval. Document of type: "
				+ filetype;
		if (pending.getComment() != null || !pending.getComment().equals("")) {
			pending.getCommHistory().append(System.getProperty("line.separator"));
			pending.getCommHistory().append(pending.getComment());
		}

		emp.setMessage(comment);
		pending.setNextApprover(requestee_user);
		pending.setComment(comment);
		pending.setStatus(Status.DOCUMENT_REQUESTED);

		reqService.addRequest(reqID, requestee);

		empService.updateEmployee(emp);
		reqService.updateRequest(pending, requestee);
		ctx.status(204);
	}

	@Override
	public void approveRequest(Context ctx) {
		log.trace("approveRequest method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		Employee emp = empService.searchEmployees(username);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getRole() == Role.COORDINATOR) {
			ctx.status(403);
			return;
		}

		UUID reqID = UUID.fromString(ctx.pathParam("requestId"));
		Request request = reqService.searchRequest(reqID);
	
		if (request == null) {
			ctx.status(404);
			ctx.html("No request matching requestID [" + reqID + "] found in DB");
			return;
		}
		
		if (!request.getRequestor().equals(username)) {
			ctx.status(400);
			ctx.html("No request matching requestID [" + reqID + "] and username (" + username + ") found in DB");
			return;
		}
	
		if (request.getStatus() == Status.APPROVED || request.getStatus() == Status.DENIED) {
			ctx.status(400);
			ctx.html("This request has already been processed");
			return;
		}

		// if request in pending review queue for manager allow approval
		Request pending = loggedUser.getPendingReview().stream().filter((req) -> req.getReqID().equals(reqID))
				.findFirst().orElse(null);
		log.debug(pending);

		if (pending == null) {
			ctx.status(403);
			ctx.html("This request isn't pending your approval");
			return;
		}

		request.getRequestees().add(loggedUser.getUsername());
	
		if (request.getComment() != null || !request.getComment().equals("")) {
			request.getCommHistory().append(System.getProperty("line.separator"));
			request.getCommHistory().append(request.getComment());
		}
		
		reqService.setNextApprover(request, emp);
		Employee approver = empService.searchEmployees(request.getNextApprover());
		reqService.addRequest(reqID, approver);

		String comment = loggedUser.getUsername() + " has approved this request. " + request.getNextApprover() + " please review";
		request.setComment(comment);
		approver.setMessage(comment);

		// update employee, this user and request in DB
		empService.updateEmployee(approver);
		empService.updateEmployee(loggedUser);
		reqService.updateRequest(request, emp);
		ctx.status(204);

	}

	@Override
	public void denyRequest(Context ctx) {
		log.trace("denyRequest method called");

		// Expecting the following variables in request
		// username, reqID and reason for rejection

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		String reason = ctx.body();
		
		log.debug(reason);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getRole() == Role.COORDINATOR) {
			ctx.status(403);
			return;
		}

		if (reason == null || reason.equals("")) {
			ctx.status(400);
			ctx.html("You need to specify a reason for declining a remimbursement request");
			return;
		}

		Employee emp = empService.searchEmployees(username);

		if (emp == null) {
			ctx.status(404);
			ctx.html("Username not found in database");
			return;
		}

		UUID reqID = UUID.fromString(ctx.pathParam("requestId"));

		// if request in pending review queue for manager, decline and remove from list
		Request pending = loggedUser.getPendingReview().stream().filter((req) -> req.getReqID().equals(reqID))
				.findFirst().orElse(null);
		log.debug(pending);

		if (pending == null) {
			ctx.status(403);
			ctx.html("This request isn't pending your approval");
			return;
		}
			
		if (!pending.getRequestor().equals(username)) {
			ctx.status(400);
			ctx.html("No request matching requestID [" + reqID + "] and username (" + username + ") found in DB");
			return;
		}
		
		if (!pending.getRequestor().equals(username)) {
			ctx.status(400);
			ctx.html("No request matching requestID [" + reqID + "] and username (" + username + ") found in DB");
			return;
		}
		
		pending.setStatus(Status.DENIED);
		if (pending.getComment() != null || !pending.getComment().equals("")) {
			pending.getCommHistory().append(System.getProperty("line.separator"));
			pending.getCommHistory().append(pending.getComment());
		}

		String comment = "Reimbursement request Denied by " + loggedUser.getUsername() + "Reason: " + reason;
		pending.setComment(comment);
		emp.setMessage(comment);

		reqService.removeRequest(pending, emp);
		for (String a : emp.getApprovalChain()) {
			Employee approver = empService.searchEmployees(a);
			reqService.removeRequest(pending, approver);
			empService.updateEmployee(approver);
		}

		// update employee, this user and request in DB
		empService.updateEmployee(emp);
		reqService.updateRequest(pending, emp);
		ctx.status(204);
	}
}
