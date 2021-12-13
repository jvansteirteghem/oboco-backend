package com.gitlab.jeeto.oboco.problem;

public class ProblemException extends Exception {
	private static final long serialVersionUID = 1L;
	private Problem problem;
	
	public ProblemException(Problem problem) {
		super(problem.getStatusCode() + ": " + problem.getCode() + " - " + problem.getDescription());
		
		this.problem = problem;
	}
	
	public ProblemException(Problem problem, Throwable cause) {
		super(problem.getStatusCode() + ": " + problem.getCode() + " - " + problem.getDescription(), cause);
		
		this.problem = problem;
	}

	public Problem getProblem() {
		return problem;
	}
}
