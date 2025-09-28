package com.example.kinomanager.dao;
import com.example.kinomanager.model.Movie;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvMovieDao implements MovieDao {
    private static final String CSV_FILE = "data/movies.csv";

    public CsvMovieDao() {
        createFileIfNotExists();
    }

    private void createFileIfNotExists() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(file);
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                     BufferedWriter writer = new BufferedWriter(osw)) {
                    // Добавляем BOM для UTF-8
                    fos.write(0xEF); // BOM байт 1
                    fos.write(0xBB); // BOM байт 2
                    fos.write(0xBF); // BOM байт 3
                    writer.write("title,type,year,genre,rating,status,progress,totalEpisodes,description\n");
                }
                System.out.println("Создан новый CSV файл");
            } catch (IOException e) {
                System.out.println("Ошибка создания файла: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(CSV_FILE), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                try {
                    Movie movie = createMovieFromParts(line);
                    movies.add(movie);
                } catch (Exception e) {
                    System.out.println("Ошибка в строке: " + line + " : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        }
        return movies;
    }

    private Movie createMovieFromParts(String line) {
        List<String> parsedFields = parseCsvLine(line);
        if (parsedFields.size() < 9) {
            throw new IllegalArgumentException("Недостаточно полей в строке CSV: " + parsedFields.size());
        }
        Movie movie = new Movie();
        movie.setTitle(parsedFields.get(0));
        movie.setType(parsedFields.get(1));
        movie.setYear(Integer.parseInt(parsedFields.get(2)));
        movie.setGenre(parsedFields.get(3));
        movie.setUserRating(Double.parseDouble(parsedFields.get(4).replace(',', '.')));
        movie.setStatus(parsedFields.get(5));
        movie.setProgress(Integer.parseInt(parsedFields.get(6)));
        movie.setTotalEpisodes(Integer.parseInt(parsedFields.get(7)));
        movie.setDescription(parsedFields.get(8));
        return movie;
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(currentField.toString());
                    currentField = new StringBuilder();
                } else {
                    currentField.append(c);
                }
            }
        }
        fields.add(currentField.toString());
        return fields;
    }

    @Override
    public void addMovie(Movie movie) {
        try (FileOutputStream fos = new FileOutputStream(CSV_FILE, true);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            String escapedTitle = escapeCsvField(movie.getTitle());
            String escapedGenre = escapeCsvField(movie.getGenre());
            String escapedStatus = escapeCsvField(movie.getStatus());
            String escapedDescription = escapeCsvField(movie.getDescription());
            String line;
            if ("сериал".equalsIgnoreCase(movie.getType())) {
                line = String.format(Locale.US, "%s,%s,%d,%s,%.1f,%s,%d,%d,%s\n",
                        escapedTitle, movie.getType(), movie.getYear(),
                        escapedGenre, movie.getUserRating(), escapedStatus,
                        movie.getProgress(), movie.getTotalEpisodes(), escapedDescription);
            } else {
                line = String.format(Locale.US, "%s,%s,%d,%s,%.1f,%s,%d,%d,%s\n",
                        escapedTitle, movie.getType(), movie.getYear(),
                        escapedGenre, movie.getUserRating(), escapedStatus,
                        movie.getProgress(), 1, escapedDescription);
            }
            writer.write(line);
            System.out.println("Добавлен фильм: " + movie.getTitle());

        } catch (IOException e) {
            System.out.println("Ошибка записи: " + e.getMessage());
        }
    }
    @Override
    public boolean movieExists(String title, int year) {
        List<Movie> movies = getAllMovies();
        return movies.stream().anyMatch(m -> m.getTitle().equals(title) && m.getYear() == year);
    }
    private String escapeCsvField(String field) {
        if (field == null) return "";
        field = field.replace("\n", " ").replace("\r", " ");
        if (field.contains(",") || field.contains("\"") || field.contains(" ")) {
            field = field.replace("\"", "\"\"");
            field = "\"" + field + "\"";
        }
        return field;
    }

    @Override
    public void updateMovie(Movie updatedMovie) {
        List<Movie> movies = getAllMovies();
        movies.removeIf(m ->
                m.getTitle().equals(updatedMovie.getTitle()) &&
                        m.getYear() == updatedMovie.getYear()
        );
        movies.add(updatedMovie);
        saveAllMovies(movies);
    }

    @Override
    public void deleteMovie(String title, int year) {
        List<Movie> movies = getAllMovies();
        movies.removeIf(m ->
                m.getTitle().equals(title) && m.getYear() == year
        );
        saveAllMovies(movies);
    }

    @Override
    public List<Movie> searchMovies(String query) {
        List<Movie> allMovies = getAllMovies();
        List<Movie> results = new ArrayList<>();
        String searchQuery = query.toLowerCase();
        for (Movie movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(searchQuery) ||
                    movie.getGenre().toLowerCase().contains(searchQuery) ||
                    movie.getType().toLowerCase().contains(searchQuery) ||
                    movie.getDescription().toLowerCase().contains(searchQuery)) {
                results.add(movie);
            }
        }
        return results;
    }

    @Override
    public List<Movie> findByStatus(String status) {
        List<Movie> allMovies = getAllMovies();
        return allMovies.stream()
                .filter(m -> m.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    @Override
    public List<Movie> findByType(String type) {
        List<Movie> allMovies = getAllMovies();
        return allMovies.stream()
                .filter(m -> m.getType().equalsIgnoreCase(type))
                .toList();
    }

    private void saveAllMovies(List<Movie> movies) {
        try (FileOutputStream fos = new FileOutputStream(CSV_FILE);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);
            writer.write("title,type,year,genre,rating,status,progress,totalEpisodes,description\n");
            for (Movie movie : movies) {
                String escapedTitle = escapeCsvField(movie.getTitle());
                String escapedGenre = escapeCsvField(movie.getGenre());
                String escapedStatus = escapeCsvField(movie.getStatus());
                String escapedDescription = escapeCsvField(movie.getDescription());
                String line;
                if ("сериал".equalsIgnoreCase(movie.getType())) {
                    line = String.format(Locale.US, "%s,%s,%d,%s,%.1f,%s,%d,%d,%s\n",
                            escapedTitle, movie.getType(), movie.getYear(),
                            escapedGenre, movie.getUserRating(), escapedStatus,
                            movie.getProgress(), movie.getTotalEpisodes(), escapedDescription);
                } else {
                    line = String.format(Locale.US, "%s,%s,%d,%s,%.1f,%s,%d,%d,%s\n",
                            escapedTitle, movie.getType(), movie.getYear(),
                            escapedGenre, movie.getUserRating(), escapedStatus,
                            movie.getProgress(), 1, escapedDescription);
                }
                writer.write(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }
}
