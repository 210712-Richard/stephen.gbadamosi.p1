package com.revature.controllers;

import io.javalin.http.Context;

public interface ManagerController {
	
	void reviewRequest(Context ctx);
		
	void requestDoc(Context ctx);
	
	void getDoc(Context ctx);

	void approveRequest(Context ctx);

	void denyRequest(Context ctx);

}