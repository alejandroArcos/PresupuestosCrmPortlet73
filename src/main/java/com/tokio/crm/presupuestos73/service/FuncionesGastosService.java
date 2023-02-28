package com.tokio.crm.presupuestos73.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tokio.crm.crmservices73.Bean.UsuarioCrm;
import com.tokio.crm.servicebuilder73.model.Agente;
import com.tokio.crm.servicebuilder73.model.Presupuesto;
import com.tokio.crm.servicebuilder73.model.User_Crm;

import java.util.List;

public interface FuncionesGastosService {

    JsonArray obtieneLosDatosPorAreaDeAgentes(List<Agente> agentes, int area, String mes, String anio, String usuario, String canal);

    JsonArray obtieneTotal(JsonArray jsonArray);

    JsonArray obtieneLosDatosPorAreaDeEjecutivos(int area, String mes, String anio, String usuario, String canal);

    JsonArray obtieneLosDatosPorAreaDeDepartamentos(int area, String mes, String anio, String usuario, String canal);

    void obtienePorcentajesPorPresupuesto(List<Presupuesto>presupuestos);

    JsonObject objetoFinal(String canal, boolean total);

    List<UsuarioCrm> obtenerUsuarios(int area);

    List<UsuarioCrm> juntaUsuarios(List<User_Crm> analistas, List<User_Crm> ejecutivos);

}
