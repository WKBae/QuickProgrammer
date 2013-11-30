package net.wkbae.quickprogrammer;

import java.util.List;
import java.util.Stack;

/**
 * 다중 작업입니다.<br>
 * 실행하면 내부의 작업들이 모두 실행됩니다.
 * @author WKBae
 */
public class MultipleJobs extends Job {
	
	private Stack<Job> jobStack;
	
	MultipleJobs() {
		jobStack = new Stack<Job>();
	}
	
	@SuppressWarnings("unchecked")
	MultipleJobs(Stack<Job> jobs){
		jobStack = (Stack<Job>)jobs.clone();
	}
	
	MultipleJobs(List<Job> jobs){
		jobStack = new Stack<>();
		jobStack.addAll(jobs);
	}
	
	@Override
	protected void execute() {
		for(Job job : jobStack){ // 처음부터 차례대로
			job.execute();
		}
	}
}
