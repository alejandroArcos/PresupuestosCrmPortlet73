package com.tokio.crm.presupuestos73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizadorUtilities.excel.Interface.CargaMasivaEmpresarial;
import com.tokio.crm.crmservices73.Bean.CRMResponse;
import com.tokio.crm.crmservices73.Bean.CatalogoMoneda;
import com.tokio.crm.crmservices73.Bean.Moneda;
import com.tokio.crm.crmservices73.Bean.Registro;
import com.tokio.crm.crmservices73.Bean.UsuarioCrm;
import com.tokio.crm.crmservices73.Constants.CrmDatabaseKey;
import com.tokio.crm.crmservices73.Interface.CrmGenerico;
import com.tokio.crm.presupuestos73.beans.TipoPresupuesto;
import com.tokio.crm.presupuestos73.constants.PresupuestosCrmPortlet73PortletKeys;
import com.tokio.crm.servicebuilder73.exception.NoSuchAgenteException;
import com.tokio.crm.servicebuilder73.exception.NoSuchPresupuestoException;
import com.tokio.crm.servicebuilder73.model.Catalogo_Detalle;
import com.tokio.crm.servicebuilder73.model.Presupuesto;
import com.tokio.crm.servicebuilder73.model.User_Crm;
import com.tokio.crm.servicebuilder73.service.AgenteLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.Catalogo_DetalleLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.PresupuestoLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.User_CrmLocalServiceUtil;

import java.io.File;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
        "mvc.command.name=/crm/presupuestos/cargarInformacion/cargaDocumento"}, service = MVCResourceCommand.class)
public class CargaInformacionCommand extends BaseMVCResourceCommand {
    private static final Log _log = LogFactoryUtil.getLog(CargaInformacionCommand.class);

    @Reference
    CargaMasivaEmpresarial _CUServices;

    @Reference
    CrmGenerico _CrmGenericoService;

