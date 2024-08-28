package com.app.note_app.repository;

import com.app.note_app.bean.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotesRepository extends MongoRepository<Note, String> {


}
