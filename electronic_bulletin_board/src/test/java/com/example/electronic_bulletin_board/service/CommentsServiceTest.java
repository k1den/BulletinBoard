package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.CommentDto;
import com.example.electronic_bulletin_board.entity.Ads;
import com.example.electronic_bulletin_board.entity.Comments;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.AdvertisementRepository;
import com.example.electronic_bulletin_board.repository.CommentsRepository;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentsServiceTest {

    @InjectMocks
    private CommentsService commentsService;

    @Mock
    private CommentsRepository commentsRepository;

    @Mock
    private AdvertisementRepository adsRepository;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    private Users mockUser;
    private Ads mockAd;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new Users();
        mockUser.setId(1);
        mockUser.setLogin("user1");
        mockUser.setRole("Пользователь");

        mockAd = new Ads();
        mockAd.setId(1);
        mockAd.setIdUsers(mockUser);
    }

    // Тест добавления комментария (успешный сценарий)
    @Test
    void testAddComment_Success() {
        CommentDto commentDto = new CommentDto();
        commentDto.setAdsId(1);
        commentDto.setComment("Это комментарий");

        Users user = new Users();
        user.setId(1);
        user.setLogin("user1");

        Ads ad = new Ads();
        ad.setId(1);
        ad.setIdUsers(user);

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(userRepository.findByLogin("user1")).thenReturn(user);
        when(adsRepository.findById(1)).thenReturn(Optional.of(ad));

        assertThrows(RuntimeException.class, () -> {
            commentsService.addComment(commentDto);
        }, "Вы не можете комментировать свое объявление");
    }

    // Тест добавления комментария (пользователь не может комментировать свое объявление)
    @Test
    void testAddComment_SameUserCannotComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setAdsId(1);
        commentDto.setComment("Это комментарий");

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(userRepository.findByLogin("user1")).thenReturn(mockUser);
        when(adsRepository.findById(1)).thenReturn(Optional.of(mockAd));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentsService.addComment(commentDto);
        });

        assertEquals("Вы не можете комментировать свое объявление", exception.getMessage());
    }

    // Тест удаления комментария (успешный сценарий)
    @Test
    void testDeleteComment_Success() {
        Comments mockComment = new Comments();
        mockComment.setId(1);
        mockComment.setUser(mockUser);
        mockComment.setComment("Комментарий для удаления");

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(userRepository.findByLogin("user1")).thenReturn(mockUser);
        when(commentsRepository.findById(1)).thenReturn(Optional.of(mockComment));

        commentsService.deleteComment(1);
        verify(commentsRepository).deleteById(1);
    }

    // Тест удаления комментария (комментарий не найден)
    @Test
    void testDeleteComment_CommentNotFound() {
        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(userRepository.findByLogin("user1")).thenReturn(mockUser);
        when(commentsRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentsService.deleteComment(1);
        });

        assertEquals("Комментарий не найден", exception.getMessage());
    }

    // Тест удаления комментария (пользователь не авторизован)
    @Test
    void testDeleteComment_UserNotAuthorized() {
        Comments mockComment = new Comments();
        mockComment.setId(1);
        Users anotherUser = new Users();
        anotherUser.setId(2);
        mockComment.setUser(anotherUser);

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(userRepository.findByLogin("user1")).thenReturn(mockUser);
        when(commentsRepository.findById(1)).thenReturn(Optional.of(mockComment));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentsService.deleteComment(1);
        });

        assertEquals("Вы не можете удалить этот комментарий", exception.getMessage());
    }

    // Тест обновления комментария (успешный сценарий)
    @Test
    void testUpdateComment_Success() {
        Comments mockComment = new Comments();
        mockComment.setId(1);
        mockComment.setUser(mockUser);
        mockComment.setComment("Старый комментарий");

        CommentDto commentDto = new CommentDto();
        commentDto.setComment("Обновленный комментарий");

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(userRepository.findByLogin("user1")).thenReturn(mockUser);
        when(commentsRepository.findById(1)).thenReturn(Optional.of(mockComment));
        when(commentsRepository.save(any(Comments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comments updatedComment = commentsService.updateComment(1, commentDto);

        assertEquals("Обновленный комментарий", updatedComment.getComment());
        verify(commentsRepository).save(mockComment);
    }

    // Тест обновления комментария (пользователь не авторизован)
    @Test
    void testUpdateComment_UserNotAuthorized() {
        Comments mockComment = new Comments();
        mockComment.setId(1);
        Users anotherUser = new Users();
        anotherUser.setId(2); // Идентификатор другого пользователя
        mockComment.setUser(anotherUser); // Комментарий принадлежит другому пользователю

        CommentDto commentDto = new CommentDto();
        commentDto.setComment("Обновленный комментарий");

        when(authService.isSessionActive()).thenReturn(true);
        when(authService.getCurrentLogin()).thenReturn("user1");
        when(userRepository.findByLogin("user1")).thenReturn(mockUser);
        when(commentsRepository.findById(1)).thenReturn(Optional.of(mockComment));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentsService.updateComment(1, commentDto);
        });

        assertEquals("Вы не можете редактировать этот комментарий", exception.getMessage());
    }


}
