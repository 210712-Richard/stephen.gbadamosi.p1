package com.revature.model;

public enum FileType {
	EMAIL, DOCUMENT;
	
	public static FileType getDocType(String filename) {
		if(filename.contains("email") || filename.contains("msg")) {
			return EMAIL;
		}
		
		return DOCUMENT;
	}
	
	public static String toString(FileType type) {
		switch(type) {
			case EMAIL: 
				return "email";
			case DOCUMENT:
				return "presentation";			
			default: 
				System.out.println("Invalid file type");
				return null;
		}
	}
}
