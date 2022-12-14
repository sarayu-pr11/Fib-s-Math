package com.nighthawk.spring_portfolio.controllers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // annotation to create a RESTful web services
@RequestMapping("/api") // prefix of API
public class Stats {
    private JSONObject body; // last run result
    private HttpStatus status; // last run status
    String last_run = null; // last run day of month

    // GET Covid 19 Stats
    @GetMapping("/stats") // added to end of prefix as endpoint
    public ResponseEntity<JSONObject> getSchedule() {

        // calls API once a day, sets body and status properties
        String today = new Date().toString().substring(0, 10);
        if (last_run == null || !today.equals(last_run)) {
            try { // APIs can fail (ie Internet or Service down)

                // RapidAPI header
                HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://real-time-statistics.p.rapidapi.com/counters"))
                .header("X-RapidAPI-Key", "9f4648d091mshd482eecf7cc5dcfp1c3796jsn99cec5dc67e0")
                .header("X-RapidAPI-Host", "real-time-statistics.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();


                // RapidAPI request and response
                HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                        HttpResponse.BodyHandlers.ofString());

                // JSONParser extracts text body and parses to JSONObject
                this.body = (JSONObject) new JSONParser().parse(response.body());
                this.status = HttpStatus.OK; // 200 success
                this.last_run = today;
            } catch (Exception e) { // capture failure info
                HashMap<String, String> status = new HashMap<>();
                status.put("status", "RapidApi failure: " + e);

                // Setup object for error
                this.body = (JSONObject) status;
                this.status = HttpStatus.INTERNAL_SERVER_ERROR; // 500 error
                this.last_run = null;
            }
        }

        // return JSONObject in RESTful style
        return new ResponseEntity<>(body, status);
    }
}