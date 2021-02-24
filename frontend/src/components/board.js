import React from "react";

import "../index.css";
import Piece from "./piece.js";
import {
  getPieceImage,
  fileRankToPos,
  getLinearIndex,
  rcToXy,
  getPieceColor,
} from "../utils";
import { Pieces, GlobalParams } from "../constants";

export default class Board extends React.Component {
  renderPiece(r, c) {
    const linearIndex = getLinearIndex(r, c);
    const squareVal = this.props.squares[r][c];
    const iconUrl = getPieceImage(squareVal);

    return (
      <Piece
        key={linearIndex}
        keyVal={linearIndex}
        squareVal={squareVal}
        iconUrl={iconUrl}
        startPos={{ r, c }}
        handleStartFn={this.props.handleStartFn}
        handleStopFn={this.props.handleStopFn}
        clickable={this.props.whoseTurn === getPieceColor(squareVal)}
      />
    );
  }

  renderSquare(fileRank, className) {
    const { r, c } = fileRankToPos(fileRank);
    const { x, y } = rcToXy(r, c);

    return (
      <div
        className={"square " + className}
        key={100 + getLinearIndex(r, c)}
        style={{
          height: GlobalParams.TILE_SIZE,
          width: GlobalParams.TILE_SIZE,
          position: "absolute",
          left: x + "px",
          top: y + "px",
          zIndex: 100 + getLinearIndex(r, c),
        }}
      />
    );
  }

  render() {
    const pieces = [];

    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        if (this.props.squares[r][c] !== Pieces.NONE) {
          pieces.push(this.renderPiece(r, c));
        }
      }
    }

    if (this.props.mobilePieceHomeSquare !== "NONE") {
      pieces.push(
        this.renderSquare(this.props.mobilePieceHomeSquare, "home-square")
      );
    }

    for (let i = 0; i < this.props.validMovesSquares.length; i++) {
      pieces.push(
        this.renderSquare(this.props.validMovesSquares[i], "move-square")
      );
    }

    return (
      <div
        className="board"
        style={{
          height: GlobalParams.BOARD_SIZE,
          width: GlobalParams.BOARD_SIZE,
        }}
      >
        {pieces}
      </div>
    );
  }
}
