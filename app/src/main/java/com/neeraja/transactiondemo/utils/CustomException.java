package com.neeraja.transactiondemo.utils;

public class CustomException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	String displayMsg;
	String detailedMsg;

	public CustomException(String displayMsg, String detailedMsg) {
		super();
		this.displayMsg = displayMsg;
		this.detailedMsg = detailedMsg;
	}

	public String getDisplayMsg() {
		return displayMsg;
	}

	public void setDisplayMsg(String displayMsg) {
		this.displayMsg = displayMsg;
	}

	public String getDetailedMsg() {
		return detailedMsg;
	}

	public void setDetailedMsg(String detailedMsg) {
		this.detailedMsg = detailedMsg;
	}

	@Override
	public String toString() {
		return // "Exception: " +
		displayMsg + detailedMsg;
	}

}
