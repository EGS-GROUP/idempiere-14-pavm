package org.adempiere.webui.dashboard;

import org.adempiere.util.Callback;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.InfoSchedule;
import org.compiere.model.MInfoWindow;
import org.compiere.model.MQuery;
import org.compiere.model.MResourceAssignment;
import org.compiere.model.MRole;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window.Mode;

public class DPQuickLinks extends DashboardPanel implements EventListener<Event> {

   public enum LinkKind {
      WINDOW,
      INFO_WINDOW,
      FORM,
      INFO_SCHEDULE
   }
	
   private static final long serialVersionUID = 4L;
   private static final CLogger log = CLogger.getCLogger(DPQuickLinks.class);
   private static final int FIRST_ROW_COLLAPSE_THRESHOLD = 4;
   private static final Object[][] INFO_LINKS;
   private static final Object[][] CONFIG_LINKS;
   
   private static final String CARD_STYLE_BASE = "padding:14px 16px;border:1px solid #e2e8f0;border-radius:10px;background:#ffffff;box-shadow:0 1px 2px rgba(15,23,42,0.06);display:flex;flex-direction:column;gap:8px;min-height:120px;transition:transform 0.15s ease, box-shadow 0.15s ease;";
   private static final String CARD_STYLE_HOVER = "padding:14px 16px;border:1px solid #e2e8f0;border-radius:10px;background:#ffffff;box-shadow:0 1px 2px rgba(15,23,42,0.06);display:flex;flex-direction:column;gap:8px;min-height:120px;transition:transform 0.15s ease, box-shadow 0.15s ease;transform:translateY(-1px);box-shadow:0 6px 18px rgba(26,115,232,0.18);";
   private static final String SECTION_HEADER_STYLE = "cursor:pointer;display:flex;align-items:center;gap:8px;padding:10px 6px 6px;border-bottom:1px solid #e2e8f0;margin:6px 0 8px;user-select:none;";
   private static final String CHEVRON_STYLE = "font-size:10px;color:#64748b;width:12px;display:inline-block;transition:transform 0.2s ease;";
   private static final String SECTION_TITLE_STYLE = "font-size:11px;font-weight:700;color:#475569;text-transform:uppercase;letter-spacing:0.05em;";
   
   private int activeDPCount = 0;
   private SectionHandle infoSection;
   private SectionHandle configSection;
   
   private static final String GRID_STYLE_EXPANDED = "display:grid;grid-template-columns:repeat(auto-fill, minmax(220px, 1fr));gap:12px;padding:6px 4px 8px;";
   private static final String GRID_STYLE_COLLAPSED = "display:none;";

   static {
      INFO_LINKS = new Object[][]{
         {LinkKind.FORM, 200020, "InfoAccount", "ISW_QL_InfoAccount_D"}, 
         {LinkKind.INFO_SCHEDULE, 0, "InfoSchedule", "ISW_QL_InfoSchedule_D"}, 
         {LinkKind.INFO_WINDOW, 200000, null, "ISW_QL_InfoProduct_D"}, 
         {LinkKind.INFO_WINDOW, 200001, null, "ISW_QL_InfoBP_D"}, 
         {LinkKind.INFO_WINDOW, 200002, null, "ISW_QL_InfoOrder_D"}, 
         {LinkKind.INFO_WINDOW, 200003, null, "ISW_QL_InfoInvoice_D"}, 
         {LinkKind.INFO_WINDOW, 200004, null, "ISW_QL_InfoShipment_D"}, 
         {LinkKind.INFO_WINDOW, 200005, null, "ISW_QL_InfoPayment_D"}, 
         {LinkKind.INFO_WINDOW, 200006, null, "ISW_QL_InfoResource_D"}, 
         {LinkKind.INFO_WINDOW, 200007, null, "ISW_QL_InfoAsset_D"}, 
         {LinkKind.INFO_WINDOW, 200020, null, "ISW_QL_InfoBatchPayment_D"}
      };
      
      CONFIG_LINKS = new Object[][]{
         {LinkKind.WINDOW, 115, "ISW_QL_Currency_T", "ISW_QL_Currency_D"},
         {LinkKind.WINDOW, 122, "ISW_QL_Country_T", "ISW_QL_Country_D"},
         {LinkKind.WINDOW, 109, "ISW_QL_Tenant_T", "ISW_QL_Tenant_D"}, 
         {LinkKind.WINDOW, 110, "ISW_QL_Org_T", "ISW_QL_Org_D"}, 
         {LinkKind.WINDOW, 117, "ISW_QL_Calendar_T", "ISW_QL_Calendar_D"},
         {LinkKind.WINDOW, 125, "ISW_QL_AcctSchema_T", "ISW_QL_AcctSchema_D"},
         {LinkKind.WINDOW, 118, "ISW_QL_CoA_T", "ISW_QL_CoA_D"}, 
         {LinkKind.WINDOW, 137, "ISW_QL_TaxRate_T", "ISW_QL_TaxRate_D"},
         {LinkKind.WINDOW, 141, "ISW_QL_PayTerm_T", "ISW_QL_PayTerm_D"}, 
         {LinkKind.WINDOW, 146, "ISW_QL_PriceList_T", "ISW_QL_PriceList_D"}, 
         {LinkKind.WINDOW, 108, "ISW_QL_User_T", "ISW_QL_User_D"}, 
         {LinkKind.WINDOW, 111, "ISW_QL_Role_T", "ISW_QL_Role_D"},
         {LinkKind.WINDOW, 123, "ISW_QL_BP_T", "ISW_QL_BP_D"}, 
         {LinkKind.WINDOW, 140, "ISW_QL_Product_T", "ISW_QL_Product_D"},
         {LinkKind.WINDOW, 139, "ISW_QL_Warehouse_T", "ISW_QL_Warehouse_D"}, 
         {LinkKind.WINDOW, 158, "ISW_QL_Bank_T", "ISW_QL_Bank_D"}
      };
   }

