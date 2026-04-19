package com.fitmanager.servlet;

import com.fitmanager.dao.ReservationDAO;
import com.fitmanager.model.Reservation;
import com.fitmanager.model.Reservation.Statut;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Servlet REST pour la gestion des rÃ©servations.
 *
 * GET  /reservations                        â†’ liste toutes les rÃ©servations
 * GET  /reservations?action=view&id=X       â†’ dÃ©tail d'une rÃ©servation
 * GET  /reservations?action=byAdherent&adherentId=X â†’ rÃ©servations d'un adhÃ©rent
 * GET  /reservations?action=byLocal&localId=X       â†’ rÃ©servations d'un local
 * POST /reservations action=create          â†’ crÃ©er une rÃ©servation
 * POST /reservations action=annuler&id=X   â†’ annuler une rÃ©servation
 * DELETE /reservations?id=X                â†’ supprimer (admin uniquement)
 */
@WebServlet(name = "ReservationServlets", urlPatterns = "/reservations")
public class ReservationServlets extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ReservationDAO reservationDAO;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Override
    public void init() throws ServletException {
        reservationDAO = new ReservationDAO();
    }

    // -------------------------------------------------------------------------
    // Routage GET
    // -------------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isEmpty() || "list".equalsIgnoreCase(action)) {
            listReservations(response);
            return;
        }

        switch (action.toLowerCase()) {
            case "view":
                viewReservation(request, response);
                break;
            case "byadherent":
                listByAdherent(request, response);
                break;
            case "bylocal":
                listByLocal(request, response);
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'action' requis");
            return;
        }

        switch (action.toLowerCase()) {
            case "create":
                createReservation(request, response);
                break;
            case "annuler":
                annulerReservation(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Action POST inconnue : " + action);
        }
    }

    // -------------------------------------------------------------------------
    // DELETE (admin)
    // -------------------------------------------------------------------------

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        deleteReservation(request, response);
    }

    // -------------------------------------------------------------------------
    // Handlers
    // -------------------------------------------------------------------------

    private void listReservations(HttpServletResponse response) throws IOException {
        try (Connection conn = getConnection()) {
            List<Reservation> list = reservationDAO.findAll(conn);
            writeJson(response, list);
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void viewReservation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'id' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            Optional<Reservation> resa = reservationDAO.findById(conn, id);
            if (resa.isPresent()) {
                writeJson(response, resa.get());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "RÃ©servation introuvable pour id=" + id);
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void listByAdherent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer adherentId = parseInteger(request.getParameter("adherentId"));
        if (adherentId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'adherentId' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            List<Reservation> list = reservationDAO.findByAdherentId(conn, adherentId);
            writeJson(response, list);
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void listByLocal(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer localId = parseInteger(request.getParameter("localId"));
        if (localId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'localId' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            List<Reservation> list = reservationDAO.findByLocalId(conn, localId);
            writeJson(response, list);
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void createReservation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Reservation resa = parseReservationFromRequest(request);
        if (resa == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "ParamÃ¨tres invalides : adherentId, localId, dateReservation, heureDebut, heureFin requis");
            return;
        }
        try (Connection conn = getConnection()) {
            int generatedId = reservationDAO.insert(conn, resa);
            resa.setId(generatedId);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, resa);
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void annulerReservation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'id' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            boolean updated = reservationDAO.updateStatut(conn, id, Statut.ANNULEE);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "RÃ©servation introuvable pour id=" + id);
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    private void deleteReservation(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ParamÃ¨tre 'id' requis");
            return;
        }
        try (Connection conn = getConnection()) {
            boolean deleted = reservationDAO.delete(conn, id);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "RÃ©servation introuvable pour id=" + id);
            }
        } catch (SQLException e) {
            sendServerError(response, e);
        }
    }

    // -------------------------------------------------------------------------
    // Parsing des paramÃ¨tres
    // -------------------------------------------------------------------------

    private Reservation parseReservationFromRequest(HttpServletRequest request) {
        Integer   adherentId = parseInteger(request.getParameter("adherentId"));
        Integer   localId    = parseInteger(request.getParameter("localId"));
        LocalDate date       = parseDate(request.getParameter("dateReservation"));
        LocalTime debut      = parseTime(request.getParameter("heureDebut"));
        LocalTime fin        = parseTime(request.getParameter("heureFin"));
        Statut    statut     = parseStatut(request.getParameter("statut"));

        if (adherentId == null || localId == null || date == null
                || debut == null || fin == null) {
            return null;
        }
        return new Reservation(adherentId, localId, date, debut, fin, statut);
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

    private static LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            // Accepte "HH:mm" ou "HH:mm:ss"
            String v = value.trim();
            if (v.length() == 5) v = v + ":00";
            return LocalTime.parse(v);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Statut parseStatut(String value) {
        if (value == null || value.trim().isEmpty()) return Statut.CONFIRMEE;
        try {
            return Statut.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return Statut.CONFIRMEE;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection(getServletContext());
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
        if (object instanceof Reservation) {
            Reservation r = (Reservation) object;
            return "{"
                    + "\"id\":"               + r.getId()
                    + ",\"adherentId\":"      + r.getAdherentId()
                    + ",\"localId\":"         + r.getLocalId()
                    + ",\"dateReservation\":" + quote(r.getDateReservation() == null ? null : r.getDateReservation().toString())
                    + ",\"heureDebut\":"      + quote(r.getHeureDebut()      == null ? null : r.getHeureDebut().toString())
                    + ",\"heureFin\":"        + quote(r.getHeureFin()        == null ? null : r.getHeureFin().toString())
                    + ",\"statut\":"          + quote(r.getStatut()          == null ? null : r.getStatut().toString())
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
}
