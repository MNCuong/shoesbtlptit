package com.example.shoes_store.Service;

import com.example.shoes_store.Repo.SupperlieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupperlieService {
    @Autowired
    private SupperlieRepo supperlieRepo;
}
