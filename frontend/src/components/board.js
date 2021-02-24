import React from "react";

import "../index.css";
import Piece from "./piece.js";
import { getPieceImage, getLinearIndex } from "../utils";
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
        handleStopFn={this.props.handleStopFn}
      />
    );
  }

  render() {
    const squares = [];

    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        if (this.props.squares[r][c] !== Pieces.NONE) {
          squares.push(this.renderPiece(r, c));
        }
      }
    }

    return (
      <div
        className="board"
        style={{
          height: GlobalParams.BOARD_SIZE,
          width: GlobalParams.BOARD_SIZE,
        }}
      >
        {squares}
      </div>
    );
  }
}
