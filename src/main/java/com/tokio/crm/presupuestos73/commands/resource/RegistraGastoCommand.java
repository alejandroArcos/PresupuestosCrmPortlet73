package com.tokio.crm.presupuestos73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.crm.crmservices73.Bean.CRMResponse;
import com.tokio.crm.crmservices73.Interface.CrmGenerico;
import com.tokio.crm.presupuestos73.beans.TipoPresupuesto;
import com.tokio.crm.presupuestos73.constants.PresupuestosCrmPortlet73PortletKeys;
import com.tokio.crm.servicebuilder73.model.Agente;
import com.tokio.crm.servicebuilder73.model.Catalogo_Detalle;
import com.tokio.crm.servicebuilder73.model.Presupuesto;
import com.tokio.crm.servicebuilder73.model.User_Crm;
import com.tokio.crm.servicebuilder73.service.AgenteLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.Catalogo_DetalleLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.PresupuestoLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.User_CrmLocalServiceUtil;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = {"javax.portlet.name=" + PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73,
        "mvc.command.name=/crm/presupuestos/gastos/registraGasto"}, service = MVCResourceCommand.class)
public class RegistraGastoCommand extends BaseMVCResourceCommand {
    @Reference
    CrmGenerico _CrmGenericoService;

    User usuario;

    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws Exception {
        CRMResponse respuesta = new CRMResponse();
        Gson gson = new Gson();
        String anio = ParamUtil.getString(resourceRequest,"anio");
        String mes = ParamUtil.getString(resourceRequest,"mes");
        long idEjecutivo = ParamUtil.getLong(resourceRequest,"idEjecutivo");
        long idAgente = ParamUtil.getLong(resourceRequest,"idAgente");
        long idDivisa = ParamUtil.getLong(resourceRequest,"idDivisa");
        double monto = ParamUtil.getDouble(resourceRequest,"monto");
        usuario = (User) resourceRequest.getAttribute(WebKeys.USER);
        Date fecha = new Date();
        long time = fecha.getTime();
        Timestamp ts = new Timestamp(time);
        Agente agente =  AgenteLocalServiceUtil.getAgente(idAgente);
        User_Crm user_crm = User_CrmLocalServiceUtil.getUser_Crm((int) idEjecutivo);
        Catalogo_Detalle catalogo_detalle = Catalogo_DetalleLocalServiceUtil.getCatalogo_Detalle(user_crm.getOficina());
        try {
            Presupuesto presupuesto = PresupuestoLocalServiceUtil.createPresupuesto(0);
            presupuesto.setId_agente(agente.getAgenteId());
            presupuesto.setId_ejecutivo(idEjecutivo);
            presupuesto.setOficina_canal(catalogo_detalle.getCodigo());
            presupuesto.setId_area(agente.getTipoNegocio());
            presupuesto.setId_moneda(idDivisa);
            presupuesto.setMes(mes);
            presupuesto.setAnio(anio);
            presupuesto.setMonto(monto);
            presupuesto.setTipo_presupuesto(TipoPresupuesto.GASTO_REAL.ordinal());
            presupuesto.setFecha_creacion(ts);
            presupuesto.setFecha_modificacion(ts);
            PresupuestoLocalServiceUtil.addPresupuesto(presupuesto);
            respuesta.setCode(0);
            respuesta.setMsg("Alta de gastos realizada con &eacute;xito");
        }catch (Exception e){
            e.printStackTrace();
            respuesta.setCode(1);
            respuesta.setMsg(e.getMessage());
        }
        String responseString = gson.toJson(respuesta);
        PrintWriter writer = resourceResponse.getWriter();
        writer.write(responseString);
    }
}
