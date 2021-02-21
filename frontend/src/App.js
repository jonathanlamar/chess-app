import "./index.css";
import Board from "./components/board.js";
import GameInfo from "./components/gameinfo.js";
import LoggyComponent from "./utils/loggyComponent";
import { Piece, Player } from "./constants";
import { initialiseChessBoard } from "./utils";

export default class App extends LoggyComponent {
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
    this.squares[newR][newC] = this.squares[r][c];
    this.squares[r][c] = Piece.NONE;

    return { r: newR, c: newC };
  };

  render() {
    return (
      <div>
        <div className="game">
          <div className="game-board">
            <Board squares={this.squares} handleStopFn={this.handleStop} />
          </div>
          <div className="game-info">
            <GameInfo
              whoseTurn={this.whoseTurn}
              whiteCapturedPieces={this.whiteCapturedPieces}
              blackCapturedPieces={this.blackCapturedPieces}
            />
          </div>
        </div>
      </div>
    );
  }
}
