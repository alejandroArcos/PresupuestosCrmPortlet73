package com.tokio.crm.presupuestos73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.crm.crmservices73.Constants.CrmDatabaseKey;
import com.tokio.crm.presupuestos73.constants.PresupuestosCrmPortlet73PortletKeys;
import com.tokio.crm.presupuestos73.service.FuncionesGastosService;
import com.tokio.crm.servicebuilder73.model.Agente;
import com.tokio.crm.servicebuilder73.service.AgenteLocalServiceUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = {"javax.portlet.name=" + PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73,
        "mvc.command.name=/crm/presupuestos/gastos/consultarReporte"}, service = MVCResourceCommand.class)
public class ConsultaReporteGastosCommand extends BaseMVCResourceCommand {

    @Reference
    FuncionesGastosService funcionesGastosService;

    User usuario;

    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws Exception {
        int area = ParamUtil.getInteger(resourceRequest, "area");
        String anio = ParamUtil.getString(resourceRequest, "anio");
        String mes = ParamUtil.getString(resourceRequest, "mes");
        String tipoReporte = ParamUtil.getString(resourceRequest, "tipoReporte");
        List<Agente> agentes = new ArrayList<>();
        JsonArray jsonArray = new JsonArray();
        Gson gson = new Gson();
        usuario = (User) resourceRequest.getAttribute(WebKeys.USER);
        try {
            switch (tipoReporte){
                //Agente
                case "0":
                    if(area==1){
                        JsonArray jsonArray1;
                        agentes.addAll(AgenteLocalServiceUtil.findByTipoNegocio(CrmDatabaseKey.NEGOCIO_M));
                        jsonArray1 = funcionesGastosService.obtieneLosDatosPorAreaDeAgentes(agentes,CrmDatabaseKey.NEGOCIO_M,mes,anio,usuario.getScreenName(),"MD");
                        JsonArray jsonArray2;
                        agentes.clear();
                        agentes.addAll(AgenteLocalServiceUtil.findByTipoNegocio(CrmDatabaseKey.NEGOCIO_J));
                        jsonArray2 = funcionesGastosService.obtieneLosDatosPorAreaDeAgentes(agentes,CrmDatabaseKey.NEGOCIO_J,mes,anio,usuario.getScreenName(),"Japanese");
                        jsonArray.addAll(jsonArray1);
                        jsonArray.addAll(jsonArray2);
                    }else{
                        agentes.addAll(AgenteLocalServiceUtil.findByTipoNegocio(area== CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.NEGOCIO_M:CrmDatabaseKey.NEGOCIO_J));
                        jsonArray = funcionesGastosService.obtieneLosDatosPorAreaDeAgentes(agentes,area==CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.NEGOCIO_M:CrmDatabaseKey.NEGOCIO_J,mes,anio,usuario.getScreenName(),area==CrmDatabaseKey.NEGOCIO_M?"MD":"Japanese");
                    }
                    jsonArray = funcionesGastosService.obtieneTotal(jsonArray);
                    break;
                //Ejecutivo
                case "1":
                    if(area==1){
                        JsonArray jsonArray1;
                        jsonArray1 = funcionesGastosService.obtieneLosDatosPorAreaDeEjecutivos(CrmDatabaseKey.AREA_NEGOCIO_M,mes,anio,usuario.getScreenName(),"MD");
                        JsonArray jsonArray2;
                        jsonArray2 = funcionesGastosService.obtieneLosDatosPorAreaDeEjecutivos(CrmDatabaseKey.AREA_NEGOCIO_J,mes,anio,usuario.getScreenName(),"Japanese");
                        jsonArray.addAll(jsonArray1);
                        jsonArray.addAll(jsonArray2);
                        jsonArray = funcionesGastosService.obtieneTotal(jsonArray);
                    }else{
                        jsonArray = funcionesGastosService.obtieneLosDatosPorAreaDeEjecutivos(area==CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.AREA_NEGOCIO_M:CrmDatabaseKey.AREA_NEGOCIO_J,mes,anio,usuario.getScreenName(),area==CrmDatabaseKey.NEGOCIO_M?"MD":"Japanese");
                    }
                    break;
                case "2": // Departamento
                    if(area==1){
                        JsonArray jsonArray1;
                        jsonArray1 = funcionesGastosService.obtieneLosDatosPorAreaDeDepartamentos(CrmDatabaseKey.AREA_NEGOCIO_M,mes,anio,usuario.getScreenName(),"MD");
                        JsonArray jsonArray2;
                        jsonArray2 = funcionesGastosService.obtieneLosDatosPorAreaDeDepartamentos(CrmDatabaseKey.AREA_NEGOCIO_J,mes,anio,usuario.getScreenName(),"Japanese");
                        jsonArray.addAll(jsonArray1);
                        jsonArray.addAll(jsonArray2);
                        jsonArray = funcionesGastosService.obtieneTotal(jsonArray);
                    }else{
                        jsonArray = funcionesGastosService.obtieneLosDatosPorAreaDeDepartamentos(area==CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.AREA_NEGOCIO_M:CrmDatabaseKey.AREA_NEGOCIO_J,mes,anio,usuario.getScreenName(),area==CrmDatabaseKey.NEGOCIO_M?"MD":"Japanese");
                    }
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        String responseString = gson.toJson(jsonArray);
        PrintWriter writer = resourceResponse.getWriter();
        writer.write(responseString);
    }
}
