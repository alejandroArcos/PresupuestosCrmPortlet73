package com.tokio.crm.presupuestos73.service.Impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.tokio.crm.crmservices73.Bean.TasaCambio;
import com.tokio.crm.crmservices73.Bean.UsuarioCrm;
import com.tokio.crm.crmservices73.Constants.CrmDatabaseKey;
import com.tokio.crm.crmservices73.Interface.CrmGenerico;
import com.tokio.crm.presupuestos73.beans.TipoPresupuesto;
import com.tokio.crm.presupuestos73.constants.PresupuestosCrmPortlet73PortletKeys;
import com.tokio.crm.presupuestos73.service.FuncionesGastosService;
import com.tokio.crm.servicebuilder73.model.Agente;
import com.tokio.crm.servicebuilder73.model.Catalogo_Detalle;
import com.tokio.crm.servicebuilder73.model.Presupuesto;
import com.tokio.crm.servicebuilder73.model.User_Crm;
import com.tokio.crm.servicebuilder73.service.Catalogo_DetalleLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.PresupuestoLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.User_CrmLocalServiceUtil;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = FuncionesGastosService.class)
public class FuncionesGastosServiceImpl implements FuncionesGastosService {
    private static final Log _log = LogFactoryUtil.getLog(FuncionesGastosServiceImpl.class);

    JsonArray jsonArray;
    JsonObject jsonObject;

    List<Presupuesto> presupuestos;
    List<UsuarioCrm> usuarioCrms;
    double acumulado_r;
    double acumulado_b;
    double porcentaje;
    double total_r;
    double total_b;
    double porcentaje_t;
    DecimalFormat df;
    DecimalFormat dfp;
    Map<Integer,String> mapaEjecutivos;
    List<TasaCambio> tasaCambios;

    @Reference
    CrmGenerico _CrmGenericoService;

    public void inicializar(){
        mapaEjecutivos = new HashMap<>();
        jsonArray = new JsonArray();
        jsonObject = new JsonObject();
        presupuestos = new ArrayList<>();
        usuarioCrms = new ArrayList<>();
        acumulado_r = 0.0d;
        acumulado_b = 0.0d;
        porcentaje = 0.0d;
        total_r = 0.0d;
        total_b = 0.0d;
        porcentaje_t = 0.0d;
        df = new DecimalFormat("##########.##");
        dfp = new DecimalFormat("####.#####");
        df.setRoundingMode(RoundingMode.FLOOR);
        dfp.setRoundingMode(RoundingMode.UP);
        tasaCambios = new ArrayList<>();
    }

    public void inicializarTasaCambio(String anio, String mes, String usuario) throws Exception {
        String numMes;
        for(int i = 1; i <= Integer.parseInt(mes); i++){
            numMes = i + "";
            if(numMes.length() == 1){
                numMes = "0" + i;
            }
            tasaCambios.add(getTasaCambio(anio,numMes,usuario));
        }
    }

