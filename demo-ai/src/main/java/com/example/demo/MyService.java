package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class MyService {

    private String answer;

    public String nextMove(int[][] board) {
        minimaxSearch(board, 2, 4);
        return answer;
    }

    // returns the number of opponent's pieces that can be flipped
    int countFlipPieces(int[][] board, int color, int row, int col, int direction) {
        int opponent = (color == 1) ? 2 : 1, player = (opponent == 2) ? 1 : 2;
        int count = 0;
        switch (direction) {
            case 0:   // up
                while (row > 0) {
                    int curr = board[--row][col];
                    if (curr == player) {
                        return count;
                    }
                    if (curr == opponent) {
                        ++count;
                    } else {   // if curr == 'x'
                        break;
                    }
                }
                break;
            case 1:   // up-right
                while (row > 0 && col < 5) {
                    int curr = board[--row][++col];
                    if (curr == player) {
                        return count;
                    }
                    if (curr == opponent) {
                        ++count;
                    } else {   // if curr == 'x'
                        break;
                    }
                }
                break;
            case 2:   // right
                while (col < 5) {
                    int curr = board[row][++col];
                    if (curr == player) {
                        return count;
                    }
                    if (curr == opponent) {
                        ++count;
                    } else {   // if curr == 'x'
                        break;
                    }
                }
                break;
            case 3:   // down-right
                while (row < 5 && col < 5) {
                    int curr = board[++row][++col];
                    if (curr == player) {
                        return count;
                    }
                    if (curr == opponent) {
                        ++count;
                    } else {   // if curr == 'x'
                        break;
                    }
                }
                break;
            case 4:   // down
                while (row < 5) {
                    int curr = board[++row][col];
                    if (curr == player) {
                        return count;
                    }
                    if (curr == opponent) {
                        ++count;
                    } else {   // if curr == 'x'
                        break;
                    }
                }
                break;
            case 5:   // down-left
                while (row < 5 && col > 0) {
                    int curr = board[++row][--col];
                    if (curr == player) {
                        return count;
                    }
                    if (curr == opponent) {
                        ++count;
                    } else {   // if curr == 'x'
                        break;
                    }
                }
                break;
            case 6:   // left
                while (col > 0) {
                    int curr = board[row][--col];
                    if (curr == player) {
                        return count;
                    }
                    if (curr == opponent) {
                        ++count;
                    } else {   // if curr == 'x'
                        break;
                    }
                }
                break;
            case 7:   // up-left
                while (row > 0 && col > 0) {
                    int curr = board[--row][--col];
                    if (curr == player) {
                        return count;
                    }
                    if (curr == opponent) {
                        ++count;
                    } else {   // if curr == 'x'
                        break;
                    }
                }
                break;
        }
        return 0;
    }

    // Is placing a piece at this grid a valid move for this player?
    boolean isValidMove(int[][] board, int color, int row, int col) {
        for (int dr = 0; dr <= 7; ++dr) {
            if (countFlipPieces(board, color, row, col, dr) > 0) {
                return true;
            }
        }
        return false;
    }

    // whether this player should pass
    boolean passTurn(int[][] board, int color) {
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 6; ++j) {
                if (board[i][j] == 0 && isValidMove(board, color, i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean gameOver(int[][] board) {
        return passTurn(board, 1) && passTurn(board, 2);
    }

    int countColorPieces(int[][] board, int color) {
        int player = (color == 1) ? 1 : 2;
        int count = 0;
//        for (const auto & i:board){
//            for (char j : i) {
//                if (j == player) {
//                    ++count;
//                }
//            }
//        }
        for (int[] row : board) {
            for (int i : row) {
                if (i == player) {
                    ++count;
                }
            }
        }
        return count;
    }

    // returns the difference between the numbers of two colors
    int heuristicScore(int[][] board, int color) {
        return countColorPieces(board, color) - countColorPieces(board, (color == 1) ? 2 : 1);
    }

    int[][] flipPieces(int[][] board, int color, int row, int col) {
        int[][] res = new int[board.length][board.length];
        for (int i = 0; i < board.length; ++i) {
            System.arraycopy(board[i], 0, res[i], 0, board.length);
        }
        int player = (color == 1) ? 1 : 2;
        res[row][col] = player;
        for (int dr = 0; dr <= 7; ++dr) {
            int i = row, j = col;
            int num = countFlipPieces(res, color, i, j, dr);
            while (num-- > 0) {
                switch (dr) {
                    case 0:   // up
                        res[--i][j] = player;
                        break;
                    case 1:   // up-right
                        res[--i][++j] = player;
                        break;
                    case 2:   // right
                        res[i][++j] = player;
                        break;
                    case 3:   // down-right
                        res[++i][++j] = player;
                        break;
                    case 4:   // down
                        res[++i][j] = player;
                        break;
                    case 5:   // down-left
                        res[++i][--j] = player;
                        break;
                    case 6:   // left
                        res[i][--j] = player;
                        break;
                    case 7:   // up-left
                        res[--i][--j] = player;
                        break;
                }
            }
        }
        return res;
    }

    private int minimaxSearch(int[][] board, int color, int depth) {
        if (depth == 0 || gameOver(board)) {
            return heuristicScore(board, color);
        }
        if (passTurn(board, color)) {
            return minimaxSearch(board, (color == 1) ? 2 : 1, depth - 1);
        }
        if (color == 2) {   // find the best move
            int maxScore = -2147483648;
            for (int i = 0; i < 6; ++i) {
                for (int j = 0; j < 6; ++j) {
                    if (board[i][j] == 0 && isValidMove(board, color, i, j)) {
                        int[][] nextBoard = flipPieces(board, color, i, j);
                        int score = minimaxSearch(nextBoard, (color == 1) ? 2 : 1, depth - 1);
                        if (score > maxScore) {
                            maxScore = score;
                            if (depth == 4) {
                                answer = "";
                                answer += (char) ('0' + i);
                                answer += (char) ('0' + j);
                            }
                        }
                    }
                }
            }
            return maxScore;
        } else {   // find the worst move
            int minScore = 2147483647;
            for (int i = 0; i < 6; ++i) {
                for (int j = 0; j < 6; ++j) {
                    if (board[i][j] == 0 && isValidMove(board, color, i, j)) {
                        int[][] nextBoard = flipPieces(board, color, i, j);
                        int score = minimaxSearch(nextBoard, (color == 1) ? 2 : 1, depth - 1);
                        if (score < minScore) {
                            minScore = score;
                        }
                    }
                }
            }
            return minScore;
        }
    }
}
