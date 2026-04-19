package com.fitmanager.servlet;

import com.fitmanager.dao.AbonnementDAO;
import com.fitmanager.model.Abonnement;
import com.fitmanager.model.Abonnement.Statut;
import com.fitmanager.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "AbonnementServlets", urlPatterns = "/abonnements")
public class AbonnementServlets extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private AbonnementDAO abonnementDAO;

    @Override
    public void init() throws ServletException {
        abonnementDAO = new AbonnementDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty() || "list".equalsIgnoreCase(action)) {
            listAbonnements(response); return;
        }
        switch (action.toLowerCase()) {
            case "view": viewAbonnement(request, response); break;
            default: response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action inconnue : " + action);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action requise"); return;
        }
        switch (action.toLowerCase()) {
            case "create": createAbonnement(request, response); break;
            case "update": updateAbonnement(request, response); break;
            default: response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action inconnue : " + action);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        deleteAbonnement(request, response);
    }

    private void listAbonnements(HttpServletResponse response) throws IOException {
        try (Connection conn = getConnection()) {
            writeJson(response, abonnementDAO.findAll(conn));
        } catch (SQLException e) { sendServerError(response, e); }
    }

    private void viewAbonnement(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametre id requis"); return; }
        try (Connection conn = getConnection()) {
            Optional<Abonnement> a = abonnementDAO.findById(conn, id);
            if (a.isPresent()) writeJson(response, a.get());
            else response.sendError(HttpServletResponse.SC_NOT_FOUND, "Abonnement introuvable id=" + id);
        } catch (SQLException e) { sendServerError(response, e); }
    }

    private void deleteAbonnement(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametre id requis"); return; }
        try (Connection conn = getConnection()) {
            if (abonnementDAO.delete(conn, id)) response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            else response.sendError(HttpServletResponse.SC_NOT_FOUND, "Abonnement introuvable id=" + id);
        } catch (SQLException e) { sendServerError(response, e); }
    }

    private void createAbonnement(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Abonnement a = parseAbonnementFromRequest(request, false);
        if (a == null) { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametres invalides"); return; }
        try (Connection conn = getConnection()) {
            int id = abonnementDAO.insert(conn, a);
            a.setId(id);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, a);
        } catch (SQLException e) { sendServerError(response, e); }
    }

    private void updateAbonnement(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Abonnement a = parseAbonnementFromRequest(request, true);
        if (a == null || a.getId() <= 0) { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametres invalides"); return; }
        try (Connection conn = getConnection()) {
            if (abonnementDAO.update(conn, a)) writeJson(response, a);
            else response.sendError(HttpServletResponse.SC_NOT_FOUND, "Abonnement introuvable id=" + a.getId());
        } catch (SQLException e) { sendServerError(response, e); }
    }

    private Abonnement parseAbonnementFromRequest(HttpServletRequest request, boolean requireId) {
        Integer id = parseInteger(request.getParameter("id"));
        Integer adherentId = parseInteger(request.getParameter("adherentId"));
        Integer localId = parseInteger(request.getParameter("localId"));
        LocalDate dateDebut = parseDate(request.getParameter("dateDebut"));
        LocalDate dateFin = parseDate(request.getParameter("dateFin"));
        Statut statut = parseStatut(request.getParameter("statut"));
        if ((requireId && id == null) || adherentId == null || localId == null) return null;
        return requireId ? new Abonnement(id, adherentId, localId, dateDebut, dateFin, statut)
                        : new Abonnement(adherentId, localId, dateDebut, dateFin, statut);
    }

    private static Integer parseInteger(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return null; }
    }
    private static LocalDate parseDate(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        try { return LocalDate.parse(v.trim()); } catch (Exception e) { return null; }
    }
    private static Statut parseStatut(String v) {
        if (v == null || v.trim().isEmpty()) return Statut.ACTIF;
        try { return Statut.fromString(v.trim()); } catch (IllegalArgumentException e) { return Statut.ACTIF; }
    }
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection(getServletContext());
    }
    private void writeJson(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter w = response.getWriter()) {
            if (object instanceof List) {
                StringBuilder sb = new StringBuilder("[");
                List<?> list = (List<?>) object;
                for (int i = 0; i < list.size(); i++) { sb.append(toJson(list.get(i))); if (i < list.size()-1) sb.append(','); }
                sb.append("]"); w.print(sb.toString());
            } else { w.print(toJson(object)); }
        }
    }
    private String toJson(Object o) {
        if (o == null) return "null";
        if (o instanceof Abonnement) {
            Abonnement a = (Abonnement) o;
            return "{\"id\":" + a.getId() + ",\"adherentId\":" + a.getAdherentId() + ",\"localId\":" + a.getLocalId()
                + ",\"dateDebut\":" + quote(a.getDateDebut()==null?null:a.getDateDebut().toString())
                + ",\"dateFin\":" + quote(a.getDateFin()==null?null:a.getDateFin().toString())
                + ",\"statut\":" + quote(a.getStatut()==null?null:a.getStatut().toString()) + "}";
        }
        return quote(o.toString());
    }
    private String quote(String v) {
        if (v == null) return "null";
        return "\"" + v.replace("\\","\\\\").replace("\"","\\\"") + "\"";
    }
    private void sendServerError(HttpServletResponse response, SQLException e) throws IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur serveur");
        getServletContext().log("Erreur SQL AbonnementServlets", e);
    }
}
