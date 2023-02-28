package com.tokio.crm.presupuestos73.portlet;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.crm.crmservices73.Bean.CatalogoMoneda;
import com.tokio.crm.crmservices73.Bean.ListaRegistro;
import com.tokio.crm.crmservices73.Bean.UsuarioCrm;
import com.tokio.crm.crmservices73.Constants.CrmDatabaseKey;
import com.tokio.crm.crmservices73.Constants.CrmServiceKey;
import com.tokio.crm.crmservices73.Interface.CrmGenerico;
import com.tokio.crm.presupuestos73.constants.PresupuestosCrmPortlet73PortletKeys;
import com.tokio.crm.presupuestos73.service.FuncionesGastosService;
import com.tokio.crm.presupuestos73.service.Impl.FuncionesGastosServiceImpl;
import com.tokio.crm.servicebuilder73.model.Agente;
import com.tokio.crm.servicebuilder73.model.User_Crm;
import com.tokio.crm.servicebuilder73.service.AgenteLocalServiceUtil;
import com.tokio.crm.servicebuilder73.service.User_CrmLocalServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author urielfloresvaldovinos
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=PresupuestosCrmPortlet73",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.requires-namespaced-parameters=false",
		"com.liferay.portlet.private-request-attributes=false"
	},
	service = Portlet.class
)
public class PresupuestosCrmPortlet73Portlet extends MVCPortlet {
	
	FuncionesGastosService funcionesGastosService = new FuncionesGastosServiceImpl();

	@Reference
	CrmGenerico _CrmGenericoService;

	User usuario;

	@Reference
	private DLAppService _dlAppService;

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws PortletException, IOException {
		
		getCurrentUrl(renderRequest);
		getAnioActual(renderRequest);
		getCatalogoMoneda(renderRequest);
		getEjecutivos(renderRequest);
		urlDocCargaArchivo(renderRequest);
		super.render(renderRequest, renderResponse);
	}
	
	private void getAnioActual ( RenderRequest renderRequest ){
		int curAnio = Calendar.getInstance().get(Calendar.YEAR);
		List<Integer> listAnio = new ArrayList<>();
		for (int i = curAnio; i > 2000 ; i--) {
			listAnio.add(i);
		}
		renderRequest.setAttribute("listAnio", listAnio);
	}
	
	private void getCurrentUrl ( RenderRequest renderRequest ){
		ThemeDisplay td  = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		renderRequest.setAttribute("curUrl", td.getURLCurrent());
	}

	private void getCatalogoMoneda(RenderRequest renderRequest){
		usuario = (User) renderRequest.getAttribute(WebKeys.USER);
		CatalogoMoneda catalogoMoneda;
		try{
			catalogoMoneda = _CrmGenericoService.getCatalogoMoneda(usuario.getScreenName(),PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73);
			renderRequest.setAttribute("listMoneda",catalogoMoneda.getLista());
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	private void getEjecutivos(RenderRequest renderRequest){
		usuario = (User) renderRequest.getAttribute(WebKeys.USER);
		int idUsuario = Integer.parseInt(usuario.getUserId() + "");
		try {
			List<UsuarioCrm> listaUsuarios = new ArrayList<>();
			User_Crm user_crm = User_CrmLocalServiceUtil.getUser_Crm(idUsuario);
			switch (user_crm.getPerfilId()){
				case CrmDatabaseKey.ID_PERFIL_MANAGER_CONTABILIDAD:
				case CrmDatabaseKey.ID_PERFIL_MANAGER_LEGAL:
				case CrmDatabaseKey.ID_PERFIL_MANAGER_VENTAS:
				case CrmDatabaseKey.ID_PERFIL_MANAGER_EMISION_SUSCRIPCION:
				case CrmDatabaseKey.ID_PERFIL_MANAGER_SINIESTROS:
					List<User_Crm> ejecutivos = User_CrmLocalServiceUtil.getUsers_CrmByPerfil(CrmDatabaseKey.ID_PERFIL_EJECUTIVO_VENTAS);
					List<User_Crm> analistas = User_CrmLocalServiceUtil.getUsers_CrmByPerfil(CrmDatabaseKey.ID_PERFIL_ANALISTA_VENTAS);
					listaUsuarios = funcionesGastosService.juntaUsuarios(analistas,ejecutivos);
					break;
				default:
					UsuarioCrm usuarioCrm = new UsuarioCrm(
						UserLocalServiceUtil.getUserById(user_crm.getUserId()).getFullName().toUpperCase(),
							user_crm.getUserId(),
							user_crm.getArea()
					);
					listaUsuarios.add(usuarioCrm);
					ListaRegistro listaAreas = _CrmGenericoService.getCatalogo(
							CrmServiceKey.TMX_CTE_ROW_TODOS,
							CrmServiceKey.TMX_CTE_TRANSACCION_GET,
							CrmServiceKey.LIST_CAT_AREA,
							CrmServiceKey.TMX_CTE_CAT_ACTIVOS,
							usuario.getScreenName(),
							PresupuestosCrmPortlet73PortletKeys.PRESUPUESTOSCRMPORTLET73);
					List<Agente> agentes = AgenteLocalServiceUtil.findByEjecutivoIdAndEstatusAgente(user_crm.getUserId(),CrmDatabaseKey.ESTATUS_AUTORIZADO);
					renderRequest.setAttribute("area",listaAreas.getLista().stream().filter(f -> f.getId() == usuarioCrm.oficina).collect(Collectors.toList()).get(0));
					renderRequest.setAttribute("usuarioCrm",usuarioCrm);
					renderRequest.setAttribute("listaAgentes",agentes);
					break;
			}
			renderRequest.setAttribute("listaUsuarios",listaUsuarios);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void urlDocCargaArchivo(RenderRequest renderRequest){
		ThemeDisplay td  = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		String pantalla = td.getURLCurrent();
		try {
			long idGroup = PortalUtil.getScopeGroupId(renderRequest);
			DLFolder fPresupuestos = DLFolderLocalServiceUtil.getFolder(idGroup, DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
					"Documentos_Aux_Presupuestos");
			FileEntry fileEntry = _dlAppService.getFileEntry(idGroup, fPresupuestos.getFolderId(), pantalla.contains("produccion")? "Layout_Produccion.xlsx": "Layout_Gastos.xlsx");
			String urlDoc = renderRequest.getScheme() + "://" + renderRequest.getServerName() + ":" + renderRequest.getServerPort() + "/documents/" + idGroup + "/" + fileEntry.getFolderId() + "/" + fileEntry.getFileName()
					+ "/" + fileEntry.getUuid();
			renderRequest.setAttribute("urlDoc", urlDoc);
		} catch (PortalException e) {
			e.printStackTrace();
		}

	}
}