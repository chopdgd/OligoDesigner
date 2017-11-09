package edu.chop.dgd.Process;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class OligoHetDimerThread implements Callable<TreeMap<String, Float>> {
	private int n;
    private int numlines;
	private int threadCount;
	private String hetdimerFilename;
    //private TreeMap<String, Float> hetDimerPairsObjectsMapMapdb_fornumfile;
    private String dataDir;
    private String heterodimerOpDir;


    public OligoHetDimerThread(int n, int numlines, int threadcount, String hetdimerFilename, String dataDir, String heterodimerOpDir) {
        this.n = n;
        this.numlines = numlines;
        this.threadCount = threadcount;
        this.hetdimerFilename = hetdimerFilename;
        this.dataDir = dataDir;
        this.heterodimerOpDir = heterodimerOpDir;
        //this.hetDimerPairsObjectsMapMapdb_fornumfile = hetDimerPairsObjectsMapMapdb_fornumfile;

    }

    /**
     *
     * @return
     * @throws Exception
     */
    public TreeMap<String, Float> call() throws Exception {

        TreeMap<String, Float> hetDimerPairsObjectsMapMapdb_fornumfile = new TreeMap<String, Float>();
        System.out.println("Job:" + String.valueOf(this.threadCount) + " starting.");
        String file1 = hetdimerFilename+"_"+n+"_1";
        String file2 = hetdimerFilename+"_"+n+"_2";
        String resultHeterodimerString = runHeterodimerAnalysisProcessBuilder(file1, file2, dataDir);
        System.out.println("getting deltaG values for HetDimer Pairs");
        if(resultHeterodimerString.length()>10){
            hetDimerPairsObjectsMapMapdb_fornumfile = getDeltaGValuesForHetDimerPairs_createMapDBHash(hetDimerPairsObjectsMapMapdb_fornumfile, dataDir, heterodimerOpDir, hetdimerFilename, n);
        }else{
            throw new FileNotFoundException();
        }

        return hetDimerPairsObjectsMapMapdb_fornumfile;
    }




    /**
     *
     * @param hetFile1
     * @param hetFile2
     * @param dataDir
     * @return
     * @throws Exception
     */
    public String runHeterodimerAnalysisProcessBuilder(String hetFile1, String hetFile2, String dataDir) throws Exception{

        String answer;
        String errAnswer="NA";

        ProcessBuilder pb = new ProcessBuilder(dataDir+"HeteroDimerAnalyzeProcess.sh",hetFile1, hetFile2);
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/primer3";
        path += ":/usr/local/blat";
        path += ":/usr/local/mfold/bin";
        path += ":/usr/local/mfold/share";
        env.put("PATH", path);

        pb.directory(new File(dataDir));
        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
        System.out.println("should've initiated the HeteroDimerAnalyzeProcess.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the HeteroDimerAnalyzeProcess.sh..");

            BufferedReader bErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder errsb = new StringBuilder();
            String errline;
            while ((errline = bErr.readLine()) != null) {
                errsb.append(errline).append("\n");
            }
            String erranswer = errsb.toString();
            System.out.println(erranswer);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {

                if(line.equals("Sorry, the BLAT/iPCR server seems to be down.  Please try again later") ||
                        line.equals("Error in TCP non-blocking connect() 61 - Connection refused") ||
                        line.equals("Couldn't connect to localhost 17779"))
                {
                    errAnswer+=line;
                }else{
                    sb.append(line).append("\n");
                }

            }
            answer = sb.toString();
            System.out.println(erranswer);
            System.out.println(answer);
            System.out.println("should have got an output from hybrid-min..");

            if(errAnswer.length()>2){
                throw new Exception("Exception: check program params!");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;
    }



    /**
     *
     * @param hetDimerPairsObjectsMapMapdb_fornumfile
     * @param dataDir
     * @param heterodimerOpDir
     * @param fileName
     * @param subpartnum
     * @return
     * @throws Exception
     */
    public TreeMap<String, Float> getDeltaGValuesForHetDimerPairs_createMapDBHash(TreeMap<String, Float> hetDimerPairsObjectsMapMapdb_fornumfile, String dataDir, String heterodimerOpDir, String fileName, int subpartnum) throws Exception{

        String hetOpFilename = dataDir+heterodimerOpDir+fileName+"_"+subpartnum+"_1_"+fileName+"_"+subpartnum+"_2.out";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(hetOpFilename)));

        try{
            String line;
            while((line=reader.readLine()) != null){
                if(line.contains("dG")){
                    String lineArr[] = line.split("\t", -1);
                    String[] oligoHeaderArr = lineArr[3].split("-", -1);
                    String hetOligoHeader1 = oligoHeaderArr[0];
                    String hetOligoHeader2 = oligoHeaderArr[1];
                    String hetdimerValue = lineArr[1].split(" = ", -1)[1];

                    Float hetDimerValueToCompare = Float.parseFloat(hetdimerValue);
                    Float hetDimerValueToCompareTo = Float.parseFloat("-10.00");

                    int comparison = hetDimerValueToCompare.compareTo(hetDimerValueToCompareTo);
                    /*if (comparison < 0) {
                        System.out.println("f1 is less than f2");
                    }
                    else if (comparison == 0) {
                        System.out.println("f1 is equal to f2");
                    }
                    else {
                        System.out.println("f1 is greater than f2");
                    }*/

                    if(Float.parseFloat(hetdimerValue) >= -10.00){
                        hetDimerPairsObjectsMapMapdb_fornumfile.put(hetOligoHeader1 + "&" + hetOligoHeader2, Float.parseFloat(hetdimerValue));
                    }
                }
            }
        }finally {
            reader.close();
        }

        return hetDimerPairsObjectsMapMapdb_fornumfile;
    }






    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getNumlines() {
        return numlines;
    }

    public void setNumlines(int numlines) {
        this.numlines = numlines;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public String getHetdimerFilename() {
        return hetdimerFilename;
    }

    public void setHetdimerFilename(String hetdimerFilename) {
        this.hetdimerFilename = hetdimerFilename;
    }


}




