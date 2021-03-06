<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"
        import="cz.muni.ics.oidc.server.elixir.GA4GHClaimSource" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<c:if test="${empty scopes}">
    <p>${langProps['no_scopes']}</p>
</c:if>
<c:if test="${not empty scopes}">
    <ul id="perun-table_with_attributes" class="perun-attributes">
        <c:forEach var="scope" items="${scopes}">
            <c:set var="scopeValue" value="${langProps[scope.value]}"/>
            <c:if test="${empty fn:trim(scopeValue)}">
                <c:set var="scopeValue" value="${scope.value}"/>
            </c:if>
            <c:set var="singleClaim" value="${fn:length(claims[scope.value]) eq 1}" />
            <li class="scope-item scope_${fn:escapeXml(scope.value)}">
                <div class="row">
                    <div class="col-sm-5">
                        <div class="checkbox-wrapper">
                            <input class="mt-0 mr-half" type="checkbox" name="scope_${ fn:escapeXml(scope.value) }" checked="checked"
                                   id="scope_${fn:escapeXml(scope.value)}" value="${fn:escapeXml(scope.value)}">
                        </div>
                        <h2 class="perun-attrname <c:out value="${classes['perun-attrname.h2.class']}"/>">
                            <label for="scope_${fn:escapeXml(scope.value)}"
                                   class="<c:out value="${classes['perun-attrname.h2.class']}"/>">${scopeValue}</label>
                        </h2>
                    </div>
                    <div class="perun-attrcontainer col-sm-7">
                        <span class="perun-attrvalue">
                            <ul class="perun-attrlist <c:out value="${classes['perun-attrcontainer.ul.class']}"/>">
                                <c:forEach var="claim" items="${claims[scope.value]}">
                                    <c:choose>
                                        <c:when test="${not singleClaim}">
                                            <li class="subclaim subclaim_${fn:escapeXml(claim.key)}">
                                                <c:set var="claimKey" value="${langProps[claim.key]}"/>
                                                <c:if test="${empty fn:trim(claimKey)}">
                                                    <c:set var="claimKey" value="${claim.key}"/>
                                                </c:if>
                                                <h3 class="visible-xs-block visible-sm-inline-block visible-md-inline-block
                                                    visible-lg-inline-block <c:out value="${classes['perun-attrlist.h3.class']}"/>">
                                                    ${claimKey}:
                                                </h3>
                                                <c:if test="${claim.value.getClass().name eq 'java.util.ArrayList'}">
                                                    <ul class="subclaim-value">visible-md-inline-block
                                                        <c:forEach var="subValue" items="${claim.value}">
                                                            <li>${subValue}</li>
                                                        </c:forEach>
                                                    </ul>
                                                </c:if>
                                                <c:if test="${not(claim.value.getClass().name eq 'java.util.ArrayList')}">
                                                    <span class="subclaim-value">${claim.value}</span>
                                                </c:if>
                                            </li>
                                        </c:when>
                                        <c:when test="${claim.value.getClass().name eq 'java.util.ArrayList'}">
                                            <c:forEach var="subValue" items="${claim.value}">
                                                <c:choose>
                                                    <c:when test="${claim.key=='ga4gh_passport_v1'}">
                                                        <li><%= GA4GHClaimSource.parseAndVerifyVisa(
                                                                (String) jspContext.findAttribute("subValue")).getPrettyString() %></li>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <li>${subValue}</li>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <li>${claim.value}</li>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </ul>
                        </span>
                    </div>
                </div>
            </li>
        </c:forEach>
    </ul>
</c:if>