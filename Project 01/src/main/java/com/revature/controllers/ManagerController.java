package com.revature.controllers;

import io.javalin.http.Context;

public interface ManagerController {
	
	void reviewRequest(Context ctx);
	
	void updateRequest(Context ctx);
	
	void getDoc(Context ctx);
}