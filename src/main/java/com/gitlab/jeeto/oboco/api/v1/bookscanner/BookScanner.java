package com.gitlab.jeeto.oboco.api.v1.bookscanner;

import javax.enterprise.context.ApplicationScoped;

import com.gitlab.jeeto.oboco.common.exception.ProblemException;

@ApplicationScoped
public interface BookScanner {
	public String getId();
	public BookScannerMode getMode();
	public BookScannerStatus getStatus();
	public void start(BookScannerMode mode) throws ProblemException;
	public void stop() throws ProblemException;
}
