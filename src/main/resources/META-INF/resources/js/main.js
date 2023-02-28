function showTableReporte(){
	showLoader();
	if(validaCampos("requeridoProduccion")) {
		var objetoSend = {
			area: $("#selArea").val(),
			anio: $("#selAnio option:selected").html(),
			mes: $("#selMes").val(),
			tipoReporte: $("input[name='group2']:checked").val()
		}
		$.post(consultarReporteProd, objetoSend).done(function (data) {
			let datos = JSON.parse(data);
			let lista = datos.lista;
			if(lista.length > 0) {
				$("#tituloTablaPresupuestos").text($("input[name='group2']:checked").siblings('label').text().replace("Por ",""));
				restartDatatable("tablaReporte");
				var dt = $('#tablaReporte').DataTable();
				dt.clear();
				if (lista.length !== 0) {
					dt
						.clear()
						.draw();
				}
				$(lista).each(function (indice, data) {
					dt.row.add([
						estaVacio(data.canal),
						formatoDinero(estaVacio(data.mes_real),'currency',2),
						formatoDinero(estaVacio(data.mes_tc),'currency',2),
						formatoDinero(estaVacio(data.mes_budget),'currency',2),
						estaVacio(data.mes_avance) + "%",
						formatoDinero(estaVacio(data.acum_real),'currency',2),
						formatoDinero(estaVacio(data.acum_tc),'currency',2),
						formatoDinero(estaVacio(data.acum_budget),'currency',2),
						estaVacio(data.acum_avance) + "%"
					]).draw(false);
				});
				$.post(consultarReporteMovimimientosProd, objetoSend).done(function (data) {
					var datos = JSON.parse(data);
					let lista = datos.lista;
					if(datos.code === 0) {
						if (lista!== undefined && lista.length > 0) {
							restartDatatable("tablaReporteMovimientos");
							var dtr = $('#tablaReporteMovimientos').DataTable();
							dtr.clear();
							if (lista.length !== 0) {
								dtr
									.clear()
									.draw();
							}
							$(lista).each(function (indice, data) {
								dtr.row.add([
									estaVacio(data.ream),
									estaVacio(data.canal),
									estaVacio(data.area),
									/*estaVacio(data.monto_usd),
									estaVacio(data.monto_pesos),*/
									formatoDinero(estaVacio(data.monto_usd),'currency',2),
									formatoDinero(estaVacio(data.monto_pesos),'currency',2),
									estaVacio(data.moneda),
									estaVacio(data.tc),
									estaVacio(data.ejecutivo),
									estaVacio(data.poliza),
									estaVacio(data.certificado),
									estaVacio(data.ramo_ventas),
									estaVacio(data.cliente),
									estaVacio(data.agente),
									estaVacio(data.fecexp),
									estaVacio(data.fecini),
									estaVacio(data.fecter),
									estaVacio(data.tipcer),
									estaVacio(data.tipmov),
									estaVacio(data.tipomovi),
									estaVacio(data.tipoendoso),
									estaVacio(data.tipcoa),
									estaVacio(data.tiponegocio),
									estaVacio(data.coaseg)
								]).draw(false);
							});
						} else {
							showMessageError(".navbar", "La consulta no encontro ningún registro.", 0);
						}
					}else {
						showMessageError(".navbar", datos.msg, 0);
					}
				});
			}else{
				showMessageError(".navbar","La consulta no encontro ningún registro.",0);
			}
			hideLoader();
		});
		$('#tableReporteDiv').removeClass('d-none');
	}
}

function validaCampos(clase){
	let requeridos = $("." + clase);
	let errores = 0;
	let salida = true;
	$.when($(requeridos).each(function (key, data){
		if($(data).is("select")){
			if($(data).val() < 0){
				showMessageError(".navbar","Error debe selecionar una opci&oacute;n para " + $("label[for='" + $(data).attr('id') + "']").text(),0);
				hideLoader();
				errores++;
			}
			if($(data).val() == null){
				showMessageError(".navbar","Error debe selecionar una opci&oacute;n para " + $("label[for='" + $(data).attr('id') + "']").text(),0);
				hideLoader();
				errores++;
			}
		}else if($(data).is("input")) {
			console.log(data);
			console.log($("label[for='" + $(data).attr('id') + "']").text());
			if($(data).val() == ""||$(data).val() == undefined){
				showMessageError(".navbar","Error debe ingresar informacion para " + $("label[for='" + $(data).attr('id') + "']").text(),0);
				hideLoader();
				errores++;
			}
		}
	})).done(function (){
		if(errores > 0){
			salida = false;
		}
	});
	return salida;
}

