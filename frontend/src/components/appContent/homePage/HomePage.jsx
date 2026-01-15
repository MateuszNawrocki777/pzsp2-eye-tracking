import { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";

import { useNewTest } from "../../../hooks/newTestContext";

import "./HomePage.css";

export default function HomePage() {
  return (
    <div className="home-container">
      <div className="home-content-container">
        <div className="home-title-section">
          <h1 className="home-header">Eye gestures</h1>
          <h2 className="home-header-description">
            Open platform for online visual attention research
          </h2>
          <h3 className="home-header-description-2">
            Conduct eye-tracking experiments directly in the browse
          </h3>
        </div>
        <div className="home-for-who-section">
          <h1>Who is this platform for?</h1>
          <div className="audience-tile-researcher">Researchers</div>
          <div className="audience-tile-psychologist">Psychologists</div>
          <div className="audience-tile-marketing">UX / Marketing</div>
          <div className="audience-tile-you">You</div>
        </div>
        <div className="home-instructions-section">
          <h1>How it works?</h1>
          <ol>
            <li>
              Create a study. Upload images (PNG / JPG / PDF) and configure
              experiment settings.
            </li>
            <li>
              Share a link Set link expiration time and/or number of allowed
              uses.{" "}
            </li>
            <li>
              Participant completes the study Calibration followed by the
              experiment in the browser.
            </li>
            <li>
              Analyze results View gaze heatmaps and export collected data.
            </li>
          </ol>
        </div>
      </div>
    </div>
  );
}
