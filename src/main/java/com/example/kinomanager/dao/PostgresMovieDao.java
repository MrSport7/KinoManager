package com.example.kinomanager.dao;
import com.example.kinomanager.model.Movie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class PostgresMovieDao implements MovieDao {
    private static final String URL = "jdbc:postgresql://localhost:5432/kinomanager";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public PostgresMovieDao() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS movies (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                type VARCHAR(50) NOT NULL,
                year INTEGER NOT NULL,
                genre VARCHAR(255),
                user_rating DECIMAL(3,1) DEFAULT 0.0,
                status VARCHAR(100),
                progress INTEGER DEFAULT 0,
                total_episodes INTEGER DEFAULT 1,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(title, year)
            )
            """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица movies создана или уже существует");
        } catch (SQLException e) {
            System.out.println("Ошибка создания таблицы: " + e.getMessage());
        }
    }

    @Override
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY title";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setTitle(rs.getString("title"));
                movie.setType(rs.getString("type"));
                movie.setYear(rs.getInt("year"));
                movie.setGenre(rs.getString("genre"));
                movie.setUserRating(rs.getDouble("user_rating"));
                movie.setStatus(rs.getString("status"));
                movie.setProgress(rs.getInt("progress"));
                movie.setTotalEpisodes(rs.getInt("total_episodes"));
                movie.setDescription(rs.getString("description"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения фильмов: " + e.getMessage());
        }
        return movies;
    }

    @Override
    public void addMovie(Movie movie) {
        String sql = """
            INSERT INTO movies (title, type, year, genre, user_rating, status, progress, total_episodes, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getType());
            stmt.setInt(3, movie.getYear());
            stmt.setString(4, movie.getGenre());
            stmt.setDouble(5, movie.getUserRating());
            stmt.setString(6, movie.getStatus());
            stmt.setInt(7, movie.getProgress());
            stmt.setInt(8, movie.getTotalEpisodes());
            stmt.setString(9, movie.getDescription());
            stmt.executeUpdate();
            System.out.println("Фильм добавлен в PostgreSQL: " + movie.getTitle());
        } catch (SQLException e) {
            System.out.println("Ошибка добавления фильма: " + e.getMessage());
        }
    }
    @Override
    public boolean movieExists(String title, int year) {
        String sql = "SELECT COUNT(*) FROM movies WHERE title = ? AND year = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка проверки существования фильма: " + e.getMessage());
        }
        return false;
    }
    @Override
    public void updateMovie(Movie movie) {
        String sql = """
            UPDATE movies 
            SET title=?, type=?, year=?, genre=?, user_rating=?, status=?, progress=?, total_episodes=?, description=?
            WHERE title=? AND year=?
            """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getType());
            stmt.setInt(3, movie.getYear());
            stmt.setString(4, movie.getGenre());
            stmt.setDouble(5, movie.getUserRating());
            stmt.setString(6, movie.getStatus());
            stmt.setInt(7, movie.getProgress());
            stmt.setInt(8, movie.getTotalEpisodes());
            stmt.setString(9, movie.getDescription());
            stmt.setString(10, movie.getTitle());
            stmt.setInt(11, movie.getYear());
            stmt.executeUpdate();
            System.out.println("Фильм обновлен в PostgreSQL: " + movie.getTitle());
        } catch (SQLException e) {
            System.out.println("Ошибка обновления фильма: " + e.getMessage());
        }
    }

    @Override
    public void deleteMovie(String title, int year) {
        String sql = "DELETE FROM movies WHERE title=? AND year=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setInt(2, year);
            stmt.executeUpdate();
            System.out.println("Фильм удален из PostgreSQL: " + title);
        } catch (SQLException e) {
            System.out.println("Ошибка удаления фильма: " + e.getMessage());
        }
    }

    @Override
    public List<Movie> searchMovies(String query) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE LOWER(title) LIKE LOWER(?) OR LOWER(genre) LIKE LOWER(?) OR LOWER(type) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setTitle(rs.getString("title"));
                movie.setType(rs.getString("type"));
                movie.setYear(rs.getInt("year"));
                movie.setGenre(rs.getString("genre"));
                movie.setUserRating(rs.getDouble("user_rating"));
                movie.setStatus(rs.getString("status"));
                movie.setProgress(rs.getInt("progress"));
                movie.setTotalEpisodes(rs.getInt("total_episodes"));
                movie.setDescription(rs.getString("description"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка поиска фильмов: " + e.getMessage());
        }
        return movies;
    }

    @Override
    public List<Movie> findByStatus(String status) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE status = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setTitle(rs.getString("title"));
                movie.setType(rs.getString("type"));
                movie.setYear(rs.getInt("year"));
                movie.setGenre(rs.getString("genre"));
                movie.setUserRating(rs.getDouble("user_rating"));
                movie.setStatus(rs.getString("status"));
                movie.setProgress(rs.getInt("progress"));
                movie.setTotalEpisodes(rs.getInt("total_episodes"));
                movie.setDescription(rs.getString("description"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка фильтрации по статусу: " + e.getMessage());
        }
        return movies;
    }

    @Override
    public List<Movie> findByType(String type) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE type = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Movie movie = new Movie();
                movie.setTitle(rs.getString("title"));
                movie.setType(rs.getString("type"));
                movie.setYear(rs.getInt("year"));
                movie.setGenre(rs.getString("genre"));
                movie.setUserRating(rs.getDouble("user_rating"));
                movie.setStatus(rs.getString("status"));
                movie.setProgress(rs.getInt("progress"));
                movie.setTotalEpisodes(rs.getInt("total_episodes"));
                movie.setDescription(rs.getString("description"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка фильтрации по типу: " + e.getMessage());
        }

        return movies;
    }
}