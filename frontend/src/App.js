import React from "react";
import "./index.css";
import Board from "./components/board.js";
import GameInfo from "./components/gameinfo.js";
import { initialiseChessBoard } from "./utils";
import Piece from "./pieces";

export class App extends React.Component {
  constructor() {
    super();
    this.squares = initialiseChessBoard();
  }

  // Will this force a re-render?
  handleStop = (r, c, newR, newC) => {
    // TODO
    this.squares[newR][newC] = this.squares[r][c];
    this.squares[r][c] = Piece.None;
  };

  render() {
    return (
      <div>
        <div className="game">
          <div className="game-board">
            <Board squares={this.squares} handleStop={this.handleStop} />
          </div>
          <div className="game-info">
            <GameInfo />
          </div>
        </div>
      </div>
    );
  }
}

export default App;
