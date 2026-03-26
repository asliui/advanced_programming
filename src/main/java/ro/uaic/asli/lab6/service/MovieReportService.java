package ro.uaic.asli.lab6.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import ro.uaic.asli.lab6.dao.MovieReportDAO;
import ro.uaic.asli.lab6.model.MovieReportRow;

import java.awt.Desktop;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MovieReportService {
    private final MovieReportDAO reportDAO = new MovieReportDAO();

    public void createAndOpenHtmlReport(Path outputHtml) {
        try {
            List<MovieReportRow> rows = reportDAO.findAll();

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
            cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
            cfg.setDefaultEncoding("UTF-8");

            Template template = cfg.getTemplate("movies-report.ftl");

            Map<String, Object> model = new HashMap<>();
            model.put("movies", rows);

            if (outputHtml.getParent() != null) {
                Files.createDirectories(outputHtml.getParent());
            }

            try (Writer writer = Files.newBufferedWriter(outputHtml, StandardCharsets.UTF_8)) {
                template.process(model, writer);
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(outputHtml.toUri());
            }
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Could not create HTML report: " + e.getMessage(), e);
        }
    }
}

