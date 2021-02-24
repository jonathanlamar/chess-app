import React from "react";

import { getPieceImage } from "../utils";
import { GlobalParams } from "../constants";

export default class CapturedPieces extends React.Component {
  render() {
    let pieceImgs = [];
    for (let i = 0; i < this.props.pieces.length; i++) {
      const pieceVal = this.props.pieces[i];

      pieceImgs.push(<img key={i} src={getPieceImage(pieceVal)} />);
    }

    return (
      <div style={{ height: GlobalParams.TILE_SIZE }}>
        <h3>{this.props.label}</h3>
        <div>{pieceImgs}</div>
      </div>
    );
  }
}
