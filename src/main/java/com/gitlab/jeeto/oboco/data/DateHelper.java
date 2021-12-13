package com.gitlab.jeeto.oboco.data;

import java.util.Date;

public class DateHelper {
	public static Date getDate() {
		// no milliseconds
		return new Date((new Date().getTime() / 1000L) * 1000L);
	}
}
