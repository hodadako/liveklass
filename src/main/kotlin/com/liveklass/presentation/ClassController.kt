package com.liveklass.presentation

import com.liveklass.application.ClassUseCase
import org.springframework.web.bind.annotation.RestController

@RestController
class ClassController(
    private val classUseCase: ClassUseCase
)
