import React from "react";

import "./index.css";
import Board from "./components/board.js";
import GameInfo from "./components/gameinfo.js";
import { Pieces, Player } from "./constants";
import { initialiseChessBoard, getPieceColor, rcToFileRank } from "./utils";
import ValidMoves from "./utils/validMoves";

export default class App extends React.Component {
  constructor() {
    super();
    this.squares = initialiseChessBoard();
    this.whoseTurn = Player.WHITE;
    this.whiteCapturedPieces = [];
    this.blackCapturedPieces = [];
    // When a piece is lifted, this holds its original board position in
    // rank-file notation, i.e., e5 for [4, 3].
    this.mobilePieceHomeSquare = "NONE";
    // When a piece is lifted, this holds all possible destination squares for
    // the piece in rank-file notation.
    this.validMovesSquares = [];
  }

  handleStart = (piece, r, c) => {
    this.mobilePieceHomeSquare = rcToFileRank(r, c);

    if (ValidMoves.isJumpPiece(piece)) {
      this.validMovesSquares = ValidMoves.allPosibleJumpMoves(
        piece,
        r,
        c,
        this.squares
      );
    } else {
      this.validMovesSquares = ValidMoves.allPossibleSlideMoves(
        piece,
        r,
        c,
        this.squares
      );
    }

    this.forceUpdate();
  };

  handleStop = (r, c, newR, newC) => {
    const movingPiece = this.squares[r][c];
    const targetLocVal = this.squares[newR][newC];

    if (getPieceColor(movingPiece) !== this.whoseTurn) {
      // Can't move other players pieces
      return this.updateGameState(r, c, r, c);
    }

    // No op invalid moves
    if (!this.validMovesSquares.includes(rcToFileRank(newR, newC))) {
      return this.updateGameState(r, c, r, c);
    }

    // Basic rules
    if (targetLocVal === Pieces.NONE) {
      return this.updateGameState(r, c, newR, newC);
    } else if (getPieceColor(targetLocVal) !== getPieceColor(movingPiece)) {
      return this.updateGameState(r, c, newR, newC);
    } else {
      return this.updateGameState(r, c, r, c); // No valid move
    }
  };

  updateGameState = (r, c, newR, newC) => {
    this.mobilePieceHomeSquare = "NONE";
    this.validMovesSquares = [];

    if (r === newR && c === newC) {
      this.forceUpdate();
      return { r, c };
    }

    const movingPiece = this.squares[r][c];
    const targetLocVal = this.squares[newR][newC];

    if (
      getPieceColor(movingPiece) === Pieces.WHITE &&
      targetLocVal !== Pieces.NONE
    ) {
      this.whiteCapturedPieces.push(targetLocVal);
    } else if (targetLocVal !== Pieces.NONE) {
      this.blackCapturedPieces.push(targetLocVal);
    }

    this.squares[newR][newC] = movingPiece;
    this.squares[r][c] = Pieces.NONE;

    this.whoseTurn =
      this.whoseTurn === Player.WHITE ? Player.BLACK : Player.WHITE;

    this.forceUpdate();
    return { r: newR, c: newC };
  };

  render() {
    return (
      <div className="game">
        <Board
          squares={this.squares}
          handleStartFn={this.handleStart}
          handleStopFn={this.handleStop}
          whoseTurn={this.whoseTurn}
          mobilePieceHomeSquare={this.mobilePieceHomeSquare}
          validMovesSquares={this.validMovesSquares}
        />
        <GameInfo
          whoseTurn={this.whoseTurn}
          whiteCapturedPieces={this.whiteCapturedPieces}
          blackCapturedPieces={this.blackCapturedPieces}
        />
      </div>
    );
  }
}
