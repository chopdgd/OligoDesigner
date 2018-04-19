<%--
  Created by IntelliJ IDEA.
  User: jayaramanp
  Date: 11/5/14
  Time: 4:09 PM
  To change this template use File | Settings | File Templates.
--%>
<%
    StringBuffer url = request.getRequestURL();
    String uri = request.getRequestURI();
    String host = url.substring(0, url.indexOf(uri));
    System.out.println(url);
    System.out.println(uri);
    System.out.println(host);
%>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- FONTS -->
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Ubuntu" type='text/css'/>
    <!-- Bootstrap -->
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
    <!-- Optional theme -->
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme.min.css">
    <link rel="stylesheet" href="https://github.com/flatlogic/awesome-bootstrap-checkbox/blob/master/awesome-bootstrap-checkbox.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-validator/0.5.3/css/bootstrapValidator.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-validator/0.5.3/css/bootstrapValidator.min.css">

    <%
        if(host.contains("dgdr7ant01")){
            System.out.println(url);
            System.out.println(uri);
            System.out.println(host);
    %>
        <link rel="stylesheet" href="/resources/css/navbar_changed.css">
        <link rel="stylesheet" href="/resources/css/awesome-bootstrap-checkbox.css">
        <link rel="shortcut icon" href="/resources/images/favicon.ico" type="image/x-icon"/>

    <%
    }else{
    %>
        <link rel="stylesheet" href="/Antholigo/resources/css/navbar_changed.css">
        <link rel="stylesheet" href="/Antholigo/resources/css/awesome-bootstrap-checkbox.css">
        <link rel="shortcut icon" href="/Antholigo/resources/images/favicon.ico" type="image/x-icon"/>

    <%

        }
    %>


    <!-- Custom styles for this template -->
    <link href="http://getbootstrap.com/examples/signin/signin.css" rel="stylesheet">


    <style>



    </style>


    <title><%=request.getParameter("pageTitle")%></title>

</head>
<body>

<nav class="navbar" role="navigation">
    <div class="container">
    <form class="navbar-form">
        <div class="navbar-header form-group">
            <%
                String projectTitle = request.getParameter("projectTitle");
            %>
            <a class="navbar-brand" href="#"><%=projectTitle%></a>
        </div>
        <ul class="nav navbar-nav" style="font-size: medium; font-family: 'Ubuntu';">
            <%
                if(host.contains("dgdr7ant01")){
                    System.out.println(url);
                    System.out.println(uri);
                    System.out.println(host);
            %>
                <li style="font-size: large"><a href=/oligo/home.html>Home</a></li>
                <li style="font-size: large"><a href="/oligo/help.html">Help</a></li>

            <%
            }else{
            %>
                <li style="font-size: large"><a href=/Antholigo/oligo/home.html>Home</a></li>
                <li style="font-size: large"><a href="/Antholigo/oligo/help.html">Help</a></li>

            <%

                }
            %>


            <li style="font-size: large"><a href="https://www.chop.edu/centers-programs/pathology-and-laboratory-medicine/contact">Contact</a></li>
        </ul>
        <ul class="nav navbar-nav navbar-right">
            <li><img src=<%=request.getParameter("logoImgPath")%> alt="dgdLogo"
                     width="200" height=65"/></li>
        </ul>
        <!--/.nav-collapse -->
    </form>
    </div>
</nav>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<!-- Latest compiled and minified JavaScript -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/1000hz-bootstrap-validator/0.11.9/validator.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/1000hz-bootstrap-validator/0.11.9/validator.min.js"></script>
<!--<script src="../../js/bootstrap-validator_0.11.9_validator.js"></script>-->

<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
<script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>

<![endif]-->


<br /> <br />