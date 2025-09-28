package com.example.kinomanager.model;
import javafx.beans.property.*;
/**
 * Класс представляет модель фильма или сериала в системе "Киноменеджер".
 * Содержит информацию о названии, типе, годе выпуска, жанре, рейтинге,
 * статусе просмотра, прогрессе и общем количестве серий.
 *
 */
public class Movie {
    /**
     * Название фильма или сериала
     */
    private SimpleStringProperty title = new SimpleStringProperty();

    /**
     * Тип контента: "фильм" или "сериал"
     */
    private SimpleStringProperty type = new SimpleStringProperty();

    /**
     * Год выпуска фильма или сериала
     */
    private SimpleIntegerProperty year = new SimpleIntegerProperty();

    /**
     * Жанр фильма или сериала
     */
    private SimpleStringProperty genre = new SimpleStringProperty();

    /**
     * Пользовательский рейтинг от 0 до 10
     */
    private SimpleDoubleProperty userRating = new SimpleDoubleProperty();

    /**
     * Статус просмотра: "Хочу посмотреть", "В процессе", "Просмотрено", "Любимое"
     */
    private SimpleStringProperty status = new SimpleStringProperty();

    /**
     * Прогресс просмотра (количество просмотренных серий)
     */
    private SimpleIntegerProperty progress = new SimpleIntegerProperty();

    /**
     * Общее количество серий (для сериалов)
     */
    private SimpleIntegerProperty totalEpisodes = new SimpleIntegerProperty(1);

    /**
     * Описание фильма или сериала
     */
    private SimpleStringProperty description = new SimpleStringProperty();

    /**
     * Конструктор по умолчанию
     */
    public Movie() {}

    /**
     * Конструктор с параметрами
     *
     * @param title название фильма/сериала
     * @param type тип контента
     * @param year год выпуска
     * @param genre жанр
     * @param userRating пользовательский рейтинг
     * @param status статус просмотра
     * @param progress прогресс просмотра
     */
    public Movie(String title, String type, int year, String genre,
                 double userRating, String status, int progress) {
        this.title.set(title);
        this.type.set(type);
        this.year.set(year);
        this.genre.set(genre);
        this.userRating.set(userRating);
        this.status.set(status);
        this.progress.set(progress);
        this.totalEpisodes.set(1);
    }

    /**
     * Возвращает название фильма/сериала
     *
     * @return название
     */
    public String getTitle() { return title.get(); }

    /**
     * Устанавливает название фильма/сериала
     *
     * @param title название
     */
    public void setTitle(String title) { this.title.set(title); }

    /**
     * Возвращает тип контента
     *
     * @return "фильм" или "сериал"
     */
    public String getType() { return type.get(); }

    /**
     * Устанавливает тип контента
     *
     * @param type "фильм" или "сериал"
     */
    public void setType(String type) { this.type.set(type); }

    /**
     * Возвращает год выпуска
     *
     * @return год выпуска
     */
    public int getYear() { return year.get(); }

    /**
     * Устанавливает год выпуска
     *
     * @param year год выпуска (должен быть между 1900 и 2030)
     */
    public void setYear(int year) { this.year.set(year); }

    /**
     * Возвращает жанр
     *
     * @return жанр
     */
    public String getGenre() { return genre.get(); }

    /**
     * Устанавливает жанр
     *
     * @param genre жанр
     */
    public void setGenre(String genre) { this.genre.set(genre); }

    /**
     * Возвращает пользовательский рейтинг
     *
     * @return рейтинг от 0 до 10
     */
    public double getUserRating() { return userRating.get(); }

    /**
     * Устанавливает пользовательский рейтинг
     *
     * @param userRating рейтинг от 0 до 10
     */
    public void setUserRating(double userRating) { this.userRating.set(userRating); }

    /**
     * Возвращает статус просмотра
     *
     * @return статус просмотра
     */
    public String getStatus() { return status.get(); }

    /**
     * Устанавливает статус просмотра
     *
     * @param status статус просмотра
     */
    public void setStatus(String status) { this.status.set(status); }

    /**
     * Возвращает прогресс просмотра
     *
     * @return количество просмотренных серий
     */
    public int getProgress() { return progress.get(); }

    /**
     * Устанавливает прогресс просмотра.
     * Автоматически обновляет статус при изменении прогресса.
     *
     * @param progress количество просмотренных серий
     * @throws IllegalArgumentException если прогресс превышает общее количество серий
     */
    public void setProgress(int progress) {
        this.progress.set(progress);
    }

    /**
     * Возвращает общее количество серий
     *
     * @return общее количество серий
     */
    public int getTotalEpisodes() { return totalEpisodes.get(); }

    /**
     * Устанавливает общее количество серий.
     * Автоматически корректирует статус при необходимости.
     *
     * @param totalEpisodes общее количество серий
     */
    public void setTotalEpisodes(int totalEpisodes) {
        this.totalEpisodes.set(totalEpisodes);
    }

    /**
     * Возвращает описание
     *
     * @return описание фильма/сериала
     */
    public String getDescription() { return description.get(); }

    /**
     * Устанавливает описание
     *
     * @param description описание фильма/сериала
     */
    public void setDescription(String description) { this.description.set(description); }

    // Property геттеры для JavaFX TableView

    /**
     * Возвращает property для названия (для JavaFX binding)
     *
     * @return property названия
     */
    public SimpleStringProperty titleProperty() { return title; }

    /**
     * Возвращает property для типа (для JavaFX binding)
     *
     * @return property типа
     */
    public SimpleStringProperty typeProperty() { return type; }

    /**
     * Возвращает property для года (для JavaFX binding)
     *
     * @return property года
     */
    public SimpleIntegerProperty yearProperty() { return year; }

    /**
     * Возвращает property для жанра (для JavaFX binding)
     *
     * @return property жанра
     */
    public SimpleStringProperty genreProperty() { return genre; }

    /**
     * Возвращает property для рейтинга (для JavaFX binding)
     *
     * @return property рейтинга
     */
    public SimpleDoubleProperty userRatingProperty() { return userRating; }

    /**
     * Возвращает property для статуса (для JavaFX binding)
     *
     * @return property статуса
     */
    public SimpleStringProperty statusProperty() { return status; }

    /**
     * Возвращает property для прогресса (для JavaFX binding)
     *
     * @return property прогресса
     */
    public SimpleIntegerProperty progressProperty() { return progress; }

    /**
     * Возвращает property для общего количества серий (для JavaFX binding)
     *
     * @return property общего количества серий
     */
    public SimpleIntegerProperty totalEpisodesProperty() { return totalEpisodes; }

    /**
     * Возвращает property для описания (для JavaFX binding)
     *
     * @return property описания
     */
    public SimpleStringProperty descriptionProperty() { return description; }
}