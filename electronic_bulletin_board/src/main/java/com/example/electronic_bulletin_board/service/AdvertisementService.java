package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.AdvertisementDto;
import com.example.electronic_bulletin_board.model.Ads;
import com.example.electronic_bulletin_board.model.Categories;
import com.example.electronic_bulletin_board.model.Users;
import com.example.electronic_bulletin_board.repository.AdvertisementRepository;
import com.example.electronic_bulletin_board.repository.CategoryRepository;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class AdvertisementService {

    @Autowired
    private AdvertisementRepository adsRepository;

    @Autowired
    private CategoryRepository categoriesRepository;

    @Autowired
    private UserRepository usersRepository;

    @Autowired
    private AuthService authService; // Добавляем AuthService

    // Добавление объявления
    public Ads addAdvertisement(AdvertisementDto advertisementDto) {
        checkUserLoggedIn(); // Проверяем, вошел ли пользователь в систему

        // Получаем текущего пользователя
        Users user = getCurrentUser();

        // Проверяем, что пользователь не является "Гостем"
        if ("Гость".equals(user.getRole())) {
            throw new RuntimeException("Гости не могут создавать объявления");
        }

        // Создаем объявление
        Ads ads = new Ads();
        ads.setTitle(advertisementDto.getTitle());
        ads.setDescription(advertisementDto.getDescription());
        ads.setPrice(advertisementDto.getPrice());
        ads.setDate(new Date()); // Автоматически устанавливаем текущую дату
        ads.setPhoto(advertisementDto.getPhoto());

        // Устанавливаем категорию
        Categories category = categoriesRepository.findById(advertisementDto.getIdCategory())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        ads.setIdCategory(category);

        // Устанавливаем пользователя
        ads.setIdUsers(user);

        return adsRepository.save(ads);
    }

    // Удаление объявления
    public void deleteAdvertisement(Integer id) {
        checkUserLoggedIn(); // Проверяем, вошел ли пользователь в систему

        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        // Получаем текущего пользователя
        Users currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!currentUser.getRole().equals("Администратор") && !ads.getIdUsers().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Вы не можете удалить это объявление");
        }

        adsRepository.deleteById(id);
    }

    // Обновление объявления
    public Ads updateAdvertisement(Integer id, AdvertisementDto advertisementDto) {
        checkUserLoggedIn(); // Проверяем, вошел ли пользователь в систему

        Ads existingAds = adsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        // Получаем текущего пользователя
        Users currentUser = getCurrentUser();

        // Проверяем права доступа
        if (!currentUser.getRole().equals("Администратор") && !existingAds.getIdUsers().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Вы не можете редактировать это объявление");
        }

        existingAds.setTitle(advertisementDto.getTitle());
        existingAds.setDescription(advertisementDto.getDescription());
        existingAds.setPrice(advertisementDto.getPrice());
        existingAds.setPhoto(advertisementDto.getPhoto());

        Categories category = categoriesRepository.findById(advertisementDto.getIdCategory())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        existingAds.setIdCategory(category);

        return adsRepository.save(existingAds);
    }

    // Получение объявления по ID
    public Ads getAdvertisementById(Integer id) {
        return adsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));
    }

    // Получение объявлений по категории
    public List<Ads> getAdvertisementsByCategoryTitle(String categoryName) {
        return adsRepository.findByIdCategory_Title(categoryName);
    }

    // Вспомогательный метод для проверки, вошел ли пользователь в систему
    private void checkUserLoggedIn() {
        if (!authService.isSessionActive()) {
            throw new RuntimeException("Пользователь не вошел в систему");
        }
    }

    // Вспомогательный метод для получения текущего пользователя
    private Users getCurrentUser() {
        String currentLogin = authService.getCurrentLogin();
        return usersRepository.findByLogin(currentLogin);
    }
}