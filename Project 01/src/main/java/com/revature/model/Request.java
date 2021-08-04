package com.revature.model;

import java.time.LocalDate;
import java.util.UUID;

enum State { NEW, IN_PROGRESS, PENDING_REVIEW, APPROVED, APPEAL }
enum Priority { LOW, MEDIUM, HIGH, URGENT }

public class Request {
	
	private UUID id;
	private Coverage type;
	private Employee requestor;
	private Double reimburseAmount;
	private Character grade;
	private LocalDate eventDate;
	private LocalDate submittedDate;
	private State status;
	private Priority SLA;
	
	
	public Request() {
		super();
		// set id
		// this.id = ;
		this.submittedDate = LocalDate.now();
		this.status = State.NEW;
		this.SLA = Priority.MEDIUM;
	}


	public UUID getId() {
		return id;
	}


	public void setId(UUID id) {
		this.id = id;
	}


	public Coverage getType() {
		return type;
	}


	public void setType(Coverage type) {
		this.type = type;
	}


	public Employee getRequestor() {
		return requestor;
	}


	public void setRequestor(Employee requestor) {
		this.requestor = requestor;
	}


	public Double getReimburseAmount() {
		return reimburseAmount;
	}


	public void setReimburseAmount(Double reimburseAmount) {
		this.reimburseAmount = reimburseAmount;
	}


	public Character getGrade() {
		return grade;
	}


	public void setGrade(Character grade) {
		this.grade = grade;
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


	public State getStatus() {
		return status;
	}


	public void setStatus(State status) {
		this.status = status;
	}


	public Priority getSLA() {
		return SLA;
	}


	public void setSLA(Priority sLA) {
		SLA = sLA;
	}
}
