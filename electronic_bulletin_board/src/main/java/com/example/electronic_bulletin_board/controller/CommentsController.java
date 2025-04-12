package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.dto.CommentDto;
import com.example.electronic_bulletin_board.entity.Comments;
import com.example.electronic_bulletin_board.service.CommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Комментарии", description = "API для управления комментариями")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    @PostMapping
    @Operation(summary = "Добавить комментарий", description = "Создает новый комментарий к объявлению")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Комментарий успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<Comments> addComment(@RequestBody CommentDto commentDto) {
        try {
            Comments newComment = commentsService.addComment(commentDto);
            return new ResponseEntity<>(newComment, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить комментарий", description = "Удаляет комментарий по его уникальному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Комментарий успешно удален"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден")
    })
    public ResponseEntity<?> deleteComment(
            @Parameter(description = "ID комментария", example = "1", required = true)
            @PathVariable Integer id) {
        try {
            commentsService.deleteComment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить комментарий", description = "Обновляет существующий комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<Comments> updateComment(
            @Parameter(description = "ID комментария", example = "1", required = true)
            @PathVariable Integer id,
            @RequestBody CommentDto commentDto) {
        try {
            Comments updatedComment = commentsService.updateComment(id, commentDto);
            return ResponseEntity.ok(updatedComment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/my-comments")
    @Operation(summary = "Получить мои комментарии", description = "Возвращает список комментариев текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев успешно получен"),
            @ApiResponse(responseCode = "403", description = "Пользователь не авторизован")
    })
    public ResponseEntity<List<Comments>> getCurrentUserComments() {
        try {
            List<Comments> userComments = commentsService.getCurrentUserComments();
            return ResponseEntity.ok(userComments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
}