   public DPQuickLinks() {
      this.setSclass("dp-quicklinks-box");
      this.activeDPCount = Env.getContextAsInt(Env.getCtx(), "#ACTIVE_DASHBOARD_COUNT_FOR_USER");
      this.buildUI();
   }

   private void buildUI() {
      // Ajuste: Aseguramos que la sección de información siempre inicie colapsada (true)
      boolean infoCollapsed = true; 
      boolean configCollapsed = true;

      int rendered = 0;
      this.infoSection = this.appendSection("ISW_QL_InfoSection", INFO_LINKS, infoCollapsed);
      if (this.infoSection != null) {
         rendered += this.infoSection.tileCount;
      }

      this.configSection = this.appendSection("ISW_QL_ConfigSection", CONFIG_LINKS, configCollapsed);
      if (this.configSection != null) {
         rendered += this.configSection.tileCount;
      }

      if (rendered == 0) {
         Label empty = new Label(Msg.getMsg(Env.getCtx(), "NoAccessGranted"));
         empty.setStyle("color:#6b7280;font-style:italic;padding:8px;");
         this.appendChild(empty);
      }
   }

   private SectionHandle appendSection(String headerMsgKey, Object[][] links, boolean collapsed) {
      Div grid = new Div();
      grid.setStyle(GRID_STYLE_EXPANDED);
      int tiles = 0;

      for(Object[] entry : links) {
         LinkKind kind = (LinkKind)entry[0];
         int targetId = (Integer)entry[1];
         if (this.isAccessible(kind, targetId)) {
            String title = this.resolveTitle(kind, targetId, (String)entry[2]);
            String desc = entry[3] != null ? Msg.getMsg(Env.getCtx(), (String)entry[3]) : null;
            grid.appendChild(this.buildCard(kind, targetId, title, desc));
            ++tiles;
         }
      }

      if (tiles == 0) {
         return null;
      } else {
         Div header = new Div();
         header.setStyle(SECTION_HEADER_STYLE);
         Label chevron = new Label(collapsed ? "▶" : "▼");
         chevron.setStyle(CHEVRON_STYLE);
         header.appendChild(chevron);
         
         String var10002 = Msg.getMsg(Env.getCtx(), headerMsgKey);
         Label title = new Label(var10002 + "  (" + tiles + ")");
         title.setStyle(SECTION_TITLE_STYLE);
         header.appendChild(title);
         
         if (collapsed) {
            grid.setStyle(GRID_STYLE_COLLAPSED);
         }

         SectionHandle handle = new SectionHandle(grid, chevron, !collapsed, tiles);
         header.addEventListener("onClick", evt -> handle.setShown(!handle.shown));

         this.appendChild(header);
         this.appendChild(grid);
         return handle;
      }
   }

