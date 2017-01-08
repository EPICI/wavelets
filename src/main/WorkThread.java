package main;

import java.util.*;

public class WorkThread extends Thread {
	
	public static final byte WAITING = 0;
	public static final byte WORKING = 1;
	public static final byte FINISHED = 2;
	public static final Comparator<WorkThread> taskListSize = new Comparator<WorkThread>(){

		@Override
		public int compare(WorkThread a, WorkThread b) {
			return b.tasks.size()-a.tasks.size();
		}
		
	};
	
	protected List<Task> tasks;
	protected ArrayList<Task> unsyncTasks;
	protected int index;
	
	//A task for the work thread to do
	public static interface Task{
		//Do work, return true if the work thread can retry
		public boolean execute();
		//Get status
		public byte getStatus();
		//Tell it to cancel
		public void cancel();
	}

	public WorkThread() {
		init();
	}

	public WorkThread(Runnable arg0) {
		super(arg0);
		init();
	}

	public WorkThread(String arg0) {
		super(arg0);
		init();
	}

	public WorkThread(ThreadGroup arg0, Runnable arg1) {
		super(arg0, arg1);
		init();
	}

	public WorkThread(ThreadGroup arg0, String arg1) {
		super(arg0, arg1);
		init();
	}

	public WorkThread(Runnable arg0, String arg1) {
		super(arg0, arg1);
		init();
	}

	public WorkThread(ThreadGroup arg0, Runnable arg1, String arg2) {
		super(arg0, arg1, arg2);
		init();
	}

	public WorkThread(ThreadGroup arg0, Runnable arg1, String arg2, long arg3) {
		super(arg0, arg1, arg2, arg3);
		init();
	}

	public void init(){
		unsyncTasks = new ArrayList<Task>();
		tasks = Collections.synchronizedList(unsyncTasks);
	}
	
	public void run(){
		while(true){
			int numTasks = tasks.size();
			if(numTasks>0){
				index = (index+1)%numTasks;
				Task nextTask = tasks.get(index);
				boolean retry = true;
				while(retry){
					if(nextTask.getStatus()==WAITING){//Only run if it is idle
						retry = nextTask.execute();
					}
				}
				if(nextTask.getStatus()==FINISHED){//If finished, remove from list
					//System.out.println("Finished a "+nextTask.getClass().getSimpleName());
					tasks.remove(index);
				}
			}else{
				try{
					Thread.sleep(10);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public void assignTask(Task task){
		//System.out.println("Assigned a "+task.getClass().getSimpleName());
		tasks.add(task);
	}
	
	public void assignTasks(ArrayList<Task> tasks){
		//System.out.println("Assigning "+Integer.toString(tasks.size())+" tasks");
		for(Task task:tasks){
			assignTask(task);
		}
	}
}
