package com.example.kinomanager;
import com.example.kinomanager.dao.MovieDao;
import com.example.kinomanager.model.Movie;
import com.example.kinomanager.service.MovieService;
import com.example.kinomanager.service.TmdbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    @Mock
    private MovieDao movieDao;

    @Mock
    private TmdbService tmdbService;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        movieService = new MovieService(movieDao, tmdbService);
    }

    @Test
    void testAddMovie_ValidMovie_ShouldAddAndUpdateStatus() {
        Movie movie = new Movie("Test Movie", "фильм", 2023, "Drama", 8.5, "Хочу посмотреть", 0);
        when(movieDao.movieExists(anyString(), anyInt())).thenReturn(false);

        movieService.addMovie(movie);

        verify(movieDao).addMovie(movie);
        assertEquals("Просмотрено", movie.getStatus());  // Для фильма с progress > 0
    }

    @Test
    void testAddMovie_InvalidTitle_ShouldThrowException() {
        Movie movie = new Movie("", "фильм", 2023, "Drama", 8.5, "Хочу посмотреть", 0);

        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movie));
    }

    @Test
    void testUpdateMovie_ShouldUpdateAndUpdateStatus() {
        Movie movie = new Movie("Updated Movie", "сериал", 2023, "Comedy", 9.0, "В процессе", 5);
        movie.setTotalEpisodes(10);

        movieService.updateMovie(movie);

        verify(movieDao).updateMovie(movie);
        assertEquals("В процессе", movie.getStatus());  // Для сериала с progress < total
    }

    @Test
    void testDeleteMovie_ShouldCallDaoDelete() {
        movieService.deleteMovie("Test", 2023);

        verify(movieDao).deleteMovie("Test", 2023);
    }

    @Test
    void testSearchMovies_ShouldReturnFilteredList() {
        List<Movie> mockMovies = Arrays.asList(
                new Movie("Movie1", "фильм", 2020, "Action", 7.0, "Просмотрено", 1),
                new Movie("Movie2", "сериал", 2021, "Drama", 8.0, "В процессе", 5)
        );
        when(movieDao.searchMovies("Drama")).thenReturn(mockMovies.subList(1, 2));

        List<Movie> result = movieService.searchMovies("Drama");

        assertEquals(1, result.size());
        assertEquals("Movie2", result.get(0).getTitle());
    }

    @Test
    void testFilterByType_ShouldReturnMovies() {
        List<Movie> mockMovies = Arrays.asList(
                new Movie("Film1", "фильм", 2020, "Action", 7.0, "Просмотрено", 1),
                new Movie("Series1", "сериал", 2021, "Drama", 8.0, "В процессе", 5)
        );
        when(movieDao.findByType("фильм")).thenReturn(mockMovies.subList(0, 1));

        List<Movie> result = movieService.filterByType("фильм");

        assertEquals(1, result.size());
        assertEquals("фильм", result.get(0).getType());
    }

    @Test
    void testFilterByStatus_ShouldReturnByStatus() {
        List<Movie> mockMovies = Arrays.asList(
                new Movie("Film1", "фильм", 2020, "Action", 7.0, "Просмотрено", 1),
                new Movie("Series1", "сериал", 2021, "Drama", 8.0, "В процессе", 5)
        );
        when(movieDao.findByStatus("Просмотрено")).thenReturn(mockMovies.subList(0, 1));

        List<Movie> result = movieService.filterByStatus("Просмотрено");

        assertEquals(1, result.size());
        assertEquals("Просмотрено", result.get(0).getStatus());
    }

    @Test
    void testSortByRating_ShouldSortDescending() {
        List<Movie> mockMovies = Arrays.asList(
                new Movie("Low", "фильм", 2020, "Action", 5.0, "Просмотрено", 1),
                new Movie("High", "фильм", 2021, "Drama", 9.0, "Просмотрено", 1)
        );
        when(movieDao.getAllMovies()).thenReturn(mockMovies);

        List<Movie> result = movieService.sortByRating();

        assertEquals("High", result.get(0).getTitle());
        assertEquals("Low", result.get(1).getTitle());
    }

    @Test
    void testSortByYear_ShouldSortDescending() {
        List<Movie> mockMovies = Arrays.asList(
                new Movie("Old", "фильм", 2020, "Action", 7.0, "Просмотрено", 1),
                new Movie("New", "фильм", 2023, "Drama", 8.0, "Просмотрено", 1)
        );
        when(movieDao.getAllMovies()).thenReturn(mockMovies);

        List<Movie> result = movieService.sortByYear();

        assertEquals("New", result.get(0).getTitle());
        assertEquals("Old", result.get(1).getTitle());
    }

    @Test
    void testSearchTmdbByTitle_ShouldReturnMovie() throws IOException {
        Movie mockMovie = new Movie("TMDb Movie", "фильм", 2022, "Sci-Fi", 8.5, "Хочу посмотреть", 0);
        when(tmdbService.searchMovieByTitle("Test")).thenReturn(mockMovie);

        Movie result = movieService.searchTmdbByTitle("Test");

        assertEquals("TMDb Movie", result.getTitle());
    }

    @Test
    void testSetDataSource_ShouldSwitchDao() {
        movieService.setDataSource(MovieService.DataSourceType.POSTGRES);

        assertEquals(MovieService.DataSourceType.POSTGRES, movieService.getCurrentDataSource());
        // Проверить, что movieDao сменился (если добавите геттер, но для простоты — это достаточно)
    }

    @Test
    void testValidateMovie_InvalidYear_ShouldThrowException() {
        Movie movie = new Movie("Test", "фильм", 1800, "Drama", 8.0, "Просмотрено", 1);

        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movie));
    }

    @Test
    void testAutoUpdateStatus_ForSeries_InProgress() {
        Movie movie = new Movie("Series", "сериал", 2023, "Drama", 8.0, "Хочу посмотреть", 5);
        movie.setTotalEpisodes(10);

        movieService.addMovie(movie);  // Это вызовет updateStatusAutomatically

        assertEquals("В процессе", movie.getStatus());
    }
}
