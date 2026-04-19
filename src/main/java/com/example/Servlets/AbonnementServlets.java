package main.java.com.example.Servlets;

import main.java.com.example.dao.AbonnementDAO;
import main.java.com.example.model.Abonnement;
import main.java.com.example.model.Abonnement.Statut;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "AbonnementServlets", urlPatterns = "/abonnements")
public class AbonnementServlets extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    private String jdbcDriver;
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private AbonnementDAO abonnementDAO;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        jdbcDriver   = getInitParam(config, "jdbcDriver",   DEFAULT_JDBC_DRIVER);
        jdbcUrl      = getInitParam(config, "jdbcUrl",      null);
        jdbcUser     = getInitParam(config, "jdbcUser",     "");
        jdbcPassword = getInitParam(config, "jdbcPassword", "");

        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            throw new ServletException("AbonnementServlets : paramètre 'jdbcUrl' requis");
        }
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new ServletException("Impossible de charger le driver JDBC : " + jdbcDriver, e);
        }
        abonnementDAO = new AbonnementDAO();
    }

    // -------------------------------------------------------------------------
    // Routage GET
    // -------------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isEmpty() || "list".equalsIgnoreCase(action)) {
            listAbonnements(response);
            return;
        }

        switch (action.toLowerCase()) {
            case "view":
                viewAbonnement(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Action GET inconnue : " + action);
        }
    }

    // -------------------------------------------------------------------------
    // Routage POST
    // -------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action requise");
            return;
        }

        switch (action.toLowerCase()) {
            case "create":
                createAbonnement(request, response);
                break;
            case "update":
                updateAbonnement(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Action POST inconnue : " + action);
        }
    }

    // -------------------------------------------------------------------------
    // CORRECTION : DELETE dans doDelete (REST correct)
    // -------------------------------------------------------------------------

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        deleteAbonnement(request, response);
    }

    // -------------------------------------------------------------------------
    // Handlers
    // -------------------------------------------------------------------------

    private void listAbonnements(HttpServletResponse response) throws IOException {
        try (Connection conn = getConnection()) {
            List<Abonnement> abonnements = abonnementDAO.findAll(conn);
            writeJson(response, abonnements);
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void viewAbonnement(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre 'id' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            Optional<Abonnement> abonnement = abonnementDAO.findById(conn, id);
            if (abonnement.isPresent()) {
                writeJson(response, abonnement.get());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Abonnement introuvable pour id=" + id);
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void deleteAbonnement(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre 'id' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            boolean deleted = abonnementDAO.delete(conn, id);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Abonnement introuvable pour id=" + id);
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void createAbonnement(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Abonnement abonnement = parseAbonnementFromRequest(request, false);
        if (abonnement == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Paramètres invalides pour créer l'abonnement");
            return;
        }
        try (Connection conn = getConnection()) {
            int generatedId = abonnementDAO.insert(conn, abonnement);
            abonnement.setId(generatedId);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, abonnement);
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void updateAbonnement(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Abonnement abonnement = parseAbonnementFromRequest(request, true);
        if (abonnement == null || abonnement.getId() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Paramètres invalides pour mettre à jour l'abonnement");
            return;
        }
        try (Connection conn = getConnection()) {
            boolean updated = abonnementDAO.update(conn, abonnement);
            if (updated) {
                writeJson(response, abonnement);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Abonnement introuvable pour id=" + abonnement.getId());
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    // -------------------------------------------------------------------------
    // Parsing des paramètres
    // -------------------------------------------------------------------------

    private Abonnement parseAbonnementFromRequest(HttpServletRequest request, boolean requireId) {
        Integer   id         = parseInteger(request.getParameter("id"));
        Integer   adherentId = parseInteger(request.getParameter("adherentId"));
        Integer   localId    = parseInteger(request.getParameter("localId"));
        LocalDate dateDebut  = parseDate(request.getParameter("dateDebut"));
        LocalDate dateFin    = parseDate(request.getParameter("dateFin"));
        Statut    statut     = parseStatut(request.getParameter("statut"));

        if ((requireId && id == null) || adherentId == null || localId == null) {
            return null;
        }

        return requireId
                ? new Abonnement(id, adherentId, localId, dateDebut, dateFin, statut)
                : new Abonnement(adherentId, localId, dateDebut, dateFin, statut);
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Statut parseStatut(String value) {
        if (value == null || value.trim().isEmpty()) return Statut.ACTIF;
        try {
            return Statut.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return Statut.ACTIF;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    private void writeJson(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            if (object instanceof List) {
                StringBuilder sb = new StringBuilder("[");
                List<?> list = (List<?>) object;
                for (int i = 0; i < list.size(); i++) {
                    sb.append(toJson(list.get(i)));
                    if (i < list.size() - 1) sb.append(',');
                }
                sb.append("]");
                writer.print(sb.toString());
            } else {
                writer.print(toJson(object));
            }
        }
    }

    private String toJson(Object object) {
        if (object == null) return "null";
        if (object instanceof Abonnement) {
            Abonnement a = (Abonnement) object;
            return "{"
                    + "\"id\":"          + a.getId()
                    + ",\"adherentId\":" + a.getAdherentId()
                    + ",\"localId\":"    + a.getLocalId()
                    + ",\"dateDebut\":"  + quote(a.getDateDebut() == null ? null : a.getDateDebut().toString())
                    + ",\"dateFin\":"    + quote(a.getDateFin()   == null ? null : a.getDateFin().toString())
                    + ",\"statut\":"     + quote(a.getStatut()    == null ? null : a.getStatut().toString())
                    + "}";
        }
        return quote(object.toString());
    }

    private String quote(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private void sendServerError(HttpServletResponse response, SQLException e) throws IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Erreur serveur : " + e.getMessage());
    }

    private String getInitParam(ServletConfig config, String name, String defaultValue) {
        String value = config.getInitParameter(name);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }
}