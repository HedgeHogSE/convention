package ru.neoflex.convention.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.neoflex.convention.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}

