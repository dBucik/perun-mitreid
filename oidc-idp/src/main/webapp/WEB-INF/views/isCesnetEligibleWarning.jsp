<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common"%>

<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>
<%

    String samlCssUrl = (String) pageContext.getAttribute("samlResourcesURL");
    List<String> cssLinks = new ArrayList<>();

    cssLinks.add(samlCssUrl + "/module.php/perun/res/css/perun_identity_go_to_registration.css");

    pageContext.setAttribute("cssLinks", cssLinks);

%>

<t:header title="${langProps['is_cesnet_eligible_warning_title']}" reqURL="${reqURL}"
          baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content">
    <div id="head">
        <h1>${langProps['is_cesnet_eligible_warning_header']}</h1>
    </div>
    <p>${langProps['is_cesnet_eligible_warning_text']}</p>
    <form method="GET" action="${action}">
        <hr/>
        <br/>
        <input type="hidden" name="target" value="${target}"/>
        <input type="hidden" name="accepted" value="true"/>
        <input type="submit" name="continueToRegistration" value="${langProps['is_cesnet_eligible_warning_continue']}"
               class="btn btn-lg btn-primary btn-block">
    </form>
</div>
</div><!-- ENDWRAP -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>