import React from "react";

import CapturedPieces from "./capturedpieces";
import { Player } from "../constants";

export default class GameInfo extends React.Component {
  render() {
    const playerString =
      this.props.whoseTurn === Player.WHITE ? "White" : "Black";

    return (
      <div>
        <div>
          <h3>{playerString + " to move."}</h3>
        </div>
        <div>
          <CapturedPieces
            label={"White captured pieces"}
            pieces={this.props.whiteCapturedPieces}
          />
        </div>
        <div>
          <CapturedPieces
            label={"Black captured pieces"}
            pieces={this.props.blackCapturedPieces}
          />
        </div>
      </div>
    );
  }
}
