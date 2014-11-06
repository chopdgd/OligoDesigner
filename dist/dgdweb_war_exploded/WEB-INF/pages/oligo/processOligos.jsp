<%@ page import="edu.chop.dgd.dgdObjects.OligoObject" %>
<%@ page import="edu.chop.dgd.dgdObjects.SequenceObject" %>
<%@ page import="edu.chop.dgd.web.DisplayMapper" %>
<%@ page import="edu.chop.dgd.web.HttpRequestFacade" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.TreeMap" %>


    <%

        HttpRequestFacade req= new HttpRequestFacade(request);
        ArrayList error = new ArrayList();
        DisplayMapper dm = new DisplayMapper(req, error);
        List<SequenceObject> sos = (List<SequenceObject>) request.getAttribute("sequenceObjects");
        String optimalOligosFile = (String) request.getAttribute("optimalOligosFile");
        String uploadedFilePath = (String) request.getAttribute("uploadedPath");
        String projectId = (String) request.getAttribute("projectId");

    %>


<jsp:include page="../common/header.jsp" flush="false">
    <jsp:param name="projectTitle" value="ANTHOLIGO" />
</jsp:include>


    <div class="jumbotron">
        <div class="panel-heading">
            <h1 class="panel-title">Project Name: <%=projectId%></h1>
            <div class="panel-heading">
                <div class="panel-body">Uploaded File Path: <%=uploadedFilePath%></div>
            </div>

            <h3 class="panel-body">Download Oligos File:</h3>
            <div class="link">

                <ul class="list-groups">
                     <li class="list-group-item"><a href="/dgdweb/oligo/FileDownload.html?file=<%=optimalOligosFile%>&object=oligo">Optimal Oligos from each section</a></li>
                </ul>
            </div>
            <div class="panel-heading">
            <br/>
            <h3 class="panel-title">Download all files in Project:</h3>
                <%
                    for(SequenceObject so : sos){
                        //String detailFile =
                %>
                        <h5 class="panel-body">For Query Region: <%=so.getChr()%>:<%=so.getStart()%>-<%=so.getStop()%></h5>
                        <div class="link">
                            <ul class="list-groups">
                                <li class="list-group-item"><a href="/dgdweb/oligo/FileDownload.html?file=<%=so.getDetailsFile()%>&object=oligo">Detailed report</a></li>
                                <li class="list-group-item"><a href="/dgdweb/oligo/FileDownload.html?file=<%=so.getSecondaryFile()%>&object=oligo">All Oligos</a></li>
                            </ul>
                        </div>
                        <br/>
                <%
                    }
                %>
            </div>
        </div>
    </div>


<jsp:include page="../common/footer.jsp" flush="false"></jsp:include>