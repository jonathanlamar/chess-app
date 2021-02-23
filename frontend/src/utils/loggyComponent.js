import React from "react";

export default class LoggyComponent extends React.Component {
  componentDidCatch(error, errorInfo) {
    console.log(error);
  }

  componentDidUpdate(prevProps, prevState) {
    console.log("Updated", this);
    console.log("Previous state:", prevProps, prevState);
  }

  componentWillMount() {
    console.log("Mounted", this);
  }

  componentWillUnmount() {
    console.log("Unmounted", this);
  }
}
