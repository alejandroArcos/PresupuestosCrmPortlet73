<%@ include file="/init.jsp" %>

<c:if test = "${fn:contains(curUrl, 'produccion')}">
	<%@ include file="./jsp/produccion.jsp" %>
</c:if>

<c:if test = "${fn:contains(curUrl, 'gastos')}">
	<%@ include file="./jsp/gastos.jsp" %>
</c:if>
