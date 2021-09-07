package com.revature.controllers;

import io.javalin.http.Context;

public interface EmployeeController {

	void login(Context ctx);
	
	void viewRequest(Context ctx);

	void getRequestHistory(Context ctx);

	void uploadDocs(Context ctx);
	
	void confirmRequest(Context ctx);
	
	void cancelRequest(Context ctx);

	void logout(Context ctx);
}
