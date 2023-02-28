<liferay-ui:success key="consultaExitosa" message="cotizacion.exito" />
<liferay-ui:error key="errorConocido" message=" ${errorMsg}" />
<liferay-ui:error key="errorDesconocido" message="cotizacion.erorDesconocido" />
<portlet:resourceURL id="/crm/presupuestos/produccion/consultarReporte" var="consultarReporteProd" cacheability="FULL"/>
<portlet:resourceURL id="/crm/presupuestos/produccion/consultarReporteMovimientos" var="consultarReporteMovimimientosProd" cacheability="FULL"/>
<portlet:resourceURL id="/crm/presupuestos/cargarInformacion/cargaDocumento" var="cargaDocumentoURL" cacheability="FULL"/>
<%-- <c:set var="permisoEmision" value="<%=RoleLocalServiceUtil.hasUserRole(user.getUserId(), user.getCompanyId(),\"TMX-EMISION EN LISTADO DE COTIZACIONES\", false)%>" /> --%>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">

<div id="customAlertJS"></div>
<c:set var="versionEntrega" scope="session" value="DESA.12062020.1130" />
<section class="upper-case-all">
	<div class="section-nav-wrapper">
		<ul class="nav nav-tabs nav-justified light-blue darken-4" role="tablist">
			<c:if test="${tipoConsulta == 1}">
				<c:set var="activePendiente" value="active" />
				<c:set var="activeTokio" value="" />
			</c:if>
			<c:if test="${tipoConsulta == 2}">
				<c:set var="activePendiente" value="" />
				<c:set var="activeTokio" value="active" />
			</c:if>

			<li class="nav-item active">
				<a class="nav-link " data-toggle="tab" href="#tab-reporte" role="tab">
					<liferay-ui:message key="PresupuestosCrmPortlet.titulo.reporte" />
				</a>
			</li>
			<li class="nav-item">
				<a class="nav-link" data-toggle="tab" href="#tab-carga" role="tab">
					<liferay-ui:message key="PresupuestosCrmPortlet.titulo.carga" />
				</a>
			</li>
		</ul>
	</div>
	<div class="tab-content">

		<c:if test="${tipoConsulta == 1}">
			<c:set var="activePendiente" value="in active" />
			<c:set var="activeTokio" value="" />
		</c:if>
		<c:if test="${tipoConsulta == 2}">
			<c:set var="activePendiente" value="" />
			<c:set var="activeTokio" value="in active" />
		</c:if>

		<!--Panel 1-->
		<div class="tab-pane in active" id="tab-reporte" role="tabpanel">
			<div class="row">
				<div class="col-md-4">
					<div class="md-form form-group">
						<select name="selArea" id="selArea" class="mdb-select requeridoProduccion">
							<option value="-1" selected disabled>Seleccionar</option>
							<option value="T">TODOS</option>
							<option value="M">MD</option>
							<option value="J">J</option>
						</select>
						<label for="selArea"><liferay-ui:message key="PresupuestosCrmPortlet.area" /></label>
					</div>
				</div>
				<div class="col-md-4">
					<div class="md-form form-group">
<!-- 						<input placeholder="Fecha Desde" type="date" id="dateAnio" name="dateAnio" class="form-control datepicker " value=""> -->
<!-- 						<label for="dateAnio">A&ntilde;o</label> -->
						<select name="selAnio" id="selAnio" class="mdb-select requeridoProduccion" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1" selected>Seleccionar</option>
							<c:forEach items="${listAnio}" var="option">
								<option value="${option}">${option}</option>
							</c:forEach>	
						</select>
						<label for="selAnio">A&ntilde;o</label>