    @Override
    public JsonArray obtieneLosDatosPorAreaDeAgentes(List<Agente> agentes, int area, String mes, String anio, String usuario, String canal){
        inicializar();
        try {
            String mesNum;
            inicializarTasaCambio(anio,mes,usuario);
            int i;
            for (Agente agente: agentes){
                acumulado_r = 0.00;
                acumulado_b = 0.00;
                porcentaje = 0.00;
                jsonObject = new JsonObject();
                for(i = 1; i <= Integer.parseInt(mes); i++){
                    mesNum = i + "";
                    if(mesNum.length() == 1){
                        mesNum = "0" + i;
                    }
                    presupuestos.addAll(PresupuestoLocalServiceUtil.getPresupuestosByGastosAgenteAndMesAndAnio(agente.getAgenteId(),mesNum,anio));
                }
                if(!presupuestos.isEmpty()){
                    obtienePorcentajesPorPresupuesto(presupuestos);
                    jsonArray.add(objetoFinal(agente.getNombre() + " " + agente.getApellidoP() + " " + agente.getApellidoM() + "-" + (!"".equals(agente.getClave())? agente.getClave() : agente.getPreclave()),false));
                    presupuestos.clear();
                }
            }
            presupuestos.clear();
            for(i = 1; i <= Integer.parseInt(mes); i++){
                mesNum = i + "";
                if(mesNum.length() == 1){
                    mesNum = "0" + i;
                }
                presupuestos.addAll(PresupuestoLocalServiceUtil.getPresupuestosByGastosAgenteAndMesAndAnioAndArea("MD".equalsIgnoreCase(canal)?CrmDatabaseKey.NEGOCIO_M:CrmDatabaseKey.NEGOCIO_J,mesNum,anio));
            }
            _log.debug(presupuestos);
            _log.debug(-1L==-1?"true":"false");
            if(!presupuestos.isEmpty()){
                obtienePorcentajesPorPresupuesto(presupuestos);
                jsonArray.add(objetoFinal("Todos " + canal,false));
            }
            jsonArray.add(objetoFinal(canal,true));
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonArray;
    }

    public JsonArray obtieneTotal(JsonArray jsonArray){
        inicializar();
        JsonObject row;
        if(jsonArray.size() != 0){
            for(int i = 0; i < jsonArray.size(); i++){
                row = jsonArray.get(i).getAsJsonObject();
                if(row.get("canal").getAsString().equalsIgnoreCase("MD")||row.get("canal").getAsString().equalsIgnoreCase("Japanese")){
                    total_r = total_r + row.get("acumulado_real").getAsDouble();
                    total_b = total_b + row.get("acumulado_budget").getAsDouble();
                }
            }
        }

        if(Double.parseDouble(df.format(total_b))!= 0.00d){
            porcentaje = (total_r/total_b);
        }
        jsonObject.addProperty("canal","TOTAL");
        jsonObject.addProperty("acumulado_real",df.format(total_r));
        jsonObject.addProperty("acumulado_budget",df.format(total_b));
        jsonObject.addProperty("porcentaje",porcentaje);
        jsonArray.add(jsonObject);
        return jsonArray;
    }

    @Override
    public JsonArray obtieneLosDatosPorAreaDeEjecutivos(int area, String mes, String anio, String usuario, String canal) {
        inicializar();
        try {
            usuarioCrms = obtenerUsuarios(area);
            String mesNum;
            inicializarTasaCambio(anio,mes,usuario);
            int i;
            for (UsuarioCrm ejecutivo: usuarioCrms){
                acumulado_r = 0.00;
                acumulado_b = 0.00;
                porcentaje = 0.00;
                jsonObject = new JsonObject();
                for(i = 1; i <= Integer.parseInt(mes); i++){
                    mesNum = i + "";
                    if(mesNum.length() == 1){
                        mesNum = "0" + i;
                    }
                    presupuestos.addAll(PresupuestoLocalServiceUtil.getPresupuestosByGastosEjecutivoAndMesAndAnio(ejecutivo.getId(),mesNum,anio));
                }
                if(!presupuestos.isEmpty()){
                    System.out.println(presupuestos);
                    obtienePorcentajesPorPresupuesto(presupuestos);
                    jsonArray.add(objetoFinal(ejecutivo.getNombre(),false));
                    presupuestos.clear();
                }
            }
            for(i = 1; i <= Integer.parseInt(mes); i++){
                mesNum = i + "";
                if(mesNum.length() == 1){
                    mesNum = "0" + i;
                }
                presupuestos.addAll(PresupuestoLocalServiceUtil.getPresupuestosByGastosEjecutivoAndMesAndAnioAndArea("MD".equalsIgnoreCase(canal)?CrmDatabaseKey.NEGOCIO_M:CrmDatabaseKey.NEGOCIO_J,mesNum,anio));
            }
            if(!presupuestos.isEmpty()){
                obtienePorcentajesPorPresupuesto(presupuestos);
                jsonArray.add(objetoFinal("Todos " + canal,false));
            }
            jsonArray.add(objetoFinal(canal,true));
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonArray;
    }


    @Override
    public JsonArray obtieneLosDatosPorAreaDeDepartamentos(int area, String mes, String anio, String usuario, String canal) {
        inicializar();
        String departamento;
        List<Integer> departamentos = new ArrayList<>();
        List<UsuarioCrm> aux;
        try {
            usuarioCrms = obtenerUsuarios(area);
            List<Catalogo_Detalle> oficinas = Catalogo_DetalleLocalServiceUtil.findByCodigo("CATOFICINA");
            usuarioCrms.stream().parallel().forEach(ejecutivo ->{
                if(!departamentos.contains(ejecutivo.getOficina())){
                    departamentos.add(ejecutivo.getOficina());
                }
            });
            String mesNum;
            inicializarTasaCambio(anio,mes,usuario);
            int i;
            for(Integer oficina: departamentos) {
                acumulado_r = 0.00;
                acumulado_b = 0.00;
                porcentaje = 0.00;
                jsonObject = new JsonObject();
                aux = usuarioCrms.stream().filter(f -> f.getOficina() == oficina).collect(Collectors.toList());
                //Cambia por el departamento
                departamento = oficinas.stream().filter(f -> f.getCatalogoDetalleId() == oficina).collect(Collectors.toList()).get(0).getDescripcion();
                if (aux.size() > 0) {
                    for (UsuarioCrm ejecutivo : aux) {
                        for(i = 1; i <= Integer.parseInt(mes); i++){
                            mesNum = i + "";
                            if(mesNum.length() == 1){
                                mesNum = "0" + i;
                            }
                            presupuestos.addAll(PresupuestoLocalServiceUtil.getPresupuestosByGastosEjecutivoAndMesAndAnio(Long.parseLong(ejecutivo.getId() + ""), mesNum, anio));
                        }
                    }
                    if(!presupuestos.isEmpty()) {
                        System.out.println(presupuestos);
                        obtienePorcentajesPorPresupuesto(presupuestos);
                        jsonArray.add(objetoFinal(departamento, false));
                        presupuestos.clear();
                    }
                }
            }
            for(i = 1; i <= Integer.parseInt(mes); i++){
                mesNum = i + "";
                if(mesNum.length() == 1){
                    mesNum = "0" + i;
                }
                presupuestos.addAll(PresupuestoLocalServiceUtil.getPresupuestosByGastosEjecutivoAndMesAndAnioAndArea("MD".equalsIgnoreCase(canal)?CrmDatabaseKey.NEGOCIO_M:CrmDatabaseKey.NEGOCIO_J,mesNum,anio));
            }
            if(!presupuestos.isEmpty()){
                obtienePorcentajesPorPresupuesto(presupuestos);
                jsonArray.add(objetoFinal("Todos " + canal,false));
            }
            jsonArray.add(objetoFinal(canal,true));
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void obtienePorcentajesPorPresupuesto(List<Presupuesto> presupuestos){
        for (Presupuesto presupuesto : presupuestos) {
            if (presupuesto.getId_moneda() == 1) {
                if (presupuesto.getTipo_presupuesto() == TipoPresupuesto.GASTO_REAL.ordinal()) {
                    acumulado_r = acumulado_r + presupuesto.getMonto();
                    total_r = total_r + presupuesto.getMonto();
                }else{
                    acumulado_b = acumulado_b + presupuesto.getMonto();
                    total_b = total_b + presupuesto.getMonto();
                }
            }else{
                if (presupuesto.getTipo_presupuesto() == TipoPresupuesto.GASTO_REAL.ordinal()) {
                    acumulado_r = acumulado_r + (tasaCambios.get(Integer.parseInt(presupuesto.getMes()) - 1).getTc() * presupuesto.getMonto());
                    total_r = total_r + (tasaCambios.get(Integer.parseInt(presupuesto.getMes()) - 1).getTc() * presupuesto.getMonto());
                }else {
                    acumulado_b = acumulado_b + (tasaCambios.get(Integer.parseInt(presupuesto.getMes()) - 1).getTc() * presupuesto.getMonto());
                    total_b = total_b + (tasaCambios.get(Integer.parseInt(presupuesto.getMes()) - 1).getTc() * presupuesto.getMonto());
                }
            }
        }
    }

    @Override
    public JsonObject objetoFinal(String canal, boolean total){
        jsonObject = new JsonObject();
        jsonObject.addProperty("canal", canal);
        if (Double.parseDouble(df.format(total?total_b:acumulado_b)) != 0.00d) {
            if(total){
                porcentaje_t = total_r / total_b;
            }else{
                porcentaje = acumulado_r / acumulado_b;
            }
        }
        jsonObject.addProperty("acumulado_real", df.format(total?total_r:acumulado_r));
        jsonObject.addProperty("acumulado_budget", df.format(total?total_b:acumulado_b));
        jsonObject.addProperty("porcentaje", dfp.format(total?porcentaje_t:porcentaje));
        return jsonObject;
    }

    @Override
    public List<UsuarioCrm> obtenerUsuarios(int area){
        List<User_Crm> ejecutivos = User_CrmLocalServiceUtil.getUsers_CrmByAreaPerfil(area,CrmDatabaseKey.ID_PERFIL_EJECUTIVO_VENTAS);
        List<User_Crm> analistas = User_CrmLocalServiceUtil.getUsers_CrmByAreaPerfil(area,CrmDatabaseKey.ID_PERFIL_ANALISTA_VENTAS);
        return juntaUsuarios(ejecutivos,analistas);
    }

    @Override
    public List<UsuarioCrm> juntaUsuarios(List<User_Crm> analistas,List<User_Crm> ejecutivos){
        return Stream.concat(ejecutivos.stream(), analistas.stream())
                .map(usr -> {
                    try {
                        return new UsuarioCrm(UserLocalServiceUtil.getUserById(usr.getUserId()).getFullName().toUpperCase(), usr.getUserId(), usr.getOficina());
                    } catch (PortalException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).collect(Collectors.toList());
    }

    public TasaCambio getTasaCambio(String anio, String mes, String usuario) throws Exception{
        return  _CrmGenericoService.getTasaCambio(anio,mes,usuario, PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73);
    }
}
