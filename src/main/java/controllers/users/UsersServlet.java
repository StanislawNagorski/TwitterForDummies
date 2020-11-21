package controllers.users;

import dao.impl.MySQLUserDAO;
import model.AppUser;
import services.AppUserService;
import services.impl.AppUserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

import static utils.ServletUtils.*;

@WebServlet (name = "Users", value = "/users")
public class UsersServlet extends HttpServlet {

    AppUserService service;

    @Override
    public void init() throws ServletException {
        service = new AppUserServiceImpl(new MySQLUserDAO());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userLogin = getUserLoginFromSession(req);

        AppUser user = service.getUserByLogin(userLogin);
        HashSet<AppUser> followedUsers = service.getFollowedUsers(user);
        HashSet<AppUser> notFollowed = service.getNotFollowed(user)
                .stream()
                .sorted(Comparator.comparingInt(o -> o.getFollowers().size()))
                .limit(5)
                .collect(Collectors.toCollection(HashSet::new));

        HashSet<AppUser> followers;
        if (req.getParameter(FOLLOWERS_OFFSET) != null) {
            int offset = Integer.parseInt(req.getParameter(FOLLOWERS_OFFSET));
            followers = service.getFollowers(user, offset, DEFAULT_LIMIT);
        } else {
            followers = service.getFollowers(user, DEFAULT_OFFSET, DEFAULT_LIMIT);
        }

        req.setAttribute(FOLLOWED_USERS, followedUsers);
        req.setAttribute(NOT_FOLLOWED_USERS, notFollowed);
        req.setAttribute(FOLLOWERS, followers);

        req.getRequestDispatcher("/users.jsp").forward(req, resp);

    }
}
