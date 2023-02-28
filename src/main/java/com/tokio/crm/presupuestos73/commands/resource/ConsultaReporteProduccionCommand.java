package com.tokio.crm.presupuestos73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.crm.crmservices73.Bean.ReporteProduccion;
import com.tokio.crm.crmservices73.Interface.CrmGenerico;
import com.tokio.crm.presupuestos73.constants.PresupuestosCrmPortlet73PortletKeys;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = {"javax.portlet.name=" + PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73,
        "mvc.command.name=/crm/presupuestos/produccion/consultarReporte"}, service = MVCResourceCommand.class)
public class ConsultaReporteProduccionCommand extends BaseMVCResourceCommand {

    @Reference
    CrmGenerico _CrmGenericoService;

    User usuario;

    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws Exception {
        String area = ParamUtil.getString(resourceRequest, "area");
        String anio = ParamUtil.getString(resourceRequest, "anio");
        String mes = ParamUtil.getString(resourceRequest, "mes");
        String tipoReporte = ParamUtil.getString(resourceRequest, "tipoReporte");
        ReporteProduccion respuesta;
        usuario=(User) resourceRequest.getAttribute(WebKeys.USER);
        Gson gson = new Gson();
        respuesta = _CrmGenericoService.getReporteProduccion(tipoReporte,area,anio,mes,usuario.getScreenName(),PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73);
        String responseString = gson.toJson(respuesta);
        PrintWriter writer = resourceResponse.getWriter();
        writer.write(responseString);
    }
}