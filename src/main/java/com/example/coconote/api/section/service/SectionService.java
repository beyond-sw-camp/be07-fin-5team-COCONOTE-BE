package com.example.coconote.api.section.service;

import com.example.coconote.api.section.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class SectionService {

    private final SectionRepository sectionRepository;
    @Autowired
    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }
}
