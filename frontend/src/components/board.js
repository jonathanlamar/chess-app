import "../index.css";
import Square from "./square.js";
import LoggyComponent from "../utils/loggyComponent";
import { getLinearIndex, getSquareShade } from "../utils";

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
    const rows = [];

    for (let r = 0; r < 8; r++) {
      const squares = [];

      for (let c = 0; c < 8; c++) {
        squares.push(this.renderSquare(r, c));
      }

      rows.push(
        <div className="board-row" key={r}>
          {squares}
        </div>
      );
    }

    return <div>{rows}</div>;
  }
}
