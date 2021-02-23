import React from "react";
import "./index.css";
import Board from "./components/board.js";
import GameInfo from "./components/gameinfo.js";
import { Piece, Player } from "./constants";
import { initialiseChessBoard, getPieceColor } from "./utils";

export default class App extends React.Component {
  constructor() {
    super();
    this.squares = initialiseChessBoard();
    this.whoseTurn = Player.WHITE;
    this.whiteCapturedPieces = [];
    this.blackCapturedPieces = [];
    console.log("Initialized App");
    console.log(this);
  }

  handleStop = (r, c, newR, newC) => {
    const movingPiece = this.squares[r][c];
    const targetLocVal = this.squares[newR][newC];

    // Basic rules
    if (targetLocVal === Piece.NONE) {
      this.squares[newR][newC] = movingPiece;
      this.squares[r][c] = Piece.NONE;

      console.log("Done updateing squares", this);
      return { r: newR, c: newC };
    } else if (getPieceColor(targetLocVal) === getPieceColor(movingPiece)) {
      console.log("Done updateing squares", this);
      return { r, c }; // Cannot move
    } else {
      this.squares[newR][newC] = movingPiece;
      this.squares[r][c] = Piece.NONE;

      if (getPieceColor(movingPiece) === Piece.WHITE) {
        this.whiteCapturedPieces.push(targetLocVal);
      } else {
        this.blackCapturedPieces.push(targetLocVal);
      }

      console.log("Done updateing squares", this);
      return {};
    }
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
