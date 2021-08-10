package com.revature.controllers;

import io.javalin.http.Context;

public interface EmployeeController {

	void login(Context ctx);

	void getRequestHistory(Context ctx);

	void uploadDocs(Context ctx);

	void logout(Context ctx);
}
