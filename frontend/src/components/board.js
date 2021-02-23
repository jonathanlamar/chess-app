import React from "react";
import "../index.css";
import Square from "./square.js";
import LoggyComponent from "../utils/loggyComponent";
import { getLinearIndex, getSquareShade } from "../utils";
import { Piece, GlobalParams } from "../constants";

export default class Board extends LoggyComponent {
  renderSquare(r, c) {
    const linearIndex = getLinearIndex(r, c);
    const shade = getSquareShade(r, c);

    return (
      <Square
        key={linearIndex}
        keyVal={linearIndex}
        squareVal={this.props.squares[r][c]}
        shade={shade}
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
