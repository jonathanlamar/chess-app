import React from "react";

import "../index.css";
import Square from "./square.js";
import { getPieceImage, getLinearIndex } from "../utils";
import { Piece, GlobalParams } from "../constants";

export default class Board extends React.Component {
  renderSquare(r, c) {
    const linearIndex = getLinearIndex(r, c);
    const squareVal = this.props.squares[r][c];
    const iconUrl = getPieceImage(squareVal);

    return (
      <Square
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
        if (this.props.squares[r][c] !== Piece.NONE) {
          squares.push(this.renderSquare(r, c));
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
