package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.dto.CommentDto;
import com.example.electronic_bulletin_board.model.Comments;
import com.example.electronic_bulletin_board.service.CommentsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "API для управления комментариями", description = "Предоставляет методы для работы с комментариями")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    // Добавление комментария
    @PostMapping
    public Comments addComment(@RequestBody CommentDto commentDto) {
        return commentsService.addComment(commentDto);
    }

    // Удаление комментария
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Integer id) {
        commentsService.deleteComment(id);
    }

    // Обновление комментария
    @PutMapping("/{id}")
    public Comments updateComment(@PathVariable Integer id, @RequestBody CommentDto commentDto) {
        return commentsService.updateComment(id, commentDto);
    }

    // Получение комментариев к объявлению
    @GetMapping("/ad/{adsId}")
    public List<Comments> getCommentsByAd(@PathVariable Integer adsId) {
        return commentsService.getCommentsByAd(adsId);
    }

    // Получение комментариев пользователя
    @GetMapping("/user/{userId}")
    public List<Comments> getUserComments(@PathVariable Integer userId) {
        return commentsService.getUserComments(userId);
    }
}