import React from "react";

import "./index.css";
import Board from "./components/board.js";
import GameInfo from "./components/gameinfo.js";
import { Piece, Player } from "./constants";
import { initialiseChessBoard, getPieceColor } from "./utils";
import ValidMoves from "./utils/validMoves";

export default class App extends React.Component {
  constructor() {
    super();
    this.squares = initialiseChessBoard();
    this.whoseTurn = Player.WHITE;
    this.whiteCapturedPieces = [];
    this.blackCapturedPieces = [];
  }

  handleStop = (r, c, newR, newC) => {
    const movingPiece = this.squares[r][c];
    const targetLocVal = this.squares[newR][newC];

    if (getPieceColor(movingPiece) !== this.whoseTurn) {
      return { r, c }; // Can't move other players pieces
    }

    // Basic rules
    if (targetLocVal === Piece.NONE) {
      return this.updateGameState(r, c, newR, newC);
    } else if (getPieceColor(targetLocVal) !== getPieceColor(movingPiece)) {
      return this.updateGameState(r, c, newR, newC);
    } else {
      return this.updateGameState(r, c, r, c); // No valid move
    }
  };

  updateGameState = (r, c, newR, newC) => {
    if (r === newR && c === newC) {
      return { r, c };
    }

    const movingPiece = this.squares[r][c];
    const targetLocVal = this.squares[newR][newC];

    if (
      getPieceColor(movingPiece) === Piece.WHITE &&
      targetLocVal !== Piece.NONE
    ) {
      this.whiteCapturedPieces.push(targetLocVal);
    } else if (targetLocVal !== Piece.NONE) {
      this.blackCapturedPieces.push(targetLocVal);
    }

    this.squares[newR][newC] = movingPiece;
    this.squares[r][c] = Piece.NONE;

    this.whoseTurn =
      this.whoseTurn === Player.WHITE ? Player.BLACK : Player.WHITE;

    this.forceUpdate();
    return { r: newR, c: newC };
  };

  render() {
    return (
      <div className="game">
        <Board squares={this.squares} handleStopFn={this.handleStop} />
        <GameInfo
          whoseTurn={this.whoseTurn}
          whiteCapturedPieces={this.whiteCapturedPieces}
          blackCapturedPieces={this.blackCapturedPieces}
        />
      </div>
    );
  }
}
