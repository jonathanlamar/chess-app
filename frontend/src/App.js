import React from "react";

import "./index.css";
import Board from "./components/board.js";
import GameInfo from "./components/gameinfo.js";
import { Pieces, Player } from "./constants";
import {
  initialiseChessBoard,
  getPieceColor,
  getPieceType,
  rcToFileRank,
  toFenString,
} from "./utils";
import * as api from "./api";

export default class App extends React.Component {
  constructor() {
    super();
    this.gameState = initialiseChessBoard();
    this.whiteCapturedPieces = [];
    this.blackCapturedPieces = [];
    // When a piece is lifted, this holds its original board position in
    // rank-file notation, i.e., e5 for [4, 3].
    this.mobilePieceHomeSquare = "NONE";
    // When a piece is lifted, this holds all possible destination squares for
    // the piece in rank-file notation.
    this.validMovesSquares = [];
  }

  handleStart = async (r, c) => {
    const fenString = toFenString(this.gameState);

    const { data: validMoves } = await api.getLegalMoves(
      fenString,
      rcToFileRank(r, c)
    );

    this.mobilePieceHomeSquare = rcToFileRank(r, c);
    this.validMovesSquares = validMoves.map((pos) =>
      rcToFileRank(pos.r, pos.c)
    );
    this.forceUpdate();
  };

  handleStop = (r, c, newR, newC) => {
    const movingPiece = this.gameState.squares[r][c];

    if (getPieceColor(movingPiece) !== this.gameState.whoseMove) {
      // Can't move other players pieces
      return this.updateGameState(r, c, r, c);
    }

    // No op invalid moves
    if (!this.validMovesSquares.includes(rcToFileRank(newR, newC))) {
      return this.updateGameState(r, c, r, c);
    }

    return this.updateGameState(r, c, newR, newC);
  };

  updateGameState = (r, c, newR, newC) => {
    this.mobilePieceHomeSquare = "NONE";
    this.validMovesSquares = [];

    // No op if no move
    if (r === newR && c === newC) {
      this.forceUpdate();
      return { r, c };
    }

    const movingPiece = this.gameState.squares[r][c];
    const targetLocVal = this.gameState.squares[newR][newC];

    // Capturing
    if (
      getPieceColor(movingPiece) === Pieces.WHITE &&
      targetLocVal !== Pieces.NONE
    ) {
      this.whiteCapturedPieces.push(targetLocVal);
    } else if (targetLocVal !== Pieces.NONE) {
      this.blackCapturedPieces.push(targetLocVal);
    }

    // En Passant capturing
    if (
      this.gameState.enPassantTargetPos &&
      getPieceType(movingPiece) === Pieces.PAWN &&
      newR === this.gameState.enPassantTargetPos.r &&
      newC === this.gameState.enPassantTargetPos.c
    ) {
      if (getPieceColor(movingPiece) === Pieces.WHITE) {
        this.whiteCapturedPieces.push(this.gameState.squares[newR + 1][newC]);
        this.gameState.squares[newR + 1][newC] = Pieces.NONE;
      } else {
        this.blackCapturedPieces.push(this.gameState.squares[newR - 1][newC]);
        this.gameState.squares[newR - 1][newC] = Pieces.NONE;
      }
    }

    // TODO: pawn promotion, castling
    // En Passant target computation
    if (getPieceType(movingPiece) === Pieces.PAWN && Math.abs(r - newR) === 2) {
      this.gameState.enPassantTargetPos = { r: (r + newR) / 2, c: c };
    } else {
      this.gameState.enPassantTargetPos = null;
    }

    this.gameState.squares[newR][newC] = movingPiece;
    this.gameState.squares[r][c] = Pieces.NONE;

    this.gameState.whoseMove =
      this.gameState.whoseMove === Player.WHITE ? Player.BLACK : Player.WHITE;

    this.forceUpdate();
    return { r: newR, c: newC };
  };

  render() {
    return (
      <div className="game">
        <Board
          squares={this.gameState.squares}
          handleStartFn={this.handleStart}
          handleStopFn={this.handleStop}
          whoseTurn={this.gameState.whoseMove}
          mobilePieceHomeSquare={this.mobilePieceHomeSquare}
          validMovesSquares={this.validMovesSquares}
        />
        <GameInfo
          whoseTurn={this.gameState.whoseMove}
          whiteCapturedPieces={this.whiteCapturedPieces}
          blackCapturedPieces={this.blackCapturedPieces}
        />
      </div>
    );
  }
}
