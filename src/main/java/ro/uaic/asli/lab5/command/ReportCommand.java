package ro.uaic.asli.lab5.command;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import ro.uaic.asli.lab5.exception.CatalogException;
import ro.uaic.asli.lab5.repository.CatalogRepository;

import java.awt.Desktop;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ReportCommand implements Command {
    private final CatalogRepository repository;
    private final Path outputHtml;

    public ReportCommand(CatalogRepository repository, Path outputHtml) {
        this.repository = repository;
        this.outputHtml = outputHtml;
    }

    @Override
    public void execute() throws CatalogException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");
        try {
            Template template = cfg.getTemplate("catalog-report.ftl");
            Map<String, Object> model = new HashMap<>();
            model.put("resources", repository.listAll());

            Files.createDirectories(outputHtml.getParent());
            try (Writer writer = Files.newBufferedWriter(outputHtml, StandardCharsets.UTF_8)) {
                template.process(model, writer);
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(outputHtml.toUri());
            }
        } catch (IOException | TemplateException e) {
            throw new CatalogException("Could not create HTML report.", e);
        }
    }
}
