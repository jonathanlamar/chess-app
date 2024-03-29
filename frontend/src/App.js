import React from "react";

import "./index.css";
import Board from "./components/board.js";
import GameInfo from "./components/gameinfo.js";
import { Pieces } from "./constants";
import {
  initialiseChessBoard,
  getPieceColor,
  rcToFileRank,
  toFenString,
  parseFenString,
  convertApiPieceToNative,
} from "./utils";
import * as api from "./api";
import LoggyComponent from "./utils/loggyComponent";

export default class App extends LoggyComponent {
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
    // For handling check
    this.checkCondition = { check: false, checkmate: false };
  }

  handleStart = async (r, c) => {
    const fenString = toFenString(this.gameState);

    const { data: validMoves } = await api.getLegalMoves(
      fenString,
      rcToFileRank(r, c)
    );

    this.mobilePieceHomeSquare = rcToFileRank(r, c);
    this.validMovesSquares = validMoves;
    this.forceUpdate();
  };

  handleStop = (r, c, newR, newC) => {
    const movingPiece = this.gameState.squares[r][c];

    if (getPieceColor(movingPiece) !== this.gameState.whoseMove) {
      // Can't move other players pieces
      this.mobilePieceHomeSquare = "NONE";
      this.validMovesSquares = [];
      this.forceUpdate();
      return { r, c };
    }

    // No op invalid moves
    if (!this.validMovesSquares.includes(rcToFileRank(newR, newC))) {
      this.mobilePieceHomeSquare = "NONE";
      this.validMovesSquares = [];
      this.forceUpdate();
      return { r, c };
    }

    return this.makePlayerMove(r, c, newR, newC);
  };

  triggerAiMove = async () => {
    const fenString = toFenString(this.gameState);
    const { data: updatedGameState } = await api.getRandomAiMove(fenString);

    // Update board
    this.updateGameState(updatedGameState);
  };

  // TODO: Rewrite this logic now that the API allows for pawn promotion.
  promotePawn = async (newPiece) => {
    // This will throw an error if pawnPromotionLocation is not set.
    this.gameState.squares[this.pawnPromotionLocation.r][
      this.pawnPromotionLocation.c
    ] = newPiece;
    this.isAwaitingPawnPromotion = false;
    this.pawnPromotionLocation = null;

    const fenString = toFenString(this.gameState);
    const { data: checkCondition } = await api.getCheckCondition(fenString);
    this.checkCondition = checkCondition;

    if (!this.checkCondition.checkmate) {
      this.triggerAiMove();
    }
  };

  makePlayerMove = async (r, c, newR, newC) => {
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

    // Update board
    this.updateGameState(updatedGameState);

    if (!this.isAwaitingPawnPromotion && !this.checkCondition.checkmate) {
      this.triggerAiMove();
    }

    return { r: newR, c: newC };
  };

  updateGameState = async (updatedGameState) => {
    this.gameState = parseFenString(updatedGameState.fen);
    this.blackCapturedPieces = this.blackCapturedPieces.concat(
      updatedGameState.blackCapturedPieces.map(convertApiPieceToNative)
    );
    this.whiteCapturedPieces = this.whiteCapturedPieces.concat(
      updatedGameState.whiteCapturedPieces.map(convertApiPieceToNative)
    );
    const { data: checkCondition } = await api.getCheckCondition(
      updatedGameState.fen
    );
    this.checkCondition = checkCondition;

    this.forceUpdate();
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
          checkCondition={this.checkCondition}
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
