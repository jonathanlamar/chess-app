import React from "react";

import CapturedPieces from "./capturedpieces";
import PawnPromotionMenu from "./pawnpromotionmenu";
import { Player } from "../constants";

export default class GameInfo extends React.Component {
  render() {
    const playerString =
      this.props.whoseTurn === Player.WHITE ? "White" : "Black";

    if (this.props.isAwaitingPawnPromotion) {
      return (
        <div>
          <div>
            <h3>
              {playerString + " to move.\t"}
              {"Full move count: " + this.props.fullMoveCount + "\t"}
              {"Half move clock: " + this.props.halfMoveClock + "\t"}
            </h3>
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
          <div>
            <PawnPromotionMenu
              promotePawnFn={this.props.promotePawnFn}
              color={this.props.pawnPromotionColor}
            />
          </div>
        </div>
      );
    } else {
      return (
        <div>
          <div>
            <h3>
              {playerString + " to move.\t"}
              {this.props.currentPlayerInCheck ? "In check" : ""}
            </h3>
          </div>
          <div>
            <h3>
              {"Full move count: " + this.props.fullMoveCount + "\t"}
              {"Half move clock: " + this.props.halfMoveClock + "\t"}
            </h3>
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
}
