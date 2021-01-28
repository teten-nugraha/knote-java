package id.ten.learnk8s.knotejava.api;

import id.ten.learnk8s.knotejava.config.KnoteProperties;
import id.ten.learnk8s.knotejava.domain.Note;
import id.ten.learnk8s.knotejava.repo.NoteRepository;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
@Controller
public class NoteController {

    @Autowired
    private NoteRepository notesRepository;

    @Autowired
    private KnoteProperties properties;

    private Parser parser = Parser.builder().build();
    private HtmlRenderer renderer = HtmlRenderer.builder().build();

    @GetMapping("/")
    public String index(Model model) {
        getAllNotes(model);
        return "index.ftl";
    }

    @PostMapping("/note")
    public String saveNotes(@RequestParam("image") MultipartFile file,
                            @RequestParam String description,
                            @RequestParam(required = false) String publish,
                            @RequestParam(required = false) String upload,
                            Model model) throws Exception {

        if (publish != null && publish.equals("Publish")) {
            saveNote(description, model);
            getAllNotes(model);
            return "redirect:/";
        }
        if (upload != null && upload.equals("Upload")) {
            if (file != null && file.getOriginalFilename() != null &&
                    !file.getOriginalFilename().isEmpty()) {
                uploadImage(file, description, model);
            }
            getAllNotes(model);
            return "index";
        }
        // After save fetch all notes again
        return "index";
    }

    private void getAllNotes(Model model) {
        List<Note> notes = notesRepository.findAll();
        Collections.reverse(notes);
        model.addAttribute("notes", notes);
    }

    private void saveNote(String description, Model model) {
        if (description != null && !description.trim().isEmpty()) {
            //You need to translate markup to HTML
            Node document = parser.parse(description.trim());
            String html = renderer.render(document);
            notesRepository.save(new Note( html));

            //After publish you need to clean up the textarea
            model.addAttribute("description", "");
        }
    }

    private void uploadImage(MultipartFile file, String description, Model model) throws Exception {
        File uploadsDir = new File(properties.getUploadDir());
        if (!uploadsDir.exists()) {
            uploadsDir.mkdir();
        }
        String fileId = UUID.randomUUID().toString() + "." +
                file.getOriginalFilename().split("\\.")[1];
        file.transferTo(new File(properties.getUploadDir() + fileId));
        model.addAttribute("description",
                description + " ![](/uploads/" + fileId + ")");
    }
}