function restartDatatable(dataTable){
	$('#' + dataTable).DataTable({
		responsive: true,
		destroy: true,
		dom: 'fBrltip',
		ordering:false,
		ordered:false,
		buttons: [
			{
				extend:    'excelHtml5',
				text:      '<a class="btn-floating btn-sm teal"><i class="far fa-file-excel"></i></a>',
				titleAttr: 'Exportar XLS',
				className:"btn-unstyled",
			},{
				extend:'pdfHtml5',
				text:      '<a class="btn-floating btn-sm btn-info"><i class="far fa-file-pdf"></i></a>',
				titleAttr: 'Exportar PDF',
				className:"btn-unstyled",
			},{
				text:      '<a id="botonMovimientos" onclick="generaExcelMovimeintosProduccion();" class="btn-floating btn-blue btn-sm"><i class="fas fa-file-code"></i></a>',
				titleAttr: 'Excel Movimientos',
				className:"btn-unstyled"
			}
		],
		columnDefs: [
			{targets: '_all', className: "py-2" }
		],
		lengthChange: true,
		language: {
			"url": spanishJson,

		},
		lengthMenu: [[5,10,15], [5,10,15]],
		pageLength: 10
	});
}

function restartDatatableGastos(dataTable){
	$('#' + dataTable).DataTable({
		responsive: true,
		destroy: true,
		dom: 'fBrltip',
		ordering:false,
		ordered:false,
		buttons: [
			{
				extend:    'excelHtml5',
				text:      '<a class="btn-floating btn-sm teal"><i class="far fa-file-excel"></i></a>',
				titleAttr: 'Exportar XLS',
				className:"btn-unstyled",
			},{
				extend:'pdfHtml5',
				text:      '<a class="btn-floating btn-sm btn-info"><i class="far fa-file-pdf"></i></a>',
				titleAttr: 'Exportar PDF',
				className:"btn-unstyled",
			},{
				text:      '<a id="botonMovimientos" onclick="generaExcelMovimeintosGastos();" class="btn-floating btn-blue btn-sm"><i class="fas fa-file-code"></i></a>',
				titleAttr: 'Excel Movimientos',
				className:"btn-unstyled"
			}
		],
		columnDefs: [
			{targets: '_all', className: "py-2" }
		],
		lengthChange: true,
		language: {
			"url": spanishJson,

		},
		lengthMenu: [[5,10,15], [5,10,15]],
		pageLength: 10
	});
	return true;
}

function generaExcelMovimeintosProduccion(){
	let dtr = $('#tablaReporteMovimientos').DataTable();
	dtr.button('.buttons-excel').trigger();
}

$("#monedaGasto").on("change",function (e){
	e.preventDefault();
	if($("#monedaGasto option:selected").html() === "MXN"){
		$("#cambioOculto").addClass("d-none");
	}else{
		if(validaCampos("requeridoDivisa")){
			$("#cambioOculto").removeClass("d-none");
			$("#tipoCambio").siblings("label").addClass("active");
			$("#montoGastoMXN").siblings("label").addClass("active");
			var objetoSend = {
				anio: $("#selAnioGasto option:selected").html(),
				mes: $("#selMesGasto").val(),
				divisa: $("#monedaGasto option:selected").html()
			}
			$.post(consultarTasaCambioFront,objetoSend).done(function (data){
				var dato = JSON.parse(data);
				$("#tipoCambio").val(dato.tc);
				if($("#montoGasto").val() !== "") {
					var montoMXN = parseFloat($("#montoGasto").val()) * dato.tc;
					$("#montoGastoMXN").val(montoMXN);
				}else{
					$("#montoGastoMXN").val(0.00);
				}
			});
		}
	}
	e.stopImmediatePropagation();
});

$("#montoGasto").on("keyup",function (e){
	e.preventDefault();
	if($("#monedaGasto option:selected").html() === "MXN"){
	}else{
		var montoMXN = parseFloat($("#montoGasto").val()) * parseFloat($("#tipoCambio").val());
		$("#montoGastoMXN").val(montoMXN.toFixed(2));
	}
	e.stopImmediatePropagation();
});

$("#ejecutivoGasto").on("change",function (e){
	e.preventDefault();
	$.post(consultaAgentesFront,{"idEjecutivo":$("#ejecutivoGasto option:selected").val()}).done(function (data){
		var datos = JSON.parse(data);
		var area = datos.pop();
		$("#areaGasto").val(area.area).siblings("label").addClass("active");
		$("#agenteGasto option:not(:first)").remove();
		$("#agenteGasto").material_select('destroy');
		$.each(datos,function (key,value){
			var opt = document.createElement('option');
			opt.value = value._agenteId;
			opt.innerHTML = value._nombre + " " + value._apellidoP + " " + value._apellidoM + "-" + value._clave;
			$('#agenteGasto').append(opt);
		});
		$("#agenteGasto").material_select();
	});
	e.stopImmediatePropagation();
});

