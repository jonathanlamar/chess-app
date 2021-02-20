import React from "react";

import "../index.css";
import Square from "./square.js";
import { getLinearIndex, getSquareShade } from "../utils";

export default class Board extends React.Component {
  constructor(props) {
    super();
    this.squares = props.squares;
    this.handleStop = props.handleStop;
  }

  renderSquare(r, c) {
    return (
      <Square
        key={getLinearIndex(r, c)}
        value={this.squares[r][c]}
        shade={getSquareShade(r, c)}
        onStop={this.onStop}
      />
    );
  }

  render() {
    const squares = [];

    for (let r = 0; r < 8; r++) {
      const row = [];

      for (let c = 0; c < 8; c++) {
        row.push(this.renderSquare(r, c));
      }

      squares.push(<div className="board-row">{row}</div>);
    }

    return <div>{squares}</div>;
  }
}
