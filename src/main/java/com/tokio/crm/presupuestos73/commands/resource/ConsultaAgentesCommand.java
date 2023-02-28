package com.tokio.crm.presupuestos73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.crm.crmservices73.Bean.ListaRegistro;
import com.tokio.crm.crmservices73.Bean.UsuarioCrm;
import com.tokio.crm.crmservices73.Constants.CrmDatabaseKey;
import com.tokio.crm.crmservices73.Constants.CrmServiceKey;
import com.tokio.crm.crmservices73.Interface.CrmGenerico;
import com.tokio.crm.presupuestos73.constants.PresupuestosCrmPortlet73PortletKeys;
import com.tokio.crm.servicebuilder73.model.Agente;
import com.tokio.crm.servicebuilder73.model.User_Crm;
import com.tokio.crm.servicebuilder73.service.AgenteLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.User_CrmLocalServiceUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = {"javax.portlet.name=" + PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73,
        "mvc.command.name=/crm/presupuestos/gastos/consultaAgentes"}, service = MVCResourceCommand.class)
public class ConsultaAgentesCommand extends BaseMVCResourceCommand {
    @Reference
    CrmGenerico _CrmGenericoService;

    User usuario;

    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws Exception {
        List<Agente> respuesta = new ArrayList<>();
        String area = "";
        Gson gson = new Gson();
        int idEjecutivo = ParamUtil.getInteger(resourceRequest,"idEjecutivo");
        usuario=(User) resourceRequest.getAttribute(WebKeys.USER);
        User_Crm user_crm = User_CrmLocalServiceUtil.getUser_Crm(idEjecutivo);
        try {
            //respuesta = AgenteLocalServiceUtil.findByEjecutivoId(idEjecutivo);
            respuesta = AgenteLocalServiceUtil.findByEjecutivoIdAndEstatusAgente(idEjecutivo, CrmDatabaseKey.ESTATUS_AUTORIZADO);
            ListaRegistro listaAreas = _CrmGenericoService.getCatalogo(
                    CrmServiceKey.TMX_CTE_ROW_TODOS,
                    CrmServiceKey.TMX_CTE_TRANSACCION_GET,
                    CrmServiceKey.LIST_CAT_AREA,
                    CrmServiceKey.TMX_CTE_CAT_ACTIVOS,
                    usuario.getScreenName(),
                    PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73);
            UsuarioCrm usuarioCrm = new UsuarioCrm(
                    UserLocalServiceUtil.getUserById(user_crm.getUserId()).getFullName().toUpperCase(),
                    user_crm.getUserId(),
                    user_crm.getArea()
            );
            area = listaAreas.getLista().stream().filter(f -> f.getId() == usuarioCrm.oficina).collect(Collectors.toList()).get(0).getValor();
        }catch (Exception e){
            e.printStackTrace();
        }
        String responseString = gson.toJson(respuesta);
        responseString = removeLastCharOptional(responseString) + ",{\"area\":\"" + area + "\"}]" ;
        PrintWriter writer = resourceResponse.getWriter();
        writer.write(responseString);
    }

    public static String removeLastCharOptional(String s) {
        return Optional.ofNullable(s)
                .filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 1))
                .orElse(s);
    }
}