<!-- 						<label for="selAnio"><liferay-ui:message key="PresupuestosCrmPortlet.anio" /></label> -->
					</div>
				</div>
				<div class="col-md-4">
					<div class="md-form form-group">
						<select name="selMes" id="selMes" class="mdb-select requeridoProduccion" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1" selected>Seleccionar</option>
							<option value="01">Enero</option>
							<option value="02">Febrero</option>
							<option value="03">Marzo</option>
							<option value="04">Abril</option>
							<option value="05">Mayo</option>
							<option value="06">Junio</option>
							<option value="07">Julio</option>
							<option value="08">Agosto</option>
							<option value="09">Septiembre</option>
							<option value="10">Octubre</option>		
							<option value="11">Noviembre</option>		
							<option value="12">Diciembre</option>		
						</select>
						<label for="selMes"><liferay-ui:message key="PresupuestosCrmPortlet.mes" /></label>
					</div>
				</div>
			</div>
			<div class="row mt-5">
				<div class="col-md-12">
					<h5>Reporte:</h5>
				</div>
				<div class="col-md-12">
					<div class="form-inline">
						<div class="form-check">
							<input class="form-check-input form-control" name="group2" type="radio" id="c_canal" checked="checked" value="1">
							<label class="form-check-label" for="c_canal">Por Canal</label>
						</div>
						<div class="form-check">
							<input class="form-check-input form-control" name="group2" type="radio" id="c_departamento" value="3">
							<label class="form-check-label" for="c_departamento">Por Departamento</label>
						</div>
						<div class="form-check">
							<input class="form-check-input form-control" name="group2" type="radio" id="c_ejecutivo" value="2">
							<label class="form-check-label" for="c_ejecutivo">Por Ejecutivo</label>
						</div>
					</div>
				</div>
			</div>
			<div class="row mt-5">
				<div class="col-sm-12 text-center">
					<div class="btn btn-blue" id="getreporte"  onclick="showTableReporte();">Generar Reporte</div>
				</div>
			</div>
			<div class="mt-5">
				<div id="tableReporteDiv" class="table-wrapper mt-5 d-none">
					<table id= "tablaReporte" class="table data-table table-bordered" style="width: 100%;">
						<thead style="color: #FFFFFF; background-color: #43aee9">
							<tr>
								<th id="tituloTablaPresupuestos">Canal</th>
								<th>Mes Real</th>
								<th>Mes T.C. BP</th>
								<th>Mes Budget</th>
								<th>% de avance</th>
								<th>Acumulado Real</th>
								<th>Acumulado T.C. BP</th>
								<th>Acumulado Budget</th>
								<th>% de avance</th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</div>
				<div id="tableMovimientoDiv" class="table-wrapper mt-5 d-none">
					<table id= "tablaReporteMovimientos" class="table data-table table-bordered" style="width: 100%;">
						<thead style="color: #FFFFFF; background-color: #43aee9">
							<tr>
								<th>ream</th>
								<th>canal</th>
								<th>area</th>
								<th>monto_usd</th>
								<th>monto_pesos</th>
								<th>moneda</th>
								<th>tc</th>
								<th>ejecutivo</th>
								<th>poliza</th>
								<th>certificado</th>
								<th>ramo_ventas</th>
								<th>cliente</th>
								<th>agente</th>
								<th>fecexp</th>
								<th>fecini</th>
								<th>fecter</th>
								<th>tipcer</th>
								<th>tipmov</th>
								<th>tipomovi</th>
								<th>tipoendoso</th>
								<th>tipcoa</th>
								<th>tiponegocio</th>
								<th>coaseg</th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</div>
			</div>
		</div>
		<!--/Panel 1-->
		<!--Panel 2-->
		<div class="tab-pane  ${ activeTokio }" id="tab-carga" role="tabpanel">
			
			<div class="row mt-5">
				<div class="col-sm-12 text-right">
					<!--<div class="btn btn-blue" id="">Descargar Layout Producci&oacute;n</div>-->
					<a class="btn btn-blue" href="${urlDoc}">Descargar Layout Producci&oacute;n</a>
				</div>
			</div>
			
			<section id="documentosAltaAgente">
				<div class="row d-flex justify-content-center">
					<div class="col-md-4">
						<form class="md-form">
							<div class="file-field big">
								<a class="btn-floating btn-lg blue lighten-1 mt-0 float-left">
									<i class="fas fa-upload" aria-hidden="true"></i>
									<input id="archivo" type="file" data-file_types="xls|xlsx" multiple>
								</a>
								<div class="file-path-wrapper">
									<input class="file-path validate" type="text" placeholder="Subir Archivo" readonly>
								</div>
							</div>
						</form>
					</div>
				</div>
			</section>
			
			<div class="row mt-5">
				<div class="col-sm-12 text-center">
					<div class="btn btn-blue" id="docCargaInformacion">Subir archivo</div>
				</div>
			</div>
			
		</div>	
	</div>
</section>

<script src="<%=request.getContextPath()%>/js/main.js?v=${version}"></script>
<script src="<%=request.getContextPath()%>/js/jquery.dataTables.min.js"></script>
<script src="<%=request.getContextPath()%>/js/dataTables.buttons.min.js"></script>
<script src="<%=request.getContextPath()%>/js/pdfmake.min.js"></script>
<script src="<%=request.getContextPath()%>/js/vfs_fonts.js"></script>
<script src="<%=request.getContextPath()%>/js/buttons.html5.min.js"></script>
<script>
	const consultarReporteProd = "${consultarReporteProd}";
	const consultarReporteMovimimientosProd = "${consultarReporteMovimimientosProd}";
	const spanishJson = "<%=request.getContextPath()%>" + "/js/dataTables.spanish.json";
	const curUrl = "${curUrl}";
	const cargaDocumentoURL = "${cargaDocumentoURL}";
</script>
