
import com.google.gson.Gson;
import dao.Sql2oDepartmentDAO;
import dao.Sql2oDepartmentNewsDAO;
import dao.Sql2oGeneralNewsDAO;
import dao.Sql2oUserDAO;
import models.Department;
import models.DepartmentNews;
import models.GeneralNews;
import models.User;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;
import static spark.Spark.staticFileLocation;

public class App {
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }

    public static void main(String[] args) {
        port(getHerokuAssignedPort());
        staticFileLocation("/public");
        Connection conn;

//        ---Local Database---
        String connectionString = "jdbc:postgresql://localhost:5432/mulas";
        Sql2o sql2o = new Sql2o(connectionString, "moringa", "Access");

//        ---heroku Database---
//        String connectionString = "jdbc:postgresql://ec2-3-215-207-12.compute-1.amazonaws.com/d5g55ik27nqpq9"; // heroku db connection string
//        Sql2o sql2o = new Sql2o(connectionString, "mqqnohfjxmhjlz", "821577bd90197e76ac9a9266af4314a1130af55634e8890097b282ee04841153"); // heroku db sql2o instance


//        String connectionString = "jdbc:postgresql://localhost:5432/orgapi";
//        Sql2o sql2o = new Sql2o(connectionString, "moringa", "Access");

        Sql2oGeneralNewsDAO generalNewsDAO = new Sql2oGeneralNewsDAO(sql2o);
        Sql2oDepartmentDAO departmentDAO = new Sql2oDepartmentDAO(sql2o);
        Sql2oUserDAO userDAO = new Sql2oUserDAO(sql2o);
        Sql2oDepartmentNewsDAO departmentNewsDAO = new Sql2oDepartmentNewsDAO(sql2o);


        Map<String, Object> model = new HashMap<>();
        Gson gson = new Gson();


//        API ROUTES
        get("/api/generalnews", (req, res) -> {
            return gson.toJson(generalNewsDAO.getAllGeneralNews());
        });

        get("/api/generalnews/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            return gson.toJson(generalNewsDAO.getGeneralNewsById(id));
        });

        get("/api/departments", (req, res) -> {
            List<Department> departments = departmentDAO.getAllDepartments();
            for (Department department : departments) {
                int departmentId = department.getId();
                List<User> usersInDepartment = departmentDAO.getDepartmentUsersById(departmentId);
                department.setDepartmentUsers(usersInDepartment);
                department.setNoOfUsers(usersInDepartment.size());
                List<DepartmentNews> newsInDepartment = departmentDAO.getDepartmentNewsById(departmentId);
                department.setDepartmentNews(newsInDepartment);
            }
            return gson.toJson(departments);
        });

        get("/api/departments/:id", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Department department = departmentDAO.getDepartmentById(id);
            int departmentId = department.getId();
            List<User> usersInDepartment = departmentDAO.getDepartmentUsersById(departmentId);
            department.setDepartmentUsers(usersInDepartment);
            department.setNoOfUsers(usersInDepartment.size());
            List<DepartmentNews> newsInDepartment = departmentDAO.getDepartmentNewsById(departmentId);
            department.setDepartmentNews(newsInDepartment);
            return gson.toJson(department);
        });

        post("/api/adddepartment", "application/json", (request, response) -> {
            Department department = gson.fromJson(request.body(), Department.class);
            departmentDAO.add(department);
            response.status(201);
            return gson.toJson(department);
        });
        post("/user/new", "application/json", (request, response) -> {
            User user = gson.fromJson(request.body(), User.class);
            userDAO.add(user);
            response.status(201);
            return gson.toJson(user);
        });
        post("/Department/new", "application/json", (request, response) -> {
            Department department = gson.fromJson(request.body(), Department.class);
            departmentDAO.add(department);
            response.status(201);
            return gson.toJson(department);
        });
        post("/DepartmentNews/new", "application/json", (request, response) -> {
            DepartmentNews departmentNews = gson.fromJson(request.body(), DepartmentNews.class);
            departmentNewsDAO.add(departmentNews);
            response.status(201);
            return gson.toJson(departmentNews);
        });
        post("/GeneralNews/new", "application/json", (request, response) -> {
            GeneralNews generalNews = gson.fromJson(request.body(), GeneralNews.class);
            generalNewsDAO.add(generalNews);
            response.status(201);
            return gson.toJson(generalNews);
        });
//        UI ROUTES

        get("/", (req, res) -> {
            model.put("departmentNews", departmentNewsDAO.getAllDepartmentNews());
            model.put("users", userDAO.getAllUsers());
            model.put("departments", departmentDAO.getAllDepartments());
            model.put("generalnews", generalNewsDAO.getAllGeneralNews());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/addgeneral", (req, res) -> {
            return new ModelAndView(model, "general-news-form.hbs");
        }, new HandlebarsTemplateEngine());

        post("/addgeneral", (req, res) -> {
            String title = req.queryParams("title");
            String content = req.queryParams("content");
            GeneralNews newGeneralNews = new GeneralNews(title, content);
            generalNewsDAO.add(newGeneralNews);
            model.put("departmentNews", departmentNewsDAO.getAllDepartmentNews());
            model.put("users", userDAO.getAllUsers());
            model.put("departments", departmentDAO.getAllDepartments());
            model.put("generalnews", generalNewsDAO.getAllGeneralNews());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/adddepartment", (req, res) -> {
            return new ModelAndView(model, "department-form.hbs");
        }, new HandlebarsTemplateEngine());

        post("/adddepartment", (req, res) -> {
            String name = req.queryParams("name");
            String description = req.queryParams("description");
            Department newDepartment = new Department(name, description);
            departmentDAO.add(newDepartment);
            model.put("departmentNews", departmentNewsDAO.getAllDepartmentNews());
            model.put("users", userDAO.getAllUsers());
            model.put("departments", departmentDAO.getAllDepartments());
            model.put("generalnews", generalNewsDAO.getAllGeneralNews());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/adduser", (req, res) -> {
            model.put("departments", departmentDAO.getAllDepartments());
            return new ModelAndView(model, "user-form.hbs");
        }, new HandlebarsTemplateEngine());

        post("/adduser", (req, res) -> {
            String name = req.queryParams("name");
            String position = req.queryParams("position");
            String role = req.queryParams("role");
            int departmentId = Integer.parseInt(req.queryParams("department"));
            User newUser = new User(name, position, role, departmentId);
            userDAO.add(newUser);
            model.put("departmentNews", departmentNewsDAO.getAllDepartmentNews());
            model.put("users", userDAO.getAllUsers());
            model.put("departments", departmentDAO.getAllDepartments());
            model.put("generalnews", generalNewsDAO.getAllGeneralNews());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/adddepartmentnews", (req, res) -> {
            model.put("departments", departmentDAO.getAllDepartments());
            return new ModelAndView(model, "department-news-form.hbs");
        }, new HandlebarsTemplateEngine());

        post("/adddepartmentnews", (req, res) -> {
            String title = req.queryParams("title");
            String content = req.queryParams("content");
            int departmentId = Integer.parseInt(req.queryParams("department"));
            DepartmentNews newDepartmentNews = new DepartmentNews(title, content, departmentId);
            departmentNewsDAO.add(newDepartmentNews);
            model.put("departmentNews", departmentNewsDAO.getAllDepartmentNews());
            model.put("users", userDAO.getAllUsers());
            model.put("departments", departmentDAO.getAllDepartments());
            model.put("generalnews", generalNewsDAO.getAllGeneralNews());
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

    }
}
