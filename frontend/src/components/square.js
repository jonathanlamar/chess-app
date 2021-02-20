import React from "react";
import Draggable from "react-draggable";

import "../index.css";
import { getPieceImage } from "../utils";
import Piece from "../pieces";

export default class Square extends React.Component {
  constructor(props) {
    super();
    this.key = props.key;
    this.value = props.value;
    this.shade = props.shade;
    this.style = getPieceImage(props.value);
    this.onStop = props.onStop;

    this.state = { dragDeltaPosition: { x: 0, y: 0 } };
  }

  handleDrag = (e, ui) => {
    const { dragDeltaPosition: deltaPos } = this.state;

    this.setState({
      dragDeltaPosition: {
        x: deltaPos.x + ui.deltaX,
        y: deltaPos.y + ui.deltaY,
      },
    });
  };

  render() {
    if (this.value === Piece.None) {
      return (
        <div
          className={"square " + this.shade}
          style={this.style}
          key={this.key}
        ></div>
      );
    } else {
      return (
        <div className={"square " + this.shade} key={this.key}>
          <Draggable
            grid={[47, 47]}
            position={this.state.dragDeltaPosition}
            onDrag={this.handleDrag}
            onStop={this.onStop}
          >
            <div className={"square transparent-square"} style={this.style} />
          </Draggable>
        </div>
      );
    }
  }
}
