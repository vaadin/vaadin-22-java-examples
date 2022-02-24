package com.example.application.data.service;

import com.example.application.data.entity.SampleBook;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SampleBookService {

    private SampleBookRepository repository;

    public SampleBookService(@Autowired SampleBookRepository repository) {
        this.repository = repository;
    }

    public Optional<SampleBook> get(UUID id) {
        return repository.findById(id);
    }

    public SampleBook update(SampleBook entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<SampleBook> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
