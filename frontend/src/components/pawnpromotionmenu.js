import React from "react";

import { getPieceImage } from "../utils";
import { GlobalParams, Pieces } from "../constants";

export default class PawnPromotionMenu extends React.Component {
  render() {
    let pieceImgs = [
      <img
        key={0}
        src={getPieceImage(this.props.color | Pieces.KNIGHT)}
        onClick={(e) =>
          this.props.promotePawnFn(this.props.color | Pieces.KNIGHT)
        }
      />,
      <img
        key={1}
        src={getPieceImage(this.props.color | Pieces.BISHOP)}
        onClick={(e) =>
          this.props.promotePawnFn(this.props.color | Pieces.BISHOP)
        }
      />,
      <img
        key={2}
        src={getPieceImage(this.props.color | Pieces.ROOK)}
        onClick={(e) =>
          this.props.promotePawnFn(this.props.color | Pieces.ROOK)
        }
      />,
      <img
        key={3}
        src={getPieceImage(this.props.color | Pieces.QUEEN)}
        onClick={(e) =>
          this.props.promotePawnFn(this.props.color | Pieces.QUEEN)
        }
      />,
    ];

    return (
      <div style={{ height: GlobalParams.TILE_SIZE }}>
        <h3>Select a piece for pawn promotion.</h3>
        <div>{pieceImgs}</div>
      </div>
    );
  }
}
