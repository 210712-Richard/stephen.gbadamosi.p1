package com.revature.model;

import java.io.Serializable;
import java.util.UUID;
import com.revature.model.*;

public interface RequestObject extends Serializable {
	Double MAX_REIMBURSEMENT = 1000.0;
	
	
	UUID getId();
	void setId(UUID id);
	
	String getRequestName();
	void setRequestName(String name);
	
	RequestType getRequestType();
	void setRequestType(RequestType name);
	
	
	
}
