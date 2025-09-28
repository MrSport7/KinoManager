package com.example.kinomanager.controller;
import com.example.kinomanager.model.Movie;
import com.example.kinomanager.service.MovieService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.*;
import java.util.List;
import java.util.Optional;

public class Controller {

    @FXML private TableView<Movie> moviesTable;
    @FXML private TableColumn<Movie, String> titleColumn;
    @FXML private TableColumn<Movie, String> typeColumn;
    @FXML private TableColumn<Movie, Integer> yearColumn;
    @FXML private TableColumn<Movie, String> genreColumn;
    @FXML private TableColumn<Movie, Double> ratingColumn;
    @FXML private TableColumn<Movie, String> statusColumn;
    @FXML private TableColumn<Movie, String> progressColumn;
    @FXML private TableColumn<Movie, String> descriptionColumn;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField yearField;
    @FXML private TextField genreField;
    @FXML private Slider ratingSlider;
    @FXML private Label ratingLabel;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField progressField;
    @FXML private TextField totalEpisodesField;
    @FXML private TextField searchField;
    @FXML private Label totalCountLabel;
    @FXML private Label moviesCountLabel;
    @FXML private Label seriesCountLabel;
    @FXML private Label watchedCountLabel;
    @FXML private TextField tmdbSearchField;
    @FXML private ComboBox<String> dataSourceComboBox;
    @FXML private TextArea descriptionField;

    private MovieService movieService = new MovieService();
    private Movie selectedMovie;
    private String originalTitle;
    private int originalYear;

    @FXML
    public void initialize() {
        setupTable();
        setupComboBoxes();
        setupSlider();
        setupListeners();
        loadMovies();
        setupTableSelection();
        updateStatistics();
        setupDataSourceComboBox();
    }

