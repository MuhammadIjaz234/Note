package com.app.note_app.controller;

import com.app.note_app.bean.Note;
import com.app.note_app.config.NoteProperties;
import com.app.note_app.repository.NotesRepository;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
public class NoteController {
    @Autowired
    private NotesRepository notesRepository;
    @Autowired
    private NoteProperties properties;
    private Parser parser = Parser.builder().build();
    private HtmlRenderer renderer = HtmlRenderer.builder().build();
    @GetMapping("/")
    public String index(Model model) {
        getAllNotes(model);
        return "index";
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
        // After save fetch all notes again
        if (upload != null && upload.equals("Upload")) {
            if (file != null && file.getOriginalFilename() != null &&
                    !file.getOriginalFilename().isEmpty()) {
                uploadImage(file, description, model);
            }
            getAllNotes(model);
            return "index";
        }
        return "index";
    }

    private void getAllNotes(Model model) {
        List<Note> notes = notesRepository.findAll();
        Collections.reverse(notes);
        model.addAttribute("notes", notes);
    }
    private void saveNote(String description, Model model) {
        if (description != null && !description.trim().isEmpty()) {
            Node document = parser.parse(description.trim());
            String html = renderer.render(document);
            notesRepository.save(new Note(null, html));
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
