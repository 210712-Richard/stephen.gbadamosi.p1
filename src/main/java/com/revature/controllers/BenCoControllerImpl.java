package com.revature.controllers;

import java.io.InputStream;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.revature.model.Employee;
import com.revature.model.FileType;
import com.revature.model.Request;
import com.revature.model.Status;
import com.revature.model.Team;
import com.revature.services.EmployeeService;
import com.revature.services.RequestService;
import com.revature.util.S3Util;

import io.javalin.http.Context;

public class BenCoControllerImpl implements BenCoController {
	private static Logger log = LogManager.getLogger(BenCoControllerImpl.class);
	private static EmployeeService empService = new EmployeeService();
	private static RequestService reqService = new RequestService();

	@Override
	public void requestDoc(Context ctx) {
		log.trace("requestDoc method called");

		// Expecting the following variables in request
		// username, reqID, requestee and extension

		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in User: " + loggedUser);
		String username = ctx.pathParam("username");

		loggedUser = empService.searchEmployees(loggedUser.getUsername());

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getDept().getName() != Team.BENEFITS) {
			ctx.status(403);
			return;
		}

		String requestee_user = ctx.header("requestee");

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
		
		if (!pending.getRequestor().equals(username)) {
			ctx.status(400);
			ctx.html("No request matching requestID [" + reqID + "] and username (" + username + ") found in DB");
			return;
		}
		
		if (pending.getStatus() == Status.APPROVED || pending.getStatus() == Status.DENIED) {
			ctx.status(400);
			ctx.html("This request has already been processed");
			return;
		}

		if (!requestee_user.equalsIgnoreCase(pending.getRequestor()) && !pending.getRequestees().stream()
				.anyMatch((appchain) -> appchain.equalsIgnoreCase(requestee_user))) {
			ctx.status(400);
			ctx.html("Requestee must be in chain of approvers for this request");
			return;
		}

		Employee requestee = empService.searchEmployees(requestee_user);

		if (requestee == null) {
			ctx.status(404);
			ctx.html("Invalid username for requestee");
			return;
		}

		String extension = ctx.header("extension");
		log.debug(extension);
		
		if (extension == null) {
			ctx.status(400);
			ctx.html("Expected File type in request header");
			return;
		}

		String filetype = FileType.toString(FileType.getDocType(extension));
		log.debug(filetype);

		if (pending.getComment() != null || !pending.getComment().equals("")) {
			pending.getCommHistory().append(System.getProperty("line.separator"));
			pending.getCommHistory().append(pending.getComment());
		}

		String comment = loggedUser.getName() + " is requesting additional information for approval. Document of type: "
				+ filetype;
		pending.setComment(comment);
		requestee.setMessage(comment);
		pending.setStatus(Status.DOCUMENT_REQUESTED);
		
		reqService.addRequest(reqID, requestee);

