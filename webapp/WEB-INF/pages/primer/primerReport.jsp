<%@ page import="edu.chop.dgd.process.primerCreate.AmpliconSeq" %>
<%@ page import="edu.chop.dgd.process.primerCreate.UcscGene" %>
<%@ page import="edu.chop.dgd.process.primerCreate.UcscGeneExon" %>
<%@ page import="edu.chop.dgd.process.primerCreate.Variation" %>
<%@ page import="edu.chop.dgd.utils.Chromosome" %>
<%@ page import="edu.chop.dgd.web.DisplayMapper" %>
<%@ page import="edu.chop.dgd.web.HttpRequestFacade" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String pageTitle = "Division of Genomic Diagnstics";
    String headContent = "Division of Genomic Diagnstics";
    String pageDescription = "Division of Genomic Diagnstics";
    String message = (String) request.getAttribute("message");
    int speciesTypeKey = 1;
    Chromosome chrObj = new Chromosome();
    ArrayList<String> chrNames = (ArrayList<String>) chrObj.getChromosomes(speciesTypeKey);
    System.out.println(chrNames);

    HttpRequestFacade req= new HttpRequestFacade(request);
    ArrayList error = new ArrayList();
    DisplayMapper dm = new DisplayMapper(req, error);

    int start = Integer.parseInt((String) request.getParameter("start"));
    int stop = Integer.parseInt((String) request.getParameter("stop"));
    String chr = "chr" + request.getParameter("selectObject");
    if(chr.equals("23")){
        chr = "chrX";
    }else if(chr.equals("24")){
        chr = "chrY";
    }


    Map<UcscGene, UcscGeneExon> geneExonMap = (Map<UcscGene, UcscGeneExon>) request.getAttribute("geneExonMap");
    List<UcscGene> geneList = (List<UcscGene>) request.getAttribute("geneList");
    List<Variation> variationList = (List<Variation>) request.getAttribute("variantsList");
    AmpliconSeq amplicon = (AmpliconSeq) request.getAttribute("amplicon");
    int varrelPos = (Integer) request.getAttribute("variantRelPos");

    String fpath = (String) request.getAttribute("outputFiles");
    String filepath = fpath.replaceAll("\\n", "");
    String pr = (String) request.getAttribute("stats");

    String cutLastCar="NA";
    String optimalPrimers[] = new String[10];

    if(pr.equals("NA")){
        cutLastCar = pr;
        optimalPrimers[0]="NA";
    }else{
        cutLastCar = pr.substring(0, pr.length()-1);
        optimalPrimers = cutLastCar.split("\\n", -1);
    }
