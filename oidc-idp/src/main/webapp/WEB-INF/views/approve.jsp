<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="org.springframework.security.core.AuthenticationException"%>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException"%>
<%@ page import="org.springframework.security.web.WebAttributes"%>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message code="approve.title" var="title"/>
<o:customHeader title="${title}"/>
<div id="content">
	<c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION" />
	<div class="row">
		<form name="confirmationForm"
			  action="${pageContext.request.contextPath.endsWith('/') ? pageContext.request.contextPath : pageContext.request.contextPath.concat('/') }authorize" method="post">
			<input id="user_oauth_approval" name="user_oauth_approval" value="true" type="hidden" />
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
			<div class="row">
				<div class="col-xs-6">
					<input type="submit" value="Yes, continue" name="yes" class="btn btn-lg btn-success btn-block" id="yesbutton">
				</div>
				<div class="col-xs-6">
					<input type="submit" value="No, cancel" name="no" class="btn btn-lg btn-light btn-block" id="nobutton">
				</div>
			</div>
			<div class="row" style="margin: .5em 0; text-align: center">
				<h4><spring:message code="approve.remember.title"/>:</h4>
				<div class="col-12 form-check">
					<input class="form-check-input" type="radio" name="remember"
						   id="remember-forever" value="until-revoked/" ${ !consent ? 'checked="checked"' : '' }>
					<label class="form-check-label" for="remember-forever" class="radio">
						<spring:message code="approve.remember.until_revoke"/>
					</label>
				</div>
				<div class="col-12 form-check">
					<input class="form-check-input" type="radio" name="remember" id="remember-hour" value="one-hour">
					<label class="form-check-label" for="remember-hour">
						<spring:message code="approve.remember.one_hour"/>
					</label>
				</div>
				<div class="col-12 form-check">
					<input class="form-check-input" type="radio" name="remember"
						   id="remember-not" value="none" ${ consent ? 'checked="checked"' : '' }>
					<label class="form-check-label" for="remember-not">
						<spring:message code="approve.remember.next_time"/>
					</label>
				</div>

			</div>
			<p>Privacy policy for the service <a target='_blank' href='<c:out value="${client.policyUri}" />'>
				<c:choose>
					<c:when test="${empty client.clientName}">
						<em><c:out value="${client.clientId}" /></em>
					</c:when>
					<c:otherwise>
						<em><c:out value="${client.clientName}" /></em>
					</c:otherwise>
				</c:choose>
			</a></p>
			<h3 id="attributeheader">Information that will be sent to
				<c:choose>
					<c:when test="${empty client.clientName}">
						<em><c:out value="${client.clientId}" /></em>
					</c:when>
					<c:otherwise>
						<em><c:out value="${client.clientName}" /></em>
					</c:otherwise>
				</c:choose>
			</h3>
			<table id="table_with_attributes" class="table attributes" summary="List the information about you that is about to be transmitted to the service you are going to login to">
				<caption>User information</caption>
				<c:forEach var="scope" items="${scopes}">
					<tr>
						<td>
							<span class="attrname" style="text-transform: capitalize;">
								<c:out value="${ fn:toLowerCase(fn:escapeXML(scope.value))}" />
							</span>
							<div class="attrvalue">
								<c:if test="${ not empty claims[scope.value] }">
									<c:forEach var="claim" items="${ claims[scope.value] }">
								 		<c:out value="${ claim.value }" /><br />
									</c:forEach>
								</c:if>
							</div>
							<input type="checkbox" name="scope_${ fn:escapeXml(scope.value) }" checked="checked"
								   id="scope_${ fn:escapeXml(scope.value) }" value="${ fn:escapeXml(scope.value) }">
						</td>
					</tr>
				</c:forEach>
			</table>
		</form>
	</div>
</div>
</div>
<o:customFooter/>
