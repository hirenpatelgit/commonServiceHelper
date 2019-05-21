package com.sample.apicallhelper;

	/* 
	*	created by Hiren Patel on 5th February 2015
	* common service response object
	*/

public class ServiceResponse {

	 
	//contain raw string of response
    public String RawResponse;
	
	//return API call is success or failed
    public boolean isSuccess = false;
	
	//contain simple message of success or error
    public String Message = "";
	
	//generate your tag to differentiate your api call
    public int Tag = 0;
	
	//status code 
    public int StatusCode = 0;
}

