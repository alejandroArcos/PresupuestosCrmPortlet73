<liferay-ui:success key="consultaExitosa" message="cotizacion.exito" />
<liferay-ui:error key="errorConocido" message=" ${errorMsg}" />
<liferay-ui:error key="errorDesconocido" message="cotizacion.erorDesconocido" />
<portlet:resourceURL id="/crm/presupuestos/gastos/consultarTasaCambio" var="consultarTasaCambioFront" cacheability="FULL"/>
<portlet:resourceURL id="/crm/presupuestos/gastos/consultaAgentes" var="consultaAgentesFront" cacheability="FULL"/>
<portlet:resourceURL id="/crm/presupuestos/gastos/registraGasto" var="registraGastoURL" cacheability="FULL"/>
<portlet:resourceURL id="/crm/presupuestos/cargarInformacion/cargaDocumento" var="cargaDocumentoURL" cacheability="FULL"/>
<portlet:resourceURL id="/crm/presupuestos/gastos/consultarReporte" var="consultarReporteURL" cacheability="FULL"/>
<portlet:resourceURL id="/crm/presupuestos/gastos/consultarReporteMovimientos" var="consultarReporteMovimientosURL" cacheability="FULL"/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css">

<div style="display: none;">
	<input type="hidden" id="rechazoCotizacionURL" value="${rechazoCotizacion}">
	<input type="hidden" id="datosMensajesURL" value="${datosMensajes}">
	<input type="hidden" id="saveComentarioURL" value="${saveComentarioURL}">
	<input type="hidden" id="datosArchivosURL" value="${datosArchivos}">
	<input type="hidden" id="solicitarTmxURL" value="${solicitarTmx}">
	<input type="hidden" id="revireURL" value="${revire}">
</div>

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
			<li class="nav-item">
				<a class="nav-link" data-toggle="tab" href="#tab-gasto" role="tab">
					Registra Gasto
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
						<select name="selArea" id="selArea" class="mdb-select requeridoResporteGastos" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1" selected disabled>Seleccionar</option>
							<option value="1">TODO</option>
							<option value="46">MD</option>
							<option value="47">J</option>
						</select>
						<label for="selArea"><liferay-ui:message key="PresupuestosCrmPortlet.area" /></label>
					</div>
				</div>
				<div class="col-md-4">
					<div class="md-form form-group">
						<select name="selAnio" id="selAnio" class="mdb-select requeridoResporteGastos" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1" disabled selected>Seleccionar</option>
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
						<select name="selMes" id="selMes" class="mdb-select requeridoResporteGastos" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1" disabled selected>Seleccionar</option>
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
							<input class="form-check-input form-control" name="group2" type="radio" id="cn_personamoral" checked="checked" value="0">
							<label class="form-check-label" for="cn_personamoral">Por Agente</label>
						</div>
						<div class="form-check">
							<input class="form-check-input form-control" name="group2" type="radio" id="cn_personafisica" value="1">
							<label class="form-check-label" for="cn_personafisica">Por Ejecutivo</label>
						</div>
						<div class="form-check">
							<input class="form-check-input form-control" name="group2" type="radio" id="byEjecutivo" value="2">
							<label class="form-check-label" for="byEjecutivo">Por Departamento</label>
						</div>
					</div>
				</div>
			</div>
			<div class="row mt-5">
				<div class="col-sm-12 text-center">
					<div class="btn btn-blue" id="getreporte" onclick="showTableReporteGastos();">Generar Reporte</div>
				</div>
			</div>
			<div class="mt-5">
				<div id="tableReporte" class="table-wrapper mt-5 d-none">
					<table id="tablaReporteGastos" class="table data-table table-bordered" style="width: 100%;">
						<thead style="color: #FFFFFF; background-color: #43aee9">
							<tr>
								<th id="tituloTablaGastos">Canal</th>
								<th>Acumulado Real</th>
								<th>Acumulado Budget</th>
								<th>% de avance</th>
							</tr>
						</thead>
						
						<tbody>
						</tbody>
					</table>
				</div>
				<div id="tableMovimientosDiv" class="table-wrapper mt-5 d-none">
					<table id="tablaMovimientosGasto" class="table data-table table-bordered" style="width: 100%;">
						<thead style="color: #FFFFFF; background-color: #43aee9">
							<tr>
								<th>&Aacute;rea</th>
								<th>Oficina/&Aacute;rea</th>
								<th>Agente</th>
								<th>Ejecutivo</th>
								<th>Mes</th>
								<th>A&ntilde;o</th>
								<th>Moneda</th>
								<th>Monto</th>
								<th>Tipo Cambio</th>
								<th>Fecha</th>
								<th>Tipo Registro</th>
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
					<!--<div class="btn btn-blue" href="${urlDoc}">Descargar Layout Producci&oacute;n</div>-->
					<a class="btn btn-blue" href="${urlDoc}">Descargar Layout Gastos</a>
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
									<input class="file-path validate" type="text"  placeholder="Subir Archivo" readonly>
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
		<!-- /Panel 2 -->
		<!-- Panel 3 -->
		<div class="tab-pane" id="tab-gasto" role="tabpanel">
			<div class="row mt-5">
				<div class="col-md-3">
					<div class="md-form form-group">
						<select name="selAnioGasto" id="selAnioGasto" class="mdb-select requeridoGastos requeridoDivisa" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1" selected disabled>Seleccionar</option>
							<c:forEach items="${listAnio}" var="option">
								<option value="${option}">${option}</option>
							</c:forEach>	
						</select>
						<label for="selAnioGasto">A&ntilde;o</label>
