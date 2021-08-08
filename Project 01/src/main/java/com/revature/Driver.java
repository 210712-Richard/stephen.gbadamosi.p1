package com.revature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJackson;

public class Driver {
	public static void main(String[] args) {
		//instantiateDatabase();
	
//		DataBaseInitializer.dropTables();
		DataBaseInitializer.createTables();
		DataBaseInitializer.populateEmployeeTable();
//		javalin();
//		System.exit(0);
	}


	public static void javalin() {
		
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
		
//		// As a user, I can log in.
//		app.post("/users", uc::login);
//		// As a user, I can register for a player account.
//		app.put("/users/:username", uc::register);
//		// As a user, I can log out.
//		app.delete("/users", uc::logout);
//		
//		// As an admin, I can upload a picture for a Gacha
//		app.put("/gachas/:gachaRarity/:gachaName/pictureUrl", gachaController::uploadPicture);
//		
//		// As a user, I can download a picture for a Gacha
//		app.get("/gachas/:gachaRarity/:gachaName/pictureUrl", gachaController::getPicture);
	}
}
