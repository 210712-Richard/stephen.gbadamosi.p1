package com.revature.model;

public enum Status {
	NEW, IN_PROGRESS, DOCUMENT_REQUESTED, PENDING_REVIEW, CANCELLED, DENIED, APPROVED, APPEAL;
	
	public int numApprovals;

	public int getNumApprovals() {
		return numApprovals;
	}

	public void setNumApprovals(int numApprovals) {
		this.numApprovals = numApprovals;
	}
}