%>




      <html lang="en">
      <head>
          <meta charset="utf-8">
          <meta http-equiv="X-UA-Compatible" content="IE=edge">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Bootstrap 101 Template</title>

          <!-- Bootstrap -->
          <link href="css/bootstrap.min.css" rel="stylesheet">
          <!-- Custom styles for this template -->
          <link href="http://getbootstrap.com/examples/signin/signin.css" rel="stylesheet">

          <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
          <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
          <!--[if lt IE 9]>
          <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
          <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
          <![endif]-->
      </head>


      <body>
      <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
      <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
      <!-- Include all compiled plugins (below), or include individual files as needed -->
      <script src="js/bootstrap.min.js"></script>

      <img src="/dgdweb/resources/images/dgd.png" alt="dgdLogo" width="100" height="75"/>

            <h3>DGD Primer Designer Report</h3>

            <table>
                <tr>
                    <td>QueryRange:<%=chr%>:<%=start%>-<%=stop%></td>
                </tr>
                <tr>
                    <td>Genes In Region:
                        <%
                            for(UcscGene g:geneList){
                                out.print(g.getGeneSymbol()+"("+g.getRefSeqGeneName()+")");
                                out.print(", ");
                            }
                        %>
                    </td>
                </tr>
                <tr>
                    <td>Exons in range:
                        <%
                            if(geneExonMap.keySet().size()>0){
                                for(UcscGene gene : geneExonMap.keySet()){
                                    out.print(geneExonMap.get(gene).getUcscExonId());
                                    out.print("("+gene.getGeneSymbol()+")");
                                    out.print(", ");
                                }
                            }else{
                                out.print("Possibly Intronic");
                            }
                        %>
                    </td>
                </tr>
                <tr>
                    <td>Variants in Range:</td>
                </tr>

                    <%
                            for(Variation v : variationList){
                    %>
                        <tr><td colspan="=3"><%=v.getVarName()%> [ <%=v.getvClass()%> ] - <%=v.getvChr()%> : <%=v.getVstart()%> : <%=v.getVstop()%> : <%=v.getvStrand()%> ( <%=v.getvObserved()%> ) </td></tr>
                    <%
                            }
                    %>



                <tr>
                    <td><br/>Results:</td>
                </tr>

                <%
                    if(!optimalPrimers[0].equals("NA")){
                %>
                <tr>
                    <td>
                        <a href="/dgdweb/primer/FileDownload.html?file=<%=filepath+"_soft.xls"%>">Your SOFT File is Here!</a>
                        <br/>
                        <a href="/dgdweb/primer/FileDownload.html?file=<%=filepath+".detail.html"%>">Get Full Report Here!</a>
                        <br/>
                        <a href="/dgdweb/primer/FileDownload.html?file=<%=filepath+"_secondary.xls"%>">Your Secondary File is Here!</a>
                    </td>
                </tr>
                <tr>
                    <td><br/>Optimal Primers:</td>
                </tr>
                <tr>
                    <td>
                        <table border=1 bgcolor="#faebd7" width=700 height=200 align=center>
                            <tr height="30">
                                <td>Left Primer</td>
                                <td>Right primer</td>
                                <td>Insilico PCR</td>
                                <td>Blat</td>
                                <td>Snp Check</td>
                                <td>Final</td>
                            </tr>
                            <%
                                for(String oPr : optimalPrimers){
                                    if(oPr.length()>1){
                                        String delims="\\t";
                                        String flags1[] = oPr.split(delims, -1);
                                        String leftSeq = flags1[0].split(":", -1)[0];
                                        String rightSeq = flags1[1].split(":", -1)[0];
                                        String insilicoFlag = flags1[2].split(":", -1)[1];

                                        if(insilicoFlag.equals("1")){
                                            insilicoFlag="YES";
                                        }else{
                                            insilicoFlag="NO";
                                        }
                                        String leftBlatFlag = flags1[3].split(":", -1)[1];
                                        if(leftBlatFlag.equals("1")){
                                            leftBlatFlag="YES";
                                        }else{
                                            leftBlatFlag="NO";
                                        }
                                        String rightBlatFlag = flags1[4].split(":", -1)[1];
                                        if(rightBlatFlag.equals("1")){
                                            rightBlatFlag="YES";
                                        }else{
                                            rightBlatFlag="NO";
                                        }
                                        String leftInsCheckFlag = flags1[5].split(":", -1)[1];
                                        if(leftInsCheckFlag.equals("1")){
                                            leftInsCheckFlag="NO";
                                        }else{
                                            leftInsCheckFlag="YES";
                                        }
                                        String rightInsCheckFlag = flags1[6].split(":", -1)[1];
                                        if(rightInsCheckFlag.equals("1")){
                                            rightInsCheckFlag="NO";
                                        }else{
                                            rightInsCheckFlag="YES";
                                        }
                                        String finalFlag = flags1[7].split(":", -1)[1];
                                        if(finalFlag.equals("1")){
                                            finalFlag="YES";
                                        }else{
                                            finalFlag="NO";
                                        }
                            %>
                            <tr height=30 style="align-content: center">
                                <td><%=leftSeq%></td>
                                <td><%=rightSeq%></td>
                                <td><%=insilicoFlag%></td>
                                <td><%=leftBlatFlag%>/<%=rightBlatFlag%></td>
                                <td><%=leftInsCheckFlag%>/<%=rightInsCheckFlag%></td>
                                <td><%=finalFlag%></td>
                            </tr>
                            <%
                                        out.print("<br/>");
                                    }
                                }


                            %>

                        </table>
                    </td>
                </tr>
                <%
                    }else{
                %>
                <tr>
                    <td>
                        NO PRIMERS FOUND.
                    </td>
                </tr>


                <%
                    }
                %>
                <tr>
                    <td><br/></td>
                </tr>


                <tr>
                    <td><br/>Amplicon Sequence:<%=amplicon.getChr()+":"+amplicon.getAmpliconStart()+".."+amplicon.getAmpliconEnd()%>
                <pre><%
                    for(int c=1; c<=amplicon.getSequence().length(); c++){
                        String value = amplicon.getSequence().substring(c - 1, c);
                        if(c==varrelPos){
                            out.print("<span style='color:red; font-weight:700;'>" + value + "</span>");
                        }else{
                            out.print(value);
                        }
                        if(c % 60 == 0){
                            out.print("\n");
                        }
                    }
                    out.println(" ");
                %></pre>
                    </td>
                </tr>
                <tr>
                    <td><br/>Amplicon Masked Sequence:<%=amplicon.getChr()+":"+amplicon.getAmpliconStart()+".."+amplicon.getAmpliconEnd()%>
                <pre><%
                    for(int c=1; c<=amplicon.getMaskedSeq().length(); c++) {
                        String value = amplicon.getMaskedSeq().substring(c - 1, c);

                        if(variationList.get(0).getVarName().equals("novel")){
                            if(c==varrelPos){

                                out.print("<span style='color:red;background-color:#02599C; font-weight:700;'>" + value + "</span>");

                            }else if(value.equals("N")){

                                out.print("<span style='color:red; font-weight:700;'>" + value + "</span>");

                            }else{
                                out.print(value);
                            }
                        }else{
                            if(value.equals("N")){
                                if(c==varrelPos){
                                    out.print("<span style='color:red;background-color:#02599C; font-weight:700;'>" + value + "</span>");
                                }else{
                                    out.print("<span style='color:red; font-weight:700;'>" + value + "</span>");
                                }
                            }else{
                                out.print(value);
                            }
                        }

                        if(c % 60 == 0){
                            out.print("\n");
                        }
                    }
                    out.println(" ");
                %>
                </pre>
                    </td>
                </tr>



    </table>
    <br />
</body>
</html>