		empService.updateEmployee(requestee);
		empService.updateEmployee(loggedUser);
		reqService.updateRequest(pending, requestee);
		ctx.status(204);
	}

	@Override
	public void getDoc(Context ctx) {
		log.trace("getDoc method called");

		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in User: " + loggedUser);
		
		String username = ctx.pathParam("username");
		loggedUser = empService.searchEmployees(loggedUser.getUsername());

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getDept().getName() != Team.BENEFITS) {
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
		
		String extension = ctx.header("extension");

		if (extension == null) {
			ctx.status(400);
			ctx.html("Expected filetype in request header");
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
			ctx.html(filetype + " not found for Request ID: " + reqID);
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
	public void modifyRequest(Context ctx) {
		log.trace("modRequest method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in User: " + loggedUser);

		Employee emp = empService.searchEmployees(username);
		Double amount = Double.valueOf(ctx.pathParam("amount"));
		log.debug("Requestor: " + emp);
		log.debug("BenCo adjusted reimbursement to: " + amount);

		if (amount == 0.0) {
			ctx.status(400);
			ctx.html("Do the right thing and deny the request instead");
			return;
		}

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getDept().getName() != Team.BENEFITS) {
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
		
		if (!request.getRequestor().equals(username)) {
			ctx.status(400);
			ctx.html("No request matching requestID [" + reqID + "] and username (" + username + ") found in DB");
			return;
		}
		
		if (!empService.scanApprovalChain(emp, loggedUser.getUsername())) {
			ctx.status(403);
			ctx.html("You do not have permission to modify a request if you aren't the designated BenCo approver");
			return;
		}

		if (request.getComment() != null && !request.getComment().equals("")) {
			request.getCommHistory().append(System.getProperty("line.separator"));
			request.getCommHistory().append(request.getComment());
		}

		String comment = "Benco [" + loggedUser.getUsername() + "] has modified reimbursement amount to " + amount
				+ " Please review and confirm if you accept.";

		request.setComment(comment);
		request.setReimburseAmount(amount);
		emp.setMessage(comment);

		request.setStatus(Status.PENDING_REVIEW);

		reqService.addRequest(reqID, emp);
		
		empService.updateEmployee(emp);
		reqService.updateRequest(request, emp);
		ctx.status(204);

	}

	@Override
	public void approveRequest(Context ctx) {
		log.trace("approveRequest method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in User: " + loggedUser);

		Employee emp = empService.searchEmployees(username);
		log.debug(emp);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getDept().getName() != Team.BENEFITS) {
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
		
		if (!empService.scanApprovalChain(emp, loggedUser.getUsername())) {
			ctx.status(403);
			ctx.html("You do not have permission to approve a request if you aren't the designated BenCo approver");
			return;
		}

		request.setStatus(Status.APPROVED);
		if (request.getComment() != null && !request.getComment().equals(""))
			request.getCommHistory().append(System.getProperty("line.separator"));
		request.getCommHistory().append(request.getComment());

		String comment = loggedUser.getName() + " has approved your request!";
		request.setComment(comment);
		emp.setMessage(comment);
		request.getCommHistory().append(System.getProperty("line.separator"));
		request.getCommHistory().append(request.getComment());

		emp.setReimburseBalance(emp.getReimburseBalance() - request.getReimburseAmount());
		emp.setReimburseRecvd(emp.getReimburseRecvd() + request.getReimburseAmount());

// Remove request from pending review lists for approval chain and requestor
		
		reqService.removeRequest(request, emp);
		for(String a : emp.getApprovalChain()) {
			Employee approver = empService.searchEmployees(a);
			reqService.removeRequest(request, approver);
			empService.updateEmployee(approver);
		}

		
		empService.updateEmployee(emp);
		reqService.updateRequest(request, emp);
		ctx.status(204);

	}

	@Override
	public void denyRequest(Context ctx) {
		log.trace("denyRequest method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in User: " + loggedUser);
		loggedUser = empService.searchEmployees(loggedUser.getUsername());
		Employee emp = empService.searchEmployees(username);
		log.debug(emp);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getDept().getName() != Team.BENEFITS) {
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
		
		if (!empService.scanApprovalChain(emp, loggedUser.getUsername())) {
			ctx.status(403);
			ctx.html("You do not have permission to deny a request if you aren't the designated BenCo approver");
			return;
		}

		request.setStatus(Status.DENIED);

		if (request.getComment() != null && !request.getComment().equals(""))
			request.getCommHistory().append(System.getProperty("line.separator"));
		request.getCommHistory().append(request.getComment());

		String comment = loggedUser.getName() + " has denied your request for " + request.getReimburseAmount()
				+ " You can request an appeal for this reimbursement if necessary";
		request.setComment(comment);
		request.getCommHistory().append(System.getProperty("line.separator"));
		request.getCommHistory().append(request.getComment());
		emp.setMessage(comment);

// Remove request from pending review list for all in approval chain and requestor
		
		reqService.removeRequest(request, emp);
		for(String a : emp.getApprovalChain()) {
			Employee approver = empService.searchEmployees(a);
			reqService.removeRequest(request, approver);
			empService.updateEmployee(approver);
		}
	
		empService.updateEmployee(emp);
		reqService.updateRequest(request, emp);
		ctx.status(204);
	}

	@Override
	public void confirmRequest(Context ctx) {
		log.trace("confirmRequest method called");

		String username = ctx.pathParam("username");
		Employee loggedUser = ctx.sessionAttribute("loggedUser");
		log.debug("Logged in User: " + loggedUser);

		loggedUser = empService.searchEmployees(loggedUser.getUsername());
		Employee emp = empService.searchEmployees(username);
		log.debug(emp);

		if (loggedUser == null) {
			ctx.status(401);
			return;
		}

		if (loggedUser.getDept().getName() != Team.BENEFITS) {
			ctx.status(403);
			return;
		}

		if (!empService.scanApprovalChain(emp, loggedUser.getUsername())) {
			ctx.status(403);
			ctx.html("You do not have change request status if you aren't the designated BenCo approver");
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
		
		request.setStatus(Status.APPROVED);
		if (request.getComment() != null && !request.getComment().equals(""))
			request.getCommHistory().append(System.getProperty("line.separator"));
		request.getCommHistory().append(request.getComment());

		String comment = loggedUser.getName() + " has approved your request!";
		request.setComment(comment);
		emp.setMessage(comment);
		request.getCommHistory().append(System.getProperty("line.separator"));
		request.getCommHistory().append(request.getComment());

		emp.setReimburseBalance(emp.getReimburseBalance() - request.getReimburseAmount());
		emp.setReimburseRecvd(emp.getReimburseRecvd() + request.getReimburseAmount());

// Remove request from pending review list for all in approval chain and requestor
		
		reqService.removeRequest(request, emp);
		for(String a : emp.getApprovalChain()) {
			Employee approver = empService.searchEmployees(a);
			reqService.removeRequest(request, approver);
			empService.updateEmployee(approver);
		}
		
		empService.updateEmployee(emp);
		reqService.updateRequest(request, emp);
		ctx.status(204);
	}

}
