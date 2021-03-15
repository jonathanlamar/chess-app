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
  parseFenString,
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

    // For handling pawn promotion
    this.isAwaitingPawnPromotion = false;
    this.pawnPromotionLocation = null;
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
      // return this.updateGameState(r, c, r, c);
      // FIXME: This can't be inside of an async function..?
      this.mobilePieceHomeSquare = "NONE";
      this.validMovesSquares = [];
      this.forceUpdate();
      return { r, c };
    }

    // No op invalid moves
    if (!this.validMovesSquares.includes(rcToFileRank(newR, newC))) {
      // return this.updateGameState(r, c, r, c);
      this.mobilePieceHomeSquare = "NONE";
      this.validMovesSquares = [];
      this.forceUpdate();
      return { r, c };
    }

    return this.updateGameState(r, c, newR, newC);
  };

  promotePawn = (newPiece) => {
    console.log("Promoting pawn: ", newPiece);
    // This will throw an error if pawnPromotionLocation is not set.
    this.gameState.squares[this.pawnPromotionLocation.r][
      this.pawnPromotionLocation.c
    ] = newPiece;
    this.isAwaitingPawnPromotion = false;
    this.pawnPromotionLocation = null;

    this.forceUpdate();
  };

  updateGameState = async (r, c, newR, newC) => {
    this.mobilePieceHomeSquare = "NONE";
    this.validMovesSquares = [];

    // Query board updating logic from backend
    const movingPieceFileRank = rcToFileRank(r, c);
    const destinationFileRank = rcToFileRank(newR, newC);
    const fenString = toFenString(this.gameState);
    const { data: updatedGameState } = await api.getUpdatedBoard(
      fenString,
      movingPieceFileRank,
      destinationFileRank
    );

    // Update board
    this.gameState = parseFenString(updatedGameState.fen);
    this.blackCapturedPieces = this.blackCapturedPieces.concat(
      updatedGameState.black
    );
    this.whiteCapturedPieces = this.whiteCapturedPieces.concat(
      updatedGameState.white
    );

    this.gameState.whoseMove =
      this.gameState.whoseMove === Player.WHITE ? Player.BLACK : Player.WHITE;

    // Trigger pawn promotion state
    const movingPiece = this.gameState.squares[r][c];
    if (movingPiece === (Pieces.WHITE | Pieces.PAWN) && newR === 0) {
      this.isAwaitingPawnPromotion = true;
      this.pawnPromotionLocation = { r: newR, c: newC };
    }
    if (movingPiece === (Pieces.BLACK | Pieces.PAWN) && newR === 7) {
      this.isAwaitingPawnPromotion = true;
      this.pawnPromotionLocation = { r: newR, c: newC };
    }

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
          fullMoveCount={this.gameState.fullMoveCount}
          halfMoveClock={this.gameState.halfMoveClock}
          whiteCapturedPieces={this.whiteCapturedPieces}
          blackCapturedPieces={this.blackCapturedPieces}
          isAwaitingPawnPromotion={this.isAwaitingPawnPromotion}
          pawnPromotionColor={
            this.isAwaitingPawnPromotion
              ? getPieceColor(
                  this.gameState.squares[this.pawnPromotionLocation.r][
                    this.pawnPromotionLocation.c
                  ]
                )
              : null
          }
          promotePawnFn={this.promotePawn}
        />
      </div>
    );
  }
}
