package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.AdvertisementDto;
import com.example.electronic_bulletin_board.entity.Ads;
import com.example.electronic_bulletin_board.entity.Categories;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.AdvertisementRepository;
import com.example.electronic_bulletin_board.repository.CategoryRepository;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
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
    private AuthService authService;

    // Добавление объявления
    public Ads addAdvertisement(AdvertisementDto advertisementDto) {
        checkUserLoggedIn();

        Users user = getCurrentUser();

        if ("Гость".equals(user.getRole())) {
            throw new RuntimeException("Гости не могут создавать объявления");
        }

        Ads ads = new Ads();
        ads.setTitle(advertisementDto.getTitle());
        ads.setDescription(advertisementDto.getDescription());
        ads.setPrice(advertisementDto.getPrice());
        ads.setDate(new Date());

        // Установка фотографии только если она не null
        if (advertisementDto.getPhoto() != null) {
            ads.setPhoto(advertisementDto.getPhoto());
        }

        Categories category = categoriesRepository.findById(advertisementDto.getIdCategory())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        ads.setIdCategory(category);
        ads.setIdUsers(user);

        return adsRepository.save(ads);
    }

    // Удаление объявления
    public void deleteAdvertisement(Integer id) {
        checkUserLoggedIn(); // Вошел ли пользователь в систему

        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        Users currentUser = getCurrentUser(); // Получение текущего пользователя

        // Проверка прав доступа
        if (!currentUser.getRole().equals("Администратор") && !ads.getIdUsers().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Вы не можете удалить это объявление");
        }

        adsRepository.deleteById(id);
    }

    // Обновление объявления
    public Ads updateAdvertisement(Integer id, AdvertisementDto advertisementDto) {
        checkUserLoggedIn(); // Вошел ли пользователь в систему

        Ads existingAds = adsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

        Users currentUser = getCurrentUser(); // Получение текущего пользователя

        // Проверка права доступа
        if (!currentUser.getRole().equals("Администратор") && !existingAds.getIdUsers().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Вы не можете редактировать это объявление");
        }

        existingAds.setTitle(advertisementDto.getTitle());
        existingAds.setDescription(advertisementDto.getDescription());
        existingAds.setPrice(advertisementDto.getPrice());

        // Обновление изображения только если оно было загружено
        if (advertisementDto.getPhoto() != null) {
            existingAds.setPhoto(advertisementDto.getPhoto());
        }

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

    // Получение всех объявлений
    public List<Ads> getAllAdvertisements() {
        return adsRepository.findAll();
    }

    // Вспомогательный метод для проверки, вошел ли пользователь в систему
    private void checkUserLoggedIn() {
        if (!authService.isSessionActive()) {
            throw new RuntimeException("Пользователь не вошел в систему");
        }
    }

    // Вспомогательный метод для получения текущего пользователя
    public Users getCurrentUser() {
        String currentLogin = authService.getCurrentLogin();
        return usersRepository.findByLogin(currentLogin);
    }

    public static byte[] compressImage(MultipartFile file, float quality) throws IOException {
        // Чтение изображения из MultipartFile
        BufferedImage image = ImageIO.read(file.getInputStream());

        // Создание ByteArrayOutputStream для хранения сжатого изображения
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);

        // Получение ImageWriter для формата JPEG
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();

        // Настройка параметров сжатия
        writer.setOutput(imageOutputStream);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality); // Установка качества сжатия (0.05f = 5%)

        // Запись сжатого изображения
        writer.write(null, new IIOImage(image, null, null), param);

        // Освобождение ресурсов
        writer.dispose();
        imageOutputStream.close();
        byteArrayOutputStream.close();

        // Возвращение сжатого изображения в виде массива байтов
        return byteArrayOutputStream.toByteArray();
    }

    public boolean isAdOwner(Integer adId, Integer userId) {
        Ads ad = adsRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено"));
        return ad.getIdUsers().getId().equals(userId);
    }

    public List<Ads> searchAdvertisementsByTitle(String title) {
        return adsRepository.findByTitleContainingIgnoreCase(title);
    }
}