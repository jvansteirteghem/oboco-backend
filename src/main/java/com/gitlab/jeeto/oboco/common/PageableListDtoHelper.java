package com.gitlab.jeeto.oboco.common;

import com.gitlab.jeeto.oboco.common.exception.Problem;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

public class PageableListDtoHelper {
	public static void validatePageableList(Integer page, Integer pageSize) throws ProblemException {
		if(page == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_PAGE_INVALID", "The page is invalid: page is null."));
		}
		
		if(page < 1) {
			throw new ProblemException(new Problem(400, "PROBLEM_PAGE_INVALID", "The page is invalid: page is < 1."));
		}
		
		if(pageSize == null) {
			throw new ProblemException(new Problem(400, "PROBLEM_PAGE_SIZE_INVALID", "The pageSize is invalid: pageSize is null."));
		}
		
		if(pageSize < 1 || pageSize > 100) {
			throw new ProblemException(new Problem(400, "PROBLEM_PAGE_SIZE_INVALID", "The pageSize is invalid: pageSize is < 1 or pageSize is > 100."));
		}
	}
}
