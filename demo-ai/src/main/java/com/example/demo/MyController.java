package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://192.168.87.66")
@RestController
@RequestMapping("/nextmove")
public class MyController {

    private final MyService myService;

    @Autowired
    public MyController(MyService myService) {
        this.myService = myService;
    }

    @GetMapping
    public String getNextMove(@RequestParam String boardString) {   // length of 64
        int[][] board = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char curr = boardString.charAt(i * 8 + j);
                if (curr == '0') {
                    board[i][j] = 0;
                } else if (curr == '1') {
                    board[i][j] = 1;
                } else if (curr == '2') {
                    board[i][j] = 2;
                }
            }
        }
//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j++) {
//                System.out.println(board[i][j]);
//            }
//            System.out.println();
//        }
        String answer = myService.nextMove(board);
        System.out.println(answer);
        return answer;
    }
}
