package com.revature.controllers;

import io.javalin.http.Context;

public interface BenCoController {

	void requestDoc(Context ctx);
	
	void getDoc(Context ctx);
	
	void modifyRequest(Context ctx);
	
	void approveRequest(Context ctx);
	
	void denyRequest(Context ctx);
	
	void confirmRequest(Context ctx);
	
}
