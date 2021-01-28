package id.ten.learnk8s.knotejava.repo;

import id.ten.learnk8s.knotejava.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {
}
