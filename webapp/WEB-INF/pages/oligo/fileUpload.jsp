<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="edu.chop.dgd.web.DisplayMapper" %>
<%@ page import="edu.chop.dgd.web.HttpRequestFacade" %>
<%@ page import="java.util.ArrayList" %>
<c:set var="req" value="${pageContext.request}" />
<c:set var="url">${req.requestURL}</c:set>
<c:set var="uri" value="${req.requestURI}" />
<%--
  Created by IntelliJ IDEA.
  User: jayaramanp
  Date: 10/30/14
  Time: 11:47 AM
  To change this template use File | Settings | File Templates.
--%>
<%
    String pageTitle = "ANTHOLIGO - Oligo Design Application";
    String headContent = "ANTHOLIGO - Oligo Design Application";
    String pageDescription = "ANTHOLIGO - Oligo Design Application";
%>

<jsp:include page="../common/header.jsp" flush="false">
    <jsp:param name="projectTitle" value="ANTHOLIGO" />
    <jsp:param name="pageTitle" value="<%=pageTitle%>"/>
</jsp:include>

<%

    HttpRequestFacade req= new HttpRequestFacade(request);
    ArrayList error = new ArrayList();
    DisplayMapper dm = new DisplayMapper(req, error);
    String pid = (String) request.getAttribute("proj_id");
    String uploadedFilePath = (String) request.getAttribute("uploads");
    String origFileName = (String) request.getAttribute("origFilename");
    String uploadResponse = (String) request.getAttribute("fileUploadResponse");
%>




<base href="${fn:substring(url, 0, fn:length(url) - fn:length(uri))}${req.contextPath}/">
<script src="js/global.js"></script>
<link rel="stylesheet" href="css/global.css">
<META HTTP-EQUIV=Refresh CONTENT="3; URL=/dgdweb/oligo/processOligos.html?uploadFolderPath=<%=uploadedFilePath%>&proj_id=<%=pid%>&origFile=<%=origFileName%>"/>


<div class="panel">
    <div class="panel-heading"><%=uploadResponse%></div>
    <p class="panel-body">
        <div class="media media-middle">
            <p class="media-middle"><img src="/dgdweb/resources/images/loader.gif" alt="dgdLoader" style="align-content: center;"/></p>
        </div>
    </p>
    <div class="alert alert-info">Please wait..while we are processing your oligos. Do not close this tab as you might lose your results.</div>
    <div class="well">
        <ul class="list-group">The following processes are running in order to create your optimal oligos set:
        <li class="list-group-item">Primer3</li>
        <li class="list-group-item">BLAT</li>
        <li class="list-group-item">Hairpin Analysis</li>
        <li class="list-group-item">Homodimer Analysis</li>
        <li class="list-group-item">Heterodimer Analysis</li>
        </ul>
    </div>
    <p class="panel-heading">We have estimated the following processing times based on the size of your region of interest</p>
    <div class="panel-body">
        <table class="table">
            <div class="row">
                <div class="col-sm-4">Region Size</div>
                <div class="col-sm-4">Processing Time</div>
            </div>
            <div class="row">
                <div class="col-sm-4">30KB</div>
                <div class="col-sm-4">~1-2min</div>
            </div>
            <div class="row">
                <div class="col-sm-4">200KB</div>
                <div class="col-sm-4">~35-40mins</div>
            </div>
            <div class="row">
                <div class="col-sm-4">1MB</div>
                <div class="col-sm-4">~2Hrs</div>
            </div>
        </table>
    </div>

</div>
<%--<form action="/dgdweb/oligo/processOligos.html?uploadFolderPath=<%=uploadedFilePath%>&proj_id=<%=pid%>"
      method="post" style="align-content: center">
    <p><img src="/dgdweb/resources/images/loader.gif" alt="dgdLoader" style="align-content: center;"/></p>
    <div>
        Please wait..wait we are processing your oligos...
        <%=uploadResponse%>
    </div>
</form>--%>
<!-- this script submits the form AFTER it has been completely loaded -->

<jsp:include page="../common/footer.jsp" flush="true"></jsp:include>