package com.revature.model;

public enum Status {
	NEW, IN_PROGRESS, PENDING_REVIEW, APPROVED, APPEAL;
	
	public int numApprovals;

	public int getNumApprovals() {
		return numApprovals;
	}

	public void setNumApprovals(int numApprovals) {
		this.numApprovals = numApprovals;
	}
}