$("#registraGasto").on("click",function (e){
	e.preventDefault();
	if(validaCampos("requeridoGastos")){
		console.log("Registra_Gasto")
		var objectSend = {
			"anio": $("#selAnioGasto option:selected").html(),
			"mes": $("#selMesGasto option:selected").val(),
			"idEjecutivo": $("#ejecutivoGasto option:selected").val(),
			"area": $("#areaGasto").val().trim(),
			"idAgente": $("#agenteGasto option:selected").val(),
			"idDivisa": $("#monedaGasto option:selected").val(),
			/*"monto": $("#monedaGasto option:selected").html()=='MXN'? $("#montoGasto").val() : $("#montoGastoMXN").val(),*/
			"monto": $("#montoGasto").val()
		};
		$.post(registraGastoURL,objectSend).done(function (data){
			console.log(data);
			var response = JSON.parse(data);
			if (response.code == 0) {
				showMessageSuccess('.navbar', response.msg, 0)
				$('#selAnioGasto').prop('selectedIndex', 0);
				$("#selAnioGasto").material_select('destroy');
				$("#selAnioGasto").material_select();
				$('#selMesGasto').prop('selectedIndex', 0);
				$("#selMesGasto").material_select('destroy');
				$("#selMesGasto").material_select();
				$('#agenteGasto').prop('selectedIndex', 0);
				$("#agenteGasto").material_select('destroy');
				$("#agenteGasto").material_select();
				$('#monedaGasto').prop('selectedIndex', 0);
				$("#monedaGasto").material_select('destroy');
				$("#monedaGasto").material_select();
				$("#montoGasto").val('');
			}else{
				showMessageError('.navbar', response.msg, 0);
			}
		});
	}
	hideLoader();
	e.stopImmediatePropagation();
});

$("#docCargaInformacion").on("click",function (){
	showLoader();
	var dataDoc = new FormData();
	dataDoc.append('docCargaInformacion', $('#archivo')[0].files[0]);
	dataDoc.append('currentURL',curUrl);
	$.ajax({
		url: cargaDocumentoURL,
		data: dataDoc,
		processData: false,
		contentType: false,
		type: 'POST',
		success: function (data) {
			if (data != "") {
				console.log(data);
				var response = JSON.parse(data);
				if (response.code == 0) {
					$('#archivo').val("");
					$(".file-path").val("");
					showMessageSuccess('.navbar', response.msg, 0)
				} else {/*Cualquier Error*/
					$('#archivo').val("");
					$(".file-path").val("");
					showMessageError('.navbar', response.msg, 0);
				}
			}
		},
		error: function (data){

		}
	});
	hideLoader();
});

function showTableReporteGastos(){
	showLoader();
	if(validaCampos("requeridoResporteGastos")) {
		let objetoSend = {
			area: $("#selArea").val(),
			anio: $("#selAnio option:selected").html(),
			mes: $("#selMes").val(),
			tipoReporte: $("input[name='group2']:checked").val()
		}
		$.post(consultarReporteURL, objetoSend).done(function (data) {
			let datos = JSON.parse(data);
			$("#tituloTablaGastos").text($("input[name='group2']:checked").siblings('label').text().replace("Por ",""));
			restartDatatableGastos("tablaReporteGastos");
			let dt = $('#tablaReporteGastos').DataTable();
			dt.clear();
			$(datos).each(function (indice, data) {
				dt.row.add([
					estaVacio(data.canal),
					formatoDinero(estaVacio(data.acumulado_real),'decimal',2),
					formatoDinero(estaVacio(data.acumulado_budget),'decimal',2),
					formatoDinero(estaVacio(data.porcentaje),'percent',2),
				]).draw(false);
			});
			hideLoader();
		});
		restartDatatableGastos("tablaMovimientosGasto");
		let dtr = $('#tablaMovimientosGasto').DataTable();
		$.ajax({
			url:consultarReporteMovimientosURL,
			type: 'post',
			data: objetoSend,
			async: false,
			success: function (data) {
				let datos = JSON.parse(data);
				console.log(datos);
				if(datos.length > 0){
					dtr.clear();
					$(datos).each(function (indice, data) {
						dtr.row.add([
							estaVacio(data.area),
							estaVacio(data.canalOficina),
							estaVacio(data.agente),
							estaVacio(data.ejecutivo),
							estaVacio(data.mes),
							estaVacio(data.anio),
							estaVacio(data.moneda),
							formatoDinero(estaVacio(data.monto),'currency',2),
							estaVacio(data.tasaCambio),
							estaVacio(data.fecha),
							estaVacio(data.tipo)
						]).draw(false);
					});
				}
			},
			error: function (request, status, error) {
				hideLoader();
			}
		});
		$('#tableReporte').removeClass('d-none');

	}
}

function generaExcelMovimeintosGastos (){
	let dtr = $('#tablaMovimientosGasto').DataTable();
	dtr.button('.buttons-excel').trigger();
}

function estaVacio(dato){
	return dato==null?"":dato;
}

function formatoDinero(number,style,digits){
	let options = {
		style: style,
		maximumFractionDigits:digits,
		useGrouping: true,
	};
	if(style == "currency"){
		options.currency = 'USD';
	}
	return (new Intl.NumberFormat('en-Latn-US', options).format(number)).replace("$","");
}