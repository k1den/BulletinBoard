package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.CommentDto;
import com.example.electronic_bulletin_board.model.Ads;
import com.example.electronic_bulletin_board.model.Comments;
import com.example.electronic_bulletin_board.model.Users;
import com.example.electronic_bulletin_board.repository.AdvertisementRepository;
import com.example.electronic_bulletin_board.repository.CommentsRepository;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentsService {

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private AdvertisementRepository adsRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    // Добавление комментария
    public Comments addComment(CommentDto commentDto) {
        checkUserLoggedIn();
        Users user = getCurrentUser();
        Ads ads = adsRepository.findById(commentDto.getAdsId())
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        // Проверяем, что пользователь не комментирует свое объявление
        if (ads.getIdUsers().getId().equals(user.getId())) {
            throw new RuntimeException("Вы не можете комментировать свое объявление");
        }

        Comments comment = new Comments();
        comment.setComment(commentDto.getComment());
        comment.setUser(user);
        comment.setAds(ads);

        return commentsRepository.save(comment);
    }

    // Удаление комментария
    public void deleteComment(Integer commentId) {
        checkUserLoggedIn();
        Users user = getCurrentUser();
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));

        // Проверяем права: может удалить автор комментария или администратор
        if (!user.getRole().equals("Администратор") && !comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Вы не можете удалить этот комментарий");
        }

        commentsRepository.deleteById(commentId);
    }

    // Обновление комментария
    public Comments updateComment(Integer commentId, CommentDto commentDto) {
        checkUserLoggedIn();
        Users user = getCurrentUser();
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));

        // Проверяем, что пользователь является автором комментария
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Вы не можете редактировать этот комментарий");
        }

        comment.setComment(commentDto.getComment());
        return commentsRepository.save(comment);
    }

    // Получение комментариев к объявлению
    public List<Comments> getCommentsByAd(Integer adsId) {
        return commentsRepository.findByAdsId(adsId);
    }

    // Получение комментариев пользователя
    public List<Comments> getUserComments(Integer userId) {
        return commentsRepository.findByUserId(userId);
    }

    private void checkUserLoggedIn() {
        if (!authService.isSessionActive()) {
            throw new RuntimeException("Пользователь не вошел в систему");
        }
    }

    private Users getCurrentUser() {
        String currentLogin = authService.getCurrentLogin();
        return userRepository.findByLogin(currentLogin);
    }
}