   private String resolveTitle(LinkKind kind, int targetId, String msgKey) {
      if (kind == LinkKind.INFO_WINDOW) {
         MInfoWindow iw = MInfoWindow.get(targetId, (String)null);
         if (iw != null) {
            String name = iw.get_Translation("Name");
            return name != null && !name.isEmpty() ? name : iw.getName();
         } else {
            return Msg.getMsg(Env.getCtx(), "Info");
         }
      } else {
         return Msg.getMsg(Env.getCtx(), msgKey);
      }
   }

   private Component buildCard(LinkKind kind, int targetId, String title, String description) {
      Div card = new Div();
      card.setStyle(CARD_STYLE_BASE);
      card.addEventListener("onMouseOver", evt -> card.setStyle(CARD_STYLE_HOVER));
      card.addEventListener("onMouseOut", evt -> card.setStyle(CARD_STYLE_BASE));
      
      Label titleLbl = new Label(title);
      titleLbl.setStyle("font-weight:700;color:#1a73e8;font-size:14px;line-height:1.3;");
      card.appendChild(titleLbl);
      if (description != null && !description.isEmpty() && !description.equals(title)) {
         Label descLbl = new Label(description);
         descLbl.setMultiline(true);
         descLbl.setStyle("font-size:12px;color:#5f6368;line-height:1.5;flex:1;");
         card.appendChild(descLbl);
      }

      Button btn = new Button(Msg.getMsg(Env.getCtx(), "ISW_QL_OpenBtn"));
      btn.setStyle("align-self:flex-start;background:#1a73e8;color:#ffffff;border:none;border-radius:6px;padding:7px 16px;font-size:12px;font-weight:600;cursor:pointer;box-shadow:0 1px 2px rgba(26,115,232,0.3);");
      btn.setAttribute("kind", kind);
      btn.setAttribute("targetId", targetId);
      btn.addEventListener("onClick", this);
      card.appendChild(btn);
      return card;
   }

   private boolean isAccessible(LinkKind kind, int targetId) {
      if (targetId < 0) {
         return false;
      }
      try {
         MRole role = MRole.getDefault();
         switch (kind) {
            case WINDOW:
               return targetId > 0 && role.getWindowAccess(targetId) != null;
            case INFO_WINDOW:
               return targetId > 0 && role.getInfoAccess(targetId) != null;
            case FORM:
               if (targetId == 200020) {
                  return role.isShowAcct() && role.isAllow_Info_Account();
               }
               return role.getFormAccess(targetId) != null;
            case INFO_SCHEDULE:
               return role.isAllow_Info_Schedule();
         }
      } catch (Exception ex) {
         log.warning("Access check failed for " + kind + "/" + targetId + ": " + ex.getMessage());
      }
      return false;
   }

   public void onEvent(Event event) throws Exception {
      Component target = event.getTarget();
      Object kindObj = target.getAttribute("kind");
      Object idObj = target.getAttribute("targetId");
      if (kindObj instanceof LinkKind kind && idObj instanceof Integer) {
         int id = (Integer)idObj;

         try {
            switch (kind) {
               case WINDOW:
                  SessionManager.getAppDesktop().openWindow(id, (MQuery)null, (Callback)null);
                  break;
               case INFO_WINDOW:
                  SessionManager.getAppDesktop().openInfo(id);
                  break;
               case FORM:
                  ADForm form = ADForm.openForm(id);
                  form.setAttribute("mode", form.getWindowMode());
                  AEnv.showWindow(form);
                  break;
               case INFO_SCHEDULE:
                  InfoSchedule is = new InfoSchedule((MResourceAssignment)null, false);
                  is.setAttribute("mode", Mode.EMBEDDED);
                  AEnv.showWindow(is);
                  break;
            }
         } catch (Exception ex) {
            log.warning("Could not open " + kind + " id=" + id + ": " + ex.getMessage());
         }
      }
   }

   // --- CLASE INTERNA PARA EL MANEJO DE SECCIONES (ENCAPSULAMIENTO) ---
   private static class SectionHandle {
      final Div grid;
      final Label chevron;
      boolean shown;
      final int tileCount;

      SectionHandle(Div grid, Label chevron, boolean shown, int tileCount) {
         this.grid = grid;
         this.chevron = chevron;
         this.shown = shown;
         this.tileCount = tileCount;
      }

      void setShown(boolean show) {
         if (show != this.shown) {
            this.shown = show;
            this.grid.setStyle(show ? GRID_STYLE_EXPANDED : GRID_STYLE_COLLAPSED);
            this.chevron.setValue(show ? "▼" : "▶");
         }
      }
   }
}