    User usuario;

    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws Exception {
        UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(resourceRequest);
        File carga = uploadRequest.getFile("docCargaInformacion");
        String url = ParamUtil.getString(resourceRequest, "currentURL");
        JsonArray jsonArray = _CUServices.readFilePresupuestos(carga);
        usuario = (User) resourceRequest.getAttribute(WebKeys.USER);
        CRMResponse respuesta = new CRMResponse();
        Gson gson = new Gson();
        Date fecha = new Date();
        long time = fecha.getTime();
        Timestamp ts = new Timestamp(time);
        List<Presupuesto> presupuestos = new ArrayList<>();
        Presupuesto presupuesto ;
        String claveAgente = "";
        String nombreEjecutivo;
        String oficinaCanal;
        String moneda;
        String area;
        String[] aux;
        List<String> error = new ArrayList<>();
        try{
            if(jsonArray.size()>1){
                String valida = jsonArray.get(0).getAsJsonObject().get("tipoLayout").getAsString();
                if(url.contains("produccion")){
                    if(!valida.equalsIgnoreCase("PRIMA")){
                        throw new Exception("El archivo cargado tiene un layout incorrecto");
                    }
                }else if(url.contains("gastos")){
                    if(!valida.equalsIgnoreCase("GASTOS")){
                        throw new Exception("El archivo cargado tiene un layout incorrecto");
                    }
                }else{
                    throw new Exception("El archivo cargado tiene un layout incorrecto");
                }
                List<User_Crm> ejecutivos = User_CrmLocalServiceUtil.getUsers_CrmByPerfil(CrmDatabaseKey.ID_PERFIL_EJECUTIVO_VENTAS);
                List<User_Crm> analistas = User_CrmLocalServiceUtil.getUsers_CrmByPerfil(CrmDatabaseKey.ID_PERFIL_ANALISTA_VENTAS);
                List<UsuarioCrm> listaUsuarios = Stream.concat(ejecutivos.stream(), analistas.stream())
                        .map(usr -> {
                            try {
                                return new UsuarioCrm(UserLocalServiceUtil.getUserById(usr.getUserId()).getFullName().toUpperCase(), usr.getUserId(), usr.getArea());
                            } catch (PortalException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .collect(Collectors.toList());
                Map<String,Long> mapaEjecutivos = new HashMap<>();
                listaUsuarios.removeAll(Collections.singleton(null));
                listaUsuarios.forEach(f->mapaEjecutivos.put(f.getNombre().trim().replace(" ",""),(long)f.getId()));
                CatalogoMoneda catalogoMoneda = _CrmGenericoService.getCatalogoMoneda(usuario.getScreenName(),PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73);
                Map<String,Integer> mapaMoneda = catalogoMoneda.getLista().stream().collect(Collectors.toMap(Moneda::getValor,Moneda::getId));
                Map<String,String> mapaOficinaCanal = new HashMap<>();
                Map<String,Integer> mapaCanalArea = new HashMap<>();

                TipoPresupuesto tipoPresupuesto = url.contains("gastos")? TipoPresupuesto.GASTO_BUDGET : TipoPresupuesto.PRODUCCION;
                if(tipoPresupuesto == TipoPresupuesto.GASTO_BUDGET){
                    List<Catalogo_Detalle> catoficinas = Catalogo_DetalleLocalServiceUtil.findByCodigo("CATOFICINA");
                    catoficinas.forEach(f -> mapaOficinaCanal.put(f.getDescripcion(),f.getCodigo()));
                }else{
                    List<Registro> catalogo_canal = _CrmGenericoService.getCatalogo(0,"","CATCANAL",0,usuario.getScreenName(),PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73).getLista();
                    catalogo_canal.forEach(f->
                    {
                        mapaOficinaCanal.put(f.getCodigo(),f.getCodigo());
                        mapaCanalArea.put(f.getCodigo(),"J".equals(f.getTipo())?CrmDatabaseKey.NEGOCIO_J:CrmDatabaseKey.NEGOCIO_M);
                    });
                }
                DecimalFormat df = new DecimalFormat("##########.##");
                df.setRoundingMode(RoundingMode.FLOOR);
                JsonArray jsonArrayAux;
                JsonObject jsonObject;
                for(int i = 1; i < jsonArray.size(); i++){
                    presupuesto = PresupuestoLocalServiceUtil.createPresupuesto(0);
                    jsonArrayAux = jsonArray.get(i).getAsJsonArray();
                    for(int j = 0; j < jsonArrayAux.size(); j++){
                        jsonObject = jsonArrayAux.get(j).getAsJsonObject();
                        switch (jsonObject.get("llave").getAsString()){
                            case "Clave de Agente":
                                claveAgente = jsonObject.get("valor").getAsString().trim().replaceAll("\\.0","").toUpperCase();
                                if("Todos".equalsIgnoreCase(claveAgente)){
                                    presupuesto.setId_agente(-1);
                                }else{
                                    presupuesto.setId_agente(AgenteLocalServiceUtil.findByClave(claveAgente).getAgenteId());
                                }
                            break;
                            case "Ejecutivo":
                                nombreEjecutivo = jsonObject.get("valor").getAsString().trim().replaceAll(" ","");
                                error.add(jsonObject.get("llave").getAsString());
                                error.add(nombreEjecutivo);
                                if("Todos".equalsIgnoreCase(nombreEjecutivo)){
                                    presupuesto.setId_ejecutivo(-1);
                                }else {
                                    presupuesto.setId_ejecutivo(mapaEjecutivos.get(nombreEjecutivo.toUpperCase()));
                                }
                                error.clear();
                            break;
                            case "Canal":
                            case "Oficina/área":
                                oficinaCanal = jsonObject.get("valor").getAsString().trim().replaceAll(" ","");
                                error.add(jsonObject.get("llave").getAsString());
                                error.add(oficinaCanal);
                                presupuesto.setOficina_canal(mapaOficinaCanal.get(oficinaCanal));
                                error.clear();
                                break;
                            case "Área MD/J":
                                area = jsonObject.get("valor").getAsString().toUpperCase();
                                if(area.startsWith("M")||area.startsWith("S")){
                                    presupuesto.setId_area(CrmDatabaseKey.NEGOCIO_M);
                                }else if(area.startsWith("J")||area.startsWith("P")){
                                    presupuesto.setId_area(CrmDatabaseKey.NEGOCIO_J);
                                }else{
                                    throw new Exception("El archivo tiene el siguiente dato invalido en la columna área: " + area);
                                }
                                break;
                            case "Mes/Anio":
                                aux = jsonObject.get("valor").getAsString().trim().split("/");
                                presupuesto.setMes(aux[0]);
                                presupuesto.setAnio(aux[1]);
                                break;
                            case "Moneda MXN/USD":
                                moneda = jsonObject.get("valor").getAsString().trim().replaceAll(" ","").toUpperCase();
                                error.add(jsonObject.get("llave").getAsString());
                                error.add(moneda);
                                presupuesto.setId_moneda(mapaMoneda.get(moneda));
                                error.clear();
                                break;
                            case "Monto":
                                presupuesto.setMonto(Double.parseDouble(df.format(Double.parseDouble(jsonObject.get("valor").getAsString()))));
                                break;
                            default:
                                throw new Exception("El archivo tiene una columna erronea");
                        }
                    }
                    presupuesto.setTipo_presupuesto(tipoPresupuesto.ordinal());
                    presupuestos.add(presupuesto);
                }
                Presupuesto presupuestoant;
                for(Presupuesto presupuesto1: presupuestos){
                    error.add("Canal");
                    error.add(presupuesto1.getOficina_canal());
                    if(tipoPresupuesto == TipoPresupuesto.PRODUCCION && ((long)mapaCanalArea.get(presupuesto1.getOficina_canal()) != presupuesto1.getId_area())){
                        throw new NullPointerException ();
                    }
                    error.clear();
                    try{
                        presupuestoant = PresupuestoLocalServiceUtil.findPresupuestosByMesAndAnioAndAreaId(presupuesto1.getId_agente(), presupuesto1.getId_ejecutivo(), presupuesto1.getOficina_canal(), presupuesto1.getId_area(),presupuesto1.getMes(),presupuesto1.getAnio());
                        presupuestoant.setTipo_presupuesto(tipoPresupuesto.ordinal());
                        presupuestoant.setId_moneda(presupuesto1.getId_moneda());
                        presupuestoant.setMonto(presupuesto1.getMonto());
                        presupuestoant.setFecha_modificacion(ts);
                        PresupuestoLocalServiceUtil.updatePresupuesto(presupuestoant);
                    }catch (NoSuchPresupuestoException presupuestoException){
                        presupuesto1.setFecha_creacion(ts);
                        presupuesto1.setFecha_modificacion(ts);
                        PresupuestoLocalServiceUtil.addPresupuesto(presupuesto1);
                    }
                }
                respuesta.setCode(0);
                respuesta.setMsg("La carga del archivo se realizo con &eacute;xito");
            }else{
                respuesta.setCode(1);
                respuesta.setMsg("El archivo no tiene infomaci&oacute;n para cargar");
            }
        }catch (NoSuchAgenteException agenteException){
            respuesta.setCode(1);
            respuesta.setMsg("La clave " + claveAgente + " no es valida para relacionarla a un Agente. ");
        }catch (NullPointerException nullPointerException){
            respuesta.setCode(1);
            respuesta.setMsg("El valor " + error.get(1) + " no es valido para la columna " + error.get(0));
        }catch (Exception e) {
            e.printStackTrace();
            respuesta.setCode(1);
            respuesta.setMsg(e.getMessage());
        }
        String responseString = gson.toJson(respuesta);
        PrintWriter writer = resourceResponse.getWriter();
        writer.write(responseString);

    }

}
