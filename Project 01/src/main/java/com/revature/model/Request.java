package com.revature.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Request {
	
	private UUID reqId;
	private String description;
	private Coverage type;
	private String requestor;
	private Double cost;
	private List<String> docs;
	private Double reimburseAmount;
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
		this.docs = new ArrayList<>();
	}
	
	public Request(String desc, Coverage type, String requestor, Double cost, LocalDate eventDate) {
		super();
		this.reqId = UUID.randomUUID();
		this.description = desc;
		this.requestor = requestor;
		this.type = type;
		this.cost = cost;
		this.eventDate = eventDate;
		this.submittedDate = LocalDate.now();
		this.docs = new ArrayList<>();
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


	public List<String> getDocs() {
		return docs;
	}

	public void setDocs(List<String> docs) {
		this.docs = docs;
	}

	public Double getReimburseAmount() {
		return reimburseAmount;
	}


	public void setReimburseAmount(Double reimburseAmount) {
		this.reimburseAmount = reimburseAmount;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((SLA == null) ? 0 : SLA.hashCode());
		result = prime * result + ((comm_history == null) ? 0 : comm_history.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((cost == null) ? 0 : cost.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
		result = prime * result + ((passingGrade == null) ? 0 : passingGrade.hashCode());
		result = prime * result + ((reimburseAmount == null) ? 0 : reimburseAmount.hashCode());
		result = prime * result + ((reqId == null) ? 0 : reqId.hashCode());
		result = prime * result + ((requestor == null) ? 0 : requestor.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((submittedDate == null) ? 0 : submittedDate.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (SLA != other.SLA)
			return false;
//		if (comm_history == null) {
//			if (other.comm_history != null)
//				return false;
//		} else if (!comm_history.equals(other.comm_history))
//			return false;
//		if (comment == null) {
//			if (other.comment != null)
//				return false;
//		} else if (!comment.equals(other.comment))
//			return false;
		if (cost == null) {
			if (other.cost != null)
				return false;
		} else if (!cost.equals(other.cost))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
//		if (docsURL == null) {
//			if (other.docsURL != null)
//				return false;
//		} else if (!docsURL.equals(other.docsURL))
//			return false;
		if (eventDate == null) {
			if (other.eventDate != null)
				return false;
		} else if (!eventDate.equals(other.eventDate))
			return false;
//		if (passingGrade == null) {
//			if (other.passingGrade != null)
//				return false;
//		} else if (!passingGrade.equals(other.passingGrade))
//			return false;
//		if (reimburseAmount == null) {
//			if (other.reimburseAmount != null)
//				return false;
//		} else if (!reimburseAmount.equals(other.reimburseAmount))
//			return false;
//		if (reqId == null) {
//			if (other.reqId != null)
//				return false;
//		} else if (!reqId.equals(other.reqId))
//			return false;
		if (requestor == null) {
			if (other.requestor != null)
				return false;
		} else if (!requestor.equals(other.requestor))
			return false;
//		if (status != other.status)
//			return false;
		if (submittedDate == null) {
			if (other.submittedDate != null)
				return false;
		} else if (!submittedDate.equals(other.submittedDate))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Request [reqId=" + reqId + ", description=" + description + ", type=" + type + ", requestor="
				+ requestor + ", cost=" + cost + ", reimburseAmount=" + reimburseAmount	+ ", passingGrade=" + passingGrade 
				+ ", eventDate=" + eventDate + ", submittedDate=" + submittedDate + ", status=" + status + ", SLA=" + SLA 
				+ ", comment=" + comment + ", comm_history=" + comm_history
				+ "]";
	}
}
