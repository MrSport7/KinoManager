package com.example.kinomanager.service;
import com.example.kinomanager.model.Movie;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TmdbService {
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlODY2MTI2YjQ1N2EyYmVkNzc3NGZkMDkyODA2Mjc5NyIsIm5iZiI6MTc1ODgyNTA4MC45MDYsInN1YiI6IjY4ZDU4YTc4YzQ5ODhlYWM0MGM4MzE3MSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.Dc8PeVL8F0hxO27gqb-o5NJovptNgC_skUNaa_RXKAg";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final Gson gson = new Gson();
    private static final int MAX_RETRIES = 3;
    private static final int BASE_RETRY_DELAY_MS = 5000;
    public Movie searchMovieByTitle(String title) throws IOException {
        String encodedTitle = URLEncoder.encode(title, "UTF-8");
        // Сначала ищем фильмы
        String movieUrl = BASE_URL + "/search/movie?query=" + encodedTitle + "&language=ru-RU";
        Movie movie = fetchFromUrl(movieUrl, true);
        if (movie != null) return movie;
        // Затем сериалы
        String tvUrl = BASE_URL + "/search/tv?query=" + encodedTitle + "&language=ru-RU";
        return fetchFromUrl(tvUrl, false);
    }

    public Movie searchMovieById(int tmdbId) throws IOException {
        String movieUrl = BASE_URL + "/movie/" + tmdbId + "?language=ru-RU";
        Movie movie = fetchFromUrl(movieUrl, true);
        if (movie != null) return movie;
        String tvUrl = BASE_URL + "/tv/" + tmdbId + "?language=ru-RU";
        return fetchFromUrl(tvUrl, false);
    }

    private Movie fetchFromUrl(String urlString, boolean isMovieSearch) throws IOException {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String jsonResponse = sendGetRequest(urlString);
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                if (jsonObject.has("results") && !jsonObject.get("results").getAsJsonArray().isJsonNull()) {
                    JsonArray results = jsonObject.getAsJsonArray("results");
                    if (results.size() > 0) {
                        JsonObject firstResult = results.get(0).getAsJsonObject();
                        int id = firstResult.get("id").getAsInt();
                        String detailsUrl;
                        if (isMovieSearch) {
                            detailsUrl = BASE_URL + "/movie/" + id + "?language=ru-RU";
                        } else {
                            detailsUrl = BASE_URL + "/tv/" + id + "?language=ru-RU";
                        }
                        String detailsJson = sendGetRequest(detailsUrl);
                        JsonObject details = JsonParser.parseString(detailsJson).getAsJsonObject();
                        return parseMovieFromJson(details, isMovieSearch);
                    }
                }
                return null;
            } catch (IOException e) {
                int delay = BASE_RETRY_DELAY_MS * (1 << (attempt - 1));
                if (attempt == MAX_RETRIES) {
                    throw new IOException("Ошибка сети после " + MAX_RETRIES + " попыток: " + e.getMessage(), e);
                }
                System.out.println("Попытка " + attempt + " провалилась. Ждем " + (delay / 1000) + " сек...");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Поток прерван во время ожидания", ie);
                }
            }
        }
        return null;
    }
    private String sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP ошибка (код " + responseCode + ")");
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    private Movie parseMovieFromJson(JsonObject json, boolean isMovie) {
        Movie movie = new Movie();
        if (isMovie) {
            // Фильм
            movie.setTitle(json.get("title").getAsString());
            if (json.has("release_date") && !json.get("release_date").isJsonNull() && !json.get("release_date").getAsString().isEmpty()) {
                try {
                    LocalDate releaseDate = LocalDate.parse(json.get("release_date").getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    movie.setYear(releaseDate.getYear());
                } catch (Exception e) {
                    movie.setYear(0);
                }
            } else {
                movie.setYear(0);
            }
            movie.setType("фильм");
            movie.setGenre(getGenresAsString(json.getAsJsonArray("genres")));
            movie.setDescription(json.has("overview") && !json.get("overview").isJsonNull() ? json.get("overview").getAsString() : "");
            movie.setTotalEpisodes(1);
        } else {
            // Сериал
            movie.setTitle(json.get("name").getAsString());
            if (json.has("first_air_date") && !json.get("first_air_date").isJsonNull() && !json.get("first_air_date").getAsString().isEmpty()) {
                try {
                    LocalDate airDate = LocalDate.parse(json.get("first_air_date").getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    movie.setYear(airDate.getYear());
                } catch (Exception e) {
                    movie.setYear(0);
                }
            } else {
                movie.setYear(0);
            }
            movie.setType("сериал");
            movie.setGenre(getGenresAsString(json.getAsJsonArray("genres")));
            movie.setDescription(json.has("overview") && !json.get("overview").isJsonNull() ? json.get("overview").getAsString() : "");
            int seasons = json.has("number_of_seasons") ? json.get("number_of_seasons").getAsInt() : 1;
            movie.setTotalEpisodes(Math.max(seasons * 10, 1));
        }
        movie.setUserRating(json.has("vote_average") && !json.get("vote_average").isJsonNull() ? json.get("vote_average").getAsDouble() : 0.0);
        movie.setStatus("Хочу посмотреть");
        movie.setProgress(0);
        return movie;
    }

    private String getGenresAsString(JsonArray genresArray) {
        if (genresArray == null) return "";
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < genresArray.size(); i++) {
            JsonObject genre = genresArray.get(i).getAsJsonObject();
            if (genre.has("name")) {
                genres.append(genre.get("name").getAsString());
                if (i < genresArray.size() - 1) genres.append(", ");
            }
        }
        return genres.toString();
    }
}