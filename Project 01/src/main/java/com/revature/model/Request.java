package com.revature.model;

import java.time.LocalDate;
import java.util.UUID;

import com.revature.data.EmployeeDAO;

public class Request {
	
	private UUID reqId;
	private String description;
	private Coverage type;
	private String requestor;
	private Double cost;
	private Double reimburseAmount;
	private String docsURL;
	private String passingGrade;
	private LocalDate eventDate;
	private LocalDate submittedDate;
	private Status status;
	private Priority SLA;
	private String comment;
	private StringBuilder comm_history;
	
	
	public Request() {
		super();
		this.reqId = UUID.randomUUID();
		this.description = "";
		this.submittedDate = LocalDate.now();
		this.status = Status.NEW;
		this.status.numApprovals = 3;
		this.SLA = Priority.MEDIUM;
		this.comment = "";
		this.comm_history = new StringBuilder();
	}
	
	public Request(String desc, Coverage type, String requestor, Double cost, LocalDate eventDate) {
		super();
		this.reqId = UUID.randomUUID();
		this.description = desc;
		this.type = type;
		this.eventDate = eventDate;
		this.submittedDate = LocalDate.now();
		this.status = Status.NEW;
		this.status.numApprovals = 3;
		this.SLA = Priority.MEDIUM;
		if(eventDate.isAfter(LocalDate.now().plusDays(3)))
			this.SLA = Priority.URGENT;
		if(eventDate.isAfter(LocalDate.now().plusWeeks(1)))
			this.SLA = Priority.HIGH;
		this.comment = "";
		this.comm_history = new StringBuilder();
	}


	public UUID getReqID() {
		return reqId;
	}


	public void setReqID(UUID rid) {
		this.reqId = rid;
	}


	public String getDescription() {		
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public Coverage getType() {
		return type;
	}


	public void setType(Coverage type) {
		this.type = type;
	}


	public String getRequestor() {
		return requestor;
	}


	public void setRequestor(Employee requestor) {
		this.requestor = requestor.getUsername();
	}

	public Double getCost() {
		return cost;
	}


	public void setCost(Double cost) {
		this.cost = cost;
	}


	public Double getReimburseAmount() {
		return reimburseAmount;
	}


	public void setReimburseAmount(Double reimburseAmount) {
		this.reimburseAmount = reimburseAmount;
	}

	public String getDocsURL() {
		return docsURL;
	}


	public void setDocsURL(String docs) {
		this.docsURL = docs;
	}


	public String getPassingGrade() {
		return passingGrade;
	}


	public void setPassingGrade(String passing_grade) {
		this.passingGrade = passing_grade;
	}


	public StringBuilder getCommHistory() {
		return comm_history;
	}


	public void setCommHistory(StringBuilder comm_history) {
		this.comm_history = comm_history;
	}


	public LocalDate getEventDate() {
		return eventDate;
	}


	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}


	public LocalDate getSubmittedDate() {
		return submittedDate;
	}


	public void setSubmittedDate(LocalDate submittedDate) {
		this.submittedDate = submittedDate;
	}


	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}


	public Priority getSLA() {
		return SLA;
	}


	public void setSLA(Priority sla) {
		SLA = sla;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		if(comment == null) {
			comment = "";
		}
		
		this.comment = comment;
	}
}
