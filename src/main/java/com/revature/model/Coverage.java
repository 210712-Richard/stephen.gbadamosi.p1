package com.revature.model;

import java.io.Serializable;

public enum Coverage implements Serializable {
	UNIVERSITY_COURSES(.80), SEMINARS(.60), CERT_PREP(.75), CERTIFICATION(1.0),
	TECH_TRAINING(.90), OTHER(.30);

	private Double value;
	
	Coverage(Double value) {
		this.value = value;
	}
	
	public Double getValue() {
		return this.value;
	}
	
	public static Coverage getCoverage(String type) {
		if(type.equalsIgnoreCase("UNIVERSITY_COURSES") || type.equalsIgnoreCase("University Course")) {
			return UNIVERSITY_COURSES;
		}
		if(type.equalsIgnoreCase("SEMINARS") || type.equalsIgnoreCase("Seminar")) {
			return SEMINARS;
		}
		if(type.equalsIgnoreCase("CERT_PREP") || type.equalsIgnoreCase("Certification Preparation Class")) {
			return CERT_PREP;
		}
		if(type.equalsIgnoreCase("CERTIFICATION") || type.equalsIgnoreCase("Certification")) {
			return CERTIFICATION;
		}
		if(type.equalsIgnoreCase("TECH_TRAINING") || type.equalsIgnoreCase("Technical Training") || type.equalsIgnoreCase("Tech Training")) {
			return TECH_TRAINING;
		}
		if(type.equalsIgnoreCase("OTHER") || type.equalsIgnoreCase("Other")) {
			return OTHER;
		}
		
		throw new RuntimeException("Incorrect Input to getCoverage(): " + type);

	}
}
