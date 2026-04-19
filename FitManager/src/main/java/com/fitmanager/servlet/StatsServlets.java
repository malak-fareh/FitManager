package com.fitmanager.servlet;

import com.fitmanager.dao.StatsDAO;
import com.fitmanager.dao.StatsDAO.LocalStat;
import com.fitmanager.dao.StatsDAO.MonthlyStat;
import com.fitmanager.model.Abonnement;
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
import java.util.List;

@WebServlet(name = "StatsServlets", urlPatterns = "/stats")
public class StatsServlets extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private StatsDAO statsDAO;

    @Override
    public void init() throws ServletException {
        statsDAO = new StatsDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) action = "summary";
        try (Connection conn = getConnection()) {
            switch (action.toLowerCase()) {
                case "total":    writeJson(response, new SimpleResponse("total", statsDAO.countTotal(conn))); break;
                case "active":   writeJson(response, new SimpleResponse("active", statsDAO.countActive(conn))); break;
                case "bystatus":
                    Abonnement.Statut st = parseStatut(request.getParameter("statut"));
                    writeJson(response, new SimpleResponse("countByStatus", statsDAO.countByStatus(conn, st))); break;
                case "bylocal":  writeJson(response, statsDAO.countByLocal(conn)); break;
                case "monthly":
                    Integer mb = parseInteger(request.getParameter("monthsBack"));
                    if (mb == null || mb <= 0) mb = 6;
                    writeJson(response, statsDAO.subscriptionsPerMonth(conn, mb)); break;
                default:
                    Integer m = parseInteger(request.getParameter("monthsBack"));
                    if (m == null || m <= 0) m = 6;
                    StatsSummary s = new StatsSummary();
                    s.total = statsDAO.countTotal(conn);
                    s.active = statsDAO.countActive(conn);
                    s.byLocal = statsDAO.countByLocal(conn);
                    s.byMonth = statsDAO.subscriptionsPerMonth(conn, m);
                    writeJson(response, s); break;
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur serveur");
            getServletContext().log("Erreur SQL StatsServlets", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection(getServletContext());
    }
    private static Integer parseInteger(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return null; }
    }
    private static Abonnement.Statut parseStatut(String v) {
        if (v == null || v.trim().isEmpty()) return Abonnement.Statut.ACTIF;
        try { return Abonnement.Statut.fromString(v.trim()); } catch (IllegalArgumentException e) { return Abonnement.Statut.ACTIF; }
    }
    private void writeJson(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter w = response.getWriter()) { w.print(toJson(object)); }
    }
    private String toJson(Object o) {
        if (o == null) return "null";
        if (o instanceof SimpleResponse) { SimpleResponse r = (SimpleResponse) o; return "{\"type\":\"" + r.type + "\",\"value\":" + r.value + "}"; }
        if (o instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) o;
            for (int i = 0; i < list.size(); i++) { sb.append(toJson(list.get(i))); if (i < list.size()-1) sb.append(','); }
            return sb.append("]").toString();
        }
        if (o instanceof LocalStat) { LocalStat s = (LocalStat) o; return "{\"localId\":" + s.getLocalId() + ",\"nom\":\"" + s.getNom() + "\",\"count\":" + s.getCount() + "}"; }
        if (o instanceof MonthlyStat) { MonthlyStat s = (MonthlyStat) o; return "{\"year\":" + s.getYear() + ",\"month\":" + s.getMonth() + ",\"count\":" + s.getCount() + "}"; }
        if (o instanceof StatsSummary) { StatsSummary s = (StatsSummary) o; return "{\"total\":" + s.total + ",\"active\":" + s.active + ",\"byLocal\":" + toJson(s.byLocal) + ",\"monthly\":" + toJson(s.byMonth) + "}"; }
        return "\"" + o.toString() + "\"";
    }
    private static class SimpleResponse { final String type; final int value; SimpleResponse(String t, int v) { type=t; value=v; } }
    private static class StatsSummary { int total; int active; List<LocalStat> byLocal; List<MonthlyStat> byMonth; }
}
