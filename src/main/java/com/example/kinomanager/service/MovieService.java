package com.example.kinomanager.service;
import com.example.kinomanager.dao.CsvMovieDao;
import com.example.kinomanager.dao.MovieDao;
import com.example.kinomanager.dao.PostgresMovieDao;
import com.example.kinomanager.model.Movie;
import java.io.IOException;
import java.util.List;

public class MovieService {
    private MovieDao movieDao;
    private TmdbService tmdbService;
    private DataSourceType currentDataSource = DataSourceType.CSV;
    public enum DataSourceType {
        CSV, POSTGRES
    }
    public MovieService() {
        this.movieDao = new CsvMovieDao();
        this.tmdbService = new TmdbService();
    }
    // Конструктор для тестирования
    public MovieService(MovieDao movieDao, TmdbService tmdbService) {
        this.movieDao = movieDao;
        this.tmdbService = tmdbService;
    }
    public void setDataSource(DataSourceType dataSource) {
        this.currentDataSource = dataSource;
        switch (dataSource) {
            case CSV:
                this.movieDao = new CsvMovieDao();
                break;
            case POSTGRES:
                this.movieDao = new PostgresMovieDao();
                break;
        }
        System.out.println("Источник данных изменен на: " + dataSource);
    }
    public DataSourceType getCurrentDataSource() {
        return currentDataSource;
    }

    // CRUD операции
    public List<Movie> getAllMovies() {
        return movieDao.getAllMovies();
    }

    public void addMovie(Movie movie) {
        validateMovie(movie);
        updateStatusAutomatically(movie);
        movieDao.addMovie(movie);
    }

    public boolean movieExists(String title, int year) {
        return movieDao.movieExists(title, year);
    }
    public void updateMovie(Movie movie) {
        validateMovie(movie);
        updateStatusAutomatically(movie);
        movieDao.updateMovie(movie);
    }

    public void deleteMovie(String title, int year) {
        movieDao.deleteMovie(title, year);
    }

    // Поиск и фильтрация
    public List<Movie> searchMovies(String query) {
        return movieDao.searchMovies(query);
    }

    public List<Movie> filterByType(String type) {
        return movieDao.findByType(type);
    }

    public List<Movie> filterByStatus(String status) {
        return movieDao.findByStatus(status);
    }

    // Сортировка
    public List<Movie> sortByRating() {
        List<Movie> all = getAllMovies();
        return all.stream()
                .sorted((m1, m2) -> Double.compare(m2.getUserRating(), m1.getUserRating()))
                .toList();
    }

    public List<Movie> sortByYear() {
        List<Movie> all = getAllMovies();
        return all.stream()
                .sorted((m1, m2) -> Integer.compare(m2.getYear(), m1.getYear()))
                .toList();
    }

    // TMDb поиск (изменено с IMDb)
    public Movie searchTmdbByTitle(String title) throws IOException {
        return tmdbService.searchMovieByTitle(title);
    }

    public Movie searchTmdbById(int tmdbId) throws IOException {
        return tmdbService.searchMovieById(tmdbId);
    }

    // Автоматическое управление статусами на основе серий
    private void updateStatusAutomatically(Movie movie) {
        if ("Любимое".equals(movie.getStatus())) {
            return;
        }
        if ("сериал".equals(movie.getType())) {
            if (movie.getProgress() > 0 && movie.getProgress() < movie.getTotalEpisodes()) {
                movie.setStatus("В процессе");
            } else if (movie.getProgress() >= movie.getTotalEpisodes() && movie.getTotalEpisodes() > 0) {
                movie.setStatus("Просмотрено");
            } else if (movie.getProgress() == 0) {
                movie.setStatus("Хочу посмотреть");
            }
        } else {
            if (movie.getProgress() > 0) {
                movie.setStatus("Просмотрено");
            } else {
                movie.setStatus("Хочу посмотреть");
            }
        }
    }

    // Валидация
    private void validateMovie(Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Введите название фильма");
        }
        if (movie.getType() == null || (!movie.getType().equals("фильм") && !movie.getType().equals("сериал"))) {
            throw new IllegalArgumentException("Тип должен быть 'фильм' или 'сериал'");
        }
        if (movie.getYear() < 1900 || movie.getYear() > 2030) {
            throw new IllegalArgumentException("Год должен быть между 1900 и 2030");
        }
        if (movie.getUserRating() < 0 || movie.getUserRating() > 10) {
            throw new IllegalArgumentException("Рейтинг должен быть от 0 до 10");
        }
        if ("сериал".equals(movie.getType()) && movie.getTotalEpisodes() <= 0) {
            throw new IllegalArgumentException("Для сериала укажите общее количество серий");
        }
    }

    // Статистика
    public int getTotalMovies() {
        return getAllMovies().size();
    }

    public int getMoviesByStatus(String status) {
        return filterByStatus(status).size();
    }
}
