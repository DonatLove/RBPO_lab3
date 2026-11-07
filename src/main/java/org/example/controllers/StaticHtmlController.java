package org.example.controllers;

import org.example.annotation.Cumponent;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@Cumponent
@RestController
public class StaticHtmlController {
    private static final MediaType HTML_UTF8 =
            MediaType.parseMediaType("text/html;charset=UTF-8");

    private static final String HTML_PAGE = """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <title>Бюро находок</title>
            </head>
            <body>
                <h1>Бюро находок</h1>
                <p>Запрос успешно обработан.</p>
            </body>
            </html>
            """;

    @PostMapping(value = "/api/static-html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> handlePost() {
        return ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .body(HTML_PAGE);
    }

    @PutMapping(value = "/api/static-html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> handlePut() {
        return ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .body(HTML_PAGE);
    }
}
