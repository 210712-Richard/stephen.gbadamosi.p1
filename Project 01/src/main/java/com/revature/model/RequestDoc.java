package com.revature.model;

import java.util.UUID;

public class RequestDoc {
	private String link;
	private UUID reqID;
	
	public RequestDoc(String link, UUID reqID) {
		this.link = link;
		this.reqID = reqID;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public UUID getReqID() {
		return reqID;
	}

	public void setReqID(UUID reqID) {
		this.reqID = reqID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((reqID == null) ? 0 : reqID.hashCode());
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
		RequestDoc other = (RequestDoc) obj;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (reqID == null) {
			if (other.reqID != null)
				return false;
		} else if (!reqID.equals(other.reqID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RequestDoc [link=" + link + ", reqID=" + reqID + "]";
	}
	
}
