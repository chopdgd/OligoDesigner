<%@ page import="edu.chop.dgd.dgdObjects.OligoObject" %>
<%@ page import="edu.chop.dgd.dgdObjects.SequenceObject" %>
<%@ page import="edu.chop.dgd.web.DisplayMapper" %>
<%@ page import="edu.chop.dgd.web.HttpRequestFacade" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.TreeMap" %>
<html>
<head>
    <title>Hello World</title>
</head>
<body>

    <%

        HttpRequestFacade req= new HttpRequestFacade(request);
        ArrayList error = new ArrayList();
        DisplayMapper dm = new DisplayMapper(req, error);
        List<SequenceObject> sos = (List<SequenceObject>) request.getAttribute("sequenceObjects");
        String optimalOligosFile = (String) request.getAttribute("optimalOligosFile");
        String uploadedFilePath = (String) request.getAttribute("uploadedPath");
        String projectId = (String) request.getAttribute("projectId");

    %>

    <h2>Report:</h2>
    <table>
        <tr>
            <td>
                <h1>Project Name: <%=projectId%></h1>
            </td>
        </tr>
        <tr>
            <td>
                Uploaded File Path: <%=uploadedFilePath%>
            </td>
        </tr>
        <tr>
            <td>
                <h3>Download Oligos File:</h3>
            </td>
            <td>
                <a href="/dgdweb/oligo/FileDownload.html?file=<%=optimalOligosFile%>&object=oligo">Optimal Oligos from each section</a>
                <br/>
            </td>
        </tr>
        <br />
        <tr>
            <td>=======================</td>
        </tr>
        <tr>
            <td>
                <h3>Download all files in Project:</h3>
            </td>
        </tr>
        <%
            for(SequenceObject so : sos){
                //String detailFile =
        %>
        <tr>
            <td>
                <h5>For Query Region: <%=so.getChr()%>:<%=so.getStart()%>-<%=so.getStop()%></h5>
            </td>
        </tr>
        <tr>
            <td>
                <a href="/dgdweb/oligo/FileDownload.html?file=<%=so.getDetailsFile()%>&object=oligo">Detailed report</a>
                <br/>
            </td>
        <tr>
            <td>
                <a href="/dgdweb/oligo/FileDownload.html?file=<%=so.getSecondaryFile()%>&object=oligo">All Oligos</a>
                <br/>
            </td>
        </tr>
        <%
            }
        %>
    </table>

</body>
</html>