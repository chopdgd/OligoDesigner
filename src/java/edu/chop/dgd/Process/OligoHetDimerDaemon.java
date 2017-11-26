package edu.chop.dgd.Process;

import org.mapdb.HTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.*;


public class OligoHetDimerDaemon extends Thread {

	private List<Callable<TreeMap<String, Float>>> taskList = null;
	private ExecutorService pool = null;
	private ExecutorCompletionService<TreeMap<String, Float>> service;
	private int totalJobs;

    //private DB db2 = DBMaker.tempFileDB().fileDeleteAfterClose().make();
    private HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb;


    public OligoHetDimerDaemon(Integer totalJobs, int concurrentJobs, HTreeMap<String, Float> allHetdimerMapMapDB) {
		System.out.println("Starting up job Daemon");
		this.totalJobs = totalJobs;
		this.pool = Executors.newFixedThreadPool(concurrentJobs);
		this.service = new ExecutorCompletionService<TreeMap<String, Float>>(pool);
		this.taskList = new ArrayList<Callable<TreeMap<String, Float>>>();
        this.allHetDimerPairsObjectsMapMapdb = allHetdimerMapMapDB;
	}

	public void addJob(Callable<TreeMap<String, Float>> job) {
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
					Callable<TreeMap<String, Float>> job = taskList.remove(0);
					service.submit(job);
				}

				Future<TreeMap<String, Float>> result = service.poll();
				if (result != null) {
					finishedJobs++;
                    System.out.println("finished jobs:"+finishedJobs);

                    allHetDimerPairsObjectsMapMapdb.putAll(result.get());

				}

				if (finishedJobs == this.totalJobs) {
					Thread.currentThread().interrupt();
				}

				if (Thread.interrupted()) {
				    throw new InterruptedException();
				}

				Thread.sleep(1000);
			}

		} catch (InterruptedException irrex) {
			System.out.println("Shutting down job pool");
			this.pool.shutdownNow();
			try {
				boolean retval = pool.awaitTermination(15, TimeUnit.SECONDS);
				if (!retval) {
					System.out.println("Time limit reached, exiting without thread termination.");
				}
			} catch (InterruptedException iex) {
				System.out.println("Interrupted while waiting for termination signal, exiting without thread termination.");
			}
			System.out.println("Pool terminated");

		} catch (ExecutionException exex) {
			System.out.println("Error retrieving thread result, exiting");
			System.exit(1);
		}
	}
	
	public HTreeMap<String, Float> getCombinedResultMap() {
		return this.allHetDimerPairsObjectsMapMapdb;
	}


}