    private void setupDataSourceComboBox() {
        dataSourceComboBox.getItems().addAll("CSV файл", "PostgreSQL");
        dataSourceComboBox.setValue("CSV файл");
        dataSourceComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("CSV файл".equals(newVal)) {
                movieService.setDataSource(MovieService.DataSourceType.CSV);
            } else if ("PostgreSQL".equals(newVal)) {
                movieService.setDataSource(MovieService.DataSourceType.POSTGRES);
            }
            loadMovies();
        });
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        yearColumn.setCellValueFactory(cellData -> cellData.getValue().yearProperty().asObject());
        genreColumn.setCellValueFactory(cellData -> cellData.getValue().genreProperty());
        ratingColumn.setCellValueFactory(cellData -> cellData.getValue().userRatingProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        // Отображение прогресса - только серии
        progressColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            if ("сериал".equals(movie.getType())) {
                return new javafx.beans.property.SimpleStringProperty(
                        String.format("%d/%d серий", movie.getProgress(), movie.getTotalEpisodes())
                );
            } else {
                return new javafx.beans.property.SimpleStringProperty(
                        movie.getProgress() > 0 ? "Просмотрено" : "Не просмотрено"
                );
            }
        });
        descriptionColumn.setCellFactory(column -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    String shortDescription = item.length() > 50 ? item.substring(0, 50) + "..." : item;
                    setText(shortDescription);
                    Tooltip tooltip = new Tooltip(item);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(300);
                    setTooltip(tooltip);
                }
            }
        });
    }

    private void setupComboBoxes() {
        typeComboBox.getItems().addAll("фильм", "сериал");
        statusComboBox.getItems().addAll("Хочу посмотреть", "В процессе", "Просмотрено", "Любимое");
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("сериал".equals(newVal)) {
                totalEpisodesField.setDisable(false);
                progressField.setPromptText("Просмотрено серий");
                totalEpisodesField.setPromptText("Всего серий");
                progressField.setText("0");
                totalEpisodesField.setText("10");
                autoUpdateStatusBasedOnProgress();
            } else {
                totalEpisodesField.setDisable(true);
                progressField.setPromptText("0 - не просмотрено, 1 - просмотрено");
                progressField.setText("0");
                totalEpisodesField.setText("1");
                autoUpdateStatusBasedOnProgress();
            }
        });
    }

    private void setupSlider() {
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingLabel.setText(String.format("%.1f", newVal));
        });
    }

    private void setupListeners() {
        progressField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.equals(oldVal)) {
                autoUpdateStatusBasedOnProgress();
            }
        });

        totalEpisodesField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.equals(oldVal)) {
                autoUpdateStatusBasedOnProgress();
            }
        });
    }

    private void autoUpdateStatusBasedOnProgress() {
        if ("Любимое".equals(statusComboBox.getValue())) {
            return;
        }
        try {
            int progress = progressField.getText().isEmpty() ? 0 : Integer.parseInt(progressField.getText());
            String type = typeComboBox.getValue();
            int totalEpisodes = totalEpisodesField.getText().isEmpty() ? 1 : Integer.parseInt(totalEpisodesField.getText());

            if (type != null) {
                if ("сериал".equals(type)) {
                    if (progress == 0) {
                        statusComboBox.setValue("Хочу посмотреть");
                    } else if (progress > 0 && progress < totalEpisodes) {
                        statusComboBox.setValue("В процессе");
                    } else if (progress >= totalEpisodes && totalEpisodes > 0) {
                        statusComboBox.setValue("Просмотрено");
                    }
                } else {
                    if (progress == 0) {
                        statusComboBox.setValue("Хочу посмотреть");
                    } else if (progress >= 1) {
                        statusComboBox.setValue("Просмотрено");
                    }
                }
            }
        } catch (NumberFormatException e) {
        }
    }

    private void setupTableSelection() {
        moviesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedMovie = newSelection;
                    if (newSelection != null) {
                        fillFormWithMovie(newSelection);
                    } else {
                        clearForm();
                    }
                });
    }

    private void fillFormWithMovie(Movie movie) {
        titleField.setText(movie.getTitle());
        typeComboBox.setValue(movie.getType());
        yearField.setText(String.valueOf(movie.getYear()));
        genreField.setText(movie.getGenre());
        ratingSlider.setValue(movie.getUserRating());
        statusComboBox.setValue(movie.getStatus());
        progressField.setText(String.valueOf(movie.getProgress()));
        totalEpisodesField.setText(String.valueOf(movie.getTotalEpisodes()));
        descriptionField.setText(movie.getDescription());
        originalTitle = movie.getTitle();
        originalYear = movie.getYear();
        if ("сериал".equals(movie.getType())) {
            totalEpisodesField.setDisable(false);
        } else {
            totalEpisodesField.setDisable(true);
        }
    }

    private void loadMovies() {
        moviesTable.setItems(FXCollections.observableArrayList(movieService.getAllMovies()));
        updateStatistics();
    }

    private void updateStatistics() {
        List<Movie> allMovies = movieService.getAllMovies();
        int total = allMovies.size();
        int moviesCount = (int) allMovies.stream().filter(m -> "фильм".equals(m.getType())).count();
        int seriesCount = total - moviesCount;
        int watchedCount = (int) allMovies.stream().filter(m -> "Просмотрено".equals(m.getStatus())).count();
        totalCountLabel.setText(String.valueOf(total));
        moviesCountLabel.setText(String.valueOf(moviesCount));
        seriesCountLabel.setText(String.valueOf(seriesCount));
        watchedCountLabel.setText(String.valueOf(watchedCount));
    }

    // CRUD операции
    @FXML
    private void handleAddMovie() {
        try {
            Movie movie = createMovieFromForm();
            // Проверка на дубликаты
            if (movieService.movieExists(movie.getTitle(), movie.getYear())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Дубликат");
                alert.setHeaderText("Фильм уже существует");
                alert.setContentText("Фильм с названием '" + movie.getTitle() + "' и годом " + movie.getYear() + " уже существует. Добавить дубликат?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return;
                }
            }
            movieService.addMovie(movie);
            loadMovies();
            clearForm();
            showInfo("Фильм '" + movie.getTitle() + "' добавлен!");
        } catch (Exception e) {
            showError("Ошибка добавления: " + e.getMessage());
        }
    }
    @FXML
    private void handleUpdateMovie() {
        if (selectedMovie != null) {
            try {
                movieService.deleteMovie(originalTitle, originalYear);
                Movie updatedMovie = createMovieFromForm();
                movieService.addMovie(updatedMovie);
                loadMovies();
                showInfo("Фильм '" + updatedMovie.getTitle() + "' обновлен!");
            } catch (Exception e) {
                showError("Ошибка обновления: " + e.getMessage());
            }
        } else {
            showError("Выберите фильм для редактирования");
        }
    }
    @FXML
    private void handleDeleteMovie() {
        if (selectedMovie != null) {
            String title = selectedMovie.getTitle();
            int year = selectedMovie.getYear();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Удаление фильма");
            alert.setContentText("Вы уверены, что хотите удалить \"" + title + "\"?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                movieService.deleteMovie(title, year);
                loadMovies();
                clearForm();
                showInfo("Фильм '" + title + "' удален!");
            }
        } else {
            showError("Выберите фильм для удаления");
        }
    }
    // Поиск и фильтрация
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            List<Movie> results = movieService.searchMovies(query);
            moviesTable.setItems(FXCollections.observableArrayList(results));
            updateSearchStatistics(results);
        } else {
            loadMovies();
        }
    }
    @FXML
    private void handleSearchTmdb() {
        String query = tmdbSearchField.getText().trim();
        if (query.isEmpty()) {
            showError("Введите название фильма или TMDb ID");
            return;
        }
        try {
            Movie movie;
            if (query.matches("\\d+")) {
                movie = movieService.searchTmdbById(Integer.parseInt(query));
            } else {
                movie = movieService.searchTmdbByTitle(query);
            }
            if (movie != null) {
                if (movie.getGenre() != null && movie.getGenre().contains(",")) {
                    movie.setGenre(movie.getGenre().replace(",", ";"));
                }
                fillFormWithTmdbMovie(movie);
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Добавить фильм?");
                confirmAlert.setHeaderText("Фильм найден: " + movie.getTitle() + " (" + movie.getYear() + ")");
                confirmAlert.setContentText("Жанр: " + movie.getGenre() + "\n\nДобавить в список автоматически?");
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    movieService.addMovie(movie);
                    loadMovies();
                    showInfo("Фильм/Сериал '" + movie.getTitle() + "' добавлен из TMDb!");
                    clearForm();
                } else {
                    showInfo("Данные загружены в форму. Нажмите 'Добавить' для сохранения.");
                }
            } else {
                showError("Фильм/Сериал не найден в TMDb.");
            }
        } catch (IOException e) {
            showError("Ошибка сети: Проверьте подключение к интернету или включите впн и попробуйте снова.");
        } catch (Exception e) {
            showError("Ошибка при поиске в TMDb: " + e.getMessage());
        }
    }

    private void fillFormWithTmdbMovie(Movie movie) {
        titleField.setText(movie.getTitle());
        typeComboBox.setValue(movie.getType());
        yearField.setText(String.valueOf(movie.getYear()));
        genreField.setText(movie.getGenre());
        ratingSlider.setValue(0.0);
        statusComboBox.setValue("Хочу посмотреть");
        progressField.setText("0");
        descriptionField.setText(movie.getDescription());
        if ("сериал".equals(movie.getType())) {
            totalEpisodesField.setDisable(false);
            totalEpisodesField.setText(String.valueOf(movie.getTotalEpisodes()));
        } else {
            totalEpisodesField.setDisable(true);
            totalEpisodesField.setText("1");
        }
        originalTitle = movie.getTitle();
        originalYear = movie.getYear();
    }

    private void updateSearchStatistics(List<Movie> results) {
        int total = results.size();
        int moviesCount = (int) results.stream().filter(m -> "фильм".equals(m.getType())).count();
        int seriesCount = total - moviesCount;
        int watchedCount = (int) results.stream().filter(m -> "Просмотрено".equals(m.getStatus())).count();
        totalCountLabel.setText(total + " (найдено)");
        moviesCountLabel.setText(String.valueOf(moviesCount));
        seriesCountLabel.setText(String.valueOf(seriesCount));
        watchedCountLabel.setText(String.valueOf(watchedCount));
    }
    @FXML
    private void handleFilterMovies() {
        List<Movie> movies = movieService.filterByType("фильм");
        moviesTable.setItems(FXCollections.observableArrayList(movies));
        updateSearchStatistics(movies);
    }

    @FXML
    private void handleFilterSeries() {
        List<Movie> series = movieService.filterByType("сериал");
        moviesTable.setItems(FXCollections.observableArrayList(series));
        updateSearchStatistics(series);
    }

    @FXML
    private void handleFilterWatched() {
        List<Movie> watched = movieService.filterByStatus("Просмотрено");
        moviesTable.setItems(FXCollections.observableArrayList(watched));
        updateSearchStatistics(watched);
    }

    @FXML
    private void handleFilterInProgress() {
        List<Movie> inProgress = movieService.filterByStatus("В процессе");
        moviesTable.setItems(FXCollections.observableArrayList(inProgress));
        updateSearchStatistics(inProgress);
    }

    @FXML
    private void handleFilterPlanned() {
        List<Movie> planned = movieService.filterByStatus("Хочу посмотреть");
        moviesTable.setItems(FXCollections.observableArrayList(planned));
        updateSearchStatistics(planned);
    }

    @FXML
    private void handleFilterFavorites() {
        List<Movie> favorites = movieService.filterByStatus("Любимое");
        moviesTable.setItems(FXCollections.observableArrayList(favorites));
        updateSearchStatistics(favorites);
    }

    @FXML
    private void handleShowAll() {
        searchField.clear();
        loadMovies();
    }
    // Сортировка
    @FXML
    private void handleSortByRating() {
        moviesTable.setItems(FXCollections.observableArrayList(movieService.sortByRating()));
    }

    @FXML
    private void handleSortByYear() {
        moviesTable.setItems(FXCollections.observableArrayList(movieService.sortByYear()));
    }

    // Дополнительные функции
    @FXML
    private void handleShowStatistics() {
        List<Movie> allMovies = movieService.getAllMovies();
        long watchedCount = allMovies.stream().filter(m -> "Просмотрено".equals(m.getStatus())).count();
        long inProgressCount = allMovies.stream().filter(m -> "В процессе".equals(m.getStatus())).count();
        long plannedCount = allMovies.stream().filter(m -> "Хочу посмотреть".equals(m.getStatus())).count();
        long favoritesCount = allMovies.stream().filter(m -> "Любимое".equals(m.getStatus())).count();
        double avgRating = allMovies.stream().mapToDouble(Movie::getUserRating).average().orElse(0.0);
        long totalSeries = allMovies.stream().filter(m -> "сериал".equals(m.getType())).count();
        long totalEpisodesWatched = allMovies.stream()
                .filter(m -> "сериал".equals(m.getType()))
                .mapToInt(Movie::getProgress)
                .sum();
        long totalEpisodes = allMovies.stream()
                .filter(m -> "сериал".equals(m.getType()))
                .mapToInt(Movie::getTotalEpisodes)
                .sum();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Статистика");
        alert.setHeaderText("Общая статистика коллекции");
        alert.setContentText(
                String.format("Всего записей: %d\n\n" +
                                "Фильмы: %d\nСериалы: %d\n\n" +
                                "Просмотрено: %d\nВ процессе: %d\nЗапланировано: %d\nЛюбимые: %d\n\n" +
                                "Средний рейтинг: %.1f\n\n" +
                                "Статистика сериалов:\nПросмотрено серий: %d/%d (%.1f%%)",
                        allMovies.size(),
                        allMovies.stream().filter(m -> "фильм".equals(m.getType())).count(),
                        totalSeries,
                        watchedCount, inProgressCount, plannedCount, favoritesCount, avgRating,
                        totalEpisodesWatched, totalEpisodes,
                        totalEpisodes > 0 ? (totalEpisodesWatched * 100.0 / totalEpisodes) : 0)
        );
        alert.show();
    }
    private String escapeCsv(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            field = field.replace("\"", "\"\"");
            field = "\"" + field + "\"";
        }
        return field;
    }
    @FXML
    private void handleClearForm() {
        clearForm();
        showInfo("Форма очищена");
    }
    @FXML
    private void handleIncrementProgress() {
        try {
            int currentProgress = progressField.getText().isEmpty() ? 0 : Integer.parseInt(progressField.getText());
            int totalEpisodes = totalEpisodesField.getText().isEmpty() ? 1 : Integer.parseInt(totalEpisodesField.getText());
            if (currentProgress < totalEpisodes) {
                progressField.setText(String.valueOf(currentProgress + 1));
                autoUpdateStatusBasedOnProgress();
                showInfo("Прогресс увеличен на 1 серию");
            } else {
                showInfo("Достигнут максимум серий");
            }
        } catch (NumberFormatException e) {
            showError("Введите корректное значение прогресса");
        }
    }
    @FXML
    private void handleDecrementProgress() {
        try {
            int currentProgress = progressField.getText().isEmpty() ? 0 : Integer.parseInt(progressField.getText());
            if (currentProgress > 0) {
                progressField.setText(String.valueOf(currentProgress - 1));
                autoUpdateStatusBasedOnProgress();
                showInfo("Прогресс уменьшен на 1 серию");
            }
        } catch (NumberFormatException e) {
            showError("Введите корректное значение прогресса");
        }
    }
    private Movie createMovieFromForm() {
        Movie movie = new Movie();
        movie.setTitle(titleField.getText().trim());
        movie.setType(typeComboBox.getValue());
        movie.setYear(Integer.parseInt(yearField.getText().trim()));
        movie.setGenre(genreField.getText().trim());
        movie.setUserRating(ratingSlider.getValue());
        movie.setDescription(descriptionField.getText().trim());
        String selectedStatus = statusComboBox.getValue();
        if (selectedStatus != null) {
            movie.setStatus(selectedStatus);
        } else {
            autoUpdateStatusBasedOnProgress();
            movie.setStatus(statusComboBox.getValue());
        }
        movie.setProgress(Integer.parseInt(progressField.getText().isEmpty() ? "0" : progressField.getText()));
        movie.setTotalEpisodes(Integer.parseInt(totalEpisodesField.getText().isEmpty() ? "1" : totalEpisodesField.getText()));
        return movie;
    }

    private void clearForm() {
        titleField.clear();
        typeComboBox.setValue(null);
        yearField.clear();
        genreField.clear();
        ratingSlider.setValue(5.0);
        statusComboBox.setValue(null);
        progressField.clear();
        totalEpisodesField.setText("1");
        totalEpisodesField.setDisable(true);
        descriptionField.clear();
        selectedMovie = null;
        originalTitle = null;
        originalYear = 0;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}