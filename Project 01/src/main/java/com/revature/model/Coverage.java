package com.revature.model;

import java.io.Serializable;

public enum Coverage implements Serializable {
	UNIVERSITY_COURSES(.80), SEMINARS(.60), CERT_PREP(.75), CERTIFICATION(1.0),
	TECH_TRAINING(.90), OTHER(.30);

	private Double type;
	
	Coverage(Double type) {
		this.type = type;
	}
	
	public Double getType() {
		return this.type;
	}
	
	public Coverage getCoverage(String type) {
		if(type.equalsIgnoreCase("UC") || type.equalsIgnoreCase("University Course")) {
			return UNIVERSITY_COURSES;
		}
		if(type.equalsIgnoreCase("SEM") || type.equalsIgnoreCase("Seminar")) {
			return SEMINARS;
		}
		if(type.equalsIgnoreCase("CPC") || type.equalsIgnoreCase("Certification Preparation Class")) {
			return CERT_PREP;
		}
		if(type.equalsIgnoreCase("CERT") || type.equalsIgnoreCase("Certification")) {
			return CERTIFICATION;
		}
		if(type.equalsIgnoreCase("TT") || type.equalsIgnoreCase("Technical Training") || type.equalsIgnoreCase("Tech Training")) {
			return TECH_TRAINING;
		}
		if(type.equalsIgnoreCase("O") || type.equalsIgnoreCase("Other")) {
			return OTHER;
		}
		
		throw new RuntimeException("Incorrect Input to getCoverage(): " + type);

	}
}
