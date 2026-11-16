package pl.edu.atar.movie

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import pl.edu.atar.base.IntegrationSpec
import pl.edu.atar.movie.domain.Movie
import pl.edu.atar.movie.domain.MovieFacade
import pl.edu.atar.movie.domain.SampleMovies
import pl.edu.atar.movie.dto.MovieDto

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class MovieControllerAcceptanceSpec extends IntegrationSpec implements SampleMovies {

    @Autowired
    MovieFacade movieFacade

    ObjectMapper objectMapper = new ObjectMapper();

    def "should get movies"() {
        given: 'inventory has Inception and Matrix'
        movieFacade.deleteAll()
        MovieDto savedSystem = movieFacade.add(inception)
        MovieDto savedTron = movieFacade.add(matrix)

        when: 'I go to /api/movies'
        ResultActions getMovies = mockMvc.perform(get("/api/movies"))

        then: 'I see all movies'
        getMovies.andExpect(status().isOk())
                .andExpect(content().json("""
            [
                {
                    "movieId": ${savedSystem.movieId},
                    "title": "${savedSystem.title}"
                },
                {
                    "movieId": ${savedTron.movieId},
                    "title": "${savedTron.title}"
                }
            ]"""))

        when: 'I go to /api/movies/{id}'
        ResultActions getMovie = mockMvc.perform(get("/api/movies/${savedSystem.movieId}"))

        then: 'I see details of that movie'
        getMovie.andExpect(status().isOk())
                .andExpect(content().json("""
            {
                "movieId": ${savedSystem.movieId},
                "title": "${savedSystem.title}"
            }"""))
    }

    // TODO: Przeanalizuj kontroler i dopisz kilka testow akceptacyjnych
    def "should add new movie"() {
        given: 'inventory is empty'
        movieFacade.deleteAll()

        when: 'I send movie data to /api/movies'
        ResultActions postNewMovie = mockMvc.perform(
                post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inception))
        )

        then: 'created status and location are returned'
        postNewMovie.andExpect(status().isCreated())
            .andExpect(header().exists('location'))

        when: 'I get first movie from the system'
        MovieDto firstMovie = movieFacade.findAll()[0]

        then: 'First movie data matches sent movie data'
        firstMovie.title == inception.title
        firstMovie.image == inception.image
        firstMovie.year == inception.year
        firstMovie.videoId == inception.videoId

    }

    def "should update movie in the system"() {
        given: "Inventory has only Inception"
        movieFacade.deleteAll()
        MovieDto savedInception = movieFacade.add(inception)

        when: 'Movie is updated by /api/movies/{id}'
        ResultActions putMovie = mockMvc.perform(
                put("/api/movies/${savedInception.movieId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(matrix))
        )

        then: 'OK status is returned'
        putMovie.andExpect(status().isOk())

        when: 'I get first movie from the system'
        MovieDto firstMovie = movieFacade.findAll()[0]

        then: 'First movie data matches new movie data, but ID hasnt changed'
        firstMovie.movieId == savedInception.movieId
        firstMovie.title == matrix.title
        firstMovie.image == matrix.image
        firstMovie.year == matrix.year
        firstMovie.videoId == matrix.videoId
    }

    def "should return 404 when asked for a movie that is not in the system"() {
        given: "Inventory has only Inception"
        movieFacade.deleteAll()
        movieFacade.add(inception)

        when: "I try to get the wrong movie"
        ResultActions getMovie = mockMvc.perform(get("/api/movies/${matrix.movieId}"))

        then: "Not found is returned"
        getMovie.andExpect(status().isNotFound())
    }

    def "should delete movie in the system"() {
        given: "Inventory has only Inception"
        movieFacade.deleteAll()
        MovieDto savedInception = movieFacade.add(inception)

        when: "I try to delete Inception"
        ResultActions delMovie = mockMvc.perform(
                delete("/api/movies/${savedInception.movieId}")
        )

        then: "OK status is returned"
        delMovie.andExpect(status().isOk())

        when: "I check movie list in the system"
        List<MovieDto> movieList = movieFacade.findAll()

        then: "Movie list is empty"
        movieList.empty
    }
}
