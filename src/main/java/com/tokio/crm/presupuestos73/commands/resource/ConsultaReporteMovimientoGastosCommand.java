package com.tokio.crm.presupuestos73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.crm.crmservices73.Bean.CatalogoMoneda;
import com.tokio.crm.crmservices73.Bean.Registro;
import com.tokio.crm.crmservices73.Bean.UsuarioCrm;
import com.tokio.crm.crmservices73.Constants.CrmDatabaseKey;
import com.tokio.crm.crmservices73.Exeption.CrmServicesException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = {"javax.portlet.name=" + PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73,
        "mvc.command.name=/crm/presupuestos/gastos/consultarReporteMovimientos"}, service = MVCResourceCommand.class)
public class ConsultaReporteMovimientoGastosCommand extends BaseMVCResourceCommand {
    @Reference
    CrmGenerico _CrmGenericoService;

    User usuario;

    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws Exception {
        int area = ParamUtil.getInteger(resourceRequest, "area");
        String anio = ParamUtil.getString(resourceRequest, "anio");
        String mes = ParamUtil.getString(resourceRequest, "mes");
        JsonArray respuesta = new JsonArray();
        usuario=(User) resourceRequest.getAttribute(WebKeys.USER);
        Gson gson = new Gson();
        List<Presupuesto> presupuestos = new ArrayList<>();
        List<Agente> agentes = new ArrayList<>();
        List<User_Crm> ejecutivos;
        List<User_Crm> analistas;
        List<UsuarioCrm> listaUsuarios;
        JsonObject jsonObject;
        CatalogoMoneda catalogoMoneda;
        Map<String,Double> mapaTipoCambio = new HashMap<>();
        try{
            if(area==1){
                    String mesNum;
                    for(int i = 1; i <= Integer.parseInt(mes); i++){
                        mesNum = i + "";
                        if(mesNum.length() == 1){
                            mesNum = "0" + i;
                        }
                        presupuestos.addAll(PresupuestoLocalServiceUtil.getPresupuestosByMesAndAnio(mesNum,anio));
                        try {
                            mapaTipoCambio.put(mesNum,_CrmGenericoService.getTasaCambio(anio,mesNum,usuario.getScreenName(),PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73).getTc());
                        }catch (CrmServicesException e){
                            e.printStackTrace();
                        }
                    }
                    ejecutivos = User_CrmLocalServiceUtil.getUsers_CrmByPerfil(CrmDatabaseKey.ID_PERFIL_EJECUTIVO_VENTAS);
                    analistas = User_CrmLocalServiceUtil.getUsers_CrmByPerfil(CrmDatabaseKey.ID_PERFIL_ANALISTA_VENTAS);
                    agentes.addAll(AgenteLocalServiceUtil.findByTipoNegocio(CrmDatabaseKey.NEGOCIO_M));
                    agentes.addAll(AgenteLocalServiceUtil.findByTipoNegocio(CrmDatabaseKey.NEGOCIO_J));
            }else{
                String mesNum;
                for(int i = 1; i <= Integer.parseInt(mes); i++){
                    mesNum = i + "";
                    if(mesNum.length() == 1){
                        mesNum = "0" + i;
                    }
                    presupuestos.addAll(PresupuestoLocalServiceUtil.getPresupuestosByMesAndAnioAndAreaId(mesNum,anio,area==CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.NEGOCIO_M:CrmDatabaseKey.NEGOCIO_J));
                }
                //presupuestos = PresupuestoLocalServiceUtil.getPresupuestosByMesAndAnioAndAreaId(mes,anio,area==CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.NEGOCIO_M:CrmDatabaseKey.NEGOCIO_J);
                ejecutivos = User_CrmLocalServiceUtil.getUsers_CrmByAreaPerfil(area==CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.AREA_NEGOCIO_M:CrmDatabaseKey.AREA_NEGOCIO_J,CrmDatabaseKey.ID_PERFIL_EJECUTIVO_VENTAS);
                analistas = User_CrmLocalServiceUtil.getUsers_CrmByAreaPerfil(area==CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.AREA_NEGOCIO_M:CrmDatabaseKey.AREA_NEGOCIO_J,CrmDatabaseKey.ID_PERFIL_ANALISTA_VENTAS);
                agentes.addAll(AgenteLocalServiceUtil.findByTipoNegocio(area==CrmDatabaseKey.NEGOCIO_M?CrmDatabaseKey.NEGOCIO_M:CrmDatabaseKey.NEGOCIO_J));
            }
            listaUsuarios = Stream.concat(ejecutivos.stream(), analistas.stream())
                    .map(usr -> {
                        try {
                            return new UsuarioCrm(UserLocalServiceUtil.getUserById(usr.getUserId()).getFullName().toUpperCase(), usr.getUserId(), usr.getOficina());
                        } catch (PortalException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
            catalogoMoneda = _CrmGenericoService.getCatalogoMoneda(usuario.getScreenName(),PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73);

            Map<Long,String> mapaAgente = new HashMap<>();

            agentes.forEach(f -> mapaAgente.put(f.getAgenteId(), f.getNombre() + " " + f.getApellidoP() + " " + f.getApellidoM() + "-" + (!"".equals(f.getClave())? f.getClave() : f.getPreclave())));

            Map<Integer,String> mapaMoneda = new HashMap<>();

            catalogoMoneda.getLista().forEach(f-> mapaMoneda.put(f.getId(), f.getValor()));

            Map<Integer,String> mapaEjecutivos = new HashMap<>();

            listaUsuarios.forEach(f -> mapaEjecutivos.put(f.getId(), f.getNombre()));

            Map<String,String> mapaOficinaArea = new HashMap<>();
            List<Registro> catalogo_canal = _CrmGenericoService.getCatalogo(0,"","CATCANAL",0,usuario.getScreenName(),PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73).getLista();
            catalogo_canal.forEach(f-> mapaOficinaArea.put(f.getCodigo(),f.getCodigo()));
            /*List<Registro> catalogo_oficina = _CrmGenericoService.getCatalogo(0,"","CATOFICIA",0,usuario.getScreenName(),PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73).getLista();
            catalogo_oficina.forEach(f -> mapaOficinaArea.put(f.getCodigo(),f.getDescripcion().split(" ")[1]));*/
            List<Catalogo_Detalle> catoficinas = Catalogo_DetalleLocalServiceUtil.findByCodigo("CATOFICINA");
            catoficinas.forEach(f -> mapaOficinaArea.put(f.getCodigo(),f.getDescripcion()));

            Map<Integer,String> mapaPresupuesto = new HashMap<>();
            mapaPresupuesto.put(0,"PRODUCCION");
            mapaPresupuesto.put(1,"GASTO BUDGET");
            mapaPresupuesto.put(2,"GASTO REAL");

            for (Presupuesto presupuesto : presupuestos) {
                if(presupuesto.getTipo_presupuesto() != TipoPresupuesto.PRODUCCION.ordinal()){
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("area",presupuesto.getId_area() == CrmDatabaseKey.NEGOCIO_M ? "MD": "Japanese");
                    jsonObject.addProperty("canalOficina",mapaOficinaArea.get(presupuesto.getOficina_canal()));
                    if(presupuesto.getId_agente() > 0) {
                        jsonObject.addProperty("agente", mapaAgente.get(presupuesto.getId_agente()));
                    }else{
                        jsonObject.addProperty("agente", "TODOS");
                    }
                    if(presupuesto.getId_ejecutivo() > 0) {
                        jsonObject.addProperty("ejecutivo",mapaEjecutivos.get((int)presupuesto.getId_ejecutivo()));
                    }else{
                        jsonObject.addProperty("ejecutivo", "TODOS");
                    }
                    jsonObject.addProperty("mes", presupuesto.getMes());
                    jsonObject.addProperty("anio",presupuesto.getAnio());
                    jsonObject.addProperty("moneda",mapaMoneda.get((int)presupuesto.getId_moneda()));
                    jsonObject.addProperty("tasaCambio",presupuesto.getId_moneda()!=1?mapaTipoCambio.get(presupuesto.getMes()) + "":"");
                    jsonObject.addProperty("monto",presupuesto.getMonto());
                    jsonObject.addProperty("fecha",presupuesto.getFecha_modificacion().toString());
                    jsonObject.addProperty("tipo",mapaPresupuesto.get(presupuesto.getTipo_presupuesto()));
                    respuesta.add(jsonObject);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        String responseString = gson.toJson(respuesta);
        PrintWriter writer = resourceResponse.getWriter();
        writer.write(responseString);
    }
}
