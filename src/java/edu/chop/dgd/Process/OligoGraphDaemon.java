package edu.chop.dgd.Process;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class OligoGraphDaemon extends Thread {

	private List<Callable<Multimap<String, String>>> taskList = null;
	private ExecutorService pool = null;
	private ExecutorCompletionService<Multimap<String, String>> service;
	private int totalJobs;
    //private DB db2 = DBMaker.tempFileDB().fileDeleteAfterClose().make();
	//private HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb = db2.hashMap("allHetDimerPairsObjectsMapMapdb").keySerializer(Serializer.STRING).valueSerializer(Serializer.FLOAT).createOrOpen();
    private Multimap<String, String> sortedOligosmultiMapsegment = LinkedListMultimap.create();

    public OligoGraphDaemon(Integer totalJobs, int concurrentJobs) {
		System.out.println("Starting up job Daemon");
		this.totalJobs = totalJobs;
		this.pool = Executors.newFixedThreadPool(concurrentJobs);
		this.service = new ExecutorCompletionService<Multimap<String, String>>(pool);
		this.taskList = new ArrayList<Callable<Multimap<String, String>>>();
	}

	public void addJob(Callable<Multimap<String, String>> job) {
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
					Callable<Multimap<String, String>> job = taskList.remove(0);
					service.submit(job);
				}

				Future<Multimap<String, String>> result = service.poll();
				if (result != null) {
					finishedJobs++;

                    Multimap<String, String> seedOligoMultimap = result.get();
                    for(String key : seedOligoMultimap.keySet()){
                        if(sortedOligosmultiMapsegment.containsKey(key)){
                            System.out.println("sortedOligosMultimap contains key!!"+seedOligoMultimap.get(key));
                        }else{
                            for(String value: seedOligoMultimap.get(key)){
                                sortedOligosmultiMapsegment.put(key, value);
                            }
                        }
                    }

                    //sortedOligosmultiMapsegment.putAll(result.get());

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
	
	public Multimap<String, String> getCombinedResultMap() {
		return this.sortedOligosmultiMapsegment;
	}


}
