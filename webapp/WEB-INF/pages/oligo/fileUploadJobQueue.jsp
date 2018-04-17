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
    <jsp:param name="logoImgPath" value="/resources/images/chop-logo_new.png"/>
</jsp:include>

<%

    HttpRequestFacade req= new HttpRequestFacade(request);
    ArrayList error = new ArrayList();
    DisplayMapper dm = new DisplayMapper(req, error);
    String pid = (String) request.getAttribute("proj_id");
    String assembly = (String) request.getAttribute("assembly");
    String uploadedFilePath = (String) request.getAttribute("uploads");
    String origFileName = (String) request.getAttribute("origFilename");
    String uploadResponse = (String) request.getAttribute("fileUploadResponse");
    String oligo_seq_separation = (String) request.getAttribute("separation");
    String email = (String) request.getAttribute("email");

    String min_gc = (String) request.getAttribute("minGC");
    String max_gc = (String) request.getAttribute("maxGC");
    String opt_gc = (String) request.getAttribute("optGC");

    String min_tm = (String) request.getAttribute("minTm");
    String max_tm = (String) request.getAttribute("maxTm");
    String opt_tm = (String) request.getAttribute("optTm");

    String min_length = (String) request.getAttribute("minLen");
    String max_length = (String) request.getAttribute("maxLen");
    String opt_length = (String) request.getAttribute("optLen");

    String na = (String) request.getAttribute("naIon");
    String mg = (String) request.getAttribute("mgIon");

    String self_any = (String) request.getAttribute("selfAny");
    String self_end = (String) request.getAttribute("selfEnd");

    String free_energy_hairpin = (String) request.getAttribute("free_energy_hairpin");
    String free_energy_homodimer = (String) request.getAttribute("free_energy_homodimer");
    String free_energy_heterodimer = (String) request.getAttribute("free_energy_heterodimer");

%>




<base href="${fn:substring(url, 0, fn:length(url) - fn:length(uri))}${req.contextPath}/">
<script src="js/global.js"></script>
<link rel="stylesheet" href="css/global.css">
<div class="container">
    <div class="media">

        <div class="media-left">
            <img src="/resources/images/taskqueue.jpeg" alt="dgdLoader" class="media-object" style="width:320px" height="275px">
        </div>
        <div class="media-right">
            <h4 class="media-heading" style="font-weight: 200"><b><%=pid%></b></h4>
            <h8><%=uploadResponse%></h8>
        </div>
        <div class="alert alert-success fade in">Your task has been queued. Your results will be emailed to you at the email address submitted.</div>
        <div class="jumbotron" style="font-size: medium;">
            <p class="panel-info" style="font-size: medium;">Estimated processing times depending on the size of your region of interest are as follows:
            <table class="table table-bordered table-condensed">
            <thead>
            <tr>
                <th class="col-sm-3">Region Size</th>
                <th class="col-sm-3">Processing Time</th>

            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="col-sm-3">30KB</td>
                <td class="col-sm-3">~1min</td>
            </tr>
            <tr>
                <td class="col-sm-3">200KB</td>
                <td class="col-sm-3">~10-15mins</td>
            </tr>
            <tr>
                <td class="col-sm-3">2.5MB</td>
                <td class="col-sm-3">~2Hrs</td>
            </tr>
            </tbody>
            </table>
            </p>
        </div>
    </div>


</div>

<jsp:include page="../common/footer.jsp" flush="true">
    <jsp:param name="contact" value="dgdbfx at email dot chop dot edu"/>
</jsp:include>