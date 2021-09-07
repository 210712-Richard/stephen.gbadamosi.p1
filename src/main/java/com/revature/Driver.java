package com.revature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.revature.controllers.*;

import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJackson;

public class Driver {
	public static void main(String[] args) {
		DataBaseInitializer DBInit = new DataBaseInitializer();
//		DBInit.dropTables();
//		DBInit.createTables();
		DBInit.populateEmployeeTable();
		DBInit.simulateRequests();
		DBInit.initiateThreads();
		javalin();

	}


	public static void javalin() {
		EmployeeControllerImpl empController = new EmployeeControllerImpl();
		ManagerControllerImpl mgrController = new ManagerControllerImpl();
		BenCoControllerImpl benController = new BenCoControllerImpl();

		// Set up Jackson to serialize LocalDates and LocalDateTimes
		ObjectMapper jackson = new ObjectMapper();
		jackson.registerModule(new JavaTimeModule());
		jackson.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		JavalinJackson.configure(jackson);
		
		// Starts the Javalin Framework
		Javalin app = Javalin.create().start(8080);
		
		// Javalin has created a web server for us and we have
		// to tell Javalin how to handle the requests it receives.
		
		// app.METHOD("URN", CALLBACK_FUNCTION);
		// The Javalin CALLBACK_FUNCTION takes an argument ctx which 
		// represents the request and the response to the request.
		// ctx.body() - The body of the request
		// ctx.html() - Sends html as the response
		// ctx.status() - changes the status of the response
		app.get("/", (ctx)->ctx.html("Welcome to the Tuition Reimbursement Management System"
				+ " - Spark Productions Inc.\nLogin to manage reimbursement requests"));
		
		// object::method <- Reference to a method as a function we can pass to a method
		
		// As an employee, I can log into TRMS
		app.post("/users", empController::login);
		
		// As an employee, I can submit new reimbursement requests
//		app.post("/users/:username", uc::register);

		// As an employee, I can view an existing request
		app.get("/users/:username/requests/:requestId", empController::viewRequest);

		// As an employee, I can view my request history
		app.get("/users/:username/requests", empController::getRequestHistory);

		// As an employee, I can log out.
		app.delete("/users", empController::logout);
		
		// As an employee, I can upload supporting docs for review
		app.put("/users/:username/requests/:requestId/docs", empController::uploadDocs);

		// As an employee, I can confirm reimbursement request if BenCo adjusts the reimbursement amount
		app.put("/users/:username/requests/:requestId", empController::confirmRequest);
		
		// As an employee, I can cancel reimbursement request if BenCo adjusts the reimbursement amount
		app.delete("/users/:username/requests/:requestId", empController::cancelRequest);
		
		// As a manager, I can download supporting docs for review
		app.get("managers/users/:username/requests/:requestId/docs", mgrController::getDoc);

		// As a manager, I can request additional information from the employee before approval (specifying filetype and requestee in header)
		app.patch("managers/users/:username/requests/:requestId", mgrController::requestDoc);

		// As a manager, I can approve a reimbursement request
		app.put("managers/users/:username/requests/:requestId", mgrController::approveRequest);

		// As a manager, I can reject a reimbursement request
		app.delete("managers/users/:username/requests/:requestId", mgrController::denyRequest);
		
		// As a BenCo, I can request additional information from anyone up the approval chain (specifying filetype and requestee in header)
		app.patch("benco/users/:username/requests/:requestId/docs", benController::requestDoc);

		// As a BenCo, I can download requested docs for review
		app.get("benco/users/:username/requests/:requestId/docs", benController::getDoc);

		// As a BenCo, I can modify reimbursement amount prior to approval
		app.patch("benco/users/:username/requests/:requestId/:amount", benController::modifyRequest);

		// As a BenCo, I can approve a reimbursement request after review
		app.put("benco/users/:username/requests/:requestId", benController::approveRequest);

		// As a BenCo, I can reject a reimbursement request provided there's a reason
		app.delete("benco/users/:username/requests/:requestId", benController::denyRequest);

		// As a BenCo, I can confirm a reimbursement after it event date and prerequisites are met
		app.post("benco/users/:username/requests/:requestId", benController::confirmRequest);


	}
}
