package edu.chop.dgd.Process;

import edu.chop.dgd.dgdObjects.*;
import edu.chop.dgd.dgdUtils.OligoUtils;
import org.mapdb.HTreeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class OligoSeedThread implements Callable<List<OligoObject>> {
	private File inputFile;
	private int threadCount;
	private int extendScore;
    private SequenceObject sequenceobject;
    private String assembly;
    String projectId;
    private HTreeMap<String, Object> hetDimermapMapDB;
    private List<OligoObject> hetDimerOligosList;
    private String dataDir;
    private String oligoOutputDir;
    private String blatInpDir;
    private String blatOpDir;
    private String mfoldInpDir;
    private String mfoldOpDir;
    private String homodimerOpDir;
    private String heterodimerInpDir;
    private String heterodimerOpDir;
    private String oligoInputDir;
    private String finalOligos;
    private String oligoProcessScriptDir;
    private String primer3OligoadditionalParams;

    public OligoSeedThread(SequenceObject so, int threadCount, String projectId, String assembly, String primer3OligoadditionalParams) {
    //public OligoSeedThread(File inputFile, int threadCount, OligoSeedFinder seedFinder, int extendScore) {
        this.threadCount = threadCount;
        this.sequenceobject = so;
        this.projectId = projectId;
        this.assembly = assembly;
        this.primer3OligoadditionalParams = primer3OligoadditionalParams;
	}

    /**
     *
     * @return
     * @throws Exception
     */
    public List<OligoObject> call() throws Exception {

        System.out.println("Job:" + String.valueOf(this.threadCount) + " starting.");
        int index = 0;
        String reportFile="";

        String inpFilename = assembly+"_"+projectId+"_"+sequenceobject.getChr()+":"+sequenceobject.getStart()+"-"+
                sequenceobject.getStop()+".txt";
        List<SequenceObjectSubsections> sosSubsList = sequenceobject.generateSequenceSubsections(inpFilename, dataDir);
        String fileName="oligoInp_"+projectId+"_"+sequenceobject.getChr()+":"+sequenceobject.getStart()+"-"+
                sequenceobject.getStop()+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());

        reportFile = generateReportSetSubsectionIds(sosSubsList, projectId, fileName, sequenceobject, primer3OligoadditionalParams);

        String detailFile = dataDir+finalOligos+fileName+"_detail.html";
        String secondaryFile = dataDir+finalOligos+fileName+"_secondary.txt";
        //create oligos, add blat results, add ids, get results from mfold, create hetdimer inputs.
        List<SequenceObjectSubsections> oligosSubsectionList = retrieveResultsFromAnalyses(fileName, sosSubsList,
                dataDir, oligoOutputDir, blatInpDir, blatOpDir, mfoldInpDir, mfoldOpDir, homodimerOpDir,
                heterodimerInpDir, heterodimerOpDir);

        List<OligoObject> heteroDimerObjectsListFromSO = new MfoldDimer().filterOligosCreateHeterodimers(oligosSubsectionList);
        sequenceobject.setHetDimerOligosList(heteroDimerObjectsListFromSO);
        //commenting this out for parallelization, it will combine results at the end.
        //hetDimerOligosList.addAll(heteroDimerObjectsListFromSO);

        System.out.println("adding for Heterodimer analysis");
        sequenceobject.setOligoObjectSubsections(oligosSubsectionList);
        sequenceobject.setDetailsFile(detailFile);
        sequenceobject.setSecondaryFile(secondaryFile);
        sequenceobject.setReportFile(reportFile);
        sequenceobject.setFilename(fileName);
        return heteroDimerObjectsListFromSO;
    }

    /**
     *
     * @param ampliconObjList
     * @param projectId
     * @param filename
     * @param so
     * @return
     * @throws Exception
     */

    private String generateReportSetSubsectionIds(List<SequenceObjectSubsections> ampliconObjList, String projectId, String filename, SequenceObject so, String primer3OligoadditionalParams) throws Exception{

        Date dt = new Date();

        //create a placeholder for filename. it should be sent in from parameter. this is for backward compatibility. although i see no reason for filename to be set here.
        String fileN="oligoInp_"+projectId+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());
        if(filename.length()>0){
            fileN = filename;
        }

        File oligoInputFile = new File(dataDir+oligoInputDir+fileN);
        PrintWriter pw = new PrintWriter(oligoInputFile);
        int counter = 0;

        for(SequenceObjectSubsections oss : ampliconObjList){
            counter+=1;
            String subsectionId = "inpSeq"+so.getChr()+":"+so.getStart()+":"+so.getStop()+"_"+counter;
            oss.setSubsectionid(subsectionId);
            pw.println("SEQUENCE_ID="+subsectionId+"\nSEQUENCE_TEMPLATE="+oss.getSubSectionSequence()+"\n"+
                    primer3OligoadditionalParams);

            System.out.println("SEQUENCE_ID="+subsectionId+"\nSEQUENCE_TEMPLATE="+oss.getSubSectionSequence()+"\n"+
                    primer3OligoadditionalParams);
        }

        pw.flush();
        pw.close();

        String resultPrimer3Blat = runOligoProcessBuilder(fileN);
        //System.out.println(resultPrimer3Blat);

        return resultPrimer3Blat;

    }


    /**
     *
     * @param inputFileName
     * @return
     * @throws Exception
     */
    public String runOligoProcessBuilder(String inputFileName) throws Exception {

        String answer;
        String errAnswer="NA";

        ProcessBuilder pb = new ProcessBuilder(oligoProcessScriptDir+"OligoProcess.sh",inputFileName);
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/primer3";
        path += ":/usr/local/blat";
        path += ":/usr/local/mfold/bin";
        path += ":/usr/local/mfold/share";
        env.put("PATH", path);

        pb.directory(new File(oligoProcessScriptDir));

        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
        System.out.println("should've initiated the oligoProcess.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the oligoProcess.sh..");

            BufferedReader bErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder errsb = new StringBuilder();
            String errline;
            while ((errline = bErr.readLine()) != null) {
                errsb.append(errline).append("\n");
            }
            String erranswer = errsb.toString();
            //System.out.println(erranswer);

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

            System.out.println("should have got an output from Primer3 and BLAT..");

            if(errAnswer.length()>2){

                throw new Exception("Exception: Blat server issues. The BLAT server may have " +
                        "not been started. Please start server using gfServer.");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;
    }


    /**
     *
     * @param fileName
     * @param osSubsList
     * @param dataDir
     * @param oligoOutputDir
     * @param blatOligoInpDir
     * @param blatOligoOpDir
     * @param mfoldInpDir
     * @param mfoldOpDir
     * @param homodimerOpDir
     * @param heterodimerInpDir
     * @param heterodimeropDir
     * @return
     * @throws Exception
     */
    public List<SequenceObjectSubsections> retrieveResultsFromAnalyses(String fileName, List<SequenceObjectSubsections> osSubsList,
                                                                       String dataDir, String oligoOutputDir, String blatOligoInpDir,
                                                                       String blatOligoOpDir, String mfoldInpDir, String mfoldOpDir,
                                                                       String homodimerOpDir, String heterodimerInpDir, String heterodimeropDir)throws Exception {


        List<OligoObject> oligoObjectsFromPrimer3 = new OligoUtils().createOligoObjsList(fileName, dataDir, oligoOutputDir);
        oligoObjectsFromPrimer3 = new OligoUtils().addIdsToOligos(fileName, oligoObjectsFromPrimer3, blatOligoInpDir, dataDir, osSubsList);
        List<BlatPsl> blatResults = new BlatPsl().createOligoBlatList(fileName, blatOligoOpDir, dataDir);
        oligoObjectsFromPrimer3 = new BlatPsl().addBlatResultsToOligos(blatResults, oligoObjectsFromPrimer3, osSubsList);
        oligoObjectsFromPrimer3 = new MfoldHairpin().getDeltaGValuesForHairpin(oligoObjectsFromPrimer3, fileName, mfoldOpDir, dataDir);
        oligoObjectsFromPrimer3 = new MfoldDimer().getDeltaGValuesForHomoDimer(oligoObjectsFromPrimer3, fileName, homodimerOpDir, dataDir);

        osSubsList = addOligoListToSubsection(oligoObjectsFromPrimer3, osSubsList);

        return osSubsList;

    }

    private List<SequenceObjectSubsections> addOligoListToSubsection(List<OligoObject> oligoObjectsFromPrimer3, List<SequenceObjectSubsections> osSubsList) {
        for(SequenceObjectSubsections sos : osSubsList){
            for(OligoObject obj : oligoObjectsFromPrimer3){
                String oligoSubsectionId = obj.getInternalPrimerId().split("_", -1)[0]+"_"+
                        obj.getInternalPrimerId().split("_",-1)[1];
                if(oligoSubsectionId.equals(sos.getSubsectionid())){
                    if(sos.getOligoList()!=null && sos.getOligoList().size()>0){
                        List<OligoObject> sosOligosList = sos.getOligoList();
                        sosOligosList.add(obj);
                        sos.setOligoList(sosOligosList);
                    }else{
                        List<OligoObject> sosOligoObjectList = new ArrayList<OligoObject>();
                        sosOligoObjectList.add(obj);
                        sos.setOligoList(sosOligoObjectList);
                    }
                }
            }
        }

        return osSubsList;
    }


	
	/*public OligoResult call() {
		System.out.println("Job:" + String.valueOf(this.threadCount) + " starting.");
		int index = 0;
		//TcrStats stats = new TcrStats();

		//Stat Dictionary
		HashMap<Integer,Integer> seqLenHist = new HashMap<Integer,Integer>();
		
		//Hit Dictionary
		HashMap<String,TcrVJGroup> hitGroups = new HashMap<String,TcrVJGroup>();
 
		long lastTime = System.currentTimeMillis();
		
		try  {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			int fileIndex = -1;
			int seqLength;
			String line;
			
			while ((line = br.readLine()) != null) {
				fileIndex++;
				if (fileIndex % 4 != 1) {
					continue;
				}
				index++;
				
				//Grab Sequence
				String seq = line.replace("\n", "");

				//Record the sequence length
				seqLength = seq.length();
				if (!seqLenHist.containsKey(Integer.valueOf(seqLength))) {
					seqLenHist.put(Integer.valueOf(seqLength), Integer.valueOf(0));
				}
				seqLenHist.put(Integer.valueOf(seqLength), Integer.valueOf(((Integer)seqLenHist.get(Integer.valueOf(seqLength))).intValue() + 1));

				//Report stuff every once in a while
				if (index % 10000 == 0) {
					long currTime = System.currentTimeMillis();
					long elapsTime = (currTime - lastTime) / 1000L;
					lastTime = currTime;
					*//*System.out.format("Thread:%d | %d %d %.2f%% | %d %d %d | %d | %d %d %d | %d %d %d | %d %n", new Object[] {Integer.valueOf(threadCount), Integer.valueOf(index), Integer.valueOf(stats.getGood()), Float.valueOf(stats.getGood() / (float)index * 100.0F),
							Integer.valueOf(stats.getPrimerFailB()), Integer.valueOf(stats.getPrimerFailV()), Integer.valueOf(stats.getPrimerFailJ()), Integer.valueOf(stats.getOrientFail()), 
							Integer.valueOf(stats.getScoreFailB()), Integer.valueOf(stats.getScoreFailV()), Integer.valueOf(stats.getScoreFailJ()), Integer.valueOf(stats.getHitClassFailBoth()), 
							Integer.valueOf(stats.getHitClassFailV()), Integer.valueOf(stats.getHitClassFailJ()), Long.valueOf(elapsTime) });*//*
				}

				//Grab potiential primer matches
				ArrayList<PrimerMatch> vHits = seedFinder.findSeeds(seq, 0);
				ArrayList<PrimerMatch> jHits = seedFinder.findSeeds(seq, 1);

				//Make sure there is a potential hit in both the V and the J regions
				if (vHits.size() == 0) {
					if (jHits.size() == 0) {
						stats.incPrimerFailB();
					}
					else {
						stats.incPrimerFailV();
					}
				}
				else if (jHits.size() == 0) {
					stats.incPrimerFailJ();
				}
				else {
					
					//Return the best scoring V and J match
					PrimerMatch vHit = scoreRegion(vHits, extendScore);
					PrimerMatch jHit = scoreRegion(jHits, extendScore);
					
					//Make sure the top scoring match is worthwhile
					if (vHit == null) {
						if (jHit == null) {
							stats.incScoreFailB();
						} else {
							stats.incScoreFailV();
						}
					}
					else if (jHit == null) {
						stats.incScoreFailJ();
					}
					else if (jHit.getMatchDir().equals(vHit.getMatchDir())) {
						stats.incOrientFail();
					}
					else {
						stats.incGood();
						TcrInstance nTcr = new TcrInstance(vHit, jHit);
						
						String hStatus = nTcr.getHitClass();
						if (hStatus.equals("none"))
							stats.incHitClassFailBoth();
						else if (hStatus.equals("V"))
							stats.incHitClassFailV();
						else if (hStatus.equals("J")) {
							stats.incHitClassFailJ();
						}

						//Check to see if the V+J combination exists
						if (!hitGroups.containsKey(nTcr.getGroupKey())) {
							hitGroups.put(nTcr.getGroupKey(), new TcrVJGroup(vHit, jHit));
						}
						
						//Assign TCR to proper group
						hitGroups.get(nTcr.getGroupKey()).checkHit(nTcr);
					}
				}
			}
		  br.close();
		} catch (IOException ioex) {
			System.out.println("Error opening/reading file: " +  ioex.getMessage());
			ioex.printStackTrace();
			System.exit(1);
		}
			
		OligoResult result = new OligoResult(stats, seqLenHist, hitGroups, index);
		System.out.println("Job:" + String.valueOf(this.threadCount) + " ending.");
		return result;
	}*/
		
/*	private PrimerMatch scoreRegion(ArrayList<PrimerMatch> matchList, int extScore) {
		for (int i = 0; i < matchList.size(); i++) {
			PrimerMatch match = matchList.get(i);
			StringBuffer res = findHit(match, match.getPrimerIdx(), match.getSeqIdx(), 4, 4, 0, 0);
			match.addScoreString(res.toString());
		}

		Collections.sort(matchList, new PrimerMatchCompare());
		Collections.reverse(matchList);

		PrimerMatch topHit = (PrimerMatch)matchList.get(0);

		float maxScore = topHit.getScoreStringScore();

		for (int i = 1; i < matchList.size(); i++) {
			PrimerMatch s = matchList.get(i);
			if (Math.abs(maxScore - s.getScoreStringScore()) >= 1.0F) break;

			if (!topHit.getPrimerName().equals(s.getPrimerName())) {
				topHit.setFancyName(topHit.getPrimerName() + "/" + s.getPrimerName());
			}
		}
		
		

		if (maxScore < extScore) {
			return null;
		}

		return topHit;
	}

	private StringBuffer findHit(PrimerMatch match, int pPos, int sPos, int indelMax, int mmMax, int indelC, int mmC) {
		if ((pPos >= match.getPrimerSeq().length()) || (sPos >= match.getTcrSeq().length()))
			return new StringBuffer("");
		if (indelC > indelMax)
			return new StringBuffer("");
		if (mmC > mmMax)
			return new StringBuffer("");
		if (match.getPrimerSeq().charAt(pPos) == match.getTcrSeq().charAt(sPos)) {
			StringBuffer val = findHit(match, pPos + 1, sPos + 1, indelMax, mmMax, indelC, mmC);
			return new StringBuffer("M" + val.toString());
		}
		StringBuffer val1 = findHit(match, pPos, sPos + 1, indelMax, mmMax, indelC + 1, mmC);
		StringBuffer val2 = findHit(match, pPos + 1, sPos, indelMax, mmMax, indelC + 1, mmC);
		StringBuffer val3 = findHit(match, pPos + 1, sPos + 1, indelMax, mmMax, indelC, mmC + 1);

		int maxVal = 0;
		String maxHit = "";
		String maxType = "";

		ArrayList<StringBuffer> sb = new ArrayList<StringBuffer>();
		sb.add(val3);
		sb.add(val1);
		sb.add(val2);

		for (int i = 0; i < sb.size(); i++) {
			StringBuffer v = (StringBuffer)sb.get(i);
			int c = 0;
			for (int j = 0; j < v.length(); j++) {
				if (((StringBuffer)sb.get(i)).charAt(j) == 'M') {
					c++;
				}
			}

			int count = v.length() - (v.length() - c);
			if (count > maxVal) {
				maxVal = count;
				maxHit = v.toString();
				if (i == 0)
					maxType = "m";
				else if (i == 1)
					maxType = "I";
				else {
					maxType = "D";
				}
			}
		}

		if (maxHit == "") {
			return new StringBuffer("");
		}
		return new StringBuffer(maxType + maxHit);
	}*/


    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getExtendScore() {
        return extendScore;
    }

    public void setExtendScore(int extendScore) {
        this.extendScore = extendScore;
    }

    public SequenceObject getSequenceobject() {
        return sequenceobject;
    }

    public void setSequenceobject(SequenceObject sequenceobject) {
        this.sequenceobject = sequenceobject;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public HTreeMap<String, Object> getHetDimermapMapDB() {
        return hetDimermapMapDB;
    }

    public void setHetDimermapMapDB(HTreeMap<String, Object> hetDimermapMapDB) {
        this.hetDimermapMapDB = hetDimermapMapDB;
    }

    public List<OligoObject> getHetDimerOligosList() {
        return hetDimerOligosList;
    }

    public void setHetDimerOligosList(List<OligoObject> hetDimerOligosList) {
        this.hetDimerOligosList = hetDimerOligosList;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getOligoOutputDir() {
        return oligoOutputDir;
    }

    public void setOligoOutputDir(String oligoOutputDir) {
        this.oligoOutputDir = oligoOutputDir;
    }

    public String getBlatInpDir() {
        return blatInpDir;
    }

    public void setBlatInpDir(String blatInpDir) {
        this.blatInpDir = blatInpDir;
    }

    public String getBlatOpDir() {
        return blatOpDir;
    }

    public void setBlatOpDir(String blatOpDir) {
        this.blatOpDir = blatOpDir;
    }

    public String getMfoldInpDir() {
        return mfoldInpDir;
    }

    public void setMfoldInpDir(String mfoldInpDir) {
        this.mfoldInpDir = mfoldInpDir;
    }

    public String getMfoldOpDir() {
        return mfoldOpDir;
    }

    public void setMfoldOpDir(String mfoldOpDir) {
        this.mfoldOpDir = mfoldOpDir;
    }

    public String getHomodimerOpDir() {
        return homodimerOpDir;
    }

    public void setHomodimerOpDir(String homodimerOpDir) {
        this.homodimerOpDir = homodimerOpDir;
    }

    public String getHeterodimerInpDir() {
        return heterodimerInpDir;
    }

    public void setHeterodimerInpDir(String heterodimerInpDir) {
        this.heterodimerInpDir = heterodimerInpDir;
    }

    public String getHeterodimerOpDir() {
        return heterodimerOpDir;
    }

    public void setHeterodimerOpDir(String heterodimerOpDir) {
        this.heterodimerOpDir = heterodimerOpDir;
    }

    public String getOligoInputDir() {
        return oligoInputDir;
    }

    public void setOligoInputDir(String oligoInputDir) {
        this.oligoInputDir = oligoInputDir;
    }

    public String getFinalOligos() {
        return finalOligos;
    }

    public void setFinalOligos(String finalOligos) {
        this.finalOligos = finalOligos;
    }

    public String getOligoProcessScriptDir() {
        return oligoProcessScriptDir;
    }

    public void setOligoProcessScriptDir(String oligoProcessScriptDir) {
        this.oligoProcessScriptDir = oligoProcessScriptDir;
    }

    public String getPrimer3OligoadditionalParams() {
        return primer3OligoadditionalParams;
    }

    public void setPrimer3OligoadditionalParams(String primer3OligoadditionalParams) {
        this.primer3OligoadditionalParams = primer3OligoadditionalParams;
    }
}




