package com.nicmus.pdns;

/**
 * 
 * @author jsabev
 *
 */
public class MissingRequiredParameterException extends JPowerAdminException{
	private static final long serialVersionUID = 8428486278838554410L;
	
	public MissingRequiredParameterException(String string){
		super(string);
	}
	
	public MissingRequiredParameterException(){
		super();
	}

	
	
}
