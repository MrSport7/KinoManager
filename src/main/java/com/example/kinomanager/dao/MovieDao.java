package com.example.kinomanager.dao;

import com.example.kinomanager.model.Movie;
import java.util.List;

public interface MovieDao {
    List<Movie> getAllMovies();
    void addMovie(Movie movie);
    boolean movieExists(String title, int year);
    void updateMovie(Movie movie);
    void deleteMovie(String title, int year);
    List<Movie> searchMovies(String query);
    List<Movie> findByStatus(String status);
    List<Movie> findByType(String type);

}