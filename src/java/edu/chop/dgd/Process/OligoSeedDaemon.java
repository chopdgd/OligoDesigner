package edu.chop.dgd.Process;

import edu.chop.dgd.dgdObjects.OligoObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class OligoSeedDaemon extends Thread {
	
	private List<Callable<List<OligoObject>>> taskList = null;
	private ExecutorService pool = null;
	private ExecutorCompletionService<List<OligoObject>> service;
	private int totalJobs;
	private List<OligoObject> combinedhetDimerlistResult = null;


    public OligoSeedDaemon(Integer totalJobs, int concurrentJobs) {
		System.out.println("Starting up job Daemon");
		this.totalJobs = totalJobs;
		this.pool = Executors.newFixedThreadPool(concurrentJobs);
		this.service = new ExecutorCompletionService<List<OligoObject>>(pool);
		this.taskList = new ArrayList<Callable<List<OligoObject>>>();
	}
	
	public void addJob(Callable<List<OligoObject>> job) {
		taskList.add(job);
	}
	
	
	public int getActive() {
		ThreadPoolExecutor tpe = ((ThreadPoolExecutor)this.pool);
		int queued = tpe.getQueue().size();
		int active = tpe.getActiveCount();
		int notCompleted = queued + active;
		return notCompleted;
	}
	
	@Override
	public void run() {
		try {
			int finishedJobs = 0;
			
			while(true) {
				if (taskList.size() > 0) {
					Callable<List<OligoObject>> job = taskList.remove(0);
					service.submit(job);
				}
				
				Future<List<OligoObject>> result = service.poll();
				if (result != null) {
					finishedJobs++;
					if (combinedhetDimerlistResult == null) {
                        combinedhetDimerlistResult = result.get();
					} else {
                        //combinedhetDimerlistResult.combine(result.get());
                        combinedhetDimerlistResult.addAll(result.get());
					}
				}
					
				if (finishedJobs == this.totalJobs) {
					Thread.currentThread().interrupt();
				}
				
				if (Thread.interrupted()) {
				    throw new InterruptedException();
				}
				
				Thread.sleep(100);
			}
					
		} catch (InterruptedException irrex) {
			System.out.println("Shutting down job pool");
			this.pool.shutdownNow();
			try {
				boolean retval = pool.awaitTermination(15, java.util.concurrent.TimeUnit.SECONDS);
				if (!retval) {
					System.out.println("Time limit reached, exiting without thread termination.");
				}
			} catch (InterruptedException iex) {
				System.out.println("Interrupted while waiting for termination signal, exiting without thread termination.");
			}
			System.out.println("Pool terminated");

		} catch (ExecutionException exex) {
			System.out.println("Error retrieving thread result, exiting");
            exex.printStackTrace();
            System.exit(1);
		}
	}
	
	public List<OligoObject> getCombinedResult() {
		return this.combinedhetDimerlistResult;
	}


}
