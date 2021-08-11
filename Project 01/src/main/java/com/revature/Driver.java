package com.revature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.revature.controllers.*;

import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJackson;

public class Driver {
	public static void main(String[] args) {
	
//		DataBaseInitializer.dropTables();
//		DataBaseInitializer.createTables();
		DataBaseInitializer.populateEmployeeTable();
		DataBaseInitializer.simulateRequests();
		javalin();

	}


	public static void javalin() {
		EmployeeControllerImpl empController = new EmployeeControllerImpl();
		RequestControllerImpl reqController = new RequestControllerImpl();
		ManagerControllerImpl mgrController = new ManagerControllerImpl();

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

		// As an employee, I can view my request history
		app.get("/users/:username/requests", empController::getRequestHistory);

		// As a user, I can log out.
		app.delete("/users", empController::logout);
		
		// As an employee, I can upload supporting docs for review
		app.put("/users/:username/requests/:requestId/docs", empController::uploadDocs);
		
		// As a manager, I can download supporting docs for review
		app.get("managers/users/:username/requests/:requestId/docs", mgrController::getDoc);

	}
}