<!-- 						<label for="selAnio"><liferay-ui:message key="PresupuestosCrmPortlet.anio" /></label> -->
					</div>
				</div>
				<div class="col-md-3">
					<div class="md-form form-group">
						<select name="selMesGasto" id="selMesGasto" class="mdb-select requeridoGastos requeridoDivisa" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1" selected disabled>Seleccionar</option>
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
						<label for="selMesGasto"><liferay-ui:message key="PresupuestosCrmPortlet.mes" /></label>
					</div>
				</div>
				<div class="col-md-3">
					<div class="md-form form-group">
						<!--<input id="ejecutivoGasto" type="text" name="ejecutivoGasto"  class="form-control" >-->
						<select name="ejecutivoGasto" id="ejecutivoGasto" class="mdb-select requeridoGastos" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />' ${usuarioCrm!=null?'disabled':''}>
							<option value="-1">Seleccionar</option>
							<c:forEach items="${listaUsuarios}" var="option">
								<option value="${option.id}" ${usuarioCrm.id==option.id?'selected':''}>${option.nombre}</option>
							</c:forEach>
						</select>
						<label for="ejecutivoGasto">Ejecutivo</label>
					</div>
				</div>
				<div class="col-md-3">
					<div class="md-form form-group">
						<input id="areaGasto" type="text" name="areaGasto" value="${area!=null?area.valor:''}" class="form-control" disabled>
						<label for="areaGasto">&Aacute;rea</label>
					</div>
				</div>
			</div>
			<div class="row mt-5">
				<div class="col-md-4">
					<div class="md-form form-group">
						<select name="agenteGasto" id="agenteGasto" class="mdb-select requeridoGastos" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1">Seleccionar</option>
							<c:forEach items="${listaAgentes}" var="option">
								<option value="${option.agenteId}">${option.nombre}  ${option.apellidoP}  ${option.apellidoM} - ${option.clave}</option>
							</c:forEach>
						</select>
						<label for="agenteGasto">Agente</label>
					</div>
				</div>
				<div class="col-md-4">
					<div class="md-form form-group">
						<select name="monedaGasto" id="monedaGasto" class="mdb-select requeridoGastos" searchable='<liferay-ui:message key="PresupuestosCrmPortlet.buscar" />'>
							<option value="-1" selected disabled>Seleccionar</option>
							<c:forEach items="${listMoneda}" var="option">
								<option value="${option.id}">${option.valor}</option>
							</c:forEach>
						</select>
						<label for="monedaGasto">Moneda</label>
					</div>
				</div>
				<div class="col-md-4">
					<div class="md-form form-group">
						<input id="montoGasto" type="number" name="montoGasto" class="form-control requeridoGastos" step="0.01">
						<label for="montoGasto">Monto</label>
					</div>
				</div>
			</div>
			<div class="row mt-5 d-none" id="cambioOculto">
				<div class="col-md-4">
					<div class="md-form form-group">
						<input id="tipoCambio" type="text" name="tipoCambio" class="form-control" readonly>
						<label for="tipoCambio">Tipo de Cambio</label>
					</div>
				</div>
				<div class="col-md-4">
					<div class="md-form form-group">
						<input id="montoGastoMXN" type="number" name="montoGastoMXN" class="form-control" step="0.01" readonly>
						<label for="montoGastoMXN">Monto MXN</label>
					</div>
				</div>
			</div>
			
			<div class="row mt-5">
				<div class="col-sm-12 text-center">
					<div class="btn btn-blue" id="registraGasto">Guardar</div>
				</div>
			</div>
			
		</div>
		<!-- /Panel 3 -->
	</div>
</section>
<script src="<%=request.getContextPath()%>/js/jquery.dataTables.min.js"></script>
<script src="<%=request.getContextPath()%>/js/dataTables.buttons.min.js"></script>
<script src="<%=request.getContextPath()%>/js/pdfmake.min.js"></script>
<script src="<%=request.getContextPath()%>/js/vfs_fonts.js"></script>
<script src="<%=request.getContextPath()%>/js/buttons.html5.min.js"></script>

<script src="<%=request.getContextPath()%>/js/main.js?v=${version}"></script>

<script>
	const consultarTasaCambioFront = "${consultarTasaCambioFront}";
	const consultaAgentesFront = "${consultaAgentesFront}";
	const registraGastoURL = "${registraGastoURL}";
	const cargaDocumentoURL = "${cargaDocumentoURL}";
	const curUrl = "${curUrl}";
	const consultarReporteURL = "${consultarReporteURL}";
	const consultarReporteMovimientosURL = "${consultarReporteMovimientosURL}";
	const spanishJson = "<%=request.getContextPath()%>" + "/js/dataTables.spanish.json";
</script